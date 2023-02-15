package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;
import org.solitaire.model.Column;
import org.solitaire.util.CardHelper;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.GameBoardTest.cards;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.spider.SpiderHelper.LAST_DECK;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelper.toArray;

class SpiderBoardTest {
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
    public void setup() {
        CardHelper.useSuit = false;
        board = build(cards).stack().peek().peek();
    }

    @Test
    void test_score() {
        var result = board.score();
        var candidates = board.candidates();

        assertEquals(4, result);
        assertNotNull(candidates);

        assertSame(candidates, board.findCandidates());
        assertNull(board.candidates());
    }

    @Test
    public void test_equals() {
        var that = new SpiderBoard(board);

        assertTrue(reflectionEquals(that, board));
    }

    @Test
    public void test_clone() {
        var clone = new SpiderBoard(board);

        assertNotSame(board, clone);
        assertTrue(reflectionEquals(clone, board));
    }

    @Test
    public void test_isClear() {
        assertFalse(board.isCleared());

        board.columns().forEach(List::clear);
        board.deck.clear();

        assertTrue(board.isCleared());
    }

    @Test
    public void test_isMovable_repeatingCandidate() {
        var candidate = Candidate.buildColumnCandidate(board.findCandidateAtColumn(5), 9);
        var path = board.path();

        assertTrue(board.isMovable(candidate));

        path.add(toArray(candidate.peek()));
        assertFalse(board.isMovable(candidate));

        path.clear();
        path.add(toArray(card("Ad")));
        assertTrue(board.isMovable(candidate));
    }

    @Test
    public void test_isMovable_king() {
        var card = card("Kd");
        var column = board.columns().get(0);

        column.add(card);
        var candidate = board.findCandidateAtColumn(0);

        assertTrue(board.isMovable(candidate));

        column.clear();
        column.add(card);
        assertFalse(board.isMovable(candidate));
    }

    @Test
    public void test_compareCandidates() {
        var a = buildCandidate(5, COLUMN, List.of(board.columns().get(5).peek()), 9);
        var b = buildCandidate(5, COLUMN, List.of(board.columns().get(5).peek()), 4);

        assertEquals(1, board.compareCandidates(null, b));
        assertEquals(-1, board.compareCandidates(a, null));

        // different target suits
        assertEquals(-1, board.compareCandidates(a, b));

        // same target suits, different chain lengths
        board.columns().get(4).set(4, card("4h"));
        board.columns().get(4).add(card("3h"));
        assertEquals(1, board.compareCandidates(a, b));
    }

    @Test
    public void test_compareDistanceToRevealCard() {
        var a = buildCandidate(3, COLUMN, List.of(board.columns().get(3).peek()), 0);
        var b = buildCandidate(3, COLUMN, List.of(board.columns().get(3).peek()), 8);

        assertEquals(0, board.compareDistanceToRevealCard(a, b));
        assertEquals(0, board.compareDistanceToRevealCard(b, a));

        board.columns().set(0, mockRun().setOpenAt(0));
        a = buildCandidate(0, COLUMN, List.of(board.columns().get(0).peek()), 2);
        assertEquals(-1, board.compareDistanceToRevealCard(a, b));
        assertEquals(1, board.compareDistanceToRevealCard(b, a));
    }

    @Test
    public void test_getDistanceToFlipCard() {
        board.columns().set(0, mockRun().setOpenAt(0));
        var a = buildCandidate(0, COLUMN, List.of(board.columns().get(0).peek()), 2);

        assertEquals(12, board.getDistanceToFlipCard(a));

        board.columns().set(0, mockRun().setOpenAt(12));
        assertEquals(0, board.getDistanceToFlipCard(a));
    }

    @Test
    public void test_compareTargetSuits() {
        // same target suits
        var a = buildCandidate(3, COLUMN, List.of(board.columns().get(3).peek()), 0);
        var b = buildCandidate(3, COLUMN, List.of(board.columns().get(3).peek()), 8);
        assertEquals(0, board.compareTargetSuits(a, b));
        assertEquals(0, board.compareTargetSuits(b, a));

        b = buildCandidate(3, COLUMN, List.of(card("9h")), 8);
        assertEquals(1, board.compareTargetSuits(a, b));
        assertEquals(-1, board.compareTargetSuits(b, a));
    }

