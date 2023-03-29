package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Columns;
import org.solitaire.util.IOHelper;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.execution.SolveExecutor.hsdDepth;
import static org.solitaire.execution.SolveExecutor.isPrint;
import static org.solitaire.execution.SolveExecutor.singleSolution;
import static org.solitaire.freecell.FreeCellHelper.buildBoard;
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
        hsdDepth(6);
        FreeCellHSD.add(true);
        singleSolution(true);
        mockFreeCell = new FreeCellHSD(buildBoard(cards));
    }

    @Test
    void test_solveByHSD() {
        freeCell.solveByHSD(freeCell.stack().pop().peek());

        assertEquals(31192, freeCell.totalScenarios());
        assertFalse(freeCell.stack().isEmpty());
        assertEquals("[4f:4c, 3f:9d, 3f:8s, 35:8h, 6$:Ad, 1$:2d]", freeCell.board().path().toString());
    }

    @Test
    void test_solve_hsd_noclone() {
        assertTrue(singleSolution());

        mockFreeCell.solve();

        assertEquals(31192, mockFreeCell.totalScenarios());
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
        public void addBoards(Collection<FreeCellBoard> boards) {
            if (add) {
                super.addBoards(boards);
                add(false);
            }
        }
    }
}