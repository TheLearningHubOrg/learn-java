package com.learninghub.catalog.http;

import com.learninghub.catalog.model.Book;
import com.learninghub.catalog.model.Isbn;
import com.learninghub.catalog.service.CatalogService;
import com.learninghub.catalog.service.LookupResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A read-only JSON API on the JDK's built-in HTTP server. Every request runs
 * on its own virtual thread.
 *
 * <pre>
 * GET /health              {"status":"UP"}
 * GET /books[?author=x]    all books, or books whose author contains x
 * GET /books/{isbn}        one book, 400 for a malformed ISBN, 404 if unknown
 * GET /stats               counts per genre and page statistics
 * </pre>
 */
public final class CatalogServer implements AutoCloseable {

    private static final String BOOKS_PREFIX = "/books/";

    private final HttpServer server;
    private final ExecutorService executor;
    private final CatalogService catalog;

    public CatalogServer(InetSocketAddress address, CatalogService catalog) throws IOException {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.server = HttpServer.create(address, 0);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(executor);
        server.createContext("/", this::handle);
    }

    public void start() {
        server.start();
    }

    /** The bound port; useful when the server was created with port 0. */
    public int port() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(0);
        executor.close();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            Response response;
            try {
                response = route(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                        exchange.getRequestURI().getRawQuery());
            } catch (RuntimeException e) {
                response = Response.error(500, "internal error");
                System.getLogger(CatalogServer.class.getName())
                        .log(System.Logger.Level.ERROR, "request failed", e);
            }
            send(exchange, response);
        } finally {
            exchange.close();
        }
    }

    Response route(String method, String path, String rawQuery) {
        if (!"GET".equals(method)) {
            return Response.error(405, "only GET is supported");
        }
        return switch (path) {
            case "/health" -> Response.ok(Map.of("status", "UP"));
            case "/books" -> {
                String author = query(rawQuery).get("author");
                yield Response.ok(author == null ? catalog.all() : catalog.byAuthor(author));
            }
            case "/stats" -> Response.ok(stats());
            default -> path.startsWith(BOOKS_PREFIX)
                    ? lookup(path.substring(BOOKS_PREFIX.length()))
                    : Response.error(404, "no route for " + path);
        };
    }

    private Response lookup(String rawIsbn) {
        return switch (catalog.lookup(rawIsbn)) {
            case LookupResult.Found(Book book) -> Response.ok(book);
            case LookupResult.NotFound(Isbn isbn) -> Response.error(404, "no book with ISBN " + isbn);
            case LookupResult.Invalid(var _, String reason) -> Response.error(400, reason);
        };
    }

    private Map<String, Object> stats() {
        var pages = catalog.pageStatistics();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("books", pages.getCount());
        body.put("byGenre", catalog.countByGenre());
        body.put("pages", Map.of("min", pages.getMin(), "max", pages.getMax(), "total", pages.getSum()));
        return body;
    }

    static Map<String, String> query(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            String value = eq < 0 ? "" : pair.substring(eq + 1);
            params.put(URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return params;
    }

    private static void send(HttpExchange exchange, Response response) throws IOException {
        byte[] bytes = Json.write(response.body()).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (response.status() == 405) {
            exchange.getResponseHeaders().set("Allow", "GET");
        }
        exchange.sendResponseHeaders(response.status(), bytes.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(bytes);
        }
    }
}