    @Test
    public void test_compareCardChains() {
        var a = buildCandidate(5, COLUMN, List.of(board.columns().get(5).peek()), 9);
        var b = buildCandidate(3, COLUMN,
                List.of(board.columns().get(3).peek(), card("8s")), 8);
        board.columns().get(8).set(4, card("Ts"));

        assertEquals(1, board.compareCardChains(a, b));
    }

    @Test
    public void test_compareKings() {
        var a = buildCandidate(0, COLUMN, card("Jd"));
        var b = buildCandidate(0, COLUMN, card("Kd"));

        assertEquals(1, board.compareKings(a, b));
        assertEquals(-1, board.compareKings(b, a));
        assertEquals(-1, board.compareKings(b, b));

        b = buildCandidate(0, COLUMN, card("Qd"));
        assertEquals(0, board.compareKings(a, b));
    }

    @Test
    public void test_updateTargetColumn() {
        board.columns().get(5).add(card("Ah"));
        var candidate = Candidate.buildColumnCandidate(board.findCandidateAtColumn(5), 9);

        assertEquals(6, board.columns().get(candidate.from()).size());
        assertEquals(5, board.columns().get(candidate.to()).size());
        var clone = board.updateBoard(candidate);

        assertNotNull(clone);
        assertEquals(4, clone.columns().get(candidate.from()).size());
        assertEquals(7, clone.columns().get(candidate.to()).size());
    }

    @Test
    public void test_drawDeck() {
        assertEquals(LAST_DECK, board.deck.size());

        assertTrue(board.drawDeck());

        assertEquals(LAST_DECK - board.columns().size(), board.deck.size());
        assertEquals("5s", board.columns().get(0).peek().raw());
        assertEquals("Qh", board.columns().get(9).peek().raw());

        board.deck.clear();

        assertFalse(board.drawDeck());
    }

    @Test
    public void test_checkForRuns() {
        var column = mockRun().setOpenAt(0);
        board.columns().set(0, column);
        var candidate = buildCandidate(0, COLUMN, List.of(column.peek()), 0);

        assertEquals(13, column.size());
        assertEquals("0:Kd", column.get(0).toString());
        assertEquals("0:Ad", column.get(12).toString());
        assertEquals(500, board.totalScore());

        var result = board.checkForRun(candidate);

        assertNotNull(result);
        assertTrue(column.isEmpty());
        assertEquals("[Kd, Qd, Jd, Td, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, Ad]", stringOfRaws(board.path().get(0)));
        assertEquals(600, board.totalScore());

        board.path().clear();
        column = mockRun().setOpenAt(0);
        board.columns().set(0, column);
        column.remove(12);
        result = board.checkForRun(candidate);
        assertNotNull(result);
        assertTrue(board.path().isEmpty());
    }

    @Test
    public void test_checkForRuns_noRuns() {
        var column = board.columns().get(0);
        var candidate = buildCandidate(0, COLUMN, List.of(column.peek()), 0);
        column.addAll(mockRun(10));

        assertEquals(16, column.size());
        assertEquals(500, board.totalScore());

        var result = board.checkForRun(candidate);

        assertNotNull(result);
        assertEquals(16, column.size());
        assertEquals(500, board.totalScore());
    }

    @Test
    public void test_appendToTarget() {
        var candidate = Candidate.buildColumnCandidate(board.findCandidateAtColumn(5), 9);
        var column = board.columns().get(candidate.to());

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
        assertEquals("33:2h", board.path().get(0)[0].toString());
        assertEquals(499, board.totalScore());
    }

    @Test
    public void test_removeFromSource() {
        var candidate = Candidate.buildColumnCandidate(board.findCandidateAtColumn(5), 9);
        var column = board.columns().get(candidate.from());

        assertEquals(5, column.size());
        assertEquals("33:2h", column.peek().toString());

        board.removeFromSource(candidate);

        assertEquals(4, column.size());
        assertNotEquals("33:2h", column.peek().toString());
    }

