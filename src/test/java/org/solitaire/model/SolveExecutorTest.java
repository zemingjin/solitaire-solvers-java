package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.THREE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class SolveExecutorTest {
    private static final String ABC = "ABC";

    @Mock Board<String, String> board;

    private SolveExecutor<String, String, Board<String, String>> executor;

    @BeforeEach
    void setup() {
        executor = new SolveExecutor<>(board);
        executor.isPrint(false);
        executor.cloner(it -> board);
        executor.solveBoard(this::solveBoard);
    }

    @Test
    void test_constructor() {
        assertNotNull(executor);
        assertEquals(ONE, executor.stack().size());
    }

    @Test
    void test_isContinuing() {
        SolveExecutor.singleSolution(true);

        assertTrue(executor.isContinuing());

        executor.solutions().add(List.of(ABC));
        assertFalse(executor.isContinuing());
    }

    @Test
    void test_solve_cleared() {
        when(board.isSolved()).thenReturn(true);
        when(board.path()).thenReturn(List.of(ABC));

        var result = executor.solve();

        verify(board, times(TWO)).isSolved();
        verify(board, times(THREE)).path();
        assertEquals("[[ABC]]", result.toString());
        assertEquals(ZERO, executor.totalScenarios());
        assertEquals(ONE, executor.maxStack());
        assertTrue(executor.stack().isEmpty());
    }

    @Test
    void test_solve_notCleared() {
        assertTrue(executor.stack().peek().add(board));
        when(board.isSolved()).thenReturn(false);

        var result = executor.solve();

        assertTrue(executor.stack().isEmpty());
        verify(board, times(THREE)).isSolved();
        verify(board, never()).path();
        assertEquals("[]", result.toString());
        assertEquals(TWO, executor.totalScenarios());
        assertEquals(ONE, executor.maxStack());
    }

    @Test
    void test_addBoard() {
        assertTrue(executor.addBoard(board));
        assertEquals(TWO, executor.stack().size());
    }

    @Test
    void test_addBoards() {
        assertTrue(executor.addBoards(List.of(board, board)));
        assertEquals(TWO, executor.stack().size());
        assertEquals(TWO, executor.stack().peek().size());
        assertFalse(executor.addBoards(List.of()));
    }

    private void solveBoard(Board<String, String> board) {
        assertNotNull(board);
    }

    @Test
    void test_getMaxScore() {
        var result = assertThrows(RuntimeException.class, () -> executor.getMaxScore(null));

        assertEquals("Not implemented", result.getMessage());
    }
}