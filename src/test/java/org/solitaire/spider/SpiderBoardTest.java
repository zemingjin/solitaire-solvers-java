package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.util.IOHelper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

class SpiderBoardTest {
    private static final String TEST_FILE = "games/spider/spider-122922-expert.txt";

    private static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private SpiderBoard board;

    private static Column mockRun() {
        return mockRun(VALUES.length());
    }

    private static Column mockRun(int length) {
        var column = new Column();

        for (int i = length; i-- > 0; ) {
            column.add(card(VALUES.charAt(i) + "d"));
        }
        return column;
    }

    @BeforeEach
    void setup() {
        useSuit(false);
        isPrint(true);
        board = build(cards).board();
    }

    @Test
    void test_score() {
        assertEquals(-173, board.score());

        board.column(9).add(board.column(5).pop());
        board.column(9).add(card("Ah"));
        board.score(0);
        assertEquals(-170, board.score());
    }

    @Test
    void test_countBlockers() {
        assertEquals(TWO, board.countBlockers(0));
        assertEquals(ZERO, board.countBlockers(1));

        board.column(2).add(card("Js"));
        assertEquals(1, board.countBlockers(2));

        board.column(3).remove(3);
        assertEquals(TWO, board.countBlockers(4));

        var card = card("8s");
        board.column(4).add(card("8s"));
        while (board.deck().contains(card)) board.deck().remove(card);
        assertEquals(ZERO, board.countBlockers(4));

        board.column(3).clear();
        assertEquals(ZERO, board.countBlockers(3));
    }

    @Test
    void test_calcSequenceScore() {
        board.column(9).set(0, card("Kh"));

        assertEquals(25, board.calcSequenceScore(9, board.column(9)));
    }

    @Test
    void test_isNotTheWholeColumn() {
        var source = board.column(1);
        var candidate = new Candidate(source.subList(1, source.size()).toArray(Card[]::new), COLUMN, 1, COLUMN, 0);

        assertTrue(board.isMovableToEmptyColumn.test(candidate));

        candidate = new Candidate(source.toArray(Card[]::new), COLUMN, 1, COLUMN, 0);
        assertTrue(board.isMovableToEmptyColumn.test(candidate));

        board.column(0).clear();
        assertFalse(board.isMovableToEmptyColumn.test(candidate));
    }

    @Test
    void test_findCandidatesOfSameSuit() {
        var candidates = board.findCandidates(board::findCandidateOfSameSuit).toList();

        assertEquals(2, candidates.size());
        assertEquals("17:5h", candidates.get(0).notation());
    }

    @Test
    void test_findCandidates_sameSuit() {
        var candidates = board.findCandidates();

        assertNotNull(candidates);
        assertEquals(6, candidates.size());
        assertEquals(7, candidates.get(0).to());
        assertEquals(9, candidates.get(1).to());

        board.column(7).clear();

        candidates = board.findCandidates();

        assertEquals(23, candidates.size());
    }

    @Test
    void test_test_findCandidatesOfSameSuit_merge_sequences() {
        mockColumn(0, 5, 0);
        mockColumn(1, 12, 6);

        var candidates = board.findCandidates(board::findCandidateOfSameSuit).toList();
        assertEquals(3, candidates.size());
        assertEquals("01:[6h, 5h, 4h, 3h, 2h, Ah]", candidates.get(0).notation());
    }

    @Test
    void test_test_findCandidatesOfSameSuit_merge_sublist() {
        mockColumn(0, 8, 0);
        mockColumn(1, 12, 6);

        var candidates = board.findCandidates(board::findCandidateOfSameSuit).toList();
        assertEquals(3, candidates.size());
        assertEquals("01:[6h, 5h, 4h, 3h, 2h, Ah]", candidates.get(0).notation());

        board.column(0).clear();
        board.column(0).openAt(-1);
        mockColumn(0, 9, 8);

        candidates = board.findCandidates(board::findCandidateOfSameSuit).toList();
        assertEquals(2, candidates.size());
        assertEquals("59:2h", candidates.get(0).notation());

    }

    @Test
    void test_copy() {
        board.column(9).add(board.column(5).pop());
        board.column(9).add(card("Ah"));
        board.score();
        var copy = new SpiderBoard(board);

        assertTrue(reflectionEquals(board, copy));
    }

    @Test
    void test_isClear() {
        assertFalse(board.isSolved());

        board.columns().forEach(List::clear);
        board.deck.clear();

        assertTrue(board.isSolved());
    }

