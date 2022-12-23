package org.solitaire.utils;

import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.model.CardHelper.cloneArray;
import static org.solitaire.model.CardHelper.cloneList;
import static org.solitaire.model.CardHelper.string;

class CardHelperTest {
    @Test
    public void test_cloneArray() {
        var original = new Card[]{buildCard(1, "3d"), buildCard(33, "As")};
        var cloned = cloneArray(original);

        assertNotNull(cloned);
        assertArrayEquals(original, cloned);
    }

    @Test
    public void test_cloneArray_exception() {
        assertNotNull(assertThrows(NullPointerException.class, () -> cloneArray(null)));
    }

    @Test
    public void test_cloneList() {
        var original = Arrays.asList("abc", "efg");
        var cloned = cloneList(original);

        assertNotNull(cloned);
        assertEquals(original, cloned);

        assertNull(cloneList(null));
    }

    @Test
    public void test_buildCard() {
        var a = buildCard(1, "Ah");
        var b = buildCard(2, "9c");

        assertEquals("1:" + a.getRaw(), a.toString());
        assertEquals("2:" + b.getRaw(), b.toString());
    }

    @Test
    public void test_buildCard_exception() {
        assertNotNull(assertThrows(AssertionError.class, () -> buildCard(1, "Bh")));
    }

    @Test
    public void test_string() {
        var a = buildCard(1, "Ah");
        var b = buildCard(2, "9c");

        assertEquals(a.getRaw() + ' ' + b.getRaw(), string(Arrays.asList(a, b)));
    }
}