package com.gradle.exportapi;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClient;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * Created by grodion on 5/21/17.
 */
public class HttpClientFactory {

    private static String defaultToHttps(final String server) {
        return server.startsWith("http") ? server : "https://" + server;
    }

    public static HttpClient<ByteBuf, ByteBuf> create(String server, final String portStr) {
        int port = 0;

        try {

            URL url = new URL(defaultToHttps(server));
            if (portStr == null) {
                port = url.getDefaultPort();
            } else if (Integer.parseInt(portStr) > 0){
                port = Integer.parseInt(portStr);
            }
            final HttpClient<ByteBuf, ByteBuf> httpClient = HttpClient.newClient(new InetSocketAddress(
                    url.getHost(), port));
            if(url.getProtocol().equals("https")) {
                return httpClient.unsafeSecure();
            } else if (url.getProtocol().equals("http")) {
                return httpClient;
            } else {
                throw new RuntimeException("Unsuported protocol");
            }
        }

        catch(MalformedURLException e){
            throw new RuntimeException(e);
        }
    }
}
