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

import static java.time.Instant.now;
import static com.gradle.exportapi.dao.BuildDAO.*;

/* @Author Russel Hart rus@gradle.com */

final class Application {

    private static final String BASIC_AUTH = System.getProperty("basic_auth");

    private static final SocketAddress GRADLE_ENTERPRISE_SERVER = new InetSocketAddress(
            System.getProperty("server"), Integer.parseInt( System.getProperty("port","443")) );

    private static final HttpClient<ByteBuf, ByteBuf> HTTP_CLIENT = HttpClient.newClient(GRADLE_ENTERPRISE_SERVER).unsafeSecure();
    private static final int THROTTLE = 5;
    private static final ObjectMapper MAPPER = new ObjectMapper();


    public static void main(String[] args) throws Exception {

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
            System.out.println("Calculating for all stored build scans");
        } else {
            since = now().minus(Duration.ofHours( Integer.parseInt(hoursStr)));
        }



        buildIdStream(since)
                .flatMap(buildId -> buildEventStream(buildId)
                                .reduce(new EventProcessor(buildId), (eventProcessor, json) -> {
                                    eventProcessor.process(json);
                                    return eventProcessor;
                                }),
                        THROTTLE
                )

                .toBlocking()
                .subscribe(
                        System.out::println
                );

        Yank.releaseDefaultConnectionPool();
    }

    private static Observable<String> buildIdStream(Instant since) {

        String lastBuildEventId = findLastBuildId();

        System.out.println("lastBuildEventId: " + lastBuildEventId);

        return buildStream(since, lastBuildEventId)
                .map(Application::parse)
                .map(json -> json.get("buildId").asText());
    }

    private static Observable<ServerSentEvent> buildStream(Instant since, String lastStreamedBuildId) {

        AtomicReference<String> _lastBuildId = new AtomicReference<>(null);

        final String buildsSinceUri = "/build-export/v1/builds/since/" + String.valueOf(since.toEpochMilli());
        System.out.println("Builds uri: " + buildsSinceUri);

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
                .doOnSubscribe(() -> System.out.println("Streaming builds..."))
                .onErrorResumeNext(t -> {
                    System.out.println("Error streaming builds, resuming from build id: " + _lastBuildId.get());
                    return buildStream(since, _lastBuildId.get());
                });
    }

    private static Observable<JsonNode> buildEventStream(String buildId) {
        return buildEventStream(buildId, null);
    }

    private static Observable<JsonNode> buildEventStream(String buildId, String lastEventId) {
        AtomicReference<String> _lastBuildEventId = new AtomicReference<>(null);

        HttpClientRequest<ByteBuf, ByteBuf> request = HTTP_CLIENT
                .createGet("/build-export/v1/build/" + buildId + "/events")
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
                .doOnSubscribe(() -> System.out.println("Streaming events for : " + buildId))
                .filter(serverSentEvent -> serverSentEvent.getEventTypeAsString().equals("BuildEvent"))
                .map(Application::parse)
                .onErrorResumeNext(t -> {
                    System.out.println("Error streaming build events, resuming from " + _lastBuildEventId.get() + "...");
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

