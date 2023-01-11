package org.solitaire.pyramid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.pyramid.PyramidTest.cards;

class PyramidHelperTest {
    @Test
    public void test_build() {
        var result = build(cards);

        assertNotNull(result);
        assertEquals(28, result.initState().cards().length);
        assertEquals(24, result.initState().deck().size());
    }

    @Test
    void test_row() {
        assertEquals(7, PyramidHelper.row(27));
        assertEquals(6, PyramidHelper.row(19));
        assertEquals(5, PyramidHelper.row(12));
        assertEquals(4, PyramidHelper.row(7));
        assertEquals(3, PyramidHelper.row(3));
        assertEquals(2, PyramidHelper.row(1));
        assertEquals(1, PyramidHelper.row(0));
    }

}