    @Test
    void test_isMovable() {
        var candidate = board.findCandidates().get(0);

        assertTrue(board.isMovable(candidate));

        board.path().add(candidate.notation());

        assertFalse(board.isMovable(candidate));
    }

    @Test
    void test_isMovable_king() {
        var card = card("Kd");
        var column = board.column(0);

        column.add(card);
        var candidate = new Candidate(toArray(card), COLUMN, 0, COLUMN, 1);

        assertTrue(board.isMovable(candidate));

        column.clear();
        column.add(card);
        assertFalse(board.isMovable(candidate));
    }

    @Test
    void test_checkForRuns() {
        var column = mockRun().openAt(0);
        board.columns().set(0, column);
        var candidate = buildCandidate(0, COLUMN, toArray(column.peek()), 0);

        assertEquals(13, column.size());
        assertEquals("0:Kd", column.get(0).toString());
        assertEquals("0:Ad", column.get(12).toString());
        assertEquals(500, board.totalScore());

        var outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        var savedOne = System.out;
        var result = board.checkForRun(candidate);

        assertNotNull(result);
        assertTrue(column.isEmpty());
        assertEquals("0$:[Kd, Qd, Jd, Td, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, Ad]", board.path().get(0));
        assertEquals(600, board.totalScore());
        assertEquals("Run: [Kd, Qd, Jd, Td, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, Ad]",
                outputStream.toString().trim());
        System.setOut(savedOne);

        board.path().clear();
        column = mockRun().openAt(0);
        board.columns().set(0, column);
        column.remove(12);
        result = board.checkForRun(candidate);
        assertNotNull(result);
        assertTrue(board.path().isEmpty());
    }

    @Test
    void test_checkForRuns_noRuns() {
        var column = board.column(0);
        var candidate = buildCandidate(0, COLUMN, toArray(column.peek()), 0);
        column.addAll(mockRun(10));

        assertEquals(16, column.size());
        assertEquals(500, board.totalScore());

        var result = board.checkForRun(candidate);

        assertNotNull(result);
        assertEquals(16, column.size());
        assertEquals(500, board.totalScore());
    }

    @Test
    void test_appendToTarget() {
        var candidates = board.findCandidates();
        var candidate = candidates.get(1);
        var column = board.column(candidate.to());

        assertEquals(5, column.size());
        assertEquals("53:3h", column.peek().toString());
        assertTrue(board.path().isEmpty());
        assertEquals(500, board.totalScore());

        board.appendToTarget(candidate);

        assertEquals(6, column.size());
        assertNotEquals("53:3h", column.peek().toString());
        assertEquals("33:2h", column.peek().toString());

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
        assertEquals("^0:[5s, 6h, Qh, 7s, Ks, 8s, 7h, 7s, 9h, Qh]", result.path().peek());
        assertTrue(range(0, board.columns().size())
                .allMatch(i -> candidate.cards()[i].equals(board.column(i).peek())));
    }

    @Test
    void test_isNoEmptyColumn() {
        assertTrue(board.isNoEmptyColumn());

        board.column(0).clear();
        assertFalse(board.isNoEmptyColumn());
    }

    @Test
    void test_removeFromSource() {
        var candidates = board.findCandidates();
        var candidate = candidates.get(1);
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

        board.deck().clear();
        assertTrue(board.drawDeck().isEmpty());
    }

    @Test
    void test_findCandidates_findCandidatesOfDifferentColors() {
        board.column(1).pop();
        board.column(9).pop();
        board.column(2).add(card("Js"));

        var candidates = board.findCandidates();

        assertNotNull(candidates);
        assertEquals(8, candidates.size());
        assertEquals(8, candidates.stream().filter(it -> it.cards().length == 1).count());
    }

    @Test
    void test_getOrderedCardsAtColumn() {
        var result = board.getOrderedCardsAtColumn(mockRun().openAt(0));

        assertNotNull(result);
        assertTrue(isNotEmpty(result));
        assertEquals(13, result.size());
        assertEquals("[Kd, Qd, Jd, Td, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, Ad]", stringOfRaws(result));
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

        board.column(1).add(card("Th"));
        board.column(0).remove(0);
        result = board.verify();
        assertEquals(2, result.size());
        assertEquals("[Extra card: Th, Missing card: 4h]", result.toString());
    }

    private void mockColumn(int col, int h, int l) {
        for (int i = h; i >= l; i--) {
            board.column(col).add(card(VALUES.charAt(i) + "h"));
        }
    }
}