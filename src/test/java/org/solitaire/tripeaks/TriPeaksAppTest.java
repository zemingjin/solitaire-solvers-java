package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.io.IOHelperTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.CardHelper.useSuit;
import static org.solitaire.tripeaks.TriPeaksApp.PATH_MISSING;
import static org.solitaire.tripeaks.TriPeaksApp.checkArgs;
import static org.solitaire.tripeaks.TriPeaksBoard.getLimit;
import static org.solitaire.tripeaks.TriPeaksBoard.setLimit;

class TriPeaksAppTest {
    private static final int MOCK_LIMIT = 100;

    @BeforeEach
    public void setup() {
        useSuit = true;
        setLimit(Integer.MAX_VALUE);
    }

    @Test
    public void test_checkArgs() {
        checkArgs(new String[]{"-l", "100"});
        assertEquals(MOCK_LIMIT, getLimit());
        assertTrue(useSuit);

        checkArgs(new String[]{"-n"});
        assertFalse(useSuit);
    }

    @Test
    public void test_main() {
        TriPeaksApp.main(new String[]{IOHelperTest.TEST_FILE, "-l", "2", "-n"});
    }

    @Test
    public void test_main_exception() {
        var ex = assertThrows(RuntimeException.class, () -> TriPeaksApp.main(new String[]{}));

        assertNotNull(ex);
        assertEquals(PATH_MISSING, ex.getMessage());
    }
}