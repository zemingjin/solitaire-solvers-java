package org.solitaire.spider;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.util.CardHelper;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.GameStateTest.cards;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.spider.SpiderHelper.LAST_DECK;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.stringOfRaws;

class SpiderStateTest {
    private SpiderState state;

    private static Column mockRun() {
        return mockRun(VALUES.length());
    }

    private static Column mockRun(int length) {
        var column = new Column();

        for (int i = length; i-- > 0; ) {
            column.add(buildCard(0, VALUES.charAt(i) + "d"));
        }
        return column;
    }

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
        state = build(cards).getInitState();
    }

    @Test
    public void test_equals() {
        var that = new SpiderState(state);

        assertTrue(EqualsBuilder.reflectionEquals(that, state));
    }

    @Test
    public void test_clone() {
        var clone = new SpiderState(state);

        assertNotSame(state, clone);
        assertTrue(EqualsBuilder.reflectionEquals(clone, state));
    }

    @Test
    public void test_isClear() {
        assertFalse(state.isCleared());

        state.columns().forEach(List::clear);
        state.deck.clear();

        assertTrue(state.isCleared());
    }

    @Test
    public void test_isMovable_repeatingCandidate() {
        var candidate = state.findCandidateAtColumn(5).setTarget(9);
        var path = state.path();

        assertTrue(state.isMovable(candidate));

        path.add(new Card[]{candidate.peek()});
        assertFalse(state.isMovable(candidate));

        path.clear();
        path.add(new Card[]{buildCard(0, "Ad")});
        assertTrue(state.isMovable(candidate));
    }

    @Test
    public void test_isMovable_king() {
        var card = buildCard(0, "Kd");
        var column = state.columns().get(0);

        column.add(card);
        var candidate = state.findCandidateAtColumn(0);

        assertTrue(state.isMovable(candidate));

        column.clear();
        column.add(card);
        assertFalse(state.isMovable(candidate));
    }

    @Test
    public void test_compareCandidates() {
        var a = buildCandidate(5, COLUMN, List.of(state.columns().get(5).peek()), 9);
        var b = buildCandidate(5, COLUMN, List.of(state.columns().get(5).peek()), 4);

        assertEquals(1, state.compareCandidates(null, b));
        assertEquals(-1, state.compareCandidates(a, null));

        // different target suits
        assertEquals(-1, state.compareCandidates(a, b));

        // same target suits, different chain lengths
        state.columns().get(4).set(4, buildCard(0, "4h"));
        state.columns().get(4).add(buildCard(0, "3h"));
        assertEquals(1, state.compareCandidates(a, b));
    }

    @Test
    public void test_compareDistanceToRevealCard() {
        var a = buildCandidate(3, COLUMN, List.of(state.columns().get(3).peek()), 0);
        var b = buildCandidate(3, COLUMN, List.of(state.columns().get(3).peek()), 8);

        assertEquals(0, state.compareDistanceToRevealCard(a, b));
        assertEquals(0, state.compareDistanceToRevealCard(b, a));

        state.columns().set(0, mockRun().setOpenAt(0));
        a = buildCandidate(0, COLUMN, List.of(state.columns().get(0).peek()), 2);
        assertEquals(-1, state.compareDistanceToRevealCard(a, b));
        assertEquals(1, state.compareDistanceToRevealCard(b, a));
    }

    @Test
    public void test_getDistanceToFlipCard() {
        state.columns().set(0, mockRun().setOpenAt(0));
        var a = buildCandidate(0, COLUMN, List.of(state.columns().get(0).peek()), 2);

        assertEquals(12, state.getDistanceToFlipCard(a));

        state.columns().set(0, mockRun().setOpenAt(12));
        assertEquals(0, state.getDistanceToFlipCard(a));
    }

    @Test
    public void test_compareTargetSuits() {
        // same target suits
        var a = buildCandidate(3, COLUMN, List.of(state.columns().get(3).peek()), 0);
        var b = buildCandidate(3, COLUMN, List.of(state.columns().get(3).peek()), 8);
        assertEquals(0, state.compareTargetSuits(a, b));
        assertEquals(0, state.compareTargetSuits(b, a));

        b = buildCandidate(3, COLUMN, List.of(buildCard(0, "9h")), 8);
        assertEquals(1, state.compareTargetSuits(a, b));
        assertEquals(-1, state.compareTargetSuits(b, a));
    }

    @Test
    public void test_compareCardChains() {
        var a = buildCandidate(5, COLUMN, List.of(state.columns().get(5).peek()), 9);
        var b = buildCandidate(3, COLUMN,
                List.of(state.columns().get(3).peek(), buildCard(0, "8s")), 8);
        state.columns().get(8).set(4, buildCard(0, "Ts"));

        assertEquals(1, state.compareCardChains(a, b));
    }

    @Test
    public void test_compareKings() {
        var a = buildCandidate(0, COLUMN, buildCard(0, "Jd"));
        var b = buildCandidate(0, COLUMN, buildCard(0, "Kd"));

        assertEquals(1, state.compareKings(a, b));
        assertEquals(-1, state.compareKings(b, a));
        assertEquals(-1, state.compareKings(b, b));

        b = buildCandidate(0, COLUMN, buildCard(0, "Qd"));
        assertEquals(0, state.compareKings(a, b));
    }

    @Test
    public void test_updateTargetColumn() {
        state.columns().get(5).add(buildCard(0, "Ah"));
        var candidate = state.findCandidateAtColumn(5).setTarget(9);

        assertEquals(6, state.columns().get(candidate.from()).size());
        assertEquals(5, state.columns().get(candidate.target()).size());
        var clone = state.updateState(candidate);

        assertNotNull(clone);
        assertEquals(4, clone.columns().get(candidate.from()).size());
        assertEquals(7, clone.columns().get(candidate.target()).size());
    }

    @Test
    public void test_drawDeck() {
        assertEquals(LAST_DECK, state.deck.size());

        assertTrue(state.drawDeck());

        assertEquals(LAST_DECK - state.columns().size(), state.deck.size());
        assertEquals("5s", state.columns().get(0).peek().raw());
        assertEquals("Qh", state.columns().get(9).peek().raw());

        state.deck.clear();

        assertFalse(state.drawDeck());
    }

    @Test
    public void test_checkForRuns() {
        var column = mockRun().setOpenAt(0);
        state.columns().set(0, column);
        var candidate = buildCandidate(0, COLUMN, List.of(column.peek()), 0);

        assertEquals(13, column.size());
        assertEquals("0:Kd", column.get(0).toString());
        assertEquals("0:Ad", column.get(12).toString());
        assertEquals(500, state.totalScore());

        var result = state.checkForRun(candidate);

        assertNotNull(result);
        assertTrue(column.isEmpty());
        assertEquals("Kd:Qd:Jd:Td:9d:8d:7d:6d:5d:4d:3d:2d:Ad", stringOfRaws(state.path().get(0)));
        assertEquals(600, state.totalScore());

        state.path().clear();
        column = mockRun().setOpenAt(0);
        state.columns().set(0, column);
        column.remove(12);
        result = state.checkForRun(candidate);
        assertNotNull(result);
        assertTrue(state.path().isEmpty());
    }

    @Test
    public void test_checkForRuns_noRuns() {
        var column = state.columns().get(0);
        var candidate = buildCandidate(0, COLUMN, List.of(column.peek()), 0);
        column.addAll(mockRun(10));

        assertEquals(16, column.size());
        assertEquals(500, state.totalScore());

        var result = state.checkForRun(candidate);

        assertNotNull(result);
        assertEquals(16, column.size());
        assertEquals(500, state.totalScore());
    }

    @Test
    public void test_appendToTarget() {
        var candidate = state.findCandidateAtColumn(5).setTarget(9);
        var column = state.columns().get(candidate.target());

        assertEquals(5, column.size());
        assertEquals("53:3h", column.peek().toString());
        assertTrue(state.path().isEmpty());
        assertEquals(500, state.totalScore());

        state.appendToTarget(candidate);

        assertEquals(6, column.size());
        assertNotEquals("53:3h", column.peek().toString());
        assertEquals("33:2h", column.peek().toString());

        assertFalse(state.path().isEmpty());
        assertEquals(1, state.path().size());
        assertEquals("33:2h", state.path().get(0)[0].toString());
        assertEquals(499, state.totalScore());
    }

    @Test
    public void test_removeFromSource() {
        var candidate = state.findCandidateAtColumn(5).setTarget(9);
        var column = state.columns().get(candidate.from());

        assertEquals(5, column.size());
        assertEquals("33:2h", column.peek().toString());

        state.removeFromSource(candidate);

        assertEquals(4, column.size());
        assertNotEquals("33:2h", column.peek().toString());
    }

    @Test
    public void test_findCandidates() {
        var targets = state.findCandidates();

        assertNotNull(targets);
        assertEquals(4, targets.size());
        assertEquals(7, targets.get(0).target());
        assertEquals(9, targets.get(1).target());
    }

    @Test
    public void test_checkMultiples() {
        var card = state.columns().get(3).peek();
        var a = buildCandidate(3, COLUMN, List.of(card), 0);
        var candidstes = List.of(a);

        assertSame(a, state.selectCandidate(candidstes));

        var b = buildCandidate(a.from(), a.origin(), a.cards(), 8);
        var result = state.selectCandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(a, result);

        state.columns().get(b.target()).set(4, buildCard(0, "Ts"));
        result = state.selectCandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(b, result);

        state.columns().get(a.target()).set(5, buildCard(0, "Ts"));
        state.columns().get(a.target()).set(4, buildCard(0, "Js"));
        result = state.selectCandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(a, result);
    }

    @Test
    public void test_findTargetsByCandidate() {
        var candidate = state.findCandidateAtColumn(5);
        var result = state.matchCandidateToTargets(candidate).toList();

        assertNotNull(result);
        assertEquals(4, result.get(0).target());
        assertEquals("Candidate(cards=[33:2h], origin=COLUMN, from=5, target=4)", result.get(0).toString());

        candidate = state.findCandidateAtColumn(1);
        result = state.matchCandidateToTargets(candidate).toList();
        assertNotNull(result);
        assertEquals(7, result.get(0).target());
        assertEquals("Candidate(cards=[11:5h], origin=COLUMN, from=1, target=7)", result.get(0).toString());
    }

    @Test
    public void test_findTargetColumn_same_column() {
        var candidate = state.findCandidateAtColumn(5);

        assertNull(state.findTargetColumn(candidate.from(), candidate));

        var result = state.findTargetColumn(9, candidate);

        assertNotNull(result);
        assertEquals("Candidate(cards=[33:2h], origin=COLUMN, from=5, target=9)", result.toString());

        candidate = buildCandidate(9, COLUMN, buildCard(0, "2h"));
        assertNull(state.findTargetColumn(9, candidate));
    }

    @Test
    public void test_findOpenCandidates() {
        var candidates = state.findOpenCandidates().toList();

        assertNotNull(candidates);
        assertEquals(10, candidates.size());
        assertEquals("Candidate(cards=[5:Th], origin=COLUMN, from=0, target=-1)", candidates.get(0).toString());
        assertEquals("Candidate(cards=[53:3h], origin=COLUMN, from=9, target=-1)", candidates.get(9).toString());

        state.columns().get(0).clear();
        candidates = state.findOpenCandidates().toList();

        assertNotNull(candidates);
        assertEquals(9, candidates.size());
        assertEquals("Candidate(cards=[11:5h], origin=COLUMN, from=1, target=-1)", candidates.get(0).toString());
        assertEquals("Candidate(cards=[53:3h], origin=COLUMN, from=9, target=-1)", candidates.get(8).toString());
    }

    @Test
    public void test_findCandidateAtColumn() {
        var candidate = state.findCandidateAtColumn(0);

        assertNotNull(candidate);
        assertEquals("Candidate(cards=[5:Th], origin=COLUMN, from=0, target=-1)", candidate.toString());

        candidate = state.findCandidateAtColumn(state.columns().size() - 1);
        assertEquals("Candidate(cards=[53:3h], origin=COLUMN, from=9, target=-1)", candidate.toString());
    }

    @Test
    public void test_getOrderedCardsAtColumn() {
        var result = state.getOrderedCardsAtColumn(mockRun().setOpenAt(0));

        assertNotNull(result);
        assertTrue(isNotEmpty(result));
        assertEquals(13, result.size());
        assertEquals("Kd:Qd:Jd:Td:9d:8d:7d:6d:5d:4d:3d:2d:Ad", stringOfRaws(result));
    }

}