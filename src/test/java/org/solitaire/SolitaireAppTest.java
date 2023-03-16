package org.solitaire;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.freecell.FreeCell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.SolitaireApp.FREECELL;
import static org.solitaire.SolitaireApp.PYRAMID;
import static org.solitaire.SolitaireApp.SINGLE_SOLUTION;
import static org.solitaire.SolitaireApp.TRIPEAKS;
import static org.solitaire.SolitaireApp.USE_SUITS;
import static org.solitaire.SolitaireApp.app;
import static org.solitaire.SolitaireApp.checkSingleSolution;
import static org.solitaire.SolitaireApp.checkUseSuits;
import static org.solitaire.SolitaireApp.main;
import static org.solitaire.io.IOHelperTest.TEST_FILE;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.CardHelperTest.ZERO;

class SolitaireAppTest {
    private final String[] ARGS = new String[]{TEST_FILE, TRIPEAKS, SINGLE_SOLUTION};

    private final SolitaireApp app = app();
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setup() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        useSuit(false);
        isPrint(false);
    }

    @Test
    void test_main() {
        assertFalse(isPrint());
        isPrint(true);
        main(ARGS);
        assertEquals(565, outputStream.toString().length());

        assertThrows(RuntimeException.class, () -> main(new String[]{}));
    }

    @Test
    void test_run_emptyArgs() {
        assertThrows(NoSuchElementException.class, () -> app.run(new String[]{}));
    }

    @Test
    void test_run() {
        ARGS[2] = USE_SUITS;
        app.run(ARGS);

        assertTrue(app.stopWatch().isStopped());
        assertTrue(useSuit());
        assertFalse(singleSolution());
        assertNotNull(app.solver());
        assertEquals(7983, app.solver().totalSolutions());
        assertEquals(557, outputStream.toString().length());
        assertTrue(outputStream.toString().contains("Max Score"));
    }

    @Test
    void test_run_pyramid() {
        ARGS[0] = "games/pyramid/pyramid-121122-expert.txt";
        ARGS[1] = PYRAMID;
        ARGS[2] = null;
        app.run(ARGS);


        assertNotNull(app.solver());
        assertEquals(512, app.solver().totalSolutions());
    }

    @Test
    void test_run_freecell() {
        ARGS[0] = "games/freecell/freecell-020623-easy.txt";
        ARGS[1] = FREECELL;
        ARGS[2] = null;

        app.run(ARGS);
        assertTrue(outputStream.toString().contains("Maximum score is not supported!"));
        assertEquals(FreeCell.SOLUTION_LIMIT, app.solver().totalSolutions());
    }

    @Test
    void test_run_freecell_nosolutions() {
        ARGS[0] = "games/freecell/freecell-022723-hard.txt";
        ARGS[1] = FREECELL;
        ARGS[2] = SINGLE_SOLUTION;

        app.run(ARGS);
        assertEquals(ZERO, app.solver().totalSolutions());
        assertFalse(outputStream.toString().contains("not supported"));
        assertFalse(outputStream.toString().contains("Max Score"));
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

        checkUseSuits(new String[]{TEST_FILE, TRIPEAKS});
        assertFalse(useSuit());
    }

    @Test
    void test_checkSingleSolution() {
        checkSingleSolution(ARGS);
        assertTrue(singleSolution());

        checkSingleSolution(new String[]{TRIPEAKS, SINGLE_SOLUTION});
        assertTrue(singleSolution());
    }

}