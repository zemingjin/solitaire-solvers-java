package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Columns;
import org.solitaire.util.IOHelper;

import java.util.Collection;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.freecell.FreeCell.SOLUTION_LIMIT;
import static org.solitaire.freecell.FreeCellHelper.buildBoard;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.util.CardHelper.useSuit;

class FreeCellTest {
    private static final String TEST_FILE = "games/freecell/freecell-020623-easy.txt";
    protected static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private FreeCell dfs;
    private FreeCellHSD hsd;

    @BeforeEach
    void setup() {
        useSuit(false);
        singleSolution(false);
        isPrint(false);
        dfs = new FreeCell(buildBoard(cards));
        FreeCellHSD.add(true);
        singleSolution(true);
        hsd = new FreeCellHSD(buildBoard(cards));
    }

    @Test
    void test_solveByHSD() {
        dfs.solveByHSD(dfs.stack().pop().peek());

        assertEquals(29797, dfs.totalScenarios());
        assertFalse(dfs.stack().isEmpty());
        assertEquals("[6F:Ad, 1F:2d, 4f:4c, 3f:9d, 3f:8s, 35:8h]", dfs.board().path().toString());
    }

    @Test
    void test_solve_hsd_noclone() {
        assertTrue(singleSolution());

        hsd.solve();

        assertEquals(38723, hsd.totalScenarios());
    }

    @Test
    void test_solve_dfs() {
        singleSolution(false);
        dfs.solve();

        assertEquals(1910, dfs.totalScenarios());
        assertEquals(SOLUTION_LIMIT, dfs.totalSolutions());
        assertEquals(98, dfs.shortestPath().size());
        assertEquals(99, dfs.longestPath().size());
    }

    @Test
    void test_solve_verify_dfs() {
        dfs.board().columns().get(0).pop();
        var result = assertThrows(RuntimeException.class, () -> dfs.solve());

        assertEquals("[Missing card: 6c]", result.getMessage());
    }

    static class FreeCellHSD extends FreeCell {
        private static boolean add = true;

        FreeCellHSD(Columns columns) {
            super(columns);
            singleSolution(true);
        }

        static void add(boolean given) {
            FreeCellHSD.add = given;
        }

        @Override
        public Consumer<Collection<FreeCellBoard>> addBoards() {
            return this::addBoards;
        }

        public void addBoards(Collection<FreeCellBoard> boards) {
            if (add) {
                super.addBoards().accept(boards);
            }
        }
    }
}