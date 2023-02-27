package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.solitaire.tripeaks.TriPeaksHelper.build;
import static org.solitaire.util.IOHelper.loadFile;

class TriPeaksTest {
    public static final String TEST_FILE = "games/tripeaks/tripeaks-120822-expert.txt";

    protected static final String[] cards = loadFile(TEST_FILE);
    private TriPeaks triPeaks;

    @BeforeEach
    public void setup() {
        triPeaks = build(cards);
        triPeaks.singleSolution(false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_getMaxScore() {
        var max = triPeaks.getMaxScore(triPeaks.solve());
        var items = getItemScores((List<Card>) max.getRight());

        assertNotNull(max);
        assertNotNull(items);
        assertEquals(16900, max.getLeft());
    }

    private List<String> getItemScores(List<Card> cards) {
        var score = 0;
        var sequenceCount = 0;
        var list = new ArrayList<String>();

        for (Card card : cards) {
            if (TriPeaksHelper.isFromDeck(card)) {
                sequenceCount = 0;
                list.add(card.raw());
            } else {
                sequenceCount++;
                score += (sequenceCount * 2 - 1) * 100 + triPeaks.checkPeakBonus(card, cards);
                list.add(card.raw() + ":" + score);
            }
        }
        return list;
    }

    @Test
    public void test_checks() {
        var p = triPeaks.getMaxScore(triPeaks.solve());

        assertNotNull(p);
        assertEquals(16900, p.getLeft());
    }

    @Test
    public void test_checks_null() {
        assertNotNull(assertThrows(NullPointerException.class, () -> triPeaks.getMaxScore(null)));
    }
}