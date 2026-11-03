package com.learninghub.catalog.service;

import com.learninghub.catalog.model.Book;
import com.learninghub.catalog.model.Genre;
import com.learninghub.catalog.model.Isbn;
import com.learninghub.catalog.store.Repository;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Business rules over a {@link Repository}. Contains no I/O and no HTTP. */
public final class CatalogService {

    private final Repository<Book, Isbn> books;

    public CatalogService(Repository<Book, Isbn> books) {
        this.books = Objects.requireNonNull(books, "books");
    }

    public List<Book> all() {
        return books.findAll();
    }

    /** Turns a raw string into one of three typed outcomes instead of throwing at the caller. */
    public LookupResult lookup(String rawIsbn) {
        final Isbn isbn;
        try {
            isbn = new Isbn(rawIsbn);
        } catch (IllegalArgumentException e) {
            return new LookupResult.Invalid(rawIsbn, e.getMessage());
        }
        return books.findById(isbn)
                .<LookupResult>map(LookupResult.Found::new)
                .orElseGet(() -> new LookupResult.NotFound(isbn));
    }

    /** Case-insensitive substring match on the author, using a fixed locale. */
    public List<Book> byAuthor(String query) {
        String needle = query.strip().toLowerCase(Locale.ROOT);
        return books.findAll().stream()
                .filter(book -> book.author().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    /** Number of books per genre. EnumMap keeps keys in declaration order. */
    public Map<Genre, Long> countByGenre() {
        return books.findAll().stream()
                .collect(Collectors.groupingBy(
                        Book::genre,
                        () -> new EnumMap<>(Genre.class),
                        Collectors.counting()));
    }

    public IntSummaryStatistics pageStatistics() {
        return books.findAll().stream().mapToInt(Book::pages).summaryStatistics();
    }

    /** The {@code limit} newest books, newest first, ties broken by title. */
    public List<Book> newest(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative: " + limit);
        }
        return books.findAll().stream()
                .sorted(Comparator.comparingInt(Book::year).reversed().thenComparing(Book::title))
                .limit(limit)
                .toList();
    }
}
