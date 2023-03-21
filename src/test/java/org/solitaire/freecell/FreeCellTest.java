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
import static org.solitaire.model.SolveExecutor.hsdDepth;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.util.CardHelper.useSuit;

class FreeCellTest {
    private static final String TEST_FILE = "games/freecell/freecell-easy-020623.txt";
    protected static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private FreeCell freeCell;
    private FreeCellHSD mockFreeCell;

    @BeforeEach
    void setup() {
        useSuit(false);
        isPrint(false);
        freeCell = new FreeCell(buildBoard(cards));
        hsdDepth(5);
        FreeCellHSD.add(true);
        singleSolution(true);
        mockFreeCell = new FreeCellHSD(buildBoard(cards));
    }

    @Test
    void test_solveByHSD() {
        freeCell.solveByHSD(freeCell.stack().pop().peek());

        assertEquals(4477, freeCell.totalScenarios());
        assertFalse(freeCell.stack().isEmpty());
        assertEquals("[4f:4c, 3f:9d, 3f:8s, 35:8h, 6$:Ad]", freeCell.board().path().toString());
    }

    @Test
    void test_solve_hsd_noclone() {
        assertTrue(singleSolution());

        mockFreeCell.solve();

        assertEquals(4477, mockFreeCell.totalScenarios());
    }

    @Test
    void test_isContinuing() {
        singleSolution(false);
        assertTrue(freeCell.isContinuing());

        freeCell.totalSolutions(SOLUTION_LIMIT - 1);
        assertTrue(freeCell.isContinuing());
        freeCell.totalSolutions(SOLUTION_LIMIT);
        assertFalse(freeCell.isContinuing());
    }

    @Test
    void test_solve_verify_dfs() {
        freeCell.board().column(0).pop();
        var result = assertThrows(RuntimeException.class, () -> freeCell.solve());

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
                add(false);
            }
        }
    }
}