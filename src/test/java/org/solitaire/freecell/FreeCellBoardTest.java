package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
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
import static org.solitaire.model.Candidate.candidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.Origin.FREECELL;
import static org.solitaire.util.BoardHelper.isNull;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.isCleared;
import static org.solitaire.util.CardHelper.suitCode;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelperTest.TWO;

public class FreeCellBoardTest {
    public static final String TEST_FILE = "games/freecell/freecell-easy-020623.txt";
    private FreeCellBoard board;

    @BeforeEach
    void setup() {
        CardHelper.useSuit(false);
        board = build(IOHelper.loadFile(TEST_FILE)).board();
        board.isInSequence(Card::isHigherWithDifferentColor);
    }

    @Test
    void test_findColumnToColumnCandidates() {
        var result = board.findColumnToColumnCandidates().toList();

        assertEquals(1, result.size());
        assertEquals("17:2d", result.get(0).notation());

        var cards = List.of(card("5h"), card("4c"), card("3d"), card("2s"));
        board.column(1).addAll(cards);
        board.resetCache();
        result = board.findColumnToColumnCandidates().toList();
        assertEquals(3, result.size());
        assertEquals("10:[5h, 4c, 3d, 2s]", result.get(0).notation());

        fillFreeCells(2, card("Ts"));
        board.resetCache();
        result = board.findColumnToColumnCandidates().toList();
        assertEquals(2, result.size());
        assertEquals("61:Ad", result.get(0).notation());

        board.column(1).remove(board.column(1).size() - 1);
        board.resetCache();
        result = board.findColumnToColumnCandidates().toList();
        assertEquals(2, result.size());
        assertEquals("10:[5h, 4c, 3d]", result.get(0).notation());

        board.column(0).clear();
        board.resetCache();
        result = board.findColumnToColumnCandidates().toList();
        assertEquals(8, result.size());
        assertEquals("10:[5h, 4c, 3d]", result.get(0).notation());
    }

    @Test
    void test_findFreeCellToColumnCandidates() {
        assertTrue(board.findFreeCellToColumnCandidates().toList().isEmpty());

        board.freeCells[0] = card("2h");
        var result = board.findFreeCellToColumnCandidates().toList();

        assertEquals(1, result.size());
        assertEquals("f7:2h", result.get(0).notation());

        board.freeCells[1] = card("Qc");
        result = board.findFreeCellToColumnCandidates().toList();
        assertEquals(1, result.size());
        assertEquals("f7:2h", result.get(0).notation());

        board.freeCells[2] = card("8c");
        result = board.findFreeCellToColumnCandidates().toList();
        assertEquals(2, result.size());
        assertEquals("f7:2h", result.get(0).notation());
        assertEquals("f3:8c", result.get(1).notation());

        board.column(0).clear();
        result = board.findFreeCellToColumnCandidates().toList();
        assertEquals(5, result.size());
        assertEquals("f0:2h", result.get(0).notation());
        assertEquals("f7:2h", result.get(1).notation());
    }

    @Test
    void test_findColumnToFreeCellCandidates() {
        var result = board.findColumnToFreeCellCandidates().toList();

        assertEquals(7, result.size());
        assertEquals("0f:6c", result.get(0).notation());

        fillFreeCells(0, card("As"));
        result = board.findColumnToFreeCellCandidates().toList();
        assertTrue(result.isEmpty());
    }

    @Test
    void test_findColumnToFoundationCandidates() {
        var result = board.findColumnToFoundationCandidates().toList();

        assertEquals(1, result.size());
        assertEquals("6$:Ad", result.get(0).notation());

        board.updateBoard(result.get(0));

        result = board.findColumnToFoundationCandidates().toList();
        assertEquals(1, result.size());
        assertEquals("1$:2d", result.get(0).notation());

        board.updateBoard(result.get(0));
        result = board.findColumnToFoundationCandidates().toList();
        assertTrue(result.isEmpty());
    }

    @Test
    void test_findFreeCellToFoundationCandidates() {
        assertTrue(board.findFreeCellToFoundationCandidates().toList().isEmpty());

        board.freeCells[0] = card("Ah");
        var result = board.findFreeCellToFoundationCandidates().toList();

        assertEquals(1, result.size());
        assertEquals("f$:Ah", result.get(0).notation());
        board.updateBoard(result.get(0));

        board.freeCells[0] = card("2c");
        assertTrue(board.findFreeCellToFoundationCandidates().toList().isEmpty());

        board.freeCells[1] = card("2h");
        result = board.findFreeCellToFoundationCandidates().toList();
        assertEquals(1, result.size());
        assertEquals("f$:2h", result.get(0).notation());

        board.freeCells[2] = card("Ac");
        result = board.findFreeCellToFoundationCandidates().toList();
        assertEquals(2, result.size());
        assertEquals("f$:2h", result.get(0).notation());
        assertEquals("f$:Ac", result.get(1).notation());
    }

