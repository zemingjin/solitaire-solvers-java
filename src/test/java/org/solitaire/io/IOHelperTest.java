package org.solitaire.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.solitaire.util.IOHelper.loadFile;

public class IOHelperTest {
    public static final String TEST_FILE = "games/tripeaks/tripeaks-easy.txt";

    @Test
    void test_loadFile() {
        assertEquals(52, loadFile(TEST_FILE).length);
    }

    @Test
    void test_loadFile_exception() {
        var result = assertThrows(RuntimeException.class, () -> loadFile("abc"));

        assertNotNull(result);
        assertEquals("abc (The system cannot find the file specified)", result.getCause().getMessage());
    }

}