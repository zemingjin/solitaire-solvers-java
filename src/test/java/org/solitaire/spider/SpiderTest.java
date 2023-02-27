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

import java.util.Collection;
import java.util.List;

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
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class SpiderTest {
    static class MockSpider extends Spider {
        private boolean first = true;

        public MockSpider(SpiderBoard board) {
            super(board.columns(), board.path(), board.totalScore(), board.deck());
        }

        @Override
        public boolean addBoards(Collection<SpiderBoard> boards) {
            if (first) {
                first = false;
                return super.addBoards(boards);
            }
            first = true;
            return false;
        }
    }

    @Mock private SpiderBoard board;

    private Spider spider;
    private Candidate candidate;

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;

        board = spy(board);
        spider = new MockSpider(build(cards).stack().peek().peek());
        spider.singleSolution(true);
        spider.cloner(i -> board);
        spider.stack().clear();
        spider.addBoard(board);

        candidate = mockCandidate();
    }

    @Test
    public void test_solve_cleared() {
        when(board.isCleared()).thenReturn(true);
        when(board.path()).thenReturn(mockPath());

        var result = spider.solve();

        assertNotNull(result);
        assertEquals(ONE, result.size());
        verify(board, times(ONE)).isCleared();
        verify(board, times(ONE)).path();
        assertEquals(ZERO, spider.totalScenarios());
    }

    @Test
    public void test_solve_solution_limit() {
        var mock = mockPath();
        range(0, SOLUTION_LIMIT).forEach(i -> spider.solutions().add(mock));

        var result = spider.solve();

        assertNotNull(result);
        assertEquals(SOLUTION_LIMIT, result.size());
        verify(board, times(ZERO)).isCleared();
        assertEquals(ZERO, spider.totalScenarios());
    }

    @Test
    public void test_solve_applyCandidates() {
        when(board.isCleared()).thenReturn(false);
        when(board.findCandidates()).thenReturn(mockCandidateList());
        when(board.updateBoard(candidate)).thenReturn(board);

        spider.solve();

        verify(board, times(ONE)).isCleared();
        verify(board, times(ONE)).findCandidates();
        verify(board, times(ONE)).updateBoard(candidate);
        verify(board, times(ZERO)).drawDeck();
        assertEquals(1, spider.totalScenarios());
    }

    @Test
    public void test_solve_applyCandidates_no_recurse() {
        when(board.updateBoard(candidate)).thenReturn(board);

        spider.applyCandidates(mockCandidateList(), board);

        verify(board, times(ZERO)).isCleared();
        verify(board, times(ZERO)).findCandidates();
        verify(board, times(ONE)).updateBoard(candidate);
        assertEquals(0, spider.totalScenarios());
    }

    @Test
    public void test_solve_drawDeck() {
        when(board.isCleared()).thenReturn(false);
        when(board.findCandidates()).thenReturn(emptyList());
        when(board.drawDeck()).thenReturn(true);

        spider.solve();

        verify(board, times(ONE)).isCleared();
        verify(board, times(ONE)).findCandidates();
        verify(board, times(ONE)).drawDeck();
        assertEquals(1, spider.totalScenarios());
        assertTrue(((MockSpider) spider).first);
    }

    @Test
    public void test_updateColumns() {
        when(board.updateBoard(candidate)).thenReturn(board);

        spider.applyCandidates(mockCandidateList(), board);

        verify(board, times(ONE)).updateBoard(candidate);
    }

    @Test
    public void test_updateColumns_null() {
        when(board.updateBoard(candidate)).thenReturn(null);

        spider.applyCandidates(mockCandidateList(), board);

        verify(board, times(ONE)).updateBoard(candidate);
    }

    @Test
    public void test_drawDeck() {
        when(board.drawDeck()).thenReturn(true);

        spider.drawDeck(board);

        verify(board, times(ONE)).drawDeck();
        assertTrue(((MockSpider) spider).first);
    }

    @Test
    public void test_drawDeck_fail() {
        when(board.drawDeck()).thenReturn(false);

        spider.drawDeck(board);

        verify(board, times(ONE)).drawDeck();
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
        path.add(toArray(buildCard(0, "Ah")));
        return path;
    }
}