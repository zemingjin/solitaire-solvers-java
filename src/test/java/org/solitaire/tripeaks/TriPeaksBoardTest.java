package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.io.IOHelper.loadFile;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.build;

class TriPeaksBoardTest {
    public static final String TEST_FILE = "src/test/resources/tripeaks-easy.txt";

    private final String[] cards = loadFile(TEST_FILE);
    private TriPeaksBoard board;

    @BeforeEach
    public void setup() {
        board = (TriPeaksBoard) build(cards);
    }

    @Test
    public void test_findAdjacentCards() {
        var openCards = board.findBoardCards();

        assertNotNull(openCards);
        assertFalse(openCards.isEmpty());
        assertEquals(2, openCards.size());
        assertTrue(openCards.get(0).toString().startsWith("24:2"));
        assertTrue(openCards.get(1).toString().startsWith("27:4"));
    }

    @Test
    public void test_isOpenCard() {
        var board = TriPeaksBoard.builder().cards(new Card[]{null, null, null, null, null}).build();

        assertTrue(board.isOpenCard(buildCard(0, "Ad")));
    }

    @Test
    public void test_isOpenCard_exception() {
        var board = TriPeaksBoard.builder().build();
        var card = buildCard(LAST_BOARD, "Ad");

        var ex = assertThrows(RuntimeException.class, () -> board.isOpenCard(card));

        assertNotNull(ex);
        assertEquals("Invalid card: " + card.toString(), ex.getMessage());
    }

    @Test
    public void test_checks() {
        var p = board.getMaxScore(board.solve());

        assertNotNull(p);
        assertEquals(17800, p.getLeft());
    }

    @Test
    public void test_checks_null() {
        assertNotNull(assertThrows(NullPointerException.class, () -> board.getMaxScore(null)));
    }
}