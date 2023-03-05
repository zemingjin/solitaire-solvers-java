package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Column;
import org.solitaire.util.IOHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.freecell.FreeCellHelper.LAST_BOARD;
import static org.solitaire.freecell.FreeCellHelper.LAST_LONG;
import static org.solitaire.freecell.FreeCellHelper.build;
import static org.solitaire.freecell.FreeCellHelper.getColumn;
import static org.solitaire.util.CardHelper.useSuit;

class FreeCellHelperTest {
    private static final String TEST_FILE = "games/freecell/freecell-020623-easy.txt";
    protected static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private List<Column> columns;

    @BeforeEach
    void setup() {
        useSuit = false;
        var freeCell = build(cards);

        assertNotNull(freeCell);
        var board = freeCell.board();

        columns = board.columns();
        assertNotNull(board);
        assertNotNull(board.columns());
    }

    @Test
    void test_build() {
        assertEquals(8, columns.size());
        assertEquals(7, columns.get(0).size());
        assertEquals(7, columns.get(3).size());
        assertEquals(6, columns.get(4).size());
        assertEquals(6, columns.get(7).size());
    }

    @Test
    void test_getColumn() {
        assertEquals(0, getColumn(0));
        assertEquals(3, getColumn(LAST_LONG - 1));
        assertEquals(4, getColumn(LAST_LONG));
        assertEquals(7, getColumn(LAST_BOARD - 1));
    }

}