    @Test
    void test_candidateToEmptyColumn() {
        var cards = toArray(card("Js"), card("Th"), card("9c"), card("8d"), card("7s"));

        var result = board.candidateToEmptyColumn(cards, 0, 7);

        assertEquals("07:[Js, Th, 9c, 8d, 7s]", result.notation());

        board.freeCells[0] = card("Ad");
        result = board.candidateToEmptyColumn(cards, 0, 7);
        assertNull(result);
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
        assertTrue(reflectionEquals(board.foundations, clone.foundations));
        assertTrue(reflectionEquals(board.freeCells, clone.freeCells));
        assertEquals(board.score(), clone.score());
    }

    @Test
    void test_findCandidates() {
        board.freeCells[0] = card("Ad");
        board.column(6).add(card("6c"));
        board.column(5).subList(1, 6).clear();
        board.column(4).add(card("Ks"));

        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertTrue(result.stream().allMatch(it -> it.cards().length == 1));
        assertEquals("17:2d", result.get(1).notation());

        fillFreeCells(TWO, card("Kd"));
        board.column(1).clear();
        board.resetCache();
        result = board.findCandidates();

        assertNotNull(result);
        assertTrue(result.stream().allMatch(it -> it.cards().length == 1));
        assertEquals(9, result.size());
        assertEquals("f$:Ad", result.get(0).notation());
    }

    @Test
    void test_findCandidates_cascades() {
        board.updateBoard(candidate(board.peek(6), COLUMN, 6, FOUNDATION, 0));
        board.updateBoard(candidate(board.peek(5), COLUMN, 5, COLUMN, 6));
        board.updateBoard(candidate(board.peek(2), COLUMN, 2, COLUMN, 5));

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
        assertEquals("17:2d", result.get(1).notation());
    }

    @Test
    void test_findFreeCellCandidates() {
        var result = board.findFreeCellToColumnCandidates().toList();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        board.freeCells[0] = card("Ac");

        result = board.findCandidates();

        assertNotNull(result);
        assertEquals(9, result.size());
        assertEquals("6$:Ad", result.get(0).notation());
    }

    @Test
    void test_findColumnCandidates() {
        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertEquals("17:2d", result.get(1).notation());
    }

    @Test
    void test_removeFromOrigin() {
        var candidate = board.findCandidates().get(0);
        var result = board.removeFromOrigin(candidate);

        assertNotNull(result);
        assertTrue(reflectionEquals(candidate, result));
        assertEquals(5, board.column(candidate.from()).size());
        assertFalse(board.column(candidate.from()).contains(candidate.peek()));
    }

    @Test
    void test_removeFromOrigin_freecell() {
        board.freeCells[0] = card("Ad");
        var candidates = board.findCandidates();
        var candidate = candidates.get(0);
        var result = board.removeFromOrigin(candidate);

        assertNotNull(result);
        assertTrue(reflectionEquals(candidate, result));
        assertFalse(board.column(candidate.from()).contains(candidate.peek()));
    }

    @Test
    void test_moveToTarget() {
        var candidate = board.findCandidates().get(0);
        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertTrue(isCleared(board.freeCells));
        assertEquals(7, result.column(candidate.to()).size());
        assertEquals("13:2d", result.column(candidate.to()).peek().toString());
        assertEquals(1, board.path().size());
        assertEquals("6$:Ad", board.path().get(0));
        assertTrue(Arrays.stream(board.freeCells).allMatch(isNull));

        result = board.updateBoard(Candidate.toColumnCandidate(candidate, 0));
        assertEquals(8, result.column(0).size());
        assertEquals("45:Ad", result.column(0).peek().toString());
        assertEquals(2, board.path().size());

        var card = board.column(0).peek();
        var ex = assertThrows(RuntimeException.class,
                () -> board.moveToTarget(candidate(card, COLUMN, 0, DECKPILE, -1)));
        assertEquals("Invalid candidate target: 0^:Ad", ex.getMessage());
    }

    @Test
    void test_moveToTarget_foundation() {
        var results = board.findCandidates();
        var candidate = results.get(0);

        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertEquals(5, board.column(candidate.from()).size());
        assertEquals("45:Ad", result.foundations[candidate.to()].toString());

    }

    @Test
    void test_moveToTarget_freeCell() {
        var results = board.findCandidates();
        var candidate = results.get(0);

        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertEquals(5, board.column(candidate.from()).size());
        assertEquals("13:2d", result.column(candidate.to()).peek().toString());
    }

    @Test
    void test_moveToTarget_freeCell_fail() {
        var candidate = candidate(board.column(0).peek(), COLUMN, 0, FREECELL, 0);

        fillFreeCells(0, card("Ad"));

        assertThrows(NoSuchElementException.class, () -> board.updateBoard(candidate));
    }

