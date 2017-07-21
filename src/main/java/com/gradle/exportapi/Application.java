package com.gradle.exportapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.exportapi.dbutil.SqlHelper;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.util.ResourceLeakDetector;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.sse.ServerSentEvent;
import org.knowm.yank.PropertiesUtils;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static com.gradle.exportapi.dao.BuildDAO.findLastBuildId;
import static java.time.Instant.now;

final class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final String BASIC_AUTH = System.getProperty("basic_auth");
    private static final HttpClient<ByteBuf, ByteBuf> HTTP_CLIENT;
    private static final Integer NUM_OF_STREAMS = Integer.valueOf(System.getProperty("num_of_streams", "5"));
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        String server = System.getProperty("server");
        String port = System.getProperty("port");
        if (server == null) {
            throw new IllegalArgumentException("'server' is a required system property");
        }
        HTTP_CLIENT = HttpClientFactory.create(server, port);
    }

    public static final String GE_EXPORT_DATABASE_PROPERTIES_KEY = "geexport.db.info";

    public static void main(String[] args) throws Exception {
        //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

        try {

            String propertiesFile = System.getProperty(GE_EXPORT_DATABASE_PROPERTIES_KEY, "db-info.properties");
            LOGGER.info("Validating connection to the target database from configuration file: {}", propertiesFile);
            Properties dbProps = PropertiesUtils.getPropertiesFromClasspath(propertiesFile);
            try {
                Yank.setupDefaultConnectionPool(dbProps);
                SqlHelper.loadSqlQueries();
            } catch (Throwable t) {
                LOGGER.error("Unable to connect to the target database.  Please validate your configuration settings.");
                throw new RuntimeException("Failed to establish connection to target database.", t);
            }

            LOGGER.info("Validating connection to Gradle Enterprise");
            // This ignores authentication
            performHealthCheck();
            // This does NOT ignore authentication
            performAuthenticationCheck();

            if (System.getProperty("createDb") != null) {
                CreateDB.run();
            }

            String hoursStr = System.getProperty("hours", "24");
            Instant since;
            if (hoursStr.equals("all")) {
                since = Instant.EPOCH;
                LOGGER.info("Calculating for all stored build scans");
            } else {
                since = now().minus(Duration.ofHours(Integer.parseInt(hoursStr)));
            }

            buildIdStream(since)
                    .flatMap(buildId -> buildEventStream(buildId)
                                    .reduce(new EventProcessor(buildId), EventProcessor::process),
                            NUM_OF_STREAMS
                    )
                    .toBlocking()
                    .subscribe(
                            EventProcessor::persist
                    );

            Yank.releaseDefaultConnectionPool();
        } catch (Exception e) {
            LOGGER.error("Export failed ", e);
            System.exit(1);
        }
    }

    public final static String BUILDS_SINCE_RESOURCE = "/build-export/v1/builds/since/";


    /**
     * Creates a get and adds authentication if needed
     *
     * @param resourcePath the end point to hit
     * @return an {@link HttpClientRequest} with auth headers (if needed)
     */
    private static HttpClientRequest<ByteBuf, ByteBuf> get(String resourcePath) {
        HttpClientRequest<ByteBuf, ByteBuf> request = HTTP_CLIENT.createGet(resourcePath);
        if (BASIC_AUTH != null) {
            request = request.addHeader("Authorization", "Basic " + BASIC_AUTH);
        }
        return request;
    }

    private static void performHealthCheck() {
        get("/info/health")
                .doOnNext(response -> {
                    LOGGER.info("Received a status code of {} from '/info/health'", response.getStatus());
                    if (!response.getStatus().equals(HttpResponseStatus.OK)) {
                        throw new RuntimeException("HealthCheck failed.  Status: " + response.getStatus());
                    }
                }).flatMap(response ->
                response.getContent()
                        .map(bb -> bb.toString(Charset.defaultCharset())))
                .toBlocking()
                .forEach(LOGGER::info);
    }

    private static void performAuthenticationCheck() {
        long timeStamp = System.currentTimeMillis();
        get(BUILDS_SINCE_RESOURCE + timeStamp)
                .doOnNext(response -> {
                    //expected response is 204 due no builds being there
                    LOGGER.info("Received a status code of {} from: " + BUILDS_SINCE_RESOURCE + timeStamp, response.getStatus());
                    if (!response.getStatus().codeClass().equals(HttpStatusClass.SUCCESS)) {
                        throw new RuntimeException("AuthenticationCheck failed.  Status: " + response.getStatus());
                    }
                }).flatMap(response ->
                response.getContent()
                        .map(bb -> bb.toString(Charset.defaultCharset())))
                .toBlocking()
                .forEach(LOGGER::info);
    }

    private static Observable<String> buildIdStream(Instant since) {

        String lastBuildEventId = findLastBuildId();

        LOGGER.info("lastBuildEventId: " + lastBuildEventId);

        return buildStream(since, lastBuildEventId)
                .map(Application::parse)
                .map(json -> json.get("buildId").asText());
    }

    private static Observable<ServerSentEvent> buildStream(Instant since, String lastStreamedBuildId) {

        AtomicReference<String> _lastBuildId = new AtomicReference<>(null);

        final String buildsSinceUri = "/build-export/v1/builds/since/" + String.valueOf(since.toEpochMilli());
        LOGGER.info("Builds uri: " + buildsSinceUri);

        HttpClientRequest<ByteBuf, ByteBuf> request = HTTP_CLIENT
                .createGet(buildsSinceUri)
                .setKeepAlive(true);
        if (BASIC_AUTH != null) {
            request = request.addHeader("Authorization", "Basic " + BASIC_AUTH);
        }

        if (lastStreamedBuildId != null) {
            request = request.addHeader("Last-Event-ID", lastStreamedBuildId);
        }

        return request
                .flatMap(HttpClientResponse::getContentAsServerSentEvents)
                .doOnNext(serverSentEvent -> _lastBuildId.set(serverSentEvent.getEventIdAsString()))
                .doOnSubscribe(() -> LOGGER.info("Streaming builds..."))
                .onErrorResumeNext(t -> {
                    LOGGER.info("Error streaming builds, resuming from build id: " + _lastBuildId.get());
                    return buildStream(since, _lastBuildId.get());
                });
    }

    protected static Observable<JsonNode> buildEventStream(String buildId) {
        return buildEventStream(buildId, null);
    }

    protected static Observable<JsonNode> buildEventStream(String buildId, String lastEventId) {
        AtomicReference<String> _lastBuildEventId = new AtomicReference<>(null);

        HttpClientRequest<ByteBuf, ByteBuf> request = HTTP_CLIENT
                .createGet("/build-export/v1/build/" + buildId + "/events?eventTypes=" + EventProcessor.EVENT_TYPES)
                .setKeepAlive(true);
        if (BASIC_AUTH != null) {
            request = request.addHeader("Authorization", "Basic " + BASIC_AUTH);
        }

        if (lastEventId != null) {
            request = request.addHeader("Last-Event-ID", lastEventId);
        }

        return request
                .flatMap(HttpClientResponse::getContentAsServerSentEvents)
                .doOnNext(serverSentEvent -> _lastBuildEventId.set(serverSentEvent.getEventIdAsString()))
                .doOnSubscribe(() -> LOGGER.info("Streaming events for build: " + buildId))
                .filter(serverSentEvent -> serverSentEvent.getEventTypeAsString().equals("BuildEvent"))
                .map(Application::parse)
                .onErrorResumeNext(t -> {
                    LOGGER.info("Error streaming build events of build " + buildId + ", resuming from event id" + _lastBuildEventId.get() + "...");
                    return buildEventStream(buildId, _lastBuildEventId.get());
                });
    }

    private static JsonNode parse(ServerSentEvent serverSentEvent) {
        try {
            return MAPPER.readTree(serverSentEvent.contentAsString());
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        } finally {
            boolean deallocated = serverSentEvent.release();
            assert deallocated;
        }
    }

}

