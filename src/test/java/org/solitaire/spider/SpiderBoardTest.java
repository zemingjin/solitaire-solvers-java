package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;
import org.solitaire.model.Column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.candidate;
import static org.solitaire.model.Candidate.columnToColumn;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.execution.SolveExecutor.isPrint;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.IOHelper.loadFile;

class SpiderBoardTest {
    private static final String TEST_FILE = "games/spider/spider-expert-122922.txt";
    private static final String EASY_SPIDER_FILE = "games/spider/spider-easy-120322.txt";

    private static final String[] cards = loadFile(TEST_FILE);

    private SpiderBoard board;

    @BeforeEach
    void setup() {
        useSuit(false);
        isPrint(true);
        board = build(cards).board();
    }

    @Test
    void test_findColumnToColumnCandidates() {
        var result = board.findColumnToColumnCandidates().toList();

        assertEquals(6, result.size());
        assertEquals("17:5h", result.get(3).notation());
        assertEquals("59:2h", result.get(result.size() - 1).notation());
    }

    @Test
    void test_findColumnToColumnCandidates_singleSuit() {
        board.singleSuit(true);

        var result = board.findColumnToColumnCandidates().toList();

        assertEquals(6, result.size());
        assertEquals("30:9s", result.get(0).notation());
        assertEquals("59:2h", result.get(result.size() - 1).notation());
    }

    @Test
    void test_findColumnToColumnCandidates_withSequence() {
        board.column(2).add(card("Js"));
        board.column(5).pop();
        board.resetCache();

        var result = board.findColumnToColumnCandidates().toList();
        assertEquals(6, result.size());
        assertEquals("25:[Qs, Js]", result.get(3).notation());
    }

    @Test
    void test_findCandidates_emptyColumns() {
        IntStream.of(0, 1, 2, 3, 4, 5, 7, 8, 9).forEach(i -> board.column(i).clear());
        board.columns().set(0, mockRun(13));
        board.column(0).pop();
        board.resetCache();

        var result = board.findCandidates();

        assertEquals(8, result.size());
        assertEquals("60:As", result.get(0).notation());
    }

    @Test
    void test_findCandidates() {
        var candidates = board.findCandidates();

        assertEquals(4, candidates.size());
        assertEquals("17:5h", candidates.get(0).notation());
    }

    @Test
    void test_findCandidates_emptyColumn() {
        board.column(7).clear();

        var candidates = board.findCandidates();

        assertEquals(12, candidates.size());
        assertEquals("07:Th", candidates.get(0).notation());
        assertEquals("97:3h", candidates.get(candidates.size() - 1).notation());
    }

    @Test
    void test_findCandidates_sequence() {
        board.column(6).clear();
        board.column(7).clear();
        board.column(0).clear();
        board.column(0).addAll(List.of(card("Ts"), card("9d"), card("8d")));
        board.column(0).openAt(0);

        var candidates = board.findCandidates();
        assertEquals(16, candidates.size());
        assertEquals("06:[9d, 8d]", candidates.get(0).notation());
        assertEquals("07:[9d, 8d]", candidates.get(1).notation());

        board.resetCache();
        board.column(0).remove(0);
        candidates = board.findCandidates();
        assertEquals(16, candidates.size());
        assertEquals("08:[9d, 8d]", candidates.get(1).notation());
    }

    @Test
    void test_optimizeCandidates() {
        board.deck().clear();
        range(0, board.columns().size()).forEach(i -> board.column(i).clear());
        board.columns().set(0, mockRun(13));
        board.column(0).pop();
        board.columns().set(4, mockRun(4));
        board.columns().set(3, mockRun(3));
        board.columns().set(2, mockRun(2));
        board.columns().set(1, mockRun(1));
        range(2, 5).forEach(i -> board.column(i).pop());
        var result = board.findCandidates();

        assertEquals(1, result.size());
        assertEquals("10:As", result.get(0).notation());
    }

    @Test
    void test_findCandidates_emptyDeck() {
        board.deck().clear();
        IntStream.of(0, 1, 2, 3, 4, 5, 7, 8, 9).forEach(i -> board.column(i).clear());
        board.columns().set(0, mockRun(13));
        board.column(0).pop();

        var result = board.findCandidates();

        assertEquals(1, result.size());
        assertEquals("60:As", result.get(0).notation());
    }

