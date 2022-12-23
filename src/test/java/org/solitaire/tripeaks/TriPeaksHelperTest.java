package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.tripeaks.TriPeaksHelper.checkMaxScore;
import static org.solitaire.tripeaks.TriPeaksHelper.checkShortestPath;
import static org.solitaire.tripeaks.TriPeaksHelper.isFromDeck;
import static org.solitaire.tripeaks.TriPeaksHelper.toCards;

class TriPeaksHelperTest {
    private Card[] cards;

    @BeforeEach
    public void setup() {
        cards = new Card[]{buildCard(29, "Ac"), buildCard(2, "Ac")};
    }

    @Test
    public void test_toCards() {
        cards = toCards(new String[]{"Ad", "As"});

        assertEquals(2, cards.length);
        assertEquals("0:" + cards[0].getRaw(), cards[0].toString());
        assertEquals("1:" + cards[1].getRaw(), cards[1].toString());
    }

    @Test
    public void test_isFromDeck() {
        assertTrue(isFromDeck(cards[0]));
        assertFalse(isFromDeck(cards[1]));
    }

    @Test
    public void test_checks() {
        var solves = Collections.singletonList(Arrays.asList(cards));
        checkMaxScore(solves);
        checkShortestPath(solves);
    }

    @Test
    public void test_checks_null() {
        assertNotNull(assertThrows(NullPointerException.class, () -> checkMaxScore(null)));
        assertNotNull(assertThrows(NullPointerException.class, () -> checkShortestPath(null)));
    }
}