package com.learninghub.catalog.model;

import java.time.Year;
import java.util.Objects;

/**
 * An immutable catalogue entry. A record gives us the constructor, accessors,
 * equals, hashCode and toString; the compact constructor adds validation.
 */
public record Book(Isbn isbn, String title, String author, int year, Genre genre, int pages) {

    /** Movable type in Europe dates from the 1450s; nothing in the catalogue is older. */
    public static final int EARLIEST_YEAR = 1450;

    public Book {
        Objects.requireNonNull(isbn, "isbn");
        Objects.requireNonNull(genre, "genre");
        title = requireText(title, "title");
        author = requireText(author, "author");
        int latest = Year.now().getValue() + 1;
        if (year < EARLIEST_YEAR || year > latest) {
            throw new IllegalArgumentException("year must be between " + EARLIEST_YEAR + " and " + latest + ": " + year);
        }
        if (pages <= 0) {
            throw new IllegalArgumentException("pages must be positive: " + pages);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.strip();
    }

    /** A "wither": records are immutable, so a change returns a new instance. */
    public Book withPages(int newPages) {
        return new Book(isbn, title, author, year, genre, newPages);
    }
}
