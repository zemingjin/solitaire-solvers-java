package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardTest {
    private static Card buildCard(String value) {
        return CardHelper.buildCard(30, value);
    }

    @BeforeEach
    public void setUp() {
        CardHelper.useSuit = false;
    }

    @Test
    public void test_Card() {
        assertThrows(AssertionError.class, () -> buildCard("ab"));
        assertThrows(AssertionError.class, () -> buildCard("as"));
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
        assertEquals(30, card.getAt());
        assertEquals("Ah", card.getRaw());
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
}
