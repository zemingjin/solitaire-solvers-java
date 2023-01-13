package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class FreeCellTest {
    private FreeCell freeCell;
    @Mock
    private FreeCellState state;

    @BeforeEach
    public void setup() {
        state = spy(state);
        freeCell = FreeCellHelper.build(FreeCellHelperTest.cards).initState(state);
        freeCell.cloner(it -> state);
    }

    @Test
    public void test_solve() {
        var candidate = buildCandidate(0, COLUMN, buildCard(0, "Ad"));

        when(state.isCleared()).thenReturn(false);
        when(state.findCandidates()).thenReturn(List.of(candidate));
        when(state.updateState(eq(candidate))).thenReturn(null);

        var result = freeCell.solve();

        assertNotNull(result);
        assertEquals(ZERO, result.size());
        assertEquals(ONE, freeCell.totalScenarios());
        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).findCandidates();
        verify(state, times(ONE)).updateState(eq(candidate));
    }

    @Test
    public void test_solve_cleared() {
        when(state.isCleared()).thenReturn(true);

        var result = freeCell.solve();

        verify(state, times(ONE)).isCleared();
        assertNotNull(result);
        assertEquals(ONE, result.size());
        assertEquals(ZERO, freeCell.totalScenarios());
    }

    @Test
    public void test_applyCandidates() {
        var candidate = buildCandidate(0, COLUMN, buildCard(0, "Ad"));

        when(state.isCleared()).thenReturn(true);
        when(state.updateState(eq(candidate))).thenReturn(state);

        freeCell.applyCandidates(List.of(candidate), state);

        assertEquals(ONE, freeCell.solutions().size());
        assertEquals(ZERO, freeCell.totalScenarios());

        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).updateState(eq(candidate));
    }

    @Test
    public void test_getMaxScore() {
        assertThrows(RuntimeException.class, () -> freeCell.getMaxScore(null));
    }

}