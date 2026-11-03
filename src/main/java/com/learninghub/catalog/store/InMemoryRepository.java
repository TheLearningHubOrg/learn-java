package com.learninghub.catalog.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A thread-safe in-memory {@link Repository}. The key is derived from the
 * entity by {@code idOf}, and {@code findAll} returns entities in {@code order}.
 *
 * <p>The wildcard types follow PECS ("producer extends, consumer super"):
 * {@code idOf} consumes a T and produces an ID, and the comparator consumes Ts.
 */
public final class InMemoryRepository<T, ID> implements Repository<T, ID> {

    private final Map<ID, T> items = new ConcurrentHashMap<>();
    private final Function<? super T, ? extends ID> idOf;
    private final Comparator<? super T> order;

    public InMemoryRepository(Function<? super T, ? extends ID> idOf, Comparator<? super T> order) {
        this.idOf = Objects.requireNonNull(idOf, "idOf");
        this.order = Objects.requireNonNull(order, "order");
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<T> findAll() {
        List<T> copy = new ArrayList<>(items.values());
        copy.sort(order);
        return List.copyOf(copy);
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity");
        items.put(idOf.apply(entity), entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        return items.remove(id) != null;
    }

    @Override
    public int count() {
        return items.size();
    }
}
