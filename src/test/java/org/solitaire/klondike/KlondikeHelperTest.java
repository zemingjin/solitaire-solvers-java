package org.solitaire.klondike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.klondike.KlondikeHelper.LAST_DECK;
import static org.solitaire.util.CardHelper.useSuit;

class KlondikeHelperTest {
    private static final String TEST_FILE = "games/klondike/klondike-122822-medium.txt";
    protected static final String[] CARDS = IOHelper.loadFile(TEST_FILE);

    @BeforeEach
    public void setup() {
        useSuit = false;
    }

    @Test
    public void test_build() {
        var klondike = KlondikeHelper.build(CARDS).stack().peek().peek();

        assertNotNull(klondike);
        assertEquals("23:Ts", klondike.deck().peek().toString());
        assertEquals("0:Jc", klondike.deck().get(0).toString());
        assertEquals(1, klondike.columns().get(0).size());
        assertEquals("24:Th", klondike.columns().get(0).get(0).toString());
        assertEquals("25:8h", klondike.columns().get(1).get(0).toString());
    }

    @Test
    public void test_clone() {
        var state = KlondikeHelper.build(CARDS).stack().peek().peek();
        var clone = new KlondikeBoard(state);

        assertTrue(reflectionEquals(clone, state));
    }

    @Test
    public void test_colStart() {
        assertEquals(LAST_DECK, KlondikeHelper.colStart(0));
        assertEquals(LAST_DECK + 1, KlondikeHelper.colStart(1));
        assertEquals(LAST_DECK + 3, KlondikeHelper.colStart(2));
        assertEquals(LAST_DECK + 6, KlondikeHelper.colStart(3));
        assertEquals(LAST_DECK + 10, KlondikeHelper.colStart(4));
        assertEquals(LAST_DECK + 15, KlondikeHelper.colStart(5));
        assertEquals(LAST_DECK + 21, KlondikeHelper.colStart(6));
    }

}