package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;
import org.solitaire.util.IOHelper;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
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
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.isCleared;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;

class FreeCellBoardTest {
    private static final String TEST_FILE = "games/freecell/freecell-020623-easy.txt";
    private FreeCellBoard board;

    @BeforeEach
    void setup() {
        CardHelper.useSuit = false;
        board = build(IOHelper.loadFile(TEST_FILE)).stack().peek().peek();
    }

    @Test
    void test_isCleared() {
        assertFalse(board.isCleared());

        board.columns().forEach(List::clear);

        assertTrue(board.isCleared());
    }

    @Test
    void test_clone() {
        var clone = new FreeCellBoard(board);

        assertNotSame(board, clone);
        assertTrue(reflectionEquals(board, clone));
    }

    @Test
    void test_findCandidates() {
        board.freeCells()[0] = buildCard(0, "Ad");
        board.columns().get(6).add(buildCard(0, "6c"));
        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(9, result.size());
        assertTrue(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals("Candidate[cards=[13:2d], origin=COLUMN, from=1, target=COLUMN, to=7]", result.get(0).toString());

        fillFreeCells(TWO, buildCard(0, "Kd"));
        result = board.findCandidates();

        assertNotNull(result);
        assertTrue(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals(9, result.size());
        assertEquals("Candidate[cards=[13:2d], origin=COLUMN, from=1, target=COLUMN, to=7]", result.get(0).toString());

        fillFreeCells(ONE, buildCard(0, "Kd"));
        result = board.findCandidates();

        assertNotNull(result);
        assertTrue(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals(1, result.size());
        assertEquals("Candidate[cards=[13:2d], origin=COLUMN, from=1, target=COLUMN, to=7]", result.get(0).toString());
    }

    @Test
    void test_findFreeCellCandidates() {
        var result = board.findFreeCellToColumnCandidates().toList();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        board.freeCells()[0] = buildCard(0, "Ac");

        result = board.findCandidates();

        assertNotNull(result);
        assertEquals(10, result.size());
        assertEquals("Candidate[cards=[0:Ac], origin=FREECELL, from=0, target=COLUMN, to=1]", result.get(1).toString());
    }

    @Test
    void test_findColumnCandidates() {
        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(9, result.size());
        assertEquals("Candidate[cards=[13:2d], origin=COLUMN, from=1, target=COLUMN, to=7]",
                result.get(0).toString());
    }

    @Test
    void test_findCandidateAtColumn_colNum() {
        var result = board.findCandidateAtColumn(0);

        assertNotNull(result);
        assertEquals("Candidate[cards=[6:6c], origin=COLUMN, from=0, target=COLUMN, to=-1]", result.toString());
    }

    @Test
    void test_findCandidateAtColumn_column() {
        var result = board.findCandidateAtColumn(board.columns().get(0));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("6:6c", result.get(0).toString());
    }

    @Test
    void test_removeFromOrigin() {
        var candidate = board.findCandidates().get(0);
        var result = board.removeFromOrigin(candidate);

        assertNotNull(result);
        assertTrue(reflectionEquals(candidate, result));
        assertEquals(6, board.columns().get(candidate.from()).size());
        assertFalse(board.columns().get(candidate.from()).contains(candidate.peek()));
    }

    @Test
    void test_removeFromOrigin_freecell() {
        board.freeCells()[0] = buildCard(0, "Ad");
        var candidates = board.findCandidates();
        var candidate = candidates.get(2);
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
        assertEquals(7, result.columns().get(candidate.to()).size());
        assertEquals("13:2d", result.columns().get(candidate.to()).peek().toString());
        assertEquals(1, board.path().size());
        assertEquals("2d", board.path().get(0).peek().raw());
        assertTrue(Arrays.stream(board.freeCells()).allMatch(Objects::isNull));

        result = board.updateBoard(buildCandidate(candidate, 0));
        assertEquals(8, result.columns().get(0).size());
        assertEquals("13:2d", result.columns().get(0).peek().toString());
        assertEquals(2, board.path().size());
    }

    @Test
    void test_moveToTarget_foundation() {
        var results = board.findCandidates();
        var candidate = results.get(1);

        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertEquals(6, board.columns().get(candidate.from()).size());
        assertEquals("6:6c", result.freeCells()[candidate.from()].toString());

    }

    @Test
    void test_moveToTarget_freeCell() {
        var results = board.findCandidates();
        var candidate = results.get(2);

        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertEquals(6, board.columns().get(candidate.from()).size());
        assertEquals("13:2d", result.freeCells()[0].toString());
    }

    @Test
    void test_moveToTarget_freeCell_fail() {
        var results = board.findCandidates();
        var candidate = results.get(2);

        fillFreeCells(0, buildCard(0, "Ad"));

        assertThrows(NoSuchElementException.class, () -> board.updateBoard(candidate));
    }


    @Test
    void test_maxCardsToMove() {
        var card = board.columns().get(0).peek();

        assertEquals(5, board.maxCardsToMove());

        board.freeCells()[0] = card;
        assertEquals(4, board.maxCardsToMove());

        board.freeCells()[1] = card;
        assertEquals(3, board.maxCardsToMove());

        board.freeCells()[2] = card;
        assertEquals(2, board.maxCardsToMove());

        board.columns().get(7).clear();
        assertEquals(4, board.maxCardsToMove());

        board.columns().get(6).clear();
        assertEquals(6, board.maxCardsToMove());
    }

    private void fillFreeCells(int from, Card card) {
        range(from, board.freeCells().length).forEach(i -> board.freeCells()[i] = card);
    }

    @Test
    void test_score() {
        assertEquals(-13.681122337132251, requireNonNull(board).score());
    }
}