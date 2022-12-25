package org.solitaire.pyramid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.io.IOHelper;
import org.solitaire.model.Card;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.pyramid.PyramidBoard.LAST_BOARD;
import static org.solitaire.pyramid.PyramidBoard.LAST_DECK;
import static org.solitaire.pyramid.PyramidBoard.build;

class PyramidBoardTest {
    private static final String TEST_FILE = "src/test/resources/pyramid-expert.txt";

    private PyramidBoard board;

    @BeforeEach
    public void setup() {
        board = build(IOHelper.loadFile(TEST_FILE));
    }

    @Test
    public void test_getMaxScore() {
        var result = board.getMaxScore(board.solve());

        assertNotNull(result);
        assertEquals(1290, result.getLeft());
    }

    @Test
    public void test_clickCard() {
        var result = board.clickCard(new Card[]{board.getCards()[24]});

        assertNotNull(result);
    }

    @Test
    public void test_click() {
        var a = buildCard(0, "Ad");
        var b = buildCard(30, "Ac");
        var c = new Card[]{a, b};

        board.click(c);

        assertNull(board.getCards()[0]);
        assertNull(board.getCards()[30]);
        assertTrue(board.getWastePile().contains(c));
    }

    @Test
    public void test_recycle() {
        var tmp = new Card[LAST_DECK - LAST_BOARD];
        System.arraycopy(board.getCards(), LAST_BOARD, tmp, 0, LAST_DECK - LAST_BOARD);
        board.click(tmp);

        assertFalse(board.drawCard().isPresent());

        board.checkDeck();
        assertTrue(board.drawCard().isPresent());
    }

    @Test
    public void test_cloneBoard() {
        var cloned = board.cloneBoard();
        assertEquals(board, cloned);
    }

    @Test
    public void test_findCardsAddingTo13() {
        var cards = board.findCardsOf13();

        assertNotNull(cards);
        assertEquals(3, cards.size());
    }

    @Test
    public void test_findOpenCards() {
        var cards = board.findOpenCards();

        assertNotNull(cards);
        assertEquals(8, cards.length);
    }

    @Test
    void test_isOpenDeckCard() {
        assertTrue(board.isOpenDeckCard(51));
        assertFalse(board.isOpenDeckCard(50));
    }

    @Test
    void test_isOpenAt() {
        assertTrue(board.isOpenAt(27));
        assertTrue(board.isOpenAt(21));
        assertFalse(board.isOpenAt(20));

        IntStream.range(21, LAST_BOARD)
                .forEach(i -> board.getCards()[i] = null);

        assertTrue(board.isOpenAt(20));
        assertTrue(board.isOpenAt(19));
        assertTrue(board.isOpenAt(18));
        assertTrue(board.isOpenAt(17));
        assertTrue(board.isOpenAt(16));
        assertTrue(board.isOpenAt(15));
        assertFalse(board.isOpenAt(14));

        IntStream.range(19, 21)
                .forEach(i -> board.getCards()[i] = null);

        assertTrue(board.isOpenAt(14));
    }

    @Test
    void test_getRow() {
        assertEquals(7, board.getRow(27));
        assertEquals(6, board.getRow(19));
        assertEquals(5, board.getRow(12));
        assertEquals(4, board.getRow(7));
        assertEquals(3, board.getRow(3));
        assertEquals(2, board.getRow(1));
        assertEquals(1, board.getRow(0));
    }

    @Test
    void test_getRow_error() {
        var ex = assertThrows(AssertionError.class, () -> board.getRow(28));

        assertNotNull(ex);
        assertEquals("Invalid board card index: 28", ex.getMessage());
    }
}