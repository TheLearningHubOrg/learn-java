package com.learninghub.catalog.service;

import com.learninghub.catalog.model.Book;
import com.learninghub.catalog.model.Genre;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogServiceTest {

    private final CatalogService catalog = new CatalogService(SampleData.repository());

    @Test
    void lookupFindsAKnownBook() {
        var found = assertInstanceOf(LookupResult.Found.class, catalog.lookup("978-1-00-000001-6"));
        assertEquals("Bytecode by Candlelight", found.book().title());
    }

    @Test
    void lookupReportsUnknownAndInvalidSeparately() {
        assertInstanceOf(LookupResult.NotFound.class, catalog.lookup("9781000000092"));
        var invalid = assertInstanceOf(LookupResult.Invalid.class, catalog.lookup("not-an-isbn"));
        assertEquals("not-an-isbn", invalid.input());
    }

    @Test
    void exhaustiveSwitchNeedsNoDefault() {
        String label = switch (catalog.lookup("9781000000023")) {
            case LookupResult.Found(Book b) -> "found " + b.year();
            case LookupResult.NotFound n -> "missing " + n.isbn();
            case LookupResult.Invalid i -> "bad " + i.reason();
        };
        assertEquals("found 2021", label);
    }

    @Test
    void searchesAuthorsCaseInsensitively() {
        List<String> titles = catalog.byAuthor("  OKAFOR ").stream().map(Book::title).toList();
        assertEquals(List.of("Bytecode by Candlelight", "Tides of the Northern Sea"), titles);
    }

    @Test
    void countsByGenreInDeclarationOrder() {
        Map<Genre, Long> counts = catalog.countByGenre();
        assertEquals(List.of(Genre.PROGRAMMING, Genre.SCIENCE, Genre.HISTORY, Genre.FICTION),
                List.copyOf(counts.keySet()));
        assertEquals(2L, counts.get(Genre.PROGRAMMING));
    }

    @Test
    void pageStatistics() {
        var stats = catalog.pageStatistics();
        assertEquals(5, stats.getCount());
        assertEquals(288, stats.getMin());
        assertEquals(530, stats.getMax());
        assertEquals(1887, stats.getSum());
    }

    @Test
    void newestSortsByYearDescending() {
        List<Integer> years = catalog.newest(3).stream().map(Book::year).toList();
        assertEquals(List.of(2025, 2024, 2023), years);
        assertTrue(catalog.newest(0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> catalog.newest(-1));
    }
}
