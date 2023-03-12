package org.solitaire.pyramid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;

import java.util.List;
import java.util.Objects;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Origin.BOARD;
import static org.solitaire.model.Origin.REMOVE;
import static org.solitaire.pyramid.PyramidHelper.LAST_BOARD;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.pyramid.PyramidTest.cards;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelper.useSuit;

class PyramidBoardTest {
    private PyramidBoard board;

    @BeforeEach
    void setup() {
        useSuit(false);
        board = build(cards).board();
    }

    @Test
    void test_verify() {
        var result = board.verify();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        board.deck().add(card("Ad"));

        result = board.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Duplicated card: Ad", result.get(0));

        board.deck().pop();
        board.cards()[0] = null;

        result = board.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Missing card: 2s", result.get(0));
    }

    @Test
    void test_findCandidates() {
        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("[9s, 4c]", stringOfRaws(result.get(0).cards()));

        assertEquals(-28, board.score());
    }

    @Test
    void test_updateState() {
        var candidate = board.findCandidates().get(0);
        var card = candidate.cards().get(0);

        board.updateBoard(candidate);

        assertFalse(board.isOpen(card));
        assertTrue(board.flippedDeck().isEmpty());
        assertEquals("[9s, 4c]", stringOfRaws(board.path().peek()));
        assertEquals(1, board.path().size());
        assert board.path().peek() != null;
        assertEquals("27:9s", Objects.requireNonNull(board.path().peek())[0].toString());

        card = board.deck().peek();
        var c = List.of(card, board.cards()[LAST_BOARD - 2]);
        board.updateBoard(new Candidate(c, BOARD, 0, REMOVE, 0));

        assertFalse(board.isOpen(card));
        assertEquals(23, board.deck().size());
        assertEquals("50:Kh", board.deck().peek().toString());
        assertEquals(2, board.path().size());
        assert board.path().peek() != null;
        assertEquals("51:Kc", Objects.requireNonNull(board.path().peek())[0].toString());

    }

    @Test
    void test_recycle() {
        assertNotNull(board.updateBoard(board.drawDeckCard()));
        assertEquals(23, board.deck().size());
        assertEquals(1, board.flippedDeck().size());

        while (!board.deck().isEmpty()) {
            board.flippedDeck().push(board.deck().pop());
        }
        board.recycleCount(1);
        assertNull(board.drawDeckCard());
        assertEquals(24, board.flippedDeck().size());

        board.recycleCount(2);
        assertNotNull(board.updateBoard(board.drawDeckCard()));
        assertEquals(23, board.deck().size());
        assertEquals(1, board.flippedDeck().size());
        assertEquals("51:Kc", board.flippedDeck().peek().toString());
        assertEquals("50:Kh", board.deck().peek().toString());
    }

    @Test
    void test_cloneBoard() {
        var cloned = new PyramidBoard(board);

        assertNotSame(board, cloned);
        assertTrue(reflectionEquals(board, cloned));
    }

    @Test
    void test_findCardsAddingTo13() {
        var cards = board.findCandidates();

        assertNotNull(cards);
        assertEquals(3, cards.size());
    }

    @Test
    void test_findOpenCards() {
        var cards = board.findOpenCards();

        assertNotNull(cards);
        assertEquals(8, cards.size());
    }

    @Test
    void test_isOpenDeckCard() {
        assertTrue(board.isOpenDeckCard(board.deck().peek()));
        assertFalse(board.isOpenDeckCard(board.deck().get(0)));
    }

    @Test
    void test_isOpenBoardCard() {
        assertTrue(board.isOpenBoardCard(27));
        assertTrue(board.isOpenBoardCard(22));
        assertFalse(board.isOpenBoardCard(20));
        assertFalse(board.isOpenBoardCard(28));
        assertFalse(board.isOpenBoardCard(-1));
    }

    @Test
    void test_isOpenAt() {
        assertTrue(board.isOpenAt(27));
        assertTrue(board.isOpenAt(21));
        assertFalse(board.isOpenAt(20));

        range(21, LAST_BOARD)
                .forEach(i -> board.cards()[i] = null);

        assertTrue(board.isOpenAt(20));
        assertTrue(board.isOpenAt(19));
        assertTrue(board.isOpenAt(18));
        assertTrue(board.isOpenAt(17));
        assertTrue(board.isOpenAt(16));
        assertTrue(board.isOpenAt(15));
        assertFalse(board.isOpenAt(14));

        range(19, 21)
                .forEach(i -> board.cards()[i] = null);

        assertTrue(board.isOpenAt(14));
    }

}