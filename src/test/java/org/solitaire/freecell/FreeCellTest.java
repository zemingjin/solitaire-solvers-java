package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Columns;
import org.solitaire.util.CardHelper;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.solitaire.freecell.FreeCellHelper.buildBoard;
import static org.solitaire.freecell.FreeCellHelperTest.cards;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.ZERO;

class FreeCellTest {
    private FreeCell dfs;
    private MockFreeCell hsd;

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
        dfs = new FreeCell(buildBoard(cards));
        dfs.singleSolution(true);
        hsd = new MockFreeCell(buildBoard(cards));
    }

    @Test
    public void test_solve_hsd() {
        MockFreeCell.add(false);
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

    static class MockFreeCell extends FreeCell {
        private static boolean add = true;

        MockFreeCell(Columns columns) {
            super(columns);
            solveByHSD(true);
            solveBoard(this::solveByHSD);
        }

        @Override
        public boolean addBoards(Collection<FreeCellBoard> boards) {
            if (add) {
                return super.addBoards(boards);
            }
            return false;
        }

        public static void add(boolean add) {
            MockFreeCell.add = add;
        }
    }
}