    @Test
    void test_score() {
        assertEquals(-34, board.score());

        board.column(9).add(board.column(5).pop());
        board.column(9).add(card("Ah"));
        board.resetCache();
        assertEquals(-31, board.score());
    }

    @Test
    void test_calcSequenceScore() {
        board.column(9).set(0, card("Kh"));

        assertEquals(10, board.calcSequences());
    }

    @Test
    void test_isNotTheWholeColumn() {
        var source = board.column(1);
        var subList = source.subList(1, source.size());
        var candidate = candidate(toArray(subList), COLUMN, 1, COLUMN, 0);

        assertTrue(board.isMovableToEmptyColumn(candidate));

        candidate = new Candidate(toArray(source), COLUMN, 1, COLUMN, 0);
        assertTrue(board.isMovableToEmptyColumn(candidate));

        board.column(0).clear();
        assertFalse(board.isMovableToEmptyColumn(candidate));
    }

    @Test
    void test_clone() {
        board.column(9).add(board.column(5).pop());
        board.column(9).add(card("Ah"));
        board.score();
        board.runs(10);
        board.singleSuit(true);
        board.isInSequence((a, b) -> true);
        var copy = new SpiderBoard(board);

        assertNotSame(board, copy);
        assertTrue(reflectionEquals(board, copy));
        assertTrue(copy.isInSequence().test(null, null));
    }

    @Test
    void test_isClear() {
        assertFalse(board.isSolved());

        board.columns().forEach(List::clear);
        board.deck.clear();

        assertTrue(board.isSolved());
    }

    @Test
    void test_getRunCandidate() {
        assertNull(board.getRunCandidate(0));

        var cards = Arrays.asList(card("Ks"), card("Qs"), card("Js"), card("Ts"), card("9s"),
                card("8s"), card("7s"), card("6s"), card("5s"), card("4s"), card("3s"),
                card("2s"), card("As"));
        board.column(0).addAll(cards);

        assertEquals("0$:[Ks, Qs, Js, Ts, 9s, 8s, 7s, 6s, 5s, 4s, 3s, 2s, As]",
                board.getRunCandidate(0).notation());
    }

    @Test
    void test_checkForRuns() {
        var column = mockRun().openAt(0);
        board.columns().set(0, column);

        assertEquals(13, column.size());
        assertEquals("0:Ks", column.get(0).toString());
        assertEquals("0:As", column.get(12).toString());
        assertEquals(500, board.totalScore());
        assertEquals(0, board.runs());
        board.resetScore();
        assertEquals(-4, board.score());

        var outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        var savedOne = System.out;
        var result = board.checkForRun(0);


        assertNotNull(result);
        assertTrue(column.isEmpty());
        assertEquals("0$:[Ks, Qs, Js, Ts, 9s, 8s, 7s, 6s, 5s, 4s, 3s, 2s, As]", result.path().get(0));
        assertEquals(600, result.totalScore());
        assertEquals("0$:[Ks, Qs, Js, Ts, 9s, 8s, 7s, 6s, 5s, 4s, 3s, 2s, As]",
                outputStream.toString().trim());
        assertEquals(1, result.runs());
        result.resetScore();
        assertEquals(9, result.score());
        System.setOut(savedOne);

        board.path().clear();
        column = mockRun().openAt(0);
        board.columns().set(0, column);
        column.remove(12);
        result = board.checkForRun(0);
        assertNotNull(result);
        assertTrue(result.path().isEmpty());
    }

    @Test
    void test_checkForRuns_noRuns() {
        var column = board.column(0);
        column.addAll(mockRun(10));

        assertEquals(16, column.size());
        assertEquals(500, board.totalScore());

        var result = board.checkForRun(0);

        assertNotNull(result);
        assertEquals(16, column.size());
        assertEquals(500, board.totalScore());
    }

    @Test
    void test_appendToTarget() {
        var candidates = board.findCandidates();
        var candidate = candidates.get(2);
        var column = board.column(candidate.to());

        assertEquals("59:2h", candidate.notation());
        assertEquals(5, column.size());
        assertEquals("53:3h", board.peek(candidate.to()).toString());
        assertTrue(board.path().isEmpty());
        assertEquals(500, board.totalScore());

        board.appendToTarget(candidate);

        assertEquals("33:2h", board.peek(candidate.to()).toString());

        assertFalse(board.path().isEmpty());
        assertEquals(1, board.path().size());
        assertEquals("59:2h", board.path().get(0));
        assertEquals(499, board.totalScore());
    }

