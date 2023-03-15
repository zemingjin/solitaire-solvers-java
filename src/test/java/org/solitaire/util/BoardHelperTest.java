package org.solitaire.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Column;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.freecell.FreeCellBoardTest.TEST_FILE;
import static org.solitaire.freecell.FreeCellHelper.build;
import static org.solitaire.util.BoardHelper.numberOfEachCard;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.useSuit;

class BoardHelperTest {
    private List<Column> columns;

    @BeforeEach
    void setup() {
        useSuit(false);
        columns = build(IOHelper.loadFile(TEST_FILE)).board().columns();
    }

    @Test
    void test_numberOfEachCard() {
        var maps = new int[4][14];

        maps[0][0] = 1;
        assertEquals(8, numberOfEachCard(maps));

        maps[1][0] = 1;
        assertEquals(4, numberOfEachCard(maps));

        maps[2][0] = 1;
        assertThrows(RuntimeException.class, () -> numberOfEachCard(maps));

        maps[3][0] = 1;
        assertEquals(1, numberOfEachCard(maps));
    }

    @Test
    void test_checkDuplicates() {
        var result = verifyBoard(columns);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        columns.get(0).add(card("Ad"));
        result = verifyBoard(columns);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Extra card: Ad", result.get(0));
    }

    @Test
    void test_checkMissing() {
        var result = verifyBoard(columns);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        columns.get(0).remove(2);
        result = verifyBoard(columns);
        assertEquals(1, result.size());
        assertEquals("Missing card: Tc", result.get(0));
    }

}