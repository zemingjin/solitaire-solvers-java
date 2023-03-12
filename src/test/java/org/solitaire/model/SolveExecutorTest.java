package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.model.SolveExecutor.isPrint;
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
        isPrint(false);
        executor = new SolveExecutor<>(board);
        executor.cloner(it -> board);
    }

    @Test
    void test_defaultSolutionConsumer() {
        executor.defaultSolutionConsumer(List.of("1"));
        assertEquals(1, executor.shortestPath().size());
        assertEquals(1, executor.longestPath().size());

        executor.defaultSolutionConsumer(List.of("1", "2"));
        assertEquals(1, executor.shortestPath().size());
        assertEquals(2, executor.longestPath().size());
        assertEquals("[1, 2]", executor.longestPath().toString());

        executor.defaultSolutionConsumer(List.of("2", "3"));
        assertEquals(1, executor.shortestPath().size());
        assertEquals(2, executor.longestPath().size());
        assertEquals("[1, 2]", executor.longestPath().toString());
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

        executor.totalSolutions(1);
        assertFalse(executor.isContinuing());
    }

    @Test
    void test_solve_cleared() {
        when(board.isSolved()).thenReturn(true);
        when(board.path()).thenReturn(List.of(ABC));

        executor.solve();

        verify(board, times(TWO)).isSolved();
        verify(board, times(ONE)).path();
        assertEquals("[ABC]", executor.shortestPath().toString());
        assertEquals("[ABC]", executor.longestPath().toString());
        assertEquals(ZERO, executor.totalScenarios());
        assertEquals(ONE, executor.maxStack());
        assertTrue(executor.stack().isEmpty());
    }

    @Test
    void test_solve_notCleared() {
        assertTrue(executor.stack().peek().add(board));
        when(board.isSolved()).thenReturn(false);

        executor.solve();

        assertTrue(executor.stack().isEmpty());
        verify(board, times(THREE)).isSolved();
        verify(board, never()).path();
        assertEquals(ZERO, executor.totalScenarios());
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

    @Test
    void test_removeSolutionConsumer() {
        Consumer<List<String>> consumer = this::mockSolutionConsumer;
        executor.addSolutionConsumer(consumer);

        assertEquals(2, executor.solutionConsumers().size());

        assertTrue(executor.removeSolutionConsumer(consumer));
        assertFalse(executor.removeSolutionConsumer(consumer));
        assertEquals(1, executor.solutionConsumers().size());
    }

    void mockSolutionConsumer(List<String> path) {
        assertTrue(nonNull(path));
    }
}