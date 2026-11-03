package com.learninghub.catalog.http;

import com.learninghub.catalog.model.Book;
import com.learninghub.catalog.model.Genre;
import com.learninghub.catalog.model.Isbn;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonTest {

    @Test
    void scalars() {
        assertEquals("null", Json.write(null));
        assertEquals("true", Json.write(true));
        assertEquals("42", Json.write(42));
        assertEquals("2.5", Json.write(2.5));
        assertEquals("\"FICTION\"", Json.write(Genre.FICTION));
    }

    @Test
    void escapesStrings() {
        assertEquals("\"a\\\"b\\\\c\\nd\\u0001\"", Json.write("a\"b\\c\nd\u0001"));
    }

    @Test
    void collectionsAndMaps() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("xs", List.of(1, 2));
        map.put("none", null);
        assertEquals("{\"xs\":[1,2],\"none\":null}", Json.write(map));

        var byGenre = new EnumMap<Genre, Long>(Genre.class);
        byGenre.put(Genre.SCIENCE, 3L);
        assertEquals("{\"SCIENCE\":3}", Json.write(byGenre));
        assertEquals("[null,\"x\"]", Json.write(Arrays.asList(null, "x")));
    }

    @Test
    void recordsBecomeObjectsAndIsbnBecomesAString() {
        var book = new Book(new Isbn("9781000000016"), "T", "A", 2020, Genre.FICTION, 10);
        assertEquals(
                "{\"isbn\":\"9781000000016\",\"title\":\"T\",\"author\":\"A\",\"year\":2020,\"genre\":\"FICTION\",\"pages\":10}",
                Json.write(book));
    }

    @Test
    void rejectsUnsupportedValues() {
        assertThrows(IllegalArgumentException.class, () -> Json.write(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Json.write(new Object()));
    }
}
