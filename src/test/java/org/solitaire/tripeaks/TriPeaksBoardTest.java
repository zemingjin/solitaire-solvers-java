package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.tripeaks.TriPeaksHelper.INI_COVERED;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_DECK;
import static org.solitaire.tripeaks.TriPeaksHelper.build;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.CardHelperTest.ZERO;
import static org.solitaire.util.IOHelper.loadFile;

class TriPeaksBoardTest {
    static final String TEST_FILE = "games/tripeaks/tripeaks-120822-expert.txt";
    static final String[] cards = loadFile(TEST_FILE);

    private TriPeaksBoard board;

    @BeforeEach
    void setup() {
        useSuit(false);
        board = build(cards).board();
    }

    @Test
    void test_findBlockersInDeck() {
        assertEquals(4, board.findBlockersInDeck(card("Qs")));

        range(LAST_BOARD + 2, LAST_DECK).forEach(i -> board.cards()[i] = null);
        assertEquals(1, board.findBlockersInDeck(card("Qs")));
    }

    @Test
    void test_calcCardBlockers() {
        assertEquals(12, board.calcCardBlockers(card("Ts")));
        assertEquals(0, board.calcCardBlockers(card("Jd")));
        assertEquals(4, board.calcCardBlockers(card("Ac")));
        assertEquals(1, board.calcCardBlockers(card("Kh")));
    }

    @Test
    void test_getCoveringCards() {
        assertTrue(board.getCoveringCards(board.cards()[27]).toList().isEmpty());
    }

    @Test
    void test_score() {
        assertEquals(-70, board.score());
    }

    @Test
    void test_verify() {
        var result = board.verify();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        board.cards()[0] = board.wastePile().peek();

        result = board.verify();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("[Duplicated card: Jc, Missing card: 5c]", result.toString());

        board.cards()[0] = null;

        result = board.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Missing card: 5c", result.get(0));
    }

    @Test
    void test_findCandidates() {
        var candidates = board.findCandidates();

        assertNotNull(candidates);
        assertEquals(2, candidates.size());
        assertEquals("[25:Tc, 22:Qh]", candidates.toString());
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
    void test_isOpenCard() {
        var state = new TriPeaksBoard(toArray(null, null, null, null, null), null);

        assertTrue(state.isOpenCard(buildCard(0, "Ad")));
    }

    @Test
    void test_isOpenCard_exception() {
        var state = new TriPeaksBoard(null, null);
        var card = buildCard(LAST_BOARD, "Ad");

        var ex = assertThrows(RuntimeException.class, () -> state.isOpenCard(card));

        assertNotNull(ex);
        assertEquals("Invalid card: " + card, ex.getMessage());
    }

    @Test
    void test_isOpenCard_allBoardCards() {
        assertTrue(Arrays.stream(board.cards(), ZERO, INI_COVERED).noneMatch(it -> board.isOpenCard(it)));
        assertTrue(Arrays.stream(board.cards(), INI_COVERED, LAST_BOARD).allMatch(board::isOpenCard));
    }

    @Test
    void test_row() {
        assertEquals(0, board.row(-1));
        assertEquals(1, board.row(0));
        assertEquals(1, board.row(1));
        assertEquals(1, board.row(2));
        assertEquals(2, board.row(3));
        assertEquals(2, board.row(8));
        assertEquals(3, board.row(9));
        assertEquals(3, board.row(INI_COVERED - 1));
        assertEquals(4, board.row(INI_COVERED));
        assertEquals(4, board.row(LAST_BOARD - 1));
        assertEquals(0, board.row(LAST_BOARD));
    }
}