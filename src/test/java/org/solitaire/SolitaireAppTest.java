package org.solitaire;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.SolitaireApp.NOSUITS;
import static org.solitaire.SolitaireApp.PYRAMID;
import static org.solitaire.SolitaireApp.TRIPEAKS;
import static org.solitaire.io.IOHelperTest.TEST_FILE;

class SolitaireAppTest {
    private static final String[] ARGS = new String[]{TEST_FILE, NOSUITS, TRIPEAKS};
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

    @Test
    public void test_run_pyramid() {
        ARGS[0] = "src/test/resources/pyramid-121122-expert.txt";
        ARGS[2] = PYRAMID;
        var result = app.run(ARGS);


        assertNotNull(result);
        assertEquals(512, result.size());
    }

}