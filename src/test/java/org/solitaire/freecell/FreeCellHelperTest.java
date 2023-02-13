package org.solitaire.freecell;

import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.freecell.FreeCellHelper.LAST_BOARD;
import static org.solitaire.freecell.FreeCellHelper.LAST_LONG;
import static org.solitaire.freecell.FreeCellHelper.build;
import static org.solitaire.freecell.FreeCellHelper.getColumn;

class FreeCellHelperTest {
    private static final String TEST_FILE = "games/freecell/freecell-122622-medium.txt";
    protected static final String[] cards = IOHelper.loadFile(TEST_FILE);

    @Test
    void test_build() {
        var freeCell = build(cards);

        assertNotNull(freeCell);

        var state = (FreeCellBoard) freeCell.stack().peek().peek();

        assertNotNull(state);
        assertNotNull(state.columns());
        assertEquals(8, state.columns().size());
        assertEquals(7, state.columns().get(0).size());
        assertEquals(7, state.columns().get(3).size());
        assertEquals(6, state.columns().get(4).size());
        assertEquals(6, state.columns().get(7).size());
    }

    @Test
    void test_getColumn() {
        assertEquals(0, getColumn(0));
        assertEquals(3, getColumn(LAST_LONG - 1));
        assertEquals(4, getColumn(LAST_LONG));
        assertEquals(7, getColumn(LAST_BOARD - 1));
    }
}