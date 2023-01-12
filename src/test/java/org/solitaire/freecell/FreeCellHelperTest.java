package org.solitaire.freecell;

import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.freecell.FreeCellHelper.build;

class FreeCellHelperTest {
    private static final String TEST_FILE = "games/freecell/freecell-122622-medium.txt";
    protected static final String[] cards = IOHelper.loadFile(TEST_FILE);

    @Test
    public void test_build() {
        var freeCell = build(cards);
        var state = freeCell.initState();

        assertNotNull(freeCell);
        assertEquals(8, state.columns().size());
        assertEquals(7, state.columns().get(0).size());
        assertEquals(7, state.columns().get(3).size());
        assertEquals(6, state.columns().get(4).size());
        assertEquals(6, state.columns().get(7).size());
    }

}