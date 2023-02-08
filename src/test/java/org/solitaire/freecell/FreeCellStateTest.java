package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.freecell.FreeCellHelper.build;
import static org.solitaire.freecell.FreeCellHelperTest.cards;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.isCleared;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;

class FreeCellStateTest {
    private FreeCellState state;

    @BeforeEach
    void setup() {
        CardHelper.useSuit = false;
        state = build(cards).stack().peek().peek();
    }

    @Test
    void test_isCleared() {
        assertFalse(state.isCleared());

        state.columns().forEach(List::clear);

        assertTrue(state.isCleared());
    }

    @Test
    void test_clone() {
        var clone = new FreeCellState(state);

        assertNotSame(state, clone);
        assertTrue(reflectionEquals(state, clone));
    }

    @Test
    void test_findCandidates() {
        state.freeCells()[0] = buildCard(0, "Ad");
        state.columns().get(6).add(buildCard(0, "6c"));
        var result = state.findCandidates();

        assertNotNull(result);
        assertEquals(5, result.size());
        assertFalse(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals("Candidate[cards=[23:5d, 27:4s], origin=COLUMN, from=3, target=6]", result.get(1).toString());
        assertEquals("Candidate[cards=[0:Ad], origin=FREECELL, from=0, target=7]", result.get(4).toString());

        fillFreeCells(TWO, buildCard(0, "Kd"));
        result = state.findCandidates();

        assertNotNull(result);
        assertFalse(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals(5, result.size());
        assertEquals("Candidate[cards=[23:5d, 27:4s], origin=COLUMN, from=3, target=6]", result.get(1).toString());

        fillFreeCells(ONE, buildCard(0, "Kd"));
        result = state.findCandidates();

        assertNotNull(result);
        assertTrue(result.stream().allMatch(it -> it.cards().size() == 1));
        assertEquals(3, result.size());
        assertEquals("Candidate[cards=[39:Th], origin=COLUMN, from=5, target=4]", result.get(1).toString());
    }

    @Test
    void test_findFreeCellCandidates() {
        var result = state.findFreeCellCandidates().toList();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        state.freeCells()[0] = buildCard(0, "Ad");

        result = state.findFreeCellCandidates().toList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Candidate[cards=[0:Ad], origin=FREECELL, from=0, target=-1]", result.get(0).toString());
    }

    @Test
    void test_findColumnCandidates() {
        var result = state.findColumnCandidates().toList();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertEquals("Candidate[cards=[23:5d, 27:4s], origin=COLUMN, from=3, target=-1]",
                result.get(3).toString());
    }

    @Test
    void test_findCandidateAtColumn_colNum() {
        var result = state.findCandidateAtColumn(0);

        assertNotNull(result);
        assertEquals("Candidate[cards=[6:7d], origin=COLUMN, from=0, target=-1]", result.toString());
    }

    @Test
    void test_findCandidateAtColumn_column() {
        var result = state.findCandidateAtColumn(state.columns().get(0));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("6:7d", result.get(0).toString());
    }

    @Test
    void test_removeFromOrigin() {
        var candidate = state.findCandidates().get(0);
        var result = state.removeFromOrigin(candidate);

        assertNotNull(result);
        assertTrue(reflectionEquals(candidate, result));
        assertEquals(6, state.columns().get(candidate.from()).size());
        assertFalse(state.columns().get(candidate.from()).contains(candidate.peek()));
    }

    @Test
    void test_removeFromOrigin_freecell() {
        state.freeCells()[0] = buildCard(0, "Ad");
        var candidates = state.findCandidates();
        var candidate = candidates.get(3);
        var result = state.removeFromOrigin(candidate);

        assertNotNull(result);
        assertTrue(reflectionEquals(candidate, result));
        assertNull(state.freeCells()[candidate.from()]);
    }

    @Test
    void test_moveToTarget() {
        var candidate = state.findCandidates().get(0);
        var result = state.updateState(candidate);

        assertNotNull(result);
        assertSame(state, result);
        assertTrue(isCleared(state.freeCells()));
        assertEquals(8, result.columns().get(candidate.target()).size());
        assertEquals("6:7d", result.columns().get(candidate.target()).peek().toString());
        assertEquals(1, state.path().size());
        assertEquals("[7d]", stringOfRaws(state.path().get(0)));
        assertTrue(Arrays.stream(state.freeCells()).allMatch(Objects::isNull));

        result = state.updateState(buildCandidate(candidate, 0));
        assertEquals(6, result.columns().get(0).size());
        assertEquals("6:7d", result.columns().get(0).peek().toString());
        assertEquals(2, state.path().size());
    }

    @Test
    void test_moveToTarget_freeCell() {
        var candidate = state.findCandidates().get(0);

        candidate = buildCandidate(candidate, -1);
        var result = state.updateState(candidate);

        assertNotNull(result);
        assertSame(state, result);
        assertFalse(isCleared(state.freeCells()));
        assertEquals("6:7d", result.freeCells()[0].toString());
    }

    @Test
    void test_moveToTarget_freeCell_fail() {
        var candidate = state.findCandidates().get(0);

        candidate = buildCandidate(candidate, -1);
        fillFreeCells(0, buildCard(0, "Ad"));
        var result = state.updateState(candidate);

        assertNull(result);
    }


    @Test
    void test_maxCardsToMove() {
        var card = state.columns().get(0).peek();

        assertEquals(5, state.maxCardsToMove());

        state.freeCells()[0] = card;
        assertEquals(4, state.maxCardsToMove());

        state.freeCells()[1] = card;
        assertEquals(3, state.maxCardsToMove());

        state.freeCells()[2] = card;
        assertEquals(2, state.maxCardsToMove());

        state.columns().get(7).clear();
        assertEquals(4, state.maxCardsToMove());

        state.columns().get(6).clear();
        assertEquals(6, state.maxCardsToMove());
    }

    private void fillFreeCells(int from, Card card) {
        range(from, state.freeCells().length).forEach(i -> state.freeCells()[i] = card);
    }
}