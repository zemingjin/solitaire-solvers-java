package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.CardHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.util.CardHelper.useSuit;

public class CardTest {
    private static Card buildCard(String value) {
        return CardHelper.buildCard(30, value);
    }

    @BeforeEach
    public void setUp() {
        useSuit(false);
    }

    @Test
    public void test_isHigherOfSameColor() {
        var card = buildCard("2d");

        assertTrue(card.isHigherOfSameColor(buildCard("Ad")));
        assertFalse(card.isHigherOfSameColor(buildCard("Ac")));
        assertFalse(card.isHigherOfSameColor(buildCard("3d")));
        assertTrue(buildCard("3d").isHigherOfSameColor(card));
    }

    @Test
    void test_isHigherOfSameSuit() {
        var card = buildCard("2d");

        assertTrue(card.isHigherOfSameSuit(buildCard("Ad")));
        assertFalse(card.isHigherOfSameSuit(buildCard("Ac")));
        assertFalse(card.isHigherOfSameSuit(buildCard("3d")));
        assertFalse(card.isHigherOfSameSuit(buildCard("3s")));
    }

    @Test
    public void test_isAdjacent() {
        assertTrue(buildCard("3d").isAdjacent(buildCard("2c")));
        assertTrue(buildCard("6d").isAdjacent(buildCard("5c")));
        assertTrue(buildCard("2d").isAdjacent(buildCard("Ac")));
        assertTrue(buildCard("Kd").isAdjacent(buildCard("Ac")));
        assertTrue(buildCard("Ad").isAdjacent(buildCard("Kc")));
    }

    @Test
    public void test_getters() {
        var card = buildCard("Ah");
        assertEquals(30, card.at());
        assertEquals("Ah", card.raw());
    }

    @Test
    public void test_equals() {
        var a = buildCard("Ah");
        var b = CardHelper.buildCard(10, "Ah");

        assertEquals(a, b);

        b = buildCard("Ad");
        assertNotEquals(a, b);
    }

    @Test
    public void test_hashCode() {
        var a = buildCard("Ah");
        var b = buildCard("Ah");

        assertEquals(a.hashCode(), b.hashCode());

        b = buildCard("Ad");
        assertNotEquals(a.hashCode(), b.hashCode());

        b = CardHelper.buildCard(31, "Ah");
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void test_isKing() {
        assertTrue(buildCard("Kh").isKing());
        assertTrue(buildCard("Ah").isNotKing());
        assertFalse(buildCard("Kh").isNotKing());
    }

    @Test
    public void test_isDifferentColor() {
        var a = buildCard("3d");
        var b = buildCard("2h");

        assertFalse(a.isDifferentColor(b));

        a = buildCard("3h");
        assertFalse(a.isDifferentColor(b));

        b = buildCard("2s");
        assertTrue(a.isDifferentColor(b));

        b = buildCard("2c");
        assertTrue(a.isDifferentColor(b));
    }

    @Test
    public void test_isHigherOrderWithDifferentColor() {
        var a = buildCard("3d");
        var b = buildCard("2s");

        assertTrue(a.isHigherWithDifferentColor(b));

        a = buildCard("3h");
        b = buildCard("2c");
        assertTrue(a.isHigherWithDifferentColor(b));

        b = buildCard("2h");
        assertFalse(a.isHigherWithDifferentColor(b));
    }

    @Test
    public void test_isDifferentColor_exception() {
        var a = buildCard("3d");
        var b = buildCard("5f");

        assertNotNull(assertThrows(RuntimeException.class, () -> a.isDifferentColor(b)));
    }

    @Test
    public void test_isLowerWithSameSuit() {
        var a = buildCard("3d");
        var b = buildCard("4d");

        assertTrue(a.isLowerWithSameSuit(b));

        b = buildCard("5d");
        assertFalse(a.isLowerWithSameSuit(b));

        b = buildCard("4h");
        assertFalse(a.isLowerWithSameSuit(b));
    }
}
