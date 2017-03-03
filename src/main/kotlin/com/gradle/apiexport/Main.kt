package com.gradle.apiexport

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.buffer.ByteBuf
import io.reactivex.netty.protocol.http.client.HttpClient
import io.reactivex.netty.protocol.http.client.HttpClientRequest
import io.reactivex.netty.protocol.http.client.HttpClientResponse
import io.reactivex.netty.protocol.http.sse.ServerSentEvent
import rx.Observable
import rx.exceptions.Exceptions

import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicReference

import java.time.Instant.now


private val GRADLE_ENTERPRISE_SERVER = InetSocketAddress(
        System.getProperty("server"), Integer.parseInt(System.getProperty("port", "443")))

private val HTTP_CLIENT = HttpClient.newClient(GRADLE_ENTERPRISE_SERVER).unsafeSecure()
private val THROTTLE = 5
private val MAPPER = ObjectMapper()


fun getGreeting(): String {
    return "GETTING READY TO STREAM!!!"
}

fun main(args: Array<String>) {
    println( getGreeting())

    val hoursStr = System.getProperty("hours")
    val since: Instant
    // default to 24hs
    if (hoursStr == null) {
        since = now().minus(Duration.ofHours(Integer.parseInt("24").toLong()))
    } else if (hoursStr == "all") {
        since = Instant.EPOCH
        println("Calculating for all stored build scans")
    } else {
        since = now().minus(Duration.ofHours(Integer.parseInt(hoursStr).toLong()))
    }


    buildStream(since)
            .map { parse(it) }
            .map { json -> json.get("buildId").asText() }
            .flatMap {buildId -> buildEventStream(buildId)}
            .filter { serverSentEvent -> serverSentEvent.getEventTypeAsString() == "BuildEvent" }
            .map { parse(it) }
            .toBlocking()
            .subscribe { println(it) }

}





internal class BuildInfo {
    var startTime: Instant? = null
    var finishTime: Instant? = null
    var success: Boolean = false
    var customValues: MutableMap<String, String> = HashMap()
    var customTags: MutableList<String> = ArrayList()

    fun duration(): Duration {
        return Duration.between(startTime!!, finishTime)
    }
}

private fun buildStream(since: Instant): Observable<ServerSentEvent> {
    return buildStream(since, null)
}

private fun buildStream(since: Instant, lastEventId: String?): Observable<ServerSentEvent> {
    //println("Build stream from " + lastEventId)
    val lastBuildId = AtomicReference<String>(null)

    val request = HTTP_CLIENT
            .createGet("/build-export/v1/builds/since/" + since.toEpochMilli().toString())
            .setKeepAlive(true)
            .addHeader("Authorization", "Basic ZG90Y29tLWRldjpwNSZZS2pUNHY0TEthZmxOZTJ5Kk5LVTA4Tm4wQFg=")

    if (lastEventId != null) {
        request.addHeader("Last-Event-ID", lastEventId)
    }

    return request
            .flatMap { it.getContentAsServerSentEvents() }
            .doOnNext { serverSentEvent -> lastBuildId.set(serverSentEvent.eventIdAsString) }
            .doOnSubscribe { println("Streaming builds...") }
            .onErrorResumeNext { t ->
                println("Error streaming builds, resuming from " + lastBuildId.get() + "...")
                buildStream(since, lastBuildId.get())
            }
}

private fun buildEventStream(buildId: String): Observable<ServerSentEvent> {
    return buildEventStream(buildId, null)
}

private fun buildEventStream(buildId: String, lastEventId: String?): Observable<ServerSentEvent> {
    val lastBuildEventId = AtomicReference<String>(null)

    val request = HTTP_CLIENT
            .createGet("/build-export/v1/build/$buildId/events")
            .setKeepAlive(true)
            .addHeader("Authorization", "Basic ZG90Y29tLWRldjpwNSZZS2pUNHY0TEthZmxOZTJ5Kk5LVTA4Tm4wQFg=")

    if (lastEventId != null) {
        request.addHeader("Last-Event-ID", lastEventId)
    }

    return request
            .flatMap { it.getContentAsServerSentEvents() }
            .doOnNext { serverSentEvent -> lastBuildEventId.set(serverSentEvent.eventIdAsString) }
            .doOnSubscribe { println("Streaming events for : " + buildId) }
            .onErrorResumeNext { t ->
                println("Error streaming build events, resuming from " + lastBuildEventId.get() + "...")
                buildEventStream(buildId, lastBuildEventId.get())
            }
}

private fun parse(serverSentEvent: ServerSentEvent): JsonNode {
    try {
        return MAPPER.readTree(serverSentEvent.contentAsString())
    } catch (e: IOException) {
        throw Exceptions.propagate(e)
    } finally {
        val deallocated = serverSentEvent.release()
        assert(deallocated)
    }
}
