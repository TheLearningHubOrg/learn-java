package com.learninghub.catalog.model;

/** Enums are full classes: they can carry fields, constructors and methods. */
public enum Genre {
    PROGRAMMING("Programming"),
    SCIENCE("Science"),
    HISTORY("History"),
    FICTION("Fiction");

    private final String label;

    Genre(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
