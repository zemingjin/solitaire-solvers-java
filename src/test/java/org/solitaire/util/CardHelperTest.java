package org.solitaire.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.solitaire.util.CardHelper.CLUB;
import static org.solitaire.util.CardHelper.DIAMOND;
import static org.solitaire.util.CardHelper.HEART;
import static org.solitaire.util.CardHelper.SPADE;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.checkDuplicates;
import static org.solitaire.util.CardHelper.diffOfValues;
import static org.solitaire.util.CardHelper.getSuit;

class CardHelperTest {
    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
    }

    @Test
    public void test_getSuit() {
        assertEquals(DIAMOND, getSuit("d"));
        assertEquals(SPADE, getSuit("s"));
        assertEquals(HEART, getSuit("h"));
        assertEquals(CLUB, getSuit("c"));
    }

    @Test
    public void test_toString() {
        assertEquals("Ah", CardHelper.stringOfRaws(buildCard(1, "Ah")));
        assertEquals("Ah:9h",
                CardHelper.stringOfRaws(new Card[]{buildCard(1, "Ah"), buildCard(2, "9h")}));
    }

    @Test
    public void test_diffOfValues() {
        var a = buildCard(0, "Ts");
        var b = buildCard(0, "9d");

        assertEquals(1, diffOfValues(a, b));
        assertEquals(10, diffOfValues(a, null));
    }

    @Test
    public void test_checkDuplicates() {
        var cards = new String[]{"9d", "9d"};

        assertThrows(RuntimeException.class, () -> checkDuplicates(cards));
    }

}