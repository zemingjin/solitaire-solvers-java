package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.solitaire.freecell.CardShuffle.MAXCOL;
import static org.solitaire.util.CardHelper.useSuit;

class CardShuffleTest {
    private CardShuffle cardShuffle;

    @BeforeEach
    void setup() {
        useSuit = false;
        cardShuffle = new CardShuffle();
    }

    @Test
    public void test_genBoard() {
        var card = cardShuffle.genBoard(1);

        assertNotNull(card);
        assertEquals(MAXCOL - 1, card.length);
        assertEquals(7, card[0].length);
        assertEquals(6, card[4].length);
        assertEquals("[4d, 6s, 9s, 3c, 4s, Qh, Ts]", Arrays.toString(card[0]));
    }

}