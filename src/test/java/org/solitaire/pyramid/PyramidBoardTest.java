package org.solitaire.pyramid;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.io.IOHelper;
import org.solitaire.model.Card;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.pyramid.PyramidBoard.LAST_BOARD;
import static org.solitaire.pyramid.PyramidBoard.build;

class PyramidBoardTest {
    private static final String TEST_FILE = "src/test/resources/pyramid-easy.txt";

    private PyramidBoard board;

    @BeforeEach
    public void setup() {
        board = build(IOHelper.loadFile(TEST_FILE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_getMaxScore() {
        var result = board.getMaxScore(board.solve());
        var counts = getItemCounts((List<Card[]>) result.getRight());

        assertNotNull(result);
        assertEquals(1285, result.getLeft());
    }

    private List<String> getItemCounts(List<Card[]> list) {
        return IntStream.range(0, list.size())
                .filter(i -> list.get(i).length > 1 || board.isKing(list.get(i)[0]))
                .mapToObj(i -> Pair.of(board.getClickScore(i, list), list.get(i)))
                .map(it -> Arrays.toString(it.getRight()) + ": " + it.getLeft())
                .toList();
    }

    @Test
    public void test_clickCard() {
        var result = board.clickCard(new Card[]{board.getCards()[24]});

        assertNotNull(result);
    }

    @Test
    public void test_click() {
        var card = board.getDeck().peek();
        var c = new Card[]{card};

        assertEquals(51, card.at());
        assertFalse(board.isKing(card));

        board.click(c);

        assertFalse(board.isOpen(card));
        assertEquals(1, board.getFlippedDeck().size());
        assertTrue(board.getPath().contains(c));

        card = board.getDeck().peek();
        c = new Card[]{card, board.getCards()[LAST_BOARD - 1]};

        assertEquals(50, card.at());
        assertFalse(board.isKing(card));

        board.click(c);

        assertFalse(board.isOpen(card));
        assertEquals(2, board.getFlippedDeck().size());
        assertTrue(board.getPath().contains(c));
    }

    @Test
    public void test_recycle() {
        while (!board.getDeck().isEmpty()) {
            board.getFlippedDeck().push(board.getDeck().pop());
        }
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
        assertEquals(5, cards.size());
    }

    @Test
    public void test_findOpenCards() {
        var cards = board.findOpenCards();

        assertNotNull(cards);
        assertEquals(8, cards.length);
    }

    @Test
    void test_isOpenDeckCard() {
        assertTrue(board.isOpenDeckCard(board.getDeck().peek()));
        assertFalse(board.isOpenDeckCard(board.getDeck().get(0)));
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