package com.gradle.exportapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.sse.ServerSentEvent;
import org.knowm.yank.PropertiesUtils;
import org.knowm.yank.Yank;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gradle.exportapi.dao.BuildDAO.findLastBuildId;
import static java.time.Instant.now;


/* @Author Russel Hart rus@gradle.com */

final class Application {

    public static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String BASIC_AUTH = System.getProperty("basic_auth");

    private static final HttpClient<ByteBuf, ByteBuf> HTTP_CLIENT = HttpClientFactory.create(System.getProperty("server"), System.getProperty("port"));

    private static final Integer NUM_OF_STREAMS = Integer.valueOf(System.getProperty("num_of_streams","5"));
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        try {
            Properties dbProps = PropertiesUtils.getPropertiesFromClasspath("POSTGRES.properties");
            Yank.setupDefaultConnectionPool(dbProps);

            if( System.getProperty("createDb") != null) {
                CreateDB.run();
            }

            String hoursStr = System.getProperty("hours");

            Instant since;
            // default to 24hs
            if(hoursStr == null) {
                since = now().minus(Duration.ofHours( Integer.parseInt("24")));
            }
            else if(hoursStr.equals("all")) {
                since = Instant.EPOCH;
                log.info("Calculating for all stored build scans");
            } else {
                since = now().minus(Duration.ofHours( Integer.parseInt(hoursStr)));
            }

            buildIdStream(since)
                .flatMap(buildId -> buildEventStream(buildId)
                    .reduce(new EventProcessor(buildId), (eventProcessor, json) -> {
                        eventProcessor.process(json);
                        return eventProcessor;
                    }),
                    NUM_OF_STREAMS
                )
                .toBlocking()
                .subscribe(
                    EventProcessor::persist
                );

            Yank.releaseDefaultConnectionPool();
        } catch (Exception e) {
            log.error("Export failed ", e);
        }
    }

    private static Observable<String> buildIdStream(Instant since) {

        String lastBuildEventId = findLastBuildId();

        log.info("lastBuildEventId: " + lastBuildEventId);

        return buildStream(since, lastBuildEventId)
                .map(Application::parse)
                .map(json -> json.get("buildId").asText());
    }

    private static Observable<ServerSentEvent> buildStream(Instant since, String lastStreamedBuildId) {

        AtomicReference<String> _lastBuildId = new AtomicReference<>(null);

        final String buildsSinceUri = "/build-export/v1/builds/since/" + String.valueOf(since.toEpochMilli());
        log.info("Builds uri: " + buildsSinceUri);

        HttpClientRequest<ByteBuf, ByteBuf> request = HTTP_CLIENT
                .createGet(buildsSinceUri)
                .setKeepAlive(true);
        if(BASIC_AUTH != null) {
            request = request.addHeader("Authorization", "Basic " + BASIC_AUTH);
        }

        if (lastStreamedBuildId != null) {
            request = request.addHeader("Last-Event-ID", lastStreamedBuildId);
        }

        return request
                .flatMap(HttpClientResponse::getContentAsServerSentEvents)
                .doOnNext(serverSentEvent -> _lastBuildId.set(serverSentEvent.getEventIdAsString()))
                .doOnSubscribe(() -> log.info("Streaming builds..."))
                .onErrorResumeNext(t -> {
                    log.info("Error streaming builds, resuming from build id: " + _lastBuildId.get());
                    return buildStream(since, _lastBuildId.get());
                });
    }

    private static Observable<JsonNode> buildEventStream(String buildId) {
        return buildEventStream(buildId, null);
    }

    private static Observable<JsonNode> buildEventStream(String buildId, String lastEventId) {
        AtomicReference<String> _lastBuildEventId = new AtomicReference<>(null);

        HttpClientRequest<ByteBuf, ByteBuf> request = HTTP_CLIENT
                .createGet("/build-export/v1/build/" + buildId + "/events?eventTypes=" + EventProcessor.EVENT_TYPES)
                .setKeepAlive(true);
        if(BASIC_AUTH != null) {
            request = request.addHeader("Authorization", "Basic " + BASIC_AUTH);
        }

        if (lastEventId != null) {
            request = request.addHeader("Last-Event-ID", lastEventId);
        }

        return request
                .flatMap(HttpClientResponse::getContentAsServerSentEvents)
                .doOnNext(serverSentEvent -> _lastBuildEventId.set(serverSentEvent.getEventIdAsString()))
                .doOnSubscribe(() -> log.info("Streaming events for build: " + buildId))
                .filter(serverSentEvent -> serverSentEvent.getEventTypeAsString().equals("BuildEvent"))
                .map(Application::parse)
                .onErrorResumeNext(t -> {
                    log.info("Error streaming build events of build " + buildId + ", resuming from event id" + _lastBuildEventId.get() + "...");
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

