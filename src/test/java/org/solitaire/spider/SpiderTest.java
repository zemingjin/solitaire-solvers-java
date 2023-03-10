package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Candidate;
import org.solitaire.model.Path;

import java.util.Collection;
import java.util.List;

import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.GameBoardTest.cards;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.spider.Spider.SOLUTION_LIMIT;
import static org.solitaire.spider.Spider.hsdDepth;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.CardHelperTest.FIVE;
import static org.solitaire.util.CardHelperTest.FOUR;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class SpiderTest {
    @Mock private SpiderBoard board;
    private Spider spider;
    private Candidate candidate;

    @BeforeEach
    public void setup() {
        useSuit(false);

        board = spy(board);
        spider = MockSpider.build();
        spider.cloner(i -> board);
        spider.stack().clear();
        spider.addBoard(board);

        candidate = mockCandidate();
    }

    @Test
    void test_solveByHSD() {
        singleSolution(true);
        hsdDepth(FOUR);
        spider = build(cards);
        spider.solveBoard(spider::solveByHSD);

        assertEquals(FOUR, hsdDepth());

        range(0, 12).forEach(i -> spider.solveByHSD(spider.stack().peek().pop()));
        var board = spider.board();
        assertNotNull(board);
        assertEquals(48, board.path().size());
    }

    @Test
    public void test_solve_cleared() {
        when(board.isSolved()).thenReturn(true);
        when(board.path()).thenReturn(mockPath());

        var result = spider.solve();

        assertNotNull(result);
        assertEquals(ONE, result.size());
        verify(board, times(TWO)).isSolved();
        verify(board, times(FIVE)).path();
        assertEquals(ZERO, spider.totalScenarios());
    }

    @Test
    public void test_solve_solution_limit() {
        var mock = mockPath();
        range(0, SOLUTION_LIMIT).forEach(i -> spider.solutions().add(mock));

        var result = spider.solve();

        assertNotNull(result);
        assertEquals(SOLUTION_LIMIT, result.size());
        assertEquals(ZERO, spider.totalScenarios());
    }

    @Test
    public void test_solve_applyCandidates() {
        when(board.isSolved()).thenReturn(false);
        when(board.findCandidates()).thenReturn(mockCandidateList());
        when(board.updateBoard(candidate)).thenReturn(board);

        spider.solve();

        verify(board, times(TWO)).isSolved();
        verify(board).findCandidates();
        verify(board).updateBoard(candidate);
        verify(board, times(ZERO)).drawDeck();
        assertEquals(1, spider.totalScenarios());
    }

    @Test
    public void test_solve_applyCandidates_no_recurse() {
        when(board.updateBoard(candidate)).thenReturn(board);

        var result = spider.applyCandidates(mockCandidateList(), board).toList();

        assertFalse(result.isEmpty());
        verify(board, times(ONE)).updateBoard(candidate);
        assertEquals(0, spider.totalScenarios());
    }

    @Test
    public void test_updateColumns() {
        when(board.updateBoard(candidate)).thenReturn(board);

        var result = spider.applyCandidates(mockCandidateList(), board).toList();

        assertFalse(result.isEmpty());
        verify(board, times(ONE)).updateBoard(candidate);
    }

    @Test
    public void test_updateColumns_null() {
        when(board.updateBoard(candidate)).thenReturn(null);

        var result = spider.applyCandidates(mockCandidateList(), board).toList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(board, times(ONE)).updateBoard(candidate);
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
        return buildCandidate(0, COLUMN, buildCard(0, "Ad"));
    }

    private Path<String> mockPath() {
        var path = new Path<String>();
        path.add(new Candidate(List.of(card("Ah")), COLUMN, 0, COLUMN, 5).notation());
        return path;
    }

    static class MockSpider extends Spider {
        private boolean first = true;

        private MockSpider(SpiderBoard board) {
            super(board.columns(), board.path(), board.totalScore(), board.deck());
        }

        public static MockSpider build() {
            singleSolution(false);
            return new MockSpider(SpiderHelper.build(cards).board());
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
}