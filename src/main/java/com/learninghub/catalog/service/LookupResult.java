package com.learninghub.catalog.service;

import com.learninghub.catalog.model.Book;
import com.learninghub.catalog.model.Isbn;

/**
 * The three outcomes of an ISBN lookup. Because the interface is sealed and
 * every permitted subtype is declared in this file, a switch over a
 * LookupResult needs no default branch, and adding a fourth outcome turns
 * every non-exhaustive switch into a compile error.
 */
public sealed interface LookupResult {

    record Found(Book book) implements LookupResult {}

    record NotFound(Isbn isbn) implements LookupResult {}

    record Invalid(String input, String reason) implements LookupResult {}
}
