package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.CardHelper.useSuit;
import static org.solitaire.tripeaks.TriPeaksApp.PATH_MISSING;
import static org.solitaire.tripeaks.TriPeaksApp.checkArgs;

class TriPeaksAppTest {
    @BeforeEach
    public void setup() {
        useSuit = true;
    }

    @Test
    public void test_checkArgs() {
        assertTrue(useSuit);

        checkArgs(new String[]{"abc", "-n"});
        assertFalse(useSuit);
    }

    @Test
    public void test_main() {
        TriPeaksApp.main(new String[]{TriPeaksBoardTest.TEST_FILE, "-l", "2", "-n"});
    }

    @Test
    public void test_main_exception() {
        var ex = assertThrows(RuntimeException.class, () -> TriPeaksApp.main(new String[]{}));

        assertNotNull(ex);
        assertEquals(PATH_MISSING, ex.getMessage());
    }
}