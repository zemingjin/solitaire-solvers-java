package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FreeCellTest {
    private FreeCell freeCell;

    @BeforeEach
    public void setup() {
        freeCell = FreeCellHelper.build(FreeCellHelperTest.cards);
    }

    @Test
    public void test_solve() {
        var result = freeCell.solve();

        assertNull(result);
    }

    @Test
    public void test_solve_cleared() {
        freeCell.getBoard().forEach(List::clear);
        var result = freeCell.solve();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void test_getMaxScore() {
        assertThrows(RuntimeException.class, () -> freeCell.getMaxScore(null));
    }

}