    @Test
    void test_updateBoard() {
        var card = board.column(0).peek();

        var result = board.updateBoard(candidate(card, COLUMN, 0, FREECELL, suitCode(card)));

        assertNotNull(result);
        assertFalse(board.column(0).contains(card));
        assertSame(card, board.freeCells[0]);

        result = board.updateBoard(candidate(card, FREECELL, 0, COLUMN, 0));
        assertNotNull(result);
        assertTrue(board.column(0).contains(card));
        assertNull(board.freeCells[0]);

        result = board.updateBoard(candidate(card, COLUMN, 0, FOUNDATION, suitCode(card)));
        assertNotNull(result);
        assertFalse(board.column(0).contains(card));
        assertSame(card, board.foundations[suitCode(card)]);

        var crd = board.column(0).peek();
        assertThrows(RuntimeException.class,
                () -> board.updateBoard(candidate(crd, DECKPILE, 0, COLUMN, suitCode(card))));
    }

    @Test
    void test_isFoundationable() {
        var card = card("Ad");
        assertTrue(board.isFoundationable(card));

        board.foundations[suitCode(card)] = card;

        assertTrue(board.isFoundationable(card("2d")));
        assertFalse(board.isFoundationable(card("2c")));
    }

    @Test
    void test_isMovable() {
        var cards = toArray(card("Ts"), card("9d"), card("8c"), card("7h"));

        assertTrue(board.isMovable(cards, 4, 0));

        cards = toArray(card("Qs"), card("Jh"), card("Ts"), card("9d"), card("8c"), card("7h"));
        assertFalse(board.isMovable(cards, 3, 0));
    }


    @Test
    void test_maxCardsToMove() {
        var card = board.column(0).peek();

        assertEquals(5, board.maxCardsToMove(1));

        board.freeCells[0] = card;
        assertEquals(4, board.maxCardsToMove(1));

        board.freeCells[1] = card;
        assertEquals(3, board.maxCardsToMove(1));

        board.freeCells[2] = card;
        assertEquals(2, board.maxCardsToMove(1));

        board.column(7).clear();
        assertEquals(3, board.maxCardsToMove(1));

        board.column(6).clear();
        assertEquals(4, board.maxCardsToMove(1));
    }

    private void fillFreeCells(int from, Card card) {
        range(from, board.freeCells.length).forEach(i -> board.freeCells[i] = card);
    }

    @Test
    void test_score() {
        var card = board.column(6).peek();
        board.updateBoard(candidate(card, COLUMN, 6, FOUNDATION, suitCode(card)));

        assertEquals(-4, board.score());

        board.resetScore();
        fillFreeCells(0, card("Js"));
        assertEquals(-10, board.score());

        board.column(7).clear();
        board.resetScore();
        assertEquals(-4, board.score());

        board.column(1).clear();
        board.resetScore();
        assertThrows(NoSuchElementException.class, () -> board.score());
    }

    @Test
    void test_calcBlockers() {
        var card = card("Qc");

        assertEquals(1, board.calcBlockers(card));

        fillFreeCells(0, card("Js"));
        assertEquals(2, board.calcBlockers(card));
        assertTrue(board.emptyFoundations());

        range(0, board.foundations.length).forEach(i -> board.foundations[i] = card("Js"));
        assertFalse(board.emptyFoundations());
        assertEquals(1, board.calcBlockers(card));

        board.foundations[0] = null;
        assertEquals(2, board.calcBlockers(card));

        board.column(0).clear();
        assertEquals(1, board.calcBlockers(card));
    }

    @Test
    void test_calcBlockerScore() {
        var card = card("Qc");

        board.foundations[suitCode(card)] = card;
        assertEquals(7, board.calcBlockerScore());

        board.foundations[suitCode(card)] = card("Kc");
        assertEquals(4, board.calcBlockerScore());
    }

    @Test
    void test_calcColumnScore() {
        assertEquals(-6, board.score());
        assertEquals(0, board.calcColumnScore());

        board.resetScore();
        board.column(7).clear();
        board.column(7).addAll(List.of(card("Qd"), card("Jc"), card("Th")));
        assertEquals(-3, board.score());
        assertEquals(3, board.calcColumnScore());

        board.resetScore();
        board.column(7).clear();
        board.column(7).addAll(List.of(card("Ks"), card("Qd"), card("Jc")));
        assertEquals(0, board.score());
        assertEquals(6, board.calcColumnScore());
    }

    @Test
    void test_isOrderedColumn() {
        var column = new Column();
        assertTrue(board.isOrderedColumn(column));

        column.add(card("Kc"));
        assertTrue(board.isOrderedColumn(column));

        column.add(card("Qh"));
        assertTrue(board.isOrderedColumn(column));

        column.add(card("Ts"));
        assertFalse(board.isOrderedColumn(column));
    }
}