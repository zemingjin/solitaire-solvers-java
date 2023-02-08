package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;

class SolveExecutorTest {
    private static final String ABC = "ABC";

    private SolveExecutor<String> executor;

    @BeforeEach
    void setup() {
        executor = new SolveExecutor<>(ABC);
        executor.stateConsumer(it -> executor.solutions().add(List.of(ABC)));
    }

    @Test
    void test_constructor() {
        assertNotNull(executor);
        assertEquals(ONE, executor.stack().size());
    }

    @Test
    void test_solve() {
        assertEquals("[ABC]", executor.solve().get(0).toString());
        assertEquals(ONE, executor.totalScenarios());
        assertEquals(ONE, executor.maxStack());
    }

    @Test
    void test_add() {
        assertTrue(executor.add(ABC));
        assertEquals(TWO, executor.stack().size());
    }

    @Test
    void test_addAll() {
        assertTrue(executor.addAll(List.of(ABC, ABC)));
        assertEquals(TWO, executor.stack().size());
        assertEquals(TWO, executor.stack().peek().size());
    }
}