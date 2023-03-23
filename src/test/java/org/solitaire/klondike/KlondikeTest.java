package org.solitaire.klondike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.util.IOHelper;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.klondike.Klondike.SOLUTION_LIMIT;
import static org.solitaire.klondike.KlondikeHelper.build;
import static org.solitaire.model.Candidate.columnToColumn;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class KlondikeTest {
    private static final String TEST_FILE = "games/klondike/event-dark-klondike-expert.txt";

    @Mock private KlondikeBoard board;

    private Klondike klondike;

    @BeforeEach
    void setup() {
        board = spy(board);
        useSuit(false);
        singleSolution(false);
        isPrint(false);
        klondike = mockKlondike();
    }

    @Test
    void test_isContinuing() {
        assertTrue(klondike.isContinuing());
        klondike.totalSolutions(SOLUTION_LIMIT);
        assertFalse(klondike.isContinuing());
    }

    @Test
    void test_solve_cleared() {
        when(board.isSolved()).thenReturn(true);
        when(board.path()).thenReturn(new Path<>());

        klondike.solve();

        assertEquals(ZERO, klondike.totalScenarios());
    }

    @Test
    void test_solve_uncleared() {
        var candidates = List.of(
                columnToColumn(card("Ac"), 0, 1),
                columnToColumn(card("3d"), 0, 2));
        when(board.isSolved()).thenReturn(false);
        when(board.findCandidates()).thenReturn(candidates);
        when(board.updateBoard(any())).thenReturn(null);

        klondike.solve();

        verify(board).isSolved();
        verify(board).findCandidates();
        verify(board, times(TWO)).updateBoard(any());
        assertEquals(ONE, klondike.totalScenarios());
    }

    @Test
    void test_solve_drawDeck() {
        when(board.isSolved()).thenReturn(false);
        when(board.findCandidates()).thenReturn(emptyList());

        klondike.solve();

        verify(board).isSolved();
        verify(board).findCandidates();
        assertEquals(ONE, klondike.totalScenarios());
    }

    @Test
    void test_solveByHDS() {
        singleSolution(true);
        klondike = build(IOHelper.loadFile(TEST_FILE));

        var board = klondike.board();
        klondike.stack().pop();
        for (int i = 0; i < 10; i++) {
            klondike.solveByHSD(board);
            board = klondike.board();
        }
        board = klondike.board();

        assertNotNull(board);
//        assertEquals(61, board.path().size());
    }

    @Test
    void test_maxScore_exception() {
        assertThrows(RuntimeException.class, () -> klondike.maxScore());
    }

    private Klondike mockKlondike() {
        var klondike = new Klondike(new Columns(), new Deck(), new ArrayList<>());

        klondike.stack().clear();
        klondike.addBoard().accept(board);
        klondike.cloner(it -> board);

        return klondike;
    }
}