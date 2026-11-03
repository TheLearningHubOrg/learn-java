package com.learninghub.catalog.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookTest {

    private static final Isbn ISBN = new Isbn("9781000000016");

    @Test
    void stripsTextFields() {
        var book = new Book(ISBN, "  Title  ", " Author ", 2020, Genre.FICTION, 100);
        assertEquals("Title", book.title());
        assertEquals("Author", book.author());
    }

    @Test
    void rejectsInvalidState() {
        assertThrows(IllegalArgumentException.class, () -> new Book(ISBN, " ", "A", 2020, Genre.FICTION, 1));
        assertThrows(IllegalArgumentException.class, () -> new Book(ISBN, "T", "A", 1200, Genre.FICTION, 1));
        assertThrows(IllegalArgumentException.class, () -> new Book(ISBN, "T", "A", 2020, Genre.FICTION, 0));
        assertThrows(NullPointerException.class, () -> new Book(null, "T", "A", 2020, Genre.FICTION, 1));
    }

    @Test
    void witherReturnsANewInstance() {
        var original = new Book(ISBN, "T", "A", 2020, Genre.SCIENCE, 100);
        var longer = original.withPages(200);
        assertNotSame(original, longer);
        assertEquals(100, original.pages());
        assertEquals(200, longer.pages());
        assertNotEquals(original, longer);
    }

    @Test
    void recordsGetValueEquality() {
        var a = new Book(ISBN, "T", "A", 2020, Genre.SCIENCE, 100);
        var b = new Book(ISBN, "T", "A", 2020, Genre.SCIENCE, 100);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
