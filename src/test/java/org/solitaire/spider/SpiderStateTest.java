package org.solitaire.spider;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Column;
import org.solitaire.util.CardHelper;

import java.util.List;

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
        state = new SpiderState(build(cards));
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

        state.getColumns().forEach(List::clear);
        state.deck.clear();

        assertTrue(state.isCleared());
    }

    @Test
    public void test_updateTargetColumn() {
        state.getColumns().get(5).add(buildCard(0, "Ah"));
        var candidate = state.findCandidateAtColumn(5).setTarget(9);

        assertEquals(6, state.getColumns().get(candidate.getFrom()).size());
        assertEquals(5, state.getColumns().get(candidate.getTarget()).size());
        var clone = state.updateState(candidate);

        assertNotNull(clone);
        assertEquals(4, clone.getColumns().get(candidate.getFrom()).size());
        assertEquals(7, clone.getColumns().get(candidate.getTarget()).size());
    }

    @Test
    public void test_drawDeck() {
        assertEquals(LAST_DECK, state.deck.size());

        assertTrue(state.drawDeck());

        assertEquals(LAST_DECK - state.getColumns().size(), state.deck.size());
        assertEquals("5s", state.getColumns().get(0).peek().raw());
        assertEquals("Qh", state.getColumns().get(9).peek().raw());

        state.deck.clear();

        assertFalse(state.drawDeck());
    }

    @Test
    public void test_checkForRuns() {
        state.getColumns().set(0, mockRun());
        var column = state.getColumns().get(0);
        var candidate = buildCandidate(0, COLUMN, List.of(column.peek()), 0);

        assertEquals(13, column.size());
        assertEquals("0:Kd", column.get(0).toString());
        assertEquals("0:Ad", column.get(12).toString());
        assertEquals(500, state.getTotalScore());

        var result = state.checkForRun(candidate);

        assertNotNull(result);
        assertTrue(column.isEmpty());
        assertEquals("Kd:Qd:Jd:Td:9d:8d:7d:6d:5d:4d:3d:2d:Ad", state.getPath().get(0));
        assertEquals(600, state.getTotalScore());
    }

    @Test
    public void test_checkForRuns_noRuns() {
        var column = state.getColumns().get(0);
        var candidate = buildCandidate(0, COLUMN, List.of(column.peek()), 0);
        column.addAll(mockRun(10));

        assertEquals(16, column.size());
        assertEquals(500, state.getTotalScore());

        var result = state.checkForRun(candidate);

        assertNotNull(result);
        assertEquals(16, column.size());
        assertEquals(500, state.getTotalScore());
    }

    @Test
    public void test_appendToTarget() {
        var candidate = state.findCandidateAtColumn(5).setTarget(9);
        var column = state.getColumns().get(candidate.getTarget());

        assertEquals(5, column.size());
        assertEquals("53:3h", column.peek().toString());
        assertTrue(state.getPath().isEmpty());
        assertEquals(500, state.getTotalScore());

        state.appendToTarget(candidate);

        assertEquals(6, column.size());
        assertNotEquals("53:3h", column.peek().toString());
        assertEquals("33:2h", column.peek().toString());

        assertFalse(state.getPath().isEmpty());
        assertEquals(1, state.getPath().size());
        assertEquals("2h", state.getPath().get(0));
        assertEquals(499, state.getTotalScore());
    }

    @Test
    public void test_removeFromSource() {
        var candidate = state.findCandidateAtColumn(5).setTarget(9);
        var column = state.getColumns().get(candidate.getFrom());

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
        assertEquals(7, targets.get(0).getTarget());
        assertEquals(9, targets.get(1).getTarget());
    }

    @Test
    public void test_checkMultiples() {
        var card = state.getColumns().get(3).peek();
        var a = buildCandidate(3, COLUMN, List.of(card), 0);
        var candidstes = List.of(a);

        assertSame(a, state.selectACandidate(candidstes));

        var b = buildCandidate(a.getFrom(), a.getOrigin(), a.getCards(), 8);
        var result = state.selectACandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(a, result);

        state.getColumns().get(b.getTarget()).set(4, buildCard(0, "Ts"));
        result = state.selectACandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(b, result);

        state.getColumns().get(a.getTarget()).set(5, buildCard(0, "Ts"));
        state.getColumns().get(a.getTarget()).set(4, buildCard(0, "Js"));
        result = state.selectACandidate(List.of(a, b));
        assertNotNull(result);
        assertSame(a, result);
    }

    @Test
    public void test_findTargetsByCandidate() {
        var candidate = state.findCandidateAtColumn(5);
        var targets = state.matchCandidateToTargets(candidate).toList();

        assertNotNull(targets);
        assertEquals(4, targets.get(0).getTarget());
        assertEquals("Candidate(cards=[33:2h], origin=COLUMN, from=5, target=4)", targets.get(0).toString());

        candidate = state.findCandidateAtColumn(1);
        targets = state.matchCandidateToTargets(candidate).toList();
        assertNotNull(targets);
        assertEquals(7, targets.get(0).getTarget());
        assertEquals("Candidate(cards=[11:5h], origin=COLUMN, from=1, target=7)", targets.get(0).toString());
    }

    @Test
    public void test_findTargetColumn_same_column() {
        var candidate = state.findCandidateAtColumn(5);

        assertNull(state.findTargetColumn(candidate.getFrom(), candidate));
        assertNotNull(state.findTargetColumn(9, candidate));
    }

    @Test
    public void test_findOpenCandidates() {
        var candidates = state.findOpenCandidates().toList();

        assertNotNull(candidates);
        assertEquals(10, candidates.size());
        assertEquals("Candidate(cards=[5:Th], origin=COLUMN, from=0, target=-1)", candidates.get(0).toString());
        assertEquals("Candidate(cards=[53:3h], origin=COLUMN, from=9, target=-1)", candidates.get(9).toString());

        state.getColumns().get(0).clear();
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

        candidate = state.findCandidateAtColumn(state.getColumns().size() - 1);
        assertEquals("Candidate(cards=[53:3h], origin=COLUMN, from=9, target=-1)", candidate.toString());
    }

}