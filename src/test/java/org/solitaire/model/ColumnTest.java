package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.solitaire.util.CardHelper.buildCard;

class ColumnTest {
    private Column column;

    @BeforeEach
    public void setup() {
        column = new Column();
        column.add(buildCard(0, "Ad"));
        column.add(buildCard(1, "2c"));
        column.openAt(column.size() - 1);
    }

    @Test
    public void test_clone() {
        var clone = new Column(column);

        assertNotSame(column, clone);
        assertEquals(column, clone);

        clone.remove(0);
        assertNotEquals(column, clone);
    }

    @Test
    public void test_remove() {
        column.remove(1);
        assertEquals(1, column.size());
        assertEquals(0, column.openAt());
    }

    @Test
    public void test_peek_exception() {
        assertNotNull(assertThrows(EmptyStackException.class, () -> new Column().peek()));
    }

    @Test
    public void test_pop_exception() {
        assertNotNull(assertThrows(EmptyStackException.class, () -> new Column().pop()));
    }
}