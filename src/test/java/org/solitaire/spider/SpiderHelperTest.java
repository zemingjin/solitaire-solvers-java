package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.execution.GameBoardTest.cards;
import static org.solitaire.spider.SpiderHelper.COL_LONG;
import static org.solitaire.spider.SpiderHelper.LAST_BOARD;
import static org.solitaire.spider.SpiderHelper.LAST_COLUMN;
import static org.solitaire.spider.SpiderHelper.LAST_DECK;
import static org.solitaire.spider.SpiderHelper.LAST_LONG;
import static org.solitaire.spider.SpiderHelper.NUM_LONG;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.spider.SpiderHelper.calcColumn;
import static org.solitaire.util.CardHelper.useSuit;

class SpiderHelperTest {
    @BeforeEach
    void setup() {
        useSuit(false);
    }

    @Test
    void test_build() {
        var state = build(cards).board();

        assertNotNull(state);
        assertEquals(LAST_COLUMN, state.columns().size());
        assertEquals(6, state.column(0).size());
        assertEquals(5, state.column(0).openAt());
        assertEquals(5, state.column(4).size());
        assertEquals(4, state.column(4).openAt());
        assertEquals("0:4h", state.column(0).get(0).toString());
        assertEquals("5:Th", state.column(0).peek().toString());
        assertEquals("18:As", state.column(3).get(0).toString());
        assertEquals("23:9s", state.column(3).peek().toString());
        assertEquals("24:Th", state.column(NUM_LONG).get(0).toString());
        assertEquals("28:3s", state.column(NUM_LONG).get(COL_LONG - 2).toString());
        assertEquals(LAST_DECK, state.deck.size());
        assertEquals("54:5s", state.deck.get(0).toString());
        assertEquals("103:9h", state.deck.get(LAST_DECK - 1).toString());
        assertTrue(state.path().isEmpty());
    }

    @SuppressWarnings("all")
    @Test
    void test_calcColumn() {
        assertEquals(0, calcColumn(0));
        assertEquals(3, calcColumn(LAST_LONG - 1));
        assertEquals(4, calcColumn(LAST_LONG));
        assertEquals(9, calcColumn(LAST_BOARD - 1));

        assertNotNull(assertThrows(IndexOutOfBoundsException.class, () -> calcColumn(LAST_BOARD)));
        assertNotNull(assertThrows(IndexOutOfBoundsException.class, () -> calcColumn(-1)));
    }

}