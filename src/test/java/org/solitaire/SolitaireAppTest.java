package org.solitaire;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.GameSolver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.solitaire.SolitaireApp.PRINT;
import static org.solitaire.SolitaireApp.PYRAMID;
import static org.solitaire.SolitaireApp.SINGLE_SOLUTION;
import static org.solitaire.SolitaireApp.TRIPEAKS;
import static org.solitaire.SolitaireApp.USE_SUITS;
import static org.solitaire.SolitaireApp.app;
import static org.solitaire.SolitaireApp.checkPrint;
import static org.solitaire.SolitaireApp.checkSingleSolution;
import static org.solitaire.SolitaireApp.checkUseSuits;
import static org.solitaire.SolitaireApp.main;
import static org.solitaire.io.IOHelperTest.TEST_FILE;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.util.CardHelper.useSuit;

@ExtendWith(MockitoExtension.class)
class SolitaireAppTest {
    private final String[] ARGS = new String[]{TEST_FILE, TRIPEAKS, SINGLE_SOLUTION};

    private final SolitaireApp app = app();
    private ByteArrayOutputStream outputStream;
    @Mock GameSolver gameSolver;

    @BeforeEach
    void setup() {
        gameSolver = spy(gameSolver);
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        useSuit(false);
        isPrint(false);
    }

    @Test
    void test_main() {
        isPrint(true);
        main(ARGS);
        assertTrue(outputStream.toString().contains("One Path(47):"));
        assertTrue(outputStream.toString().contains(TEST_FILE));

        assertThrows(RuntimeException.class, () -> main(new String[]{}));
        assertFalse(isPrint());
    }

    @Test
    void test_check() {
        checkPrint(ARGS);
        assertFalse(isPrint());

        ARGS[1] = PRINT;
        checkPrint(ARGS);
        assertTrue(isPrint());
    }

    @Test
    void test_checkMaxScore_exception() {
        when(gameSolver.maxScore()).thenThrow(new RuntimeException("HHh"));

        app.checkMaxScore(gameSolver);
        assertEquals("HHh", outputStream.toString().trim());
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
        assertTrue(outputStream.toString().contains("Max Score"));
        assertTrue(outputStream.toString().contains("Longest Path"));
        assertEquals(14, app.solver().maxDepth());
    }

    @Test
    void test_run_pyramid() {
        ARGS[0] = "games/pyramid/pyramid-121122-expert.txt";
        ARGS[1] = PYRAMID;
        ARGS[2] = null;
        app.run(ARGS);


        assertNotNull(app.solver());
        assertEquals(1536, app.solver().totalSolutions());
        assertEquals(11, app.solver().maxDepth());
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