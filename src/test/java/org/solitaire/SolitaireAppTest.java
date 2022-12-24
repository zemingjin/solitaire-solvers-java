package org.solitaire;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.io.IOHelperTest.TEST_FILE;

class SolitaireAppTest {
    private static final String[] ARGS = new String[]{TEST_FILE, "-n", "-t"};
    private SolitaireApp app;

    @BeforeEach
    public void setup() {
        app = new SolitaireApp();
    }

    @Test
    public void test_main() {
        SolitaireApp.main(ARGS);
    }

    @Test
    public void test_run() {
        var result = app.run(ARGS);

        assertNotNull(result);
        assertEquals(7983, result.size());
    }

}