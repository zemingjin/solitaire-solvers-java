package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.spider.SpiderHelperTest.cards;

class SpiderTest {
    private Spider spider;

    @BeforeEach
    public void setup() {
        spider = build(cards);
    }

    @Test
    public void test_solve() {
        var result = spider.solve();

        assertNull(result);
    }

    @Test
    public void test_getMaxScore() {
        var result = spider.getMaxScore(spider.solve());

        assertNotNull(result);
        assertEquals(0, result.getLeft());
        assertNotNull(result.getRight());
        assertTrue(result.getRight().isEmpty());
    }
}