package org.solitaire.klondike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.klondike.KlondikeHelper.LAST_DECK;
import static org.solitaire.model.CardHelper.useSuit;

class KlondikeHelperTest {
    private static final String TEST_FILE = "games/klondike/klondike-122822-medium.txt";
    protected static final String[] CARDS = IOHelper.loadFile(TEST_FILE);

    @BeforeEach
    public void setup() {
        useSuit = false;
    }

    @Test
    public void test_build() {
        var klondike = KlondikeHelper.build(CARDS);

        assertNotNull(klondike);
        assertEquals("23:7d", klondike.getDeck().peek().toString());
        assertEquals("0:8d", klondike.getDeck().get(0).toString());
        assertEquals(1, klondike.getColumns().get(0).size());
        assertEquals("24:Th", klondike.getColumns().get(0).get(0).toString());
        assertEquals("25:8h", klondike.getColumns().get(1).get(0).toString());
    }

    @Test
    public void test_clone() {
        var klondike = KlondikeHelper.build(CARDS);
        var clone = KlondikeHelper.clone(klondike);

        assertEquals(klondike, clone);
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