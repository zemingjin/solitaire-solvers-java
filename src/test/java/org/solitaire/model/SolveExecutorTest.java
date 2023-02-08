package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class SolveExecutorTest {
    private static final String ABC = "ABC";

    @Mock
    Board<String> board;

    private SolveExecutor<Board<String>> executor;

    @BeforeEach
    void setup() {
        executor = new SolveExecutor<>(board);
        executor.solveBoard(it -> executor.solutions().add(List.of(ABC)));
    }

    @Test
    void test_constructor() {
        assertNotNull(executor);
        assertEquals(ONE, executor.stack().size());
    }

    @Test
    void test_solve() {
        when(board.isCleared()).thenReturn(true);
        when(board.path()).thenReturn(List.of(ABC));

        assertEquals("[ABC]", executor.solve().get(0).toString());
        assertEquals(ZERO, executor.totalScenarios());
        assertEquals(ONE, executor.maxStack());
    }

    @Test
    void test_add() {
        assertTrue(executor.add(board));
        assertEquals(TWO, executor.stack().size());
    }

    @Test
    void test_addAll() {
        assertTrue(executor.addAll(List.of(board, board)));
        assertEquals(TWO, executor.stack().size());
        assertEquals(TWO, executor.stack().peek().size());
    }
}