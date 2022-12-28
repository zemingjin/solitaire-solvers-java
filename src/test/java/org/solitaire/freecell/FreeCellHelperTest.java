package org.solitaire.freecell;

import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.freecell.FreeCellHelper.build;
import static org.solitaire.freecell.FreeCellHelper.cloneGame;

class FreeCellHelperTest {
    private static final String TEST_FILE = "games/freecell/freecell-122622-medium.txt";
    protected static final String[] cards = IOHelper.loadFile(TEST_FILE);

    @Test
    public void test_build() {
        var freeCell = build(cards);

        assertNotNull(freeCell);
        assertEquals(8, freeCell.getBoard().size());
        assertEquals(7, freeCell.getBoard().get(0).size());
        assertEquals(7, freeCell.getBoard().get(3).size());
        assertEquals(6, freeCell.getBoard().get(4).size());
        assertEquals(6, freeCell.getBoard().get(7).size());
    }

    @Test
    public void test_cloneGame() {
        var game = build(cards);
        var clone = cloneGame(game);

        assertEquals(game, clone);
    }

}