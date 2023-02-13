package org.solitaire.klondike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.klondike.KlondikeHelper.build;
import static org.solitaire.klondike.KlondikeHelperTest.CARDS;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class KlondikeTest {
    @Mock
    private KlondikeBoard state;
    private Klondike klondike;
    private KlondikeBoard initState;

    @BeforeEach
    public void setup() {
        state = spy(state);
        CardHelper.useSuit = false;
        klondike = build(CARDS);
        initState = klondike.stack().peek().peek();
        klondike.stack().clear();
        klondike.addBoard(state);
        klondike.cloner(it -> state);
    }

    @Test
    public void test_solve() {
        klondike.stack().clear();
        klondike.addBoard(initState);
        klondike.cloner(KlondikeBoard::new);

//        assertEquals(LIMIT_SOLUTIONS, klondike.solve().size());
        assertEquals(0, klondike.solve().size());
    }

    @Test
    public void test_getMaxScore() {
        var result = klondike.getMaxScore(null);

        assertNull(result);
    }

    @Test
    public void test_solve_cleared() {
        when(state.isCleared()).thenReturn(true);
        when(state.path()).thenReturn(new Path<>());
        var result = klondike.solve();

        assertNotNull(result);
        assertEquals(ONE, result.size());
        assertTrue(result.get(0).isEmpty());
        assertEquals(ZERO, klondike.totalScenarios());
    }

    @Test
    public void test_solve_uncleared() {
        var candidates = List.of(
                buildCandidate(4, COLUMN, initState.columns().get(4).peek()),
                buildCandidate(3, COLUMN, initState.columns().get(3).peek()));
        when(state.isCleared()).thenReturn(false);
        when(state.findCandidates()).thenReturn(candidates);
        when(state.updateBoard(any())).thenReturn(null);

        klondike.solve();

        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).findCandidates();
        verify(state, times(TWO)).updateBoard(any());
        assertEquals(1, klondike.totalScenarios());
    }

    @Test
    public void test_solve_drawDeck() {
        when(state.isCleared()).thenReturn(false);
        when(state.findCandidates()).thenReturn(emptyList());
        when(state.drawDeckCards()).thenReturn(null);

        klondike.solve();

        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).findCandidates();
        verify(state, times(ONE)).drawDeckCards();
        assertEquals(1, klondike.totalScenarios());
    }

}