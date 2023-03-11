package org.solitaire.pyramid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.pyramid.PyramidHelper.cardAt;
import static org.solitaire.pyramid.PyramidHelper.getScore;
import static org.solitaire.pyramid.PyramidHelper.isRowCleared;
import static org.solitaire.pyramid.PyramidHelper.row;
import static org.solitaire.pyramid.PyramidTest.cards;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelper.useSuit;

class PyramidHelperTest {
    private Pyramid pyramid;

    @BeforeEach
    void setup() {
        useSuit(false);
        singleSolution(false);
        pyramid = build(cards);
        pyramid.isPrint(false);
    }

    @Test
    void test_build() {
        assertNotNull(pyramid);
        assertEquals(28, Objects.requireNonNull(pyramid.stack().peek().peek()).cards().length);
        assertEquals(24, Objects.requireNonNull(pyramid.stack().peek().peek()).deck().size());

        pyramid.solve();
        var maxScore = pyramid.maxScore();
        assertNotNull(maxScore);
        assertEquals(1290, maxScore.getLeft());
        assertEquals(28, maxScore.getRight().size());
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
        pyramid.solve();
        var list = (List<Card[]>) pyramid.shortestPath();

        assertEquals(30, getScore(row(cardAt(list.get(9)).at()), 9, list));
        assertEquals(55, getScore(row(cardAt(list.get(16)).at()), 16, list));
        assertEquals(5, getScore(row(cardAt(list.get(17)).at()), 17, list));
    }

    @Test
    void test_isRowCleared() {
        pyramid.solve();
        var list = (List<Card[]>) pyramid.shortestPath();

        assertTrue(isRowCleared(row(cardAt(list.get(9)).at()), 9, list));
        assertTrue(isRowCleared(row(cardAt(list.get(16)).at()), 16, list));
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
    }

}