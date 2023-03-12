package org.solitaire.pyramid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.util.CardHelper.useSuit;

class PyramidTest {
    protected static final String TEST_FILE = "games/pyramid/pyramid-121122-expert.txt";
    static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private Pyramid pyramid;

    @BeforeEach
    void setup() {
        useSuit(false);
        isPrint(false);
        singleSolution(false);
        pyramid = build(cards);
    }

    @Test
    void test_solve() {
        pyramid.solve();

        assertEquals(5467, pyramid.totalScenarios());
        assertEquals(512, pyramid.totalSolutions());
        assertEquals(28, pyramid.shortestPath().size());
        assertEquals(28, pyramid.longestPath().size());
    }
}