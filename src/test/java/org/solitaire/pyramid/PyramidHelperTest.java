package org.solitaire.pyramid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.execution.SolveExecutor.singleSolution;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.pyramid.PyramidHelper.cardAt;
import static org.solitaire.pyramid.PyramidHelper.countCardsCleared;
import static org.solitaire.pyramid.PyramidHelper.getScore;
import static org.solitaire.pyramid.PyramidHelper.isRowCleared;
import static org.solitaire.pyramid.PyramidHelper.row;
import static org.solitaire.pyramid.PyramidHelper.scoringOnly;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelper.useSuit;

class PyramidHelperTest {
    protected static final String TEST_FILE = "games/pyramid/pyramid-expert-030523.txt";
    private static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private static Pyramid pyramid;

    @BeforeEach
    void setup() {
        useSuit(false);
        if (pyramid == null) {
            pyramid = build(cards);
            singleSolution(false);
            Pyramid.isPrint(false);
            pyramid.solve();
        }
    }

    @Test
    void test_build() {
        assertNotNull(pyramid);

        assertEquals(46, pyramid.longestPath().size());
        assertEquals(44, pyramid.shortestPath().size());

        var maxScore = pyramid.maxScore();
        assertNotNull(maxScore);
        assertEquals(1290, maxScore.getLeft());
        assertEquals(45, maxScore.getRight().size());
    }

    @Test
    void test_cardAt() {
        var a = buildCard(1, "As");
        var b = buildCard(27, "Qs");

        assertSame(b, cardAt(toArray(a, b)));
        assertSame(b, cardAt(toArray(b, a)));

        b = buildCard(30, "Qs");

        assertSame(a, cardAt(toArray(a, b)));
        assertSame(a, cardAt(toArray(b, a)));

        b = buildCard(1, "Qs");

        assertSame(a, cardAt(toArray(a, b)));
        assertSame(b, cardAt(toArray(b, a)));
    }


    @Test
    void test_getScore() {
        var list = scoringOnly(pyramid.shortestPath());

//        var total = new AtomicInteger(0);
//
//        for (int i = 0; i < list.size(); i++) {
//            total.set(total.get() + getClickScore(i, list));
//            System.out.printf("%d: %d%s\n", i, total.get(), Arrays.toString(list.get(i)));
//        }
//
        assertEquals(5, getScore(row(cardAt(list.get(3)).at()), 3, list));
        assertEquals(30, getScore(row(cardAt(list.get(12)).at()), 12, list));
        assertEquals(55, getScore(row(cardAt(list.get(15)).at()), 15, list));

        assertEquals(1290, getScore(pyramid.shortestPath()).getLeft());
    }

    @Test
    void test_isRowCleared() {
        var list = scoringOnly(pyramid.shortestPath());

        assertEquals(7, countCardsCleared(7, 12, list));
        assertTrue(isRowCleared(row(cardAt(list.get(12)).at()), 12, list));
        assertEquals(7, countCardsCleared(7, 25, list));
        assertTrue(isRowCleared(row(cardAt(list.get(19)).at()), 19, list));
    }

    @Test
    void test_row() {
        assertEquals(7, row(27));
        assertEquals(6, row(19));
        assertEquals(5, row(12));
        assertEquals(4, row(7));
        assertEquals(3, row(3));
        assertEquals(2, row(1));
        assertEquals(1, row(0));
        assertThrows(RuntimeException.class, () -> row(-1));
        assertThrows(RuntimeException.class, () -> row(28));
    }

}