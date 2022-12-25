package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.solitaire.model.CardHelper.CLUB;
import static org.solitaire.model.CardHelper.DIAMOND;
import static org.solitaire.model.CardHelper.HEART;
import static org.solitaire.model.CardHelper.SPADE;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.model.CardHelper.getSuit;

class CardHelperTest {
    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
    }

    @Test
    public void test_getSuit() {
        assertEquals(DIAMOND, getSuit('d'));
        assertEquals(SPADE, getSuit('s'));
        assertEquals(HEART, getSuit('h'));
        assertEquals(CLUB, getSuit('c'));
    }

    @Test
    public void test_toString() {
        assertEquals("Ah", CardHelper.toString(buildCard(1, "Ah")));
        assertEquals("Ah:9h",
                CardHelper.toString(new Card[]{buildCard(1, "Ah"), buildCard(2, "9h")}));
    }

}