    @Test
    public void test_findCandidates() {
        var targets = board.findCandidates();

        assertNotNull(targets);
        assertEquals(4, targets.size());
        assertEquals(7, targets.get(0).to());
        assertEquals(9, targets.get(1).to());
    }

    @Test
    public void test_checkMultiples() {
        var card = board.columns().get(3).peek();
        var a = buildCandidate(3, COLUMN, List.of(card), 0);
        var candidstes = List.of(a);

        assertSame(a, board.selectCandidate(candidstes));

        var b = buildCandidate(a.from(), a.origin(), a.cards(), 8);
        var result = board.selectCandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(a, result);

        board.columns().get(b.to()).set(4, card("Ts"));
        result = board.selectCandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(b, result);

        board.columns().get(a.to()).set(5, card("Ts"));
        board.columns().get(a.to()).set(4, card("Js"));
        result = board.selectCandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(a, result);
    }

    @Test
    public void test_findTargetsByCandidate() {
        var candidate = board.findCandidateAtColumn(5);
        var result = board.matchCandidateToTargets(candidate).toList();

        assertNotNull(result);
        assertEquals(4, result.get(0).to());
        assertEquals("Candidate[cards=[33:2h], origin=COLUMN, from=5, target=COLUMN, to=4]", result.get(0).toString());

        candidate = board.findCandidateAtColumn(1);
        result = board.matchCandidateToTargets(candidate).toList();
        assertNotNull(result);
        assertEquals(7, result.get(0).to());
        assertEquals("Candidate[cards=[11:5h], origin=COLUMN, from=1, target=COLUMN, to=7]", result.get(0).toString());
    }

    @Test
    public void test_findTargetColumn_same_column() {
        var candidate = board.findCandidateAtColumn(5);

        assertNull(board.findTargetColumn(candidate.from(), candidate));

        var result = board.findTargetColumn(9, candidate);

        assertNotNull(result);
        assertEquals("Candidate[cards=[33:2h], origin=COLUMN, from=5, target=COLUMN, to=9]", result.toString());

        candidate = buildCandidate(9, COLUMN, card("2h"));
        assertNull(board.findTargetColumn(9, candidate));
    }

    @Test
    public void test_findOpenCandidates() {
        var candidates = board.findOpenCandidates().toList();

        assertNotNull(candidates);
        assertEquals(10, candidates.size());
        assertEquals("Candidate[cards=[5:Th], origin=COLUMN, from=0, target=COLUMN, to=-1]", candidates.get(0).toString());
        assertEquals("Candidate[cards=[53:3h], origin=COLUMN, from=9, target=COLUMN, to=-1]", candidates.get(9).toString());

        board.columns().get(0).clear();
        candidates = board.findOpenCandidates().toList();

        assertNotNull(candidates);
        assertEquals(9, candidates.size());
        assertEquals("Candidate[cards=[11:5h], origin=COLUMN, from=1, target=COLUMN, to=-1]", candidates.get(0).toString());
        assertEquals("Candidate[cards=[53:3h], origin=COLUMN, from=9, target=COLUMN, to=-1]", candidates.get(8).toString());
    }

    @Test
    public void test_findCandidateAtColumn() {
        var candidate = board.findCandidateAtColumn(0);

        assertNotNull(candidate);
        assertEquals("Candidate[cards=[5:Th], origin=COLUMN, from=0, target=COLUMN, to=-1]", candidate.toString());

        candidate = board.findCandidateAtColumn(board.columns().size() - 1);
        assertEquals("Candidate[cards=[53:3h], origin=COLUMN, from=9, target=COLUMN, to=-1]", candidate.toString());
    }

    @Test
    public void test_getOrderedCardsAtColumn() {
        var result = board.getOrderedCardsAtColumn(mockRun().setOpenAt(0));

        assertNotNull(result);
        assertTrue(isNotEmpty(result));
        assertEquals(13, result.size());
        assertEquals("[Kd, Qd, Jd, Td, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, Ad]", stringOfRaws(result));
    }

}