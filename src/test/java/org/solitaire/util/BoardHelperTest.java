package org.solitaire.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Column;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.freecell.FreeCellBoardTest.TEST_FILE;
import static org.solitaire.freecell.FreeCellHelper.build;
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