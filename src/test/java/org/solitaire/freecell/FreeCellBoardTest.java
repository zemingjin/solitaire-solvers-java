package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;
import org.solitaire.util.IOHelper;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.freecell.FreeCellHelper.build;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.Origin.FREECELL;
import static org.solitaire.util.BoardHelper.isNull;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.isCleared;
import static org.solitaire.util.CardHelper.suitCode;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

public class FreeCellBoardTest {
    public static final String TEST_FILE = "games/freecell/freecell-easy-020623.txt";
    private FreeCellBoard board;

    @BeforeEach
    void setup() {
        CardHelper.useSuit(false);
        board = build(IOHelper.loadFile(TEST_FILE)).board();
    }

    @Test
    void test_verifyBoard() {
        var result = board.verify();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        board.column(0).add(card("Ad"));
        result = board.verify();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void test_isCleared() {
        assertFalse(board.isSolved());

        board.columns().forEach(List::clear);

        assertTrue(board.isSolved());
    }

    @Test
    void test_clone() {
        var clone = new FreeCellBoard(board);

        assertNotSame(board, clone);
        assertEquals(board.columns(), clone.columns());
        assertTrue(reflectionEquals(board.foundations(), clone.foundations()));
        assertTrue(reflectionEquals(board.freeCells(), clone.freeCells()));
        assertEquals(board.score(), clone.score());
    }

    @Test
    void test_findCandidates() {
        board.freeCells()[0] = card("Ad");
        board.column(6).add(card("6c"));
        board.column(5).subList(1, 6).clear();
        board.column(4).add(card("Ks"));

        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertTrue(result.stream().allMatch(it -> it.cards().length == 1));
        assertEquals("17:2d", result.get(0).notation());

        fillFreeCells(TWO, card("Kd"));
        board.column(1).clear();
        result = board.findCandidates();

        assertNotNull(result);
        assertTrue(result.stream().allMatch(it -> it.cards().length == 1));
        assertEquals(9, result.size());
        assertEquals("01:6c", result.get(0).notation());
    }

    @Test
    void test_findCandidates_cascades() {
        board.updateBoard(new Candidate(toArray(board.column(6).peek()), COLUMN, 6, FOUNDATION, 0));
        board.updateBoard(new Candidate(toArray(board.column(5).peek()), COLUMN, 5, COLUMN, 6));
        board.updateBoard(new Candidate(toArray(board.column(2).peek()), COLUMN, 2, COLUMN, 5));

        var column = board.column(0);
        while (column.size() > 1) column.remove(0);

        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    void test_findCandidates_noFreeCells() {
        fillFreeCells(0, card("Kd"));

        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(it -> it.cards().length == 1));
        assertEquals("17:2d", result.get(0).notation());
    }

    @Test
    void test_findFreeCellCandidates() {
        var result = board.findFreeCellToColumnCandidates().toList();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        board.freeCells()[0] = card("Ac");

        result = board.findCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertEquals("f$:Ac", result.get(6).notation());
    }

    @Test
    void test_findColumnCandidates() {
        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertEquals("17:2d", result.get(0).notation());
    }

    @Test
    void test_findCandidateAtColumn_colNum() {
        var result = board.findCandidateAtColumn(0);

        assertNotNull(result);
        assertEquals("0_:6c", result.notation());

        while (board.column(0).size() > 1) board.column(0).pop();
        assertEquals(1, board.findCandidateAtColumn(board.column(0)).size());

        var card = board.column(6).peek();
        board.foundations()[suitCode(card)] = card;
        assertNull(board.findCandidateAtColumn(1));
    }

    @Test
    void test_findCandidateAtColumn_column() {
        var result = board.findCandidateAtColumn(board.column(0));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("6:6c", result.get(0).toString());
    }

    @Test
    void test_findColumnToFreeCellCandidates() {
        var toColumns = List.of(new Candidate(toArray(board.column(1).peek()), COLUMN, 1, COLUMN, 7));

        var candidates = board.findColumnToFreeCellCandidates(toColumns).toList();

        assertEquals(6, candidates.size());
    }

    @Test
    void test_cleanupCandidates() {
        var a = toArray(card("Ad"));
        var b = toArray(card("Ts"));
        var candidates = Arrays.asList(
                new Candidate(a, FREECELL, 0, FOUNDATION, 0),
                new Candidate(a, COLUMN, 0, COLUMN, 3),
                new Candidate(b, FREECELL, 0, COLUMN, 3),
                new Candidate(b, COLUMN, 0, FOUNDATION, 0));

        var result = board.cleanupCandidates(candidates);
        assertEquals(2, result.size());
        assertEquals(FOUNDATION, result.get(0).target());
        assertEquals(FOUNDATION, result.get(1).target());
    }

    @Test
    void test_removeFromOrigin() {
        var candidate = board.findCandidates().get(0);
        var result = board.removeFromOrigin(candidate);

        assertNotNull(result);
        assertTrue(reflectionEquals(candidate, result));
        assertEquals(6, board.column(candidate.from()).size());
        assertFalse(board.column(candidate.from()).contains(candidate.peek()));
    }

    @Test
    void test_removeFromOrigin_freecell() {
        board.freeCells()[0] = card("Ad");
        var candidates = board.findCandidates();
        var candidate = candidates.get(0);
        var result = board.removeFromOrigin(candidate);

        assertNotNull(result);
        assertTrue(reflectionEquals(candidate, result));
        assertNull(board.freeCells()[candidate.from()]);
    }

    @Test
    void test_moveToTarget() {
        var candidate = board.findCandidates().get(0);
        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertTrue(isCleared(board.freeCells()));
        assertEquals(7, result.column(candidate.to()).size());
        assertEquals("13:2d", result.column(candidate.to()).peek().toString());
        assertEquals(1, board.path().size());
        assertEquals("17:2d", board.path().get(0));
        assertTrue(Arrays.stream(board.freeCells()).allMatch(isNull));

        result = board.updateBoard(Candidate.buildColumnCandidate(candidate, 0));
        assertEquals(8, result.column(0).size());
        assertEquals("13:2d", result.column(0).peek().toString());
        assertEquals(2, board.path().size());

        var card = board.column(0).peek();
        var ex = assertThrows(RuntimeException.class,
                () -> board.moveToTarget(new Candidate(toArray(card), COLUMN, 0, DECKPILE, -1)));
        assertEquals("Invalid candidate target: 0^:2d", ex.getMessage());
    }

    @Test
    void test_moveToTarget_foundation() {
        var results = board.findCandidates();
        var candidate = results.get(1);

        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertEquals(6, board.column(candidate.from()).size());
        assertEquals("6:6c", result.freeCells()[candidate.from()].toString());

    }

    @Test
    void test_moveToTarget_freeCell() {
        var results = board.findCandidates();
        var candidate = results.get(0);

        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertEquals(6, board.column(candidate.from()).size());
        assertEquals("13:2d", result.column(candidate.to()).peek().toString());
    }

    @Test
    void test_moveToTarget_freeCell_fail() {
        var candidate = new Candidate(toArray(board.column(0).peek()), COLUMN, 0, FREECELL, 0);

        fillFreeCells(0, card("Ad"));

        assertThrows(NoSuchElementException.class, () -> board.updateBoard(candidate));
    }

    @Test
    void test_updateBoard() {
        var card = board.column(0).peek();

        var result = board.updateBoard(buildCandidate(0, COLUMN, FREECELL, card));

        assertNotNull(result);
        assertFalse(board.column(0).contains(card));
        assertSame(card, board.freeCells()[0]);

        result = board.updateBoard(new Candidate(toArray(card), FREECELL, 0, COLUMN, 0));
        assertNotNull(result);
        assertTrue(board.column(0).contains(card));
        assertNull(board.freeCells()[0]);

        result = board.updateBoard(new Candidate(toArray(card), COLUMN, 0, FOUNDATION, -1));
        assertNotNull(result);
        assertFalse(board.column(0).contains(card));
        assertSame(card, board.foundations()[suitCode(card)]);

        var crd = board.column(0).peek();
        assertThrows(RuntimeException.class,
                () -> board.updateBoard(new Candidate(toArray(crd), DECKPILE, 0, COLUMN, -1)));
    }

    @Test
    void test_isFoundationable() {
        var card = card("Ad");
        assertTrue(board.isFoundationable(card));

        board.foundations()[suitCode(card)] = card;

        assertTrue(board.isFoundationable(card("2d")));
        assertFalse(board.isFoundationable(card("2c")));
    }

    @Test
    void test_isAppendableToColumn() {
        var card = board.column(1).peek();

        assertTrue(board.isAppendableToColumn(7, buildCandidate(1, COLUMN, COLUMN, card)));

        card = board.column(6).peek();
        assertFalse(board.isAppendableToColumn(1, buildCandidate(6, COLUMN, COLUMN, card)));

        var cards = toArray(card("8d"), card("7s"), card("6d"), card("5c"), card("4h"), card("3s"));
        fillFreeCells(0, card);
        var result = board.getTargetCandidates(buildCandidate(0, COLUMN, cards)).toList();
        assertTrue(result.isEmpty());

        board.column(6).clear();
        assertFalse(board.isAppendableToColumn(1, buildCandidate(6, COLUMN, COLUMN, card)));

        card = board.column(0).peek();
        assertTrue(board.isAppendableToColumn(6, buildCandidate(0, COLUMN, toArray(card))));

        board.column(0).subList(1, board.column(0).size()).clear();
        assertFalse(board.isAppendableToColumn(6, buildCandidate(0, COLUMN, toArray(card))));

        board.column(0).clear();
        assertTrue(board.isAppendableToColumn(0, new Candidate(toArray(card), FREECELL, 0, COLUMN, 0)));
    }

    @Test
    void test_isMovable() {
        var cards = toArray(card("5d"));
        var candidate = buildCandidate(0, COLUMN, cards);

        assertTrue(board.isMovable(candidate, 0));

        cards = toArray(card("6d"), card("5d"), card("4d"), card("3d"), card("2d"), card("Ad"));
        candidate = buildCandidate(0, COLUMN, cards);

        assertFalse(board.isMovable(candidate, 0));

        assertThrows(NullPointerException.class, () -> board.isMovable(null, 0));
    }


    @Test
    void test_maxCardsToMove() {
        var card = board.column(0).peek();

        assertEquals(5, board.maxCardsToMove(1));

        board.freeCells()[0] = card;
        assertEquals(4, board.maxCardsToMove(1));

        board.freeCells()[1] = card;
        assertEquals(3, board.maxCardsToMove(1));

        board.freeCells()[2] = card;
        assertEquals(2, board.maxCardsToMove(1));

        board.column(7).clear();
        assertEquals(3, board.maxCardsToMove(1));

        board.column(6).clear();
        assertEquals(4, board.maxCardsToMove(1));
    }

    private void fillFreeCells(int from, Card card) {
        range(from, board.freeCells().length).forEach(i -> board.freeCells()[i] = card);
    }

    @Test
    void test_getCardsInSequence() {
        assertEquals(ONE, board.getCardsInSequence(board.column(0)));

        board.column(0).add(card("5d"));
        assertEquals(TWO, board.getCardsInSequence(board.column(0)));

        board.column(0).clear();
        assertEquals(ZERO, board.getCardsInSequence(board.column(0)));
    }

    @Test
    void test_score() {
        var card = board.column(6).peek();
        board.updateBoard(buildCandidate(6, COLUMN, FOUNDATION, card));

        assertEquals(-5, board.score());

        board.score(0);
        fillFreeCells(0, card("Js"));
        assertEquals(-11, board.score());

        board.column(7).clear();
        board.score(0);
        assertEquals(-5, board.score());

        board.column(1).clear();
        board.score(0);
        assertThrows(NoSuchElementException.class, () -> board.score());
    }

    @Test
    void test_calcBlockerScore() {
        var card = card("Qc");

        board.foundations()[suitCode(card)] = card;
        assertEquals(7, board.calcBlockerScore());

        board.foundations()[suitCode(card)] = card("Kc");
        assertEquals(4, board.calcBlockerScore());
    }
}