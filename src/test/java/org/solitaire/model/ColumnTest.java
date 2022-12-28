package org.solitaire.model;

import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColumnTest {

    @Test
    public void test_peek_exception() {
        assertNotNull(assertThrows(EmptyStackException.class, () -> new Column().peek()));
    }

    @Test
    public void test_pop_exception() {
        assertNotNull(assertThrows(EmptyStackException.class, () -> new Column().pop()));
    }
}