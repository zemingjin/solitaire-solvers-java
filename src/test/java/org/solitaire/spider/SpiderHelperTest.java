package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.io.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.CardHelper.useSuit;
import static org.solitaire.spider.SpiderHelper.COL_LONG;
import static org.solitaire.spider.SpiderHelper.LAST_COLUMN;
import static org.solitaire.spider.SpiderHelper.LAST_DECK;
import static org.solitaire.spider.SpiderHelper.NUM_LONG;
import static org.solitaire.spider.SpiderHelper.build;

class SpiderHelperTest {
    protected static final String TEST_FILE = "games/spider/spider-122822-easy.txt";

    protected final static String[] cards = IOHelper.loadFile(TEST_FILE);

    @BeforeEach
    public void setup() {
        useSuit = false;
    }

    @Test
    public void test_build() {
        var spider = build(cards);

        assertNotNull(spider);
        assertEquals(LAST_COLUMN, spider.getBoard().size());
        assertEquals(6, spider.getBoard().get(0).cards().size());
        assertEquals(5, spider.getBoard().get(4).cards().size());
        assertEquals("5:3s", spider.getBoard().get(0).cards().get(0).toString());
        assertEquals("0:3s", spider.getBoard().get(0).cards().get(COL_LONG - 1).toString());
        assertEquals("5:3s", spider.getBoard().get(0).cards().get(0).toString());
        assertEquals("0:3s", spider.getBoard().get(0).cards().get(COL_LONG - 1).toString());
        assertEquals("28:9s", spider.getBoard().get(NUM_LONG).cards().get(0).toString());
        assertEquals("24:Ks", spider.getBoard().get(NUM_LONG).cards().get(COL_LONG - 2).toString());
        assertEquals(LAST_DECK, spider.getDeck().size());
        assertEquals("54:5s", spider.getDeck().get(0).toString());
        assertEquals("103:2s", spider.getDeck().get(LAST_DECK - 1).toString());
        assertTrue(spider.getPath().isEmpty());
    }

}