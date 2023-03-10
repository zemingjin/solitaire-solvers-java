package org.solitaire;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.SolitaireApp.PYRAMID;
import static org.solitaire.SolitaireApp.SINGLE_SOLUTION;
import static org.solitaire.SolitaireApp.TRIPEAKS;
import static org.solitaire.SolitaireApp.USE_SUITS;
import static org.solitaire.SolitaireApp.checkSingleSolution;
import static org.solitaire.SolitaireApp.checkUseSuits;
import static org.solitaire.io.IOHelperTest.TEST_FILE;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.util.CardHelper.useSuit;

class SolitaireAppTest {
    private static final String[] ARGS = new String[]{TEST_FILE, USE_SUITS, TRIPEAKS};
    private SolitaireApp app;

    @BeforeEach
    void setup() {
        useSuit(false);
        app = new SolitaireApp();
    }

    @Test
    public void test_main() {
        SolitaireApp.main(ARGS);
    }

    @Test
    public void test_run() {
        var result = app.run(ARGS);

        assertNotNull(result);
        assertEquals(7983, result.size());
    }

    @Test
    public void test_run_pyramid() {
        ARGS[0] = "games/pyramid/pyramid-121122-expert.txt";
        ARGS[2] = PYRAMID;
        var result = app.run(ARGS);


        assertNotNull(result);
        assertEquals(512, result.size());
    }

    @Test
    void getSolverType() {
        assertEquals(TRIPEAKS, app.getSolverType(new String[]{TEST_FILE, TRIPEAKS}));

        var ex = assertThrows(RuntimeException.class, () -> app.getSolverType(new String[]{TEST_FILE}));

        assertNotNull(ex);
        assertEquals("Missing solver type; '-t', '-p', '-k', '-f', or '-s'", ex.getMessage());
    }

    @Test
    void test_checkUseSuits() {
        checkUseSuits(ARGS);
        assertTrue(useSuit());

        checkUseSuits(new String[]{TEST_FILE, TRIPEAKS});
        assertFalse(useSuit());
    }

    @Test
    void test_checkSingleSolution() {
        checkSingleSolution(ARGS);
        assertFalse(singleSolution());

        checkSingleSolution(new String[]{TRIPEAKS, SINGLE_SOLUTION});
        assertTrue(singleSolution());
    }


}