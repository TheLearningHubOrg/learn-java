package com.learninghub.catalog.http;

import com.learninghub.catalog.model.Isbn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.Map;

/**
 * A small JSON writer built on pattern matching for switch. It serialises
 * strings, numbers, booleans, enums, maps, collections and public records,
 * which is all the starter API needs, with no third-party dependency.
 */
public final class Json {

    private Json() {
    }

    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        append(out, value);
        return out.toString();
    }

    private static void append(StringBuilder out, Object value) {
        switch (value) {
            case null -> out.append("null");
            case String s -> quote(out, s);
            case Boolean b -> out.append(b.booleanValue());
            case Double d when d.isNaN() || d.isInfinite() ->
                    throw new IllegalArgumentException("JSON cannot represent " + d);
            case Number n -> out.append(n);
            case Enum<?> e -> quote(out, e.name());
            // Isbn is a record, so this case must come before "case Record":
            // the compiler rejects a case that an earlier case dominates.
            case Isbn isbn -> quote(out, isbn.value());
            case Map<?, ?> map -> appendMap(out, map);
            case Collection<?> items -> appendCollection(out, items);
            case Record record -> appendRecord(out, record);
            default -> throw new IllegalArgumentException(
                    "unsupported type for JSON: " + value.getClass().getName());
        }
    }

    private static void appendMap(StringBuilder out, Map<?, ?> map) {
        out.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            Object key = entry.getKey();
            quote(out, key instanceof Enum<?> e ? e.name() : String.valueOf(key));
            out.append(':');
            append(out, entry.getValue());
        }
        out.append('}');
    }

    private static void appendCollection(StringBuilder out, Collection<?> items) {
        out.append('[');
        boolean first = true;
        for (Object item : items) {
            if (!first) {
                out.append(',');
            }
            first = false;
            append(out, item);
        }
        out.append(']');
    }

    private static void appendRecord(StringBuilder out, Record record) {
        out.append('{');
        RecordComponent[] components = record.getClass().getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            if (i > 0) {
                out.append(',');
            }
            quote(out, components[i].getName());
            out.append(':');
            try {
                append(out, components[i].getAccessor().invoke(record));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("cannot read " + components[i].getName(), e);
            }
        }
        out.append('}');
    }

    static void quote(StringBuilder out, String s) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
