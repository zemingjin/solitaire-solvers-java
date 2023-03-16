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
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.spider.Spider.SOLUTION_LIMIT;
import static org.solitaire.spider.Spider.hsdDepth;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.CardHelperTest.FIVE;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.SIX;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class SpiderTest {
    @Mock private SpiderBoard board;
    private Spider spider;
    private Candidate candidate;

    @BeforeEach
    void setup() {
        useSuit(false);
        isPrint(false);

        board = spy(board);
        spider = MockSpider.build();
        spider.cloner(i -> board);
        spider.stack().clear();
        spider.addBoard(board);

        candidate = mockCandidate();
    }

    @Test
    void test_solveByHSD() {
        singleSolution(false);
        hsdDepth(FIVE);
        spider = build(cards);
        assertTrue(singleSolution());

        assertEquals(SIX, hsdDepth());

        range(0, 5).forEach(i -> spider.solveByHSD(spider.stack().peek().pop()));
        var board = spider.board();
        assertNotNull(board);
        assertEquals(30, board.path().size());
    }

    @Test
    void test_isContinuing() {
        singleSolution(false);
        assertTrue(spider.isContinuing());

        spider.totalSolutions(SOLUTION_LIMIT - 1);
        assertTrue(spider.isContinuing());

        spider.totalSolutions(SOLUTION_LIMIT);
        assertFalse(spider.isContinuing());
    }

    @Test
    void test_solve_cleared() {
        when(board.isSolved()).thenReturn(true);
        when(board.path()).thenReturn(mockPath());

        spider.solve();

        assertEquals(ONE, spider.totalSolutions());
        verify(board, times(ONE)).isSolved();
        verify(board, times(ONE)).path();
        assertEquals(ZERO, spider.totalScenarios());
    }

    @Test
    void test_solve_solution_limit() {
        assertTrue(spider.isContinuing());
        spider.totalSolutions(SOLUTION_LIMIT);
        assertFalse(spider.isContinuing());

        spider.solve();

        assertEquals(SOLUTION_LIMIT, spider.totalSolutions());
        assertEquals(ZERO, spider.totalScenarios());
    }

    @Test
    void test_solve_applyCandidates() {
        when(board.isSolved()).thenReturn(false);
        when(board.findCandidates()).thenReturn(mockCandidateList());
        when(board.updateBoard(candidate)).thenReturn(null);

        spider.solve();

        verify(board, times(ONE)).isSolved();
        verify(board).findCandidates();
        verify(board).updateBoard(candidate);
        verify(board, times(ZERO)).drawDeck();
        assertEquals(1, spider.totalScenarios());
    }

    @Test
    void test_solve_applyCandidates_no_recurse() {
        when(board.updateBoard(candidate)).thenReturn(board);

        var result = spider.applyCandidates(mockCandidateList(), board).toList();

        assertFalse(result.isEmpty());
        verify(board).updateBoard(candidate);
        assertEquals(ZERO, spider.totalScenarios());
    }

    @Test
    void test_updateColumns() {
        when(board.updateBoard(candidate)).thenReturn(board);

        var result = spider.applyCandidates(mockCandidateList(), board).toList();

        assertFalse(result.isEmpty());
        verify(board, times(ONE)).updateBoard(candidate);
    }

    @Test
    void test_updateColumns_null() {
        when(board.updateBoard(candidate)).thenReturn(null);

        var result = spider.applyCandidates(mockCandidateList(), board).toList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(board, times(ONE)).updateBoard(candidate);
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

        static MockSpider build() {
            singleSolution(false);
            return new MockSpider(SpiderHelper.build(cards).board());
        }

        @Override
        public void addBoards(Collection<SpiderBoard> boards) {
            if (first) {
                first = false;
                super.addBoards(boards);
            }
            first = true;
        }
    }
}