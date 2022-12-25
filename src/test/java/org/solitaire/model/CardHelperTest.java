package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.model.CardHelper.getSuit;

class CardHelperTest {
    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
    }

    @Test
    public void test_getSuit() {
        assertEquals("♦", getSuit('d'));
        assertEquals("♠", getSuit('s'));
        assertEquals("♥", getSuit('h'));
        assertEquals("♣", getSuit('c'));
    }

    @Test
    public void test_toString() {
        assertEquals("Ah", CardHelper.toString(buildCard(1, "Ah")));
        assertEquals("[Ah:9h]",
                CardHelper.toString(new Card[]{buildCard(1, "Ah"), buildCard(2, "9h")}));
    }

}