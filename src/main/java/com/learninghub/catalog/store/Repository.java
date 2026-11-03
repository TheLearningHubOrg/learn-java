package com.learninghub.catalog.store;

import java.util.List;
import java.util.Optional;

/**
 * A generic storage port. {@code T} is the entity type, {@code ID} its key.
 * Callers depend on this interface, never on a concrete store.
 */
public interface Repository<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    /** Inserts or replaces the entity and returns it. */
    T save(T entity);

    boolean deleteById(ID id);

    int count();

    /** Default methods let an interface grow without breaking implementers. */
    default boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}
