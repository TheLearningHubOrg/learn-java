package com.learninghub.catalog;

import com.learninghub.catalog.http.CatalogServer;
import com.learninghub.catalog.service.CatalogService;
import com.learninghub.catalog.service.SampleData;

import java.io.IOException;
import java.net.InetSocketAddress;

/** Entry point: reads PORT, binds 0.0.0.0 and serves the catalogue until the JVM stops. */
public final class App {

    private App() {
    }

    public static void main(String[] args) throws IOException {
        int port = parsePort(System.getenv("PORT"));
        var catalog = new CatalogService(SampleData.repository());
        var server = new CatalogServer(new InetSocketAddress("0.0.0.0", port), catalog);
        server.start();
        // Stop the server cleanly on SIGTERM (docker stop) or Ctrl+C.
        Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().name("shutdown").unstarted(server::close));
        System.out.printf("catalog listening on http://0.0.0.0:%d (Java %s)%n", server.port(), Runtime.version());
    }

    static int parsePort(String raw) {
        if (raw == null || raw.isBlank()) {
            return 8080;
        }
        try {
            int port = Integer.parseInt(raw.strip());
            if (port < 0 || port > 65_535) {
                throw new IllegalArgumentException("PORT out of range: " + raw);
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PORT is not a number: " + raw, e);
        }
    }
}
