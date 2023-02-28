package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.CardHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.build;
import static org.solitaire.tripeaks.TriPeaksTest.cards;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.toArray;

class TriPeaksBoardTest {
    private TriPeaksBoard board;

    @BeforeEach
    void setup() {
        CardHelper.useSuit = false;
        board = build(cards).stack().peek().peek();
    }

    @Test
    void test_findCandidates() {
        var candidates = board.findCandidates();

        assertNotNull(candidates);
        assertEquals(2, candidates.size());
        assertEquals("[25:Tc, 22:Qh]", candidates.toString());
    }

    @Test
    void test_score() {
        assertEquals(1, board.score());
    }

    @Test
    void test_updateBoard() {
        var card = board.findCandidates().get(0);

        var result = board.updateBoard(card);

        assertSame(board, result);
        assertEquals(card, board.path().get(board.path().size() - 1));

        assertNull(board.updateBoard(null));
    }

    @Test
    public void test_isOpenCard() {
        var state = new TriPeaksBoard(toArray(null, null, null, null, null), null);

        assertTrue(state.isOpenCard(buildCard(0, "Ad")));
    }

    @Test
    public void test_isOpenCard_exception() {
        var state = new TriPeaksBoard(null, null);
        var card = buildCard(LAST_BOARD, "Ad");

        var ex = assertThrows(RuntimeException.class, () -> state.isOpenCard(card));

        assertNotNull(ex);
        assertEquals("Invalid card: " + card, ex.getMessage());
    }
}