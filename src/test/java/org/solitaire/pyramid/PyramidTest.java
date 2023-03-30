package org.solitaire.pyramid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.solitaire.execution.SolveExecutor.isPrint;
import static org.solitaire.execution.SolveExecutor.singleSolution;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.util.CardHelper.useSuit;

class PyramidTest {
    protected static final String TEST_FILE = "games/pyramid/pyramid-expert-030523.txt";
    static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private static Pyramid pyramid;

    @BeforeEach
    void setup() {
        useSuit(false);
        isPrint(false);
        singleSolution(false);
        if (isNull(pyramid)) {
            pyramid = build(cards);
            assertThrows(NullPointerException.class, () -> pyramid.maxScore());
            pyramid.solve();
            assertNotNull(pyramid.maxScore());
        }
    }

    @Test
    void test_build() {
        assertEquals(2, pyramid.solutionConsumers().size());
    }

    @Test
    void test_solve() {
        assertEquals(7230, pyramid.totalScenarios());
        assertEquals(768, pyramid.totalSolutions());
        assertEquals(44, pyramid.shortestPath().size());
        assertEquals(46, pyramid.longestPath().size());
    }

    @Test
    void test_maxScore() {
        assertEquals(1290, pyramid.maxScore().getLeft());
        assertEquals(339, pyramid.pathString(pyramid.maxScore().getRight()).length());
    }
}