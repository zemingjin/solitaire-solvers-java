package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.build;
import static org.solitaire.tripeaks.TriPeaksTest.cards;
import static org.solitaire.util.CardHelper.buildCard;

class TriPeaksStateTest {
    private TriPeaksState state;

    @BeforeEach
    void setup() {
        CardHelper.useSuit = false;
        state = build(cards).initState();
    }

    @Test
    public void test_findAdjacentCards() {
        var openCards = state.findCandidates();

        assertNotNull(openCards);
        assertFalse(openCards.isEmpty());
        assertEquals(2, openCards.size());
        assertTrue(openCards.get(0).toString().startsWith("25:T"));
        assertTrue(openCards.get(1).toString().startsWith("22:Q"));
    }

    @Test
    public void test_isOpenCard() {
        var state = new TriPeaksState(new Card[]{null, null, null, null, null}, null);

        assertTrue(state.isOpenCard(buildCard(0, "Ad")));
    }

    @Test
    public void test_isOpenCard_exception() {
        var state = new TriPeaksState(null, null);
        var card = buildCard(LAST_BOARD, "Ad");

        var ex = assertThrows(RuntimeException.class, () -> state.isOpenCard(card));

        assertNotNull(ex);
        assertEquals("Invalid card: " + card, ex.getMessage());
    }


}