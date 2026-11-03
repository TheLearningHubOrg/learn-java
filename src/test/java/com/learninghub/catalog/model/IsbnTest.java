package com.learninghub.catalog.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsbnTest {

    @Test
    void normalisesHyphensAndSpaces() {
        assertEquals("9781000000016", new Isbn("978-1-00-000001-6").value());
        assertEquals("9781000000016", new Isbn("978 1 00 000001 6").value());
    }

    @Test
    void computesTheCheckDigit() {
        assertEquals(6, Isbn.checkDigit("978100000001"));
        assertEquals(0, Isbn.checkDigit("978100000003"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "97810000000161", "978100000001X", "abcdefghijklm"})
    void rejectsWrongShape(String raw) {
        var e = assertThrows(IllegalArgumentException.class, () -> new Isbn(raw));
        assertTrue(e.getMessage().contains("13 digits"), e.getMessage());
    }

    @Test
    void rejectsBadChecksum() {
        var e = assertThrows(IllegalArgumentException.class, () -> new Isbn("9781000000017"));
        assertTrue(e.getMessage().contains("expected check digit 6"), e.getMessage());
    }

    @Test
    void equalityIsByValue() {
        assertEquals(new Isbn("978-1-00-000001-6"), new Isbn("9781000000016"));
    }
}
