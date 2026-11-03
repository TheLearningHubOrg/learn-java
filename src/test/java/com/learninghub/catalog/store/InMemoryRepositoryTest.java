package com.learninghub.catalog.store;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryTest {

    record Item(int id, String name) {}

    private final Repository<Item, Integer> repo =
            new InMemoryRepository<>(Item::id, Comparator.comparing(Item::name));

    @Test
    void savesFindsAndOrders() {
        repo.save(new Item(2, "b"));
        repo.save(new Item(1, "c"));
        repo.save(new Item(3, "a"));

        assertEquals(3, repo.count());
        assertEquals("c", repo.findById(1).orElseThrow().name());
        assertEquals(List.of("a", "b", "c"), repo.findAll().stream().map(Item::name).toList());
    }

    @Test
    void saveReplacesByKey() {
        repo.save(new Item(1, "old"));
        repo.save(new Item(1, "new"));
        assertEquals(1, repo.count());
        assertEquals("new", repo.findById(1).orElseThrow().name());
    }

    @Test
    void deleteAndExists() {
        repo.save(new Item(1, "x"));
        assertTrue(repo.existsById(1));
        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
        assertFalse(repo.existsById(1));
    }

    @Test
    void findAllReturnsAnUnmodifiableSnapshot() {
        repo.save(new Item(1, "x"));
        List<Item> snapshot = repo.findAll();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new Item(2, "y")));
        repo.save(new Item(2, "y"));
        assertEquals(1, snapshot.size());
    }
}
