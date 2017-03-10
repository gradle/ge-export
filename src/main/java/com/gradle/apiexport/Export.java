package com.gradle.apiexport;

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


/* @Author Russel Hart rus@gradle.com */

public final class Export {

    private static final SocketAddress GRADLE_ENTERPRISE_SERVER = new InetSocketAddress("ubuntu16", 443);

    private static final HttpClient<ByteBuf, ByteBuf> HTTP_CLIENT = HttpClient.newClient(GRADLE_ENTERPRISE_SERVER).unsafeSecure();
    private static final int THROTTLE = 5;
    private static final ObjectMapper MAPPER = new ObjectMapper();


    public static void main(String[] args) throws Exception {

        Properties dbProps = PropertiesUtils.getPropertiesFromClasspath("POSTGRES.properties");
        Yank.setupDefaultConnectionPool(dbProps);

        Instant since1Day = now().minus(Duration.ofHours(12));

        buildIdStream(since1Day)
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

        return buildStream(since, null)
                .map(Export::parse)
                .map(json -> json.get("buildId").asText());
    }

    private static Observable<ServerSentEvent> buildStream(Instant since, String lastEventId) {
        System.out.println("Build stream from " + lastEventId);
        AtomicReference<String> lastBuildId = new AtomicReference<>(null);

        HttpClientRequest<ByteBuf, ByteBuf> request = HTTP_CLIENT
                .createGet("/build-export/v1/builds/since/" + String.valueOf(since.toEpochMilli()))
                .setKeepAlive(true)
                .addHeader("Authorization", "Basic ZG90Y29tLWRldjpwNSZZS2pUNHY0TEthZmxOZTJ5Kk5LVTA4Tm4wQFg=");

        if (lastEventId != null) {
            request.addHeader("Last-Event-ID", lastEventId);
        }

        return request
                .flatMap(HttpClientResponse::getContentAsServerSentEvents)
                .doOnNext(serverSentEvent -> lastBuildId.set(serverSentEvent.getEventIdAsString()))
                .doOnSubscribe(() -> System.out.println("Streaming builds..."))
                .onErrorResumeNext(t -> {
                    System.out.println("Error streaming builds, resuming from " + lastBuildId.get() + "...");
                    return buildStream(since, lastBuildId.get());
                });
    }

    private static Observable<JsonNode> buildEventStream(String buildId) {
        return buildEventStream(buildId, null);
    }

    private static Observable<JsonNode> buildEventStream(String buildId, String lastEventId) {
        AtomicReference<String> lastBuildEventId = new AtomicReference<>(null);

        HttpClientRequest<ByteBuf, ByteBuf> request = HTTP_CLIENT
                .createGet("/build-export/v1/build/" + buildId + "/events")
                .setKeepAlive(true)
                .addHeader("Authorization", "Basic ZG90Y29tLWRldjpwNSZZS2pUNHY0TEthZmxOZTJ5Kk5LVTA4Tm4wQFg=");

        if (lastEventId != null) {
            request.addHeader("Last-Event-ID", lastEventId);
        }

        return request
                .flatMap(HttpClientResponse::getContentAsServerSentEvents)
                .doOnNext(serverSentEvent -> lastBuildEventId.set(serverSentEvent.getEventIdAsString()))
                .doOnSubscribe(() -> System.out.println("Streaming events for : " + buildId))
                .filter(serverSentEvent -> serverSentEvent.getEventTypeAsString().equals("BuildEvent"))
                .map(Export::parse)
                .onErrorResumeNext(t -> {
                    System.out.println("Error streaming build events, resuming from " + lastBuildEventId.get() + "...");
                    return buildEventStream(buildId, lastBuildEventId.get());
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

