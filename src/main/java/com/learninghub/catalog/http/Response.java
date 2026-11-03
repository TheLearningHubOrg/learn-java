package com.learninghub.catalog.http;

import java.util.Map;

/** An HTTP status plus a body that {@link Json} can serialise. */
public record Response(int status, Object body) {

    public static Response ok(Object body) {
        return new Response(200, body);
    }

    public static Response error(int status, String message) {
        return new Response(status, Map.of("error", message));
    }
}
