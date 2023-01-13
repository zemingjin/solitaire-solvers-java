package org.solitaire.pyramid;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.pyramid.PyramidHelper.cardAt;
import static org.solitaire.pyramid.PyramidHelper.getScore;
import static org.solitaire.pyramid.PyramidHelper.isRowCleared;
import static org.solitaire.pyramid.PyramidHelper.row;
import static org.solitaire.pyramid.PyramidTest.cards;
import static org.solitaire.util.CardHelper.buildCard;

@SuppressWarnings("unchecked rawtypes")
class PyramidHelperTest {
    private Pyramid pyramid;
    private Pair<Integer, List> maxScore;
    private List<Card[]> list;

    @BeforeEach
    void setup() {
        CardHelper.useSuit = false;
        pyramid = build(cards);
        maxScore = pyramid.getMaxScore(pyramid.solve());
        list = (List<Card[]>) maxScore.getRight();
    }

    @Test
    public void test_build() {
        assertNotNull(pyramid);
        assertEquals(28, pyramid.initState().cards().length);
        assertEquals(24, pyramid.initState().deck().size());

        assertNotNull(maxScore);
        assertEquals(1290, maxScore.getLeft());
        assertEquals(28, maxScore.getRight().size());
    }

    @Test
    public void test_cardAt() {
        var a = buildCard(1, "As");
        var b = buildCard(27, "Qs");

        assertSame(b, cardAt(new Card[]{a, b}));
        assertSame(b, cardAt(new Card[]{b, a}));

        b = buildCard(30, "Qs");

        assertSame(a, cardAt(new Card[]{a, b}));
        assertSame(a, cardAt(new Card[]{b, a}));
    }


    @Test
    public void test_getScore() {
        assertEquals(30, getScore(row(cardAt(list.get(9)).at()), 9, list));
        assertEquals(55, getScore(row(cardAt(list.get(16)).at()), 16, list));
        assertEquals(5, getScore(row(cardAt(list.get(17)).at()), 17, list));
    }

    @Test
    public void test_isRowCleared() {
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