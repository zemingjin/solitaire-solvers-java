package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Columns;
import org.solitaire.util.CardHelper;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.freecell.FreeCellHelper.buildBoard;
import static org.solitaire.freecell.FreeCellHelperTest.cards;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.ZERO;

class FreeCellTest {
    private FreeCell dfs;
    private FreeCellHSD hsd;

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
        dfs = new FreeCell(buildBoard(cards));
        dfs.singleSolution(true);
        FreeCellHSD.add(true);
        hsd = new FreeCellHSD(buildBoard(cards));
    }

    @Test
    public void test_solveByHSD() {
        dfs.solveByHSD(dfs.stack().pop().peek());

        assertEquals(ZERO, dfs.totalScenarios());
        assertFalse(dfs.stack().isEmpty());
        assertEquals("[6F:Ad, 1F:2d, 4f:4c, 3f:9d, 3f:8s, 35:8h]", dfs.board().path().toString());
    }

    @Test
    public void test_solve_hsd_noclone() {
        hsd.cloner(a -> null);
        assertTrue(hsd.singleSolution());

        var result = hsd.solve();

        assertNotNull(result);
        assertEquals(ZERO, result.size());
        assertEquals(ONE, hsd.totalScenarios());
    }

    @Test
    public void test_solve_dfs() {
        var result = dfs.solve();

        assertNotNull(result);
        assertEquals(ONE, result.size());
        assertEquals(121, dfs.totalScenarios());
    }

    @Test
    public void test_getMaxScore() {
        assertThrows(RuntimeException.class, () -> dfs.getMaxScore(null));
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
            solveBoard(this::solveByHSD);
        }

        @Override
        public boolean addBoards(Collection<FreeCellBoard> boards) {
            return add && super.addBoards(boards);
        }

        public static void add(boolean add) {
            FreeCellHSD.add = add;
        }
    }
}