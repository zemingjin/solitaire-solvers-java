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
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
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
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.isCleared;
import static org.solitaire.util.CardHelper.suitCode;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

class FreeCellBoardTest {
    protected static final String TEST_FILE = "games/freecell/freecell-020623-easy.txt";
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
        board.freeCells()[0] = card("Ad");
        board.columns().get(6).add(card("6c"));

        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(9, result.size());
        assertTrue(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals("Candidate[cards=[13:2d], origin=COLUMN, from=1, target=COLUMN, to=7]",
                result.get(0).toString());

        fillFreeCells(TWO, card("Kd"));
        board.columns().get(1).clear();
        result = board.findCandidates();

        assertNotNull(result);
        assertTrue(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals(17, result.size());
        assertEquals("Candidate[cards=[6:6c], origin=COLUMN, from=0, target=COLUMN, to=1]",
                result.get(0).toString());
    }

    @Test
    void test_findCandidates_cascades() {
        board.updateBoard(new Candidate(List.of(board.columns().get(6).peek()), COLUMN, 6, FOUNDATION, 0));
        board.updateBoard(new Candidate(List.of(board.columns().get(5).peek()), COLUMN, 5, COLUMN, 6));
        board.updateBoard(new Candidate(List.of(board.columns().get(2).peek()), COLUMN, 2, COLUMN, 5));

        var column = board.columns().get(0);
        while (column.size() > 1) column.remove(0);

        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(9, result.size());
    }

    @Test
    void test_findCandidates_noFreeCells() {
        fillFreeCells(0, card("Kd"));

        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals("Candidate[cards=[13:2d], origin=COLUMN, from=1, target=COLUMN, to=7]",
                result.get(0).toString());
    }

    @Test
    void test_findFreeCellCandidates() {
        var result = board.findFreeCellToColumnCandidates().toList();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        board.freeCells()[0] = card("Ac");

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
        assertEquals(7, result.columns().get(candidate.to()).size());
        assertEquals("13:2d", result.columns().get(candidate.to()).peek().toString());
        assertEquals(1, board.path().size());
        assertEquals("17:2d", board.path().get(0));
        assertTrue(Arrays.stream(board.freeCells()).allMatch(Objects::isNull));

        result = board.updateBoard(Candidate.buildColumnCandidate(candidate, 0));
        assertEquals(8, result.columns().get(0).size());
        assertEquals("13:2d", result.columns().get(0).peek().toString());
        assertEquals(2, board.path().size());

        var card = board.columns().get(0).peek();
        var ex = assertThrows(RuntimeException.class,
                () -> board.moveToTarget(new Candidate(List.of(card), COLUMN, 0, DECKPILE, -1)));
        assertEquals("Invalid Target: Candidate[cards=[13:2d], origin=COLUMN, from=0, target=DECKPILE, to=-1]",
                ex.getMessage());
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
        var candidate = results.get(0);

        var result = board.updateBoard(candidate);

        assertNotNull(result);
        assertSame(board, result);
        assertEquals(6, board.columns().get(candidate.from()).size());
        assertEquals("13:2d", result.columns().get(candidate.to()).peek().toString());
    }

    @Test
    void test_moveToTarget_freeCell_fail() {
        var candidate = new Candidate(List.of(board.columns().get(0).peek()), COLUMN, 0, FREECELL, 0);

        fillFreeCells(0, card("Ad"));

        assertThrows(NoSuchElementException.class, () -> board.updateBoard(candidate));
    }

    @Test
    void test_updateBoard() {
        var card = board.columns().get(0).peek();

        var result = board.updateBoard(buildCandidate(0, COLUMN, FREECELL, card));

        assertNotNull(result);
        assertFalse(board.columns().get(0).contains(card));
        assertSame(card, board.freeCells()[0]);

        result = board.updateBoard(new Candidate(List.of(card), FREECELL, 0, COLUMN, 0));
        assertNotNull(result);
        assertTrue(board.columns().get(0).contains(card));
        assertNull(board.freeCells()[0]);

        result = board.updateBoard(new Candidate(List.of(card), COLUMN, 0, FOUNDATION, -1));
        assertNotNull(result);
        assertFalse(board.columns().get(0).contains(card));
        assertSame(card, board.foundations()[suitCode(card)]);

        var crd = board.columns().get(0).peek();
        assertThrows(RuntimeException.class,
                () -> board.updateBoard(new Candidate(List.of(crd), DECKPILE, 0, COLUMN, -1)));
    }

    @Test
    void test_isFoundationable() {
        var card = card("Ad");
        assertTrue(board.isFoundationable(card));

        board.foundations()[suitCode(card)] = card;

        assertTrue(board.isFoundationable(card("2d")));
        assertFalse(board.isFoundationable(card("2c")));

        assertThrows(NullPointerException.class, () -> board.isFoundationable(null));
    }

    @Test
    void test_isAppendableToColumn() {
        var card = board.columns().get(1).peek();

        assertTrue(board.isAppendableToColumn(7, buildCandidate(1, COLUMN, COLUMN, card)));

        card = board.columns().get(6).peek();
        assertFalse(board.isAppendableToColumn(1, buildCandidate(6, COLUMN, COLUMN, card)));

        var cards = List.of(card("8d"), card("7s"), card("6d"), card("5c"), card("4h"), card("3s"));
        fillFreeCells(0, card);
        var result = board.getTargetCandidates(buildCandidate(0, COLUMN, cards)).toList();
        assertTrue(result.isEmpty());

        board.columns().get(6).clear();
        assertFalse(board.isAppendableToColumn(1, buildCandidate(6, COLUMN, COLUMN, card)));
    }

    @Test
    void test_isMovable() {
        var cards = List.of(card("5d"));
        var candidate = buildCandidate(0, COLUMN, cards);

        assertTrue(board.isMovable(candidate));

        cards = List.of(card("6d"), card("5d"), card("4d"), card("3d"), card("2d"), card("Ad"));
        candidate = buildCandidate(0, COLUMN, cards);

        assertFalse(board.isMovable(candidate));

        assertThrows(NullPointerException.class, () -> board.isMovable(null));
    }

    @Test
    void test_checkColumnToFoundation() {
        board.columns().get(0).clear();
        var card = board.columns().get(1).peek();

        board.checkColumnToFoundation();

        assertSame(card, board.foundations()[suitCode(card)]);
    }

    @Test
    void test_checkFreeCellToFoundation() {
        var card = board.columns().get(6).peek();
        board.updateBoard(buildCandidate(6, COLUMN, FREECELL, card));
        card = board.columns().get(1).peek();

        board.checkFoundationCandidates();
        assertSame(card, board.foundations()[suitCode(card)]);
        assertTrue(Stream.of(board.freeCells()).allMatch(Objects::isNull));

        card = board.columns().get(7).peek();
        board.updateBoard(buildCandidate(7, COLUMN, FREECELL, card));

        board.checkFoundationCandidates();
        assertSame(card, board.freeCells()[0]);
    }

    @Test
    void test_checkFoundationCandidates() {
        board.updateBoard(buildCandidate(0, COLUMN, FREECELL, board.columns().get(0).peek()));
        var result = board.checkFoundationCandidates();

        assertSame(board, result);
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
        assertEquals(3, board.maxCardsToMove());

        board.columns().get(6).clear();
        assertEquals(4, board.maxCardsToMove());
    }

    private void fillFreeCells(int from, Card card) {
        range(from, board.freeCells().length).forEach(i -> board.freeCells()[i] = card);
    }

    @Test
    void test_getCardsInSequence() {
        assertEquals(ONE, board.getCardsInSequence(board.columns().get(0)));

        board.columns().get(0).add(card("5d"));
        assertEquals(TWO, board.getCardsInSequence(board.columns().get(0)));

        board.columns().get(0).clear();
        assertEquals(ZERO, board.getCardsInSequence(board.columns().get(0)));
    }

    @Test
    void test_score() {
        var card = board.columns().get(6).peek();
        board.updateBoard(buildCandidate(6, COLUMN, FOUNDATION, card));

        assertEquals(6, board.score());

        board.score(0);
        fillFreeCells(0, card("Js"));
        assertEquals(12, board.score());

        board.columns().get(7).clear();
        board.score(0);
        assertEquals(6, board.score());

        board.columns().get(1).clear();
        board.score(0);
        assertThrows(NoSuchElementException.class, () -> board.score());
    }

    private void mockFoundations() {
        var a = card("Kd");
        var b = card("9h");
        var c = card("5c");
        var d = card("As");

        board.foundations()[suitCode(a)] = a;
        board.foundations()[suitCode(b)] = b;
        board.foundations()[suitCode(c)] = c;
        board.foundations()[suitCode(d)] = d;

        clearColumns();
    }

    private void clearColumns() {
        board.columns().forEach(column -> {
            for (int i = column.size() - 1; i >= 0; i--) {
                var card = column.get(i);
                var foundation = board.foundations()[suitCode(card)];

                if (nonNull(foundation) && card.isSameSuit(foundation) && card.rank() <= foundation.rank()) {
                    column.remove(i);
                }
            }
        });
    }
}