package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.spider.SpiderHelper.COL_LONG;
import static org.solitaire.spider.SpiderHelper.LAST_COLUMN;
import static org.solitaire.spider.SpiderHelper.LAST_DECK;
import static org.solitaire.spider.SpiderHelper.NUM_LONG;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.useSuit;

class SpiderHelperTest {
    protected static final String TEST_FILE = "games/spider/spider-122922-expert.txt";

    protected final static String[] cards = IOHelper.loadFile(TEST_FILE);

    @BeforeEach
    public void setup() {
        useSuit = false;
    }

    @Test
    public void test_build() {
        var spider = build(cards);

        assertNotNull(spider);
        assertEquals(LAST_COLUMN, spider.getColumns().size());
        assertEquals(6, spider.getColumns().get(0).size());
        assertEquals(5, spider.getColumns().get(4).size());
        assertEquals("0:4h", spider.getColumns().get(0).get(0).toString());
        assertEquals("5:Th", spider.getColumns().get(0).peek().toString());
        assertEquals("18:As", spider.getColumns().get(3).get(0).toString());
        assertEquals("23:9s", spider.getColumns().get(3).peek().toString());
        assertEquals("24:Ah", spider.getColumns().get(NUM_LONG).get(0).toString());
        assertEquals("28:3s", spider.getColumns().get(NUM_LONG).get(COL_LONG - 2).toString());
        assertEquals(LAST_DECK, spider.getDeck().size());
        assertEquals("54:5s", spider.getDeck().get(0).toString());
        assertEquals("103:9h", spider.getDeck().get(LAST_DECK - 1).toString());
        assertTrue(spider.getPath().isEmpty());
    }

    @Test
    public void test_clone() {
        var spider = build(cards);
        var clone = SpiderHelper.clone(spider);

        assertNotSame(spider, clone);
        assertEquals(spider, clone);
    }

}