package com.learninghub.catalog.http;

import com.learninghub.catalog.service.CatalogService;
import com.learninghub.catalog.service.SampleData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Starts the real server on a free port (port 0) and talks to it over HTTP. */
class CatalogServerTest {

    private static CatalogServer server;
    private static HttpClient client;

    @BeforeAll
    static void start() throws IOException {
        server = new CatalogServer(new InetSocketAddress("127.0.0.1", 0), new CatalogService(SampleData.repository()));
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stop() {
        client.close();
        server.close();
    }

    private static HttpResponse<String> get(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void health() throws Exception {
        var response = get("/health");
        assertEquals(200, response.statusCode());
        assertEquals("{\"status\":\"UP\"}", response.body());
        assertTrue(response.headers().firstValue("Content-Type").orElseThrow().startsWith("application/json"));
    }

    @Test
    void lookupStatusCodes() throws Exception {
        assertEquals(200, get("/books/978-1-00-000001-6").statusCode());
        assertEquals(404, get("/books/9781000000092").statusCode());
        var bad = get("/books/123");
        assertEquals(400, bad.statusCode());
        assertTrue(bad.body().contains("13 digits"), bad.body());
    }

    @Test
    void authorFilterDecodesTheQuery() throws Exception {
        var response = get("/books?author=lin%20moreau");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("The Patient Collector"), response.body());
        assertTrue(!response.body().contains("Ada Okafor"), response.body());
    }

    @Test
    void stats() throws Exception {
        var response = get("/stats");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("{\"books\":5,\"byGenre\":{\"PROGRAMMING\":2"), response.body());
    }

    @Test
    void unknownRouteAndWrongMethod() throws Exception {
        assertEquals(404, get("/nope").statusCode());
        var post = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + "/books"))
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        var response = client.send(post, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode());
        assertEquals("GET", response.headers().firstValue("Allow").orElseThrow());
    }
}
