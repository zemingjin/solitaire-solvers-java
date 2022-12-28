package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.build;
import static org.solitaire.util.IOHelper.loadFile;

class TriPeaksTest {
    public static final String TEST_FILE = "games/tripeaks/tripeaks-120822-expert.txt";

    private final String[] cards = loadFile(TEST_FILE);
    private TriPeaks board;

    @BeforeEach
    public void setup() {
        board = (TriPeaks) build(cards);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_getMaxScore() {
        var max = board.getMaxScore(board.solve());
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
                score += (sequenceCount * 2 - 1) * 100 + board.checkPeakBonus(card, cards);
                list.add(card.raw() + ":" + score);
            }
        }
        return list;
    }

    @Test
    public void test_findAdjacentCards() {
        var openCards = board.findBoardCards();

        assertNotNull(openCards);
        assertFalse(openCards.isEmpty());
        assertEquals(2, openCards.size());
        assertTrue(openCards.get(0).toString().startsWith("25:T"));
        assertTrue(openCards.get(1).toString().startsWith("22:Q"));
    }

    @Test
    public void test_isOpenCard() {
        var board = TriPeaks.builder().cards(new Card[]{null, null, null, null, null}).build();

        assertTrue(board.isOpenCard(buildCard(0, "Ad")));
    }

    @Test
    public void test_isOpenCard_exception() {
        var board = TriPeaks.builder().build();
        var card = buildCard(LAST_BOARD, "Ad");

        var ex = assertThrows(RuntimeException.class, () -> board.isOpenCard(card));

        assertNotNull(ex);
        assertEquals("Invalid card: " + card.toString(), ex.getMessage());
    }

    @Test
    public void test_checks() {
        var p = board.getMaxScore(board.solve());

        assertNotNull(p);
        assertEquals(16900, p.getLeft());
    }

    @Test
    public void test_checks_null() {
        assertNotNull(assertThrows(NullPointerException.class, () -> board.getMaxScore(null)));
    }
}