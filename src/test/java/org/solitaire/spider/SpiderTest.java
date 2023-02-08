package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.model.GameBoardTest.cards;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.spider.Spider.SOLUTION_LIMIT;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.ZERO;
import static org.solitaire.util.ReflectHelper.setField;

@ExtendWith(MockitoExtension.class)
class SpiderTest {
    @Mock
    private SpiderBoard state;
    private Spider spider;
    private Candidate candidate;

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;

        state = spy(state);
        spider = build(cards);
        setField(spider, "cloner", (Cloner) i -> state);
        spider.stack().clear();
        spider.add(state);

        candidate = mockCandidate();
    }

    @Test
    public void test_solve_cleared() {
        when(state.isCleared()).thenReturn(true);
        when(state.path()).thenReturn(mockPath());

        var result = spider.solve();

        assertNotNull(result);
        assertEquals(ONE, result.size());
        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).path();
        assertEquals(ZERO, spider.totalScenarios());
    }

    @Test
    public void test_solve_solution_limit() {
        var mock = mockPath();
        range(0, SOLUTION_LIMIT).forEach(i -> spider.solutions().add(mock));

        var result = spider.solve();

        assertNotNull(result);
        assertEquals(SOLUTION_LIMIT, result.size());
        verify(state, times(ONE)).isCleared();
        assertEquals(ONE, spider.totalScenarios());
    }

    @Test
    public void test_solve_applyCandidates() {
        when(state.isCleared()).thenReturn(false);
        when(state.findCandidates()).thenReturn(mockCandidateList());
        when(state.updateBoard(candidate)).thenReturn(null);

        spider.solve();

        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).findCandidates();
        verify(state, times(ONE)).updateBoard(candidate);
        assertEquals(1, spider.totalScenarios());
    }

    @Test
    public void test_solve_applyCandidates_no_recurse() {
        when(state.updateBoard(candidate)).thenReturn(state);

        spider.applyCandidates(mockCandidateList(), state);

        verify(state, times(ZERO)).isCleared();
        verify(state, times(ZERO)).findCandidates();
        verify(state, times(ONE)).updateBoard(candidate);
        assertEquals(0, spider.totalScenarios());
    }

    @Test
    public void test_solve_drawDeck() {
        when(state.isCleared()).thenReturn(false);
        when(state.findCandidates()).thenReturn(emptyList());
        when(state.drawDeck()).thenReturn(false);

        spider.solve();

        verify(state, times(ONE)).isCleared();
        verify(state, times(ONE)).findCandidates();
        verify(state, times(ONE)).drawDeck();
        assertEquals(1, spider.totalScenarios());
    }

    @Test
    public void test_drawDeck_recurse() {
        when(state.drawDeck()).thenReturn(true);

        spider.drawDeck(state);

        verify(state, times(ZERO)).isCleared();
        verify(state, times(ZERO)).findCandidates();
        verify(state, times(ONE)).drawDeck();
        assertEquals(0, spider.totalScenarios());
    }

    @Test
    public void test_updateColumns() {
        setField(spider, "cloner", (Cloner) i -> state);
        when(state.updateBoard(candidate)).thenReturn(state);

        spider.applyCandidates(mockCandidateList(), state);

        verify(state, times(ONE)).updateBoard(candidate);
    }

    @Test
    public void test_updateColumns_null() {
        setField(spider, "cloner", (Cloner) i -> state);
        when(state.updateBoard(candidate)).thenReturn(null);

        spider.applyCandidates(mockCandidateList(), state);

        verify(state, times(ONE)).updateBoard(candidate);
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

    private Path<Card[]> mockPath() {
        var path = new Path<Card[]>();
        path.add(new Card[]{buildCard(0, "Ah")});
        return path;
    }

    interface Cloner extends Function<SpiderBoard, SpiderBoard> {
    }
}