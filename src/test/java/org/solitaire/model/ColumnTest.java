package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.solitaire.util.CardHelper.card;

class ColumnTest {
    private Column column;

    @BeforeEach
    void setup() {
        column = new Column();
        column.add(card("Ad"));
        column.add(card("2c"));
        column.openAt(column.size() - 1);
    }

    @Test
    void test_clone() {
        var clone = new Column(column);

        assertNotSame(column, clone);
        assertEquals(column, clone);

        clone.remove(0);
        assertNotEquals(column, clone);
    }

    @Test
    void test_remove() {
        column.add(card("Ts"));
        column.openAt(2);
        column.remove(1);
        assertEquals(2, column.size());
        assertEquals(1, column.openAt());

        column.add(card("2c"));
        column.openAt(1);

        column.remove(0);
        assertEquals(2, column.size());
        assertEquals(0, column.openAt());
    }

    @Test
    void test_pop() {
        column.add(card("Ad"));
        column.openAt(column.size() - 1);
        column.pop();
        assertEquals(2, column.size());
        assertEquals(1, column.openAt());
    }

    @Test
    void test_peek_exception() {
        assertNotNull(assertThrows(EmptyStackException.class, () -> new Column().peek()));
    }

    @Test
    void test_pop_exception() {
        assertNotNull(assertThrows(EmptyStackException.class, () -> new Column().pop()));
    }
}