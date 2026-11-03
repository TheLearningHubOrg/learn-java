package com.learninghub.catalog.service;

import com.learninghub.catalog.model.Book;
import com.learninghub.catalog.model.Genre;
import com.learninghub.catalog.model.Isbn;
import com.learninghub.catalog.store.InMemoryRepository;
import com.learninghub.catalog.store.Repository;

import java.util.Comparator;
import java.util.List;

/** Fictional seed data. The ISBNs carry valid checksums but belong to no real book. */
public final class SampleData {

    private SampleData() {
        // static helpers only
    }

    public static List<Book> books() {
        return List.of(
                new Book(new Isbn("978-1-00-000001-6"), "Bytecode by Candlelight", "Ada Okafor", 2024, Genre.PROGRAMMING, 412),
                new Book(new Isbn("978-1-00-000002-3"), "The Patient Collector", "Lin Moreau", 2021, Genre.PROGRAMMING, 288),
                new Book(new Isbn("978-1-00-000003-0"), "Tides of the Northern Sea", "Ada Okafor", 2019, Genre.HISTORY, 530),
                new Book(new Isbn("978-1-00-000004-7"), "Small Particles, Large Questions", "Rui Tanaka", 2023, Genre.SCIENCE, 301),
                new Book(new Isbn("978-1-00-000005-4"), "The Lighthouse Ledger", "Mara Lindqvist", 2025, Genre.FICTION, 356));
    }

    public static Repository<Book, Isbn> repository() {
        Repository<Book, Isbn> repo = new InMemoryRepository<>(Book::isbn, Comparator.comparing(Book::title));
        books().forEach(repo::save);
        return repo;
    }
}
