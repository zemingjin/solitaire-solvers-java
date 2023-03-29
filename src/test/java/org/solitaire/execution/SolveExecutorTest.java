package org.solitaire.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Board;
import org.solitaire.model.BoardStack;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.execution.SolveExecutor.isPrint;
import static org.solitaire.execution.SolveExecutor.singleSolution;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.THREE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class SolveExecutorTest {
    private static final String ABC = "ABC";

    @Mock Board<String, String> board;
    private ByteArrayOutputStream outputStream;

    private SolveExecutor<String, String, Board<String, String>> executor;

    @BeforeEach
    void setup() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        isPrint(false);
        executor = new SolveExecutor<>(board);
        executor.cloner(it -> board);
    }

    @Test
    void test_defaultSolutionConsumer() {
        singleSolution(false);
        executor.defaultSolutionConsumer(List.of("1"));
        assertEquals(1, executor.shortestPath().size());
        assertEquals(1, executor.longestPath().size());

        executor.defaultSolutionConsumer(List.of("2"));
        assertEquals("[1]", executor.shortestPath().toString());

        executor.defaultSolutionConsumer(List.of("1", "2"));
        assertEquals(1, executor.shortestPath().size());
        assertEquals(2, executor.longestPath().size());
        assertEquals("[1, 2]", executor.longestPath().toString());

        executor.defaultSolutionConsumer(List.of("2", "3"));
        assertEquals(1, executor.shortestPath().size());
        assertEquals(2, executor.longestPath().size());
        assertEquals("[1, 2]", executor.longestPath().toString());

        assertEquals("", outputStream.toString());

        isPrint(true);
        executor.defaultSolutionConsumer(List.of("2", "3"));
        assertTrue(outputStream.toString().contains("2: 2, 3"));
    }

    @Test
    void test_defaultSolutionConsumer_singleSolution() {
        singleSolution(true);

        executor.defaultSolutionConsumer(List.of("1"));

        assertEquals(1, executor.shortestPath().size());
        assertNull(executor.longestPath());
        assertEquals("", outputStream.toString());
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

        verify(board, times(ONE)).isSolved();
        verify(board, times(ONE)).path();
        assertEquals("[ABC]", executor.shortestPath().toString());
        assertEquals("[ABC]", executor.longestPath().toString());
        assertEquals(ZERO, executor.totalScenarios());
        assertEquals(ONE, executor.maxDepth());
        assertTrue(executor.stack().isEmpty());
    }

    @Test
    void test_solve_notCleared() {
        assertTrue(executor.stack().peek().add(board));
        when(board.isSolved()).thenReturn(false);

        executor.solve();

        assertTrue(executor.stack().isEmpty());
        verify(board, times(TWO)).isSolved();
        verify(board, never()).path();
        assertEquals(TWO, executor.totalScenarios());
        assertEquals(ONE, executor.maxDepth());
    }

    @Test
    void test_addBoard() {
        assertEquals(ONE, executor.stack().size());
        executor.addBoard(board);
        assertEquals(TWO, executor.stack().size());
        executor.addBoard(null);
    }

    @Test
    void test_addBoards() {
        executor.addBoards(List.of(board, board));
        assertEquals(TWO, executor.stack().size());
        assertEquals(TWO, executor.stack().peek().size());
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

    @Test
    void test_checkMaxDepth() {
        assertEquals(ZERO, executor.maxDepth());
        executor.checkMaxDepth();

        assertEquals(ONE, executor.maxDepth());
        Integer maxDepth = executor.maxDepth();

        executor.checkMaxDepth();
        assertSame(maxDepth, executor.maxDepth());

        executor.maxDepth(3);
        assertTrue(executor.stack().add(new BoardStack<>(board)));
        executor.checkMaxDepth();

        assertEquals(THREE, executor.maxDepth());
    }

    @Test
    void test_pathString() {
        assertEquals(ABC, executor.pathString(List.of(ABC)));
    }

    @Test
    void test_maxScore_exception() {
        assertThrows(RuntimeException.class, () -> executor.maxScore());
    }

    void mockSolutionConsumer(List<String> path) {
        assertTrue(nonNull(path));
    }

}