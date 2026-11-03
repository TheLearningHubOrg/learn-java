package com.learninghub.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppTest {

    @Test
    void portDefaultsTo8080() {
        assertEquals(8080, App.parsePort(null));
        assertEquals(8080, App.parsePort(" "));
    }

    @Test
    void portIsParsedAndValidated() {
        assertEquals(9090, App.parsePort("9090"));
        assertThrows(IllegalArgumentException.class, () -> App.parsePort("http"));
        assertThrows(IllegalArgumentException.class, () -> App.parsePort("70000"));
    }
}
