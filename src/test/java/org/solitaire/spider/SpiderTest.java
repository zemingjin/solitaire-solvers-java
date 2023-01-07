package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Candidate;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.model.GameStateTest.cards;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.ReflectHelper.setField;
import static org.solitaire.util.SolitaireHelper.getTotalScenarios;
import static org.solitaire.util.SolitaireHelper.setTotalScenarios;

@ExtendWith(MockitoExtension.class)
class SpiderTest {
    public static final int ONE = 1;
    @Mock
    private SpiderState state;
    private Spider spider;
    private Candidate candidate;

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
        setTotalScenarios(0);

        state = spy(state);
        spider = build(cards);
        setField(spider, "cloner", (Cloner) i -> state);

        candidate = mockCandidate();
    }

    @Test
    public void test_solve_applyCandidates() {
        when(state.isCleared()).thenReturn(false);
        when(state.findCandidates()).thenReturn(mockCandidateList());
        when(state.updateState(candidate)).thenReturn(null);

        spider.solve(state);

        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).findCandidates();
        verify(state, times(ONE)).updateState(candidate);
        assertEquals(1, getTotalScenarios());
    }

    @Test
    public void test_solve_drawDeck() {
        when(state.isCleared()).thenReturn(false);
        when(state.findCandidates()).thenReturn(emptyList());
        when(state.drawDeck()).thenReturn(false);
        setField(spider, "cloner", (Cloner) i -> state);

        spider.solve(state);

        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).findCandidates();
        verify(state, times(ONE)).drawDeck();
        assertEquals(1, getTotalScenarios());
    }

    @Test
    public void test_solve_cleared() {
        when(state.isCleared()).thenReturn(true);
        when(state.getPath()).thenReturn(mockPath());
        setField(spider, "state", state);

        var result = spider.solve();

        assertNotNull(result);
        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).getPath();
        assertEquals(ONE, result.size());
        assertEquals("[mock solution]", result.get(0).toString());
        assertEquals(0, getTotalScenarios());
    }

    @Test
    public void test_updateColumns() {
        setField(spider, "cloner", (Cloner) i -> state);
        when(state.updateState(candidate)).thenReturn(state);

        spider.applyCandidates(mockCandidateList(), state);

        verify(state, times(ONE)).updateState(candidate);
    }

    @Test
    public void test_updateColumns_null() {
        setField(spider, "cloner", (Cloner) i -> state);
        when(state.updateState(candidate)).thenReturn(null);

        spider.applyCandidates(mockCandidateList(), state);

        verify(state, times(ONE)).updateState(candidate);
    }

    @Test
    public void test_drawDeck() {
        when(state.drawDeck()).thenReturn(false);

        spider.drawDeck(state);

        verify(state, times(ONE)).drawDeck();
    }

    @Test
    public void test_drawDeck_fail() {
        when(state.drawDeck()).thenReturn(false);

        spider.drawDeck(state);

        verify(state, times(ONE)).drawDeck();
    }

    @Test
    public void test_getMaxScore() {
        var result = spider.getMaxScore(spider.solve());

        assertNotNull(result);
        assertEquals(0, result.getLeft());
        assertNotNull(result.getRight());
        assertTrue(result.getRight().isEmpty());
    }

    private List<Candidate> mockCandidateList() {
        return List.of(candidate);
    }

    private Candidate mockCandidate() {
        return Candidate.buildCandidate(0, COLUMN, buildCard(0, "Ad"));
    }

    private Path<String> mockPath() {
        var path = new Path<String>();
        path.add("mock solution");
        return path;
    }

    interface Cloner extends Function<SpiderState, SpiderState> {
    }
}