    @Test
    void test_appendToTarget_deck() {
        var candidate = board.drawDeck().get(0);

        assertEquals(0, board.path().size());
        var result = board.appendToTarget(candidate);
        assertEquals(1, result.path().size());
        assertEquals("^^:[5s, 6h, Qh, 7s, Ks, 8s, 7h, 7s, 9h, Qh]", result.path().peek());
        assertTrue(range(0, board.columns().size())
                .allMatch(i -> candidate.cards()[i].equals(board.column(i).peek())));
    }

    @Test
    void test_isNoEmptyColumn() {
        assertTrue(board.noEmptyColumns());

        board.column(0).clear();
        assertFalse(board.noEmptyColumns());
    }

    @Test
    void test_removeFromSource() {
        var candidates = board.findCandidates();
        var candidate = candidates.get(2);
        var column = board.column(candidate.from());

        assertEquals(5, column.size());
        assertEquals("33:2h", column.peek().toString());

        board.removeFromSource(candidate);

        assertEquals(4, column.size());
        assertNotEquals("33:2h", column.peek().toString());

        assertEquals(50, board.deck().size());
        board.removeFromSource(board.drawDeck().get(0));
        assertEquals(40, board.deck().size());
    }

    @Test
    void test_drawDeck() {
        var candidates = board.drawDeck();
        assertEquals(1, candidates.size());
        assertEquals("^0:[5s, 6h, Qh, 7s, Ks, 8s, 7h, 7s, 9h, Qh]", candidates.get(0).notation());

        board.column(0).clear();
        assertThrows(RuntimeException.class, () -> board.drawDeck());

        board.deck().clear();
        assertTrue(board.drawDeck().isEmpty());
    }

    @Test
    void test_reduceCandidates() {
        var candidates = List.of(columnToColumn(board.peek(3), 3, 0),
                columnToColumn(toArray(board.peek(3), card("8s")), 3, 0),
                columnToColumn(toArray(card("9h"), card("8h"), card("7h")), 3, 0));

        var result = board.optimizedCandidates(candidates.stream());

        assertEquals(1, result.size());
        assertEquals("30:[9h, 8h, 7h]", result.get(0).notation());

        board.column(9).clear();
        result = board.optimizedCandidates(candidates.stream());

        assertEquals(1, result.size());
        assertEquals("30:[9h, 8h, 7h]", result.get(0).notation());

        board.column(8).clear();
        result = board.optimizedCandidates(candidates.stream());

        assertEquals(2, result.size());
        assertEquals("30:[9h, 8h, 7h]", result.get(0).notation());
        assertEquals("30:[9s, 8s]", result.get(1).notation());

        board.column(7).clear();
        result = board.optimizedCandidates(candidates.stream());
        assertEquals(3, result.size());
    }

    @Test
    void test_updateBoard() {
        var candidates = board.drawDeck();
        var deck = board.deck();

        assertNotNull(board.updateBoard(candidates.get(0)));

        assertNotSame(deck, board.deck);
    }

    @Test
    void test_isNotEmpty() {
        assertTrue(board.isNotEmpty.test(0));

        board.column(0).clear();
        assertFalse(board.isNotEmpty.test(0));
    }

    @Test
    void test_verify() {
        var result = board.verify();

        assertTrue(result.isEmpty());
        assertFalse(board.singleSuit());

        board.column(1).add(card("Th"));
        board.column(0).remove(0);
        result = board.verify();
        assertEquals(2, result.size());
        assertEquals("[Extra card: Th, Missing card: 4h]", result.toString());
    }

    @Test
    void test_singleSuit() {
        var board = build(loadFile(EASY_SPIDER_FILE)).board();

        assertEquals(0, board.verify().size());
        assertTrue(board.singleSuit());
    }

    private static Column mockRun() {
        return mockRun(VALUES.length());
    }

    private static Column mockRun(int length) {
        var column = new Column();

        for (int i = length; i-- > 0; ) {
            column.add(card(VALUES.charAt(i) + "s"));
        }
        return column;
    }

}