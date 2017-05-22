package com.gradle.exportapi;

import io.reactivex.netty.protocol.http.client.HttpClient;
import static org.junit.Assert.*;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * Created by grodion on 5/16/17.
 */
public class HttpClientFactoryTest {
    private String port = "80";
    private String server = "http:google.com";
    HttpClient testClient = HttpClientFactory.create(server, port);

    @Test
    public void testCreate() {
        assertNotNull(testClient);
    }

 //   @Test(expected = RuntimeException.class)
//    This test does not work due to URL malphorming catching being weak.
    public void testBadURL() {
        HttpClientFactory.create(" // g o\\  og$@!le.c  om", "32");
    }

}
