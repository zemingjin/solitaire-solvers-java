package org.solitaire.pyramid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.pyramid.PyramidHelper.LAST_BOARD;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.pyramid.PyramidTest.cards;
import static org.solitaire.util.CardHelper.stringOfRaws;

class PyramidStateTest {
    private PyramidState state;

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
        state = build(cards).initState();
    }

    @Test
    public void test_findCandidates() {
        var result = state.findCandidates();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("[Kc]", stringOfRaws(result.get(0)));
    }

    @Test
    public void test_updateState() {
        state.drawDeckCards();

        var card = state.flippedDeck().peek();
        var c = new Card[]{card};

        state.updateState(c);

        assertFalse(state.isOpen(card));
        assertTrue(state.flippedDeck.isEmpty());
        assertTrue(state.path().contains(c));
        assertEquals(1, state.path().size());
        assert state.path.peek() != null;
        assertEquals("51:Kc", state.path.peek()[0].toString());

        card = state.deck().peek();
        c = new Card[]{card, state.cards()[LAST_BOARD - 1]};
        state.updateState(c);

        assertFalse(state.isOpen(card));
        assertEquals(22, state.deck.size());
        assertEquals("49:6c", state.deck.peek().toString());
        assertTrue(state.path().contains(c));
        assertEquals(2, state.path().size());
        assert state.path.peek() != null;
        assertEquals("50:Kh", state.path.peek()[0].toString());

        state.drawDeckCards();
        card = state.deck().peek();
        state.updateState(new Card[]{card});

    }

    @Test
    public void test_recycle() {
        assertNotNull(state.drawDeckCards());
        assertEquals(23, state.deck.size());
        assertEquals(1, state.flippedDeck.size());

        while (!state.deck().isEmpty()) {
            state.flippedDeck().push(state.deck().pop());
        }
        state.recycleCount = 1;
        assertNull(state.drawDeckCards());
        assertEquals(24, state.flippedDeck().size());

        state.recycleCount = 2;
        assertNotNull(state.drawDeckCards());
        assertEquals(23, state.deck.size());
        assertEquals(1, state.flippedDeck.size());
        assertEquals("51:Kc", state.flippedDeck.peek().toString());
        assertEquals("50:Kh", state.deck.peek().toString());
    }

    @Test
    public void test_cloneBoard() {
        var cloned = new PyramidState(state);

        assertNotSame(state, cloned);
        assertTrue(reflectionEquals(state, cloned));
    }

    @Test
    public void test_findCardsAddingTo13() {
        var cards = state.findCandidates();

        assertNotNull(cards);
        assertEquals(3, cards.size());
    }

    @Test
    public void test_findOpenCards() {
        var cards = state.findOpenCards();

        assertNotNull(cards);
        assertEquals(8, cards.size());
    }

    @Test
    void test_isOpenDeckCard() {
        assertTrue(state.isOpenDeckCard(state.deck().peek()));
        assertFalse(state.isOpenDeckCard(state.deck().get(0)));
    }

    @Test
    void test_isOpenBoardCard() {
        assertTrue(state.isOpenBoardCard(27));
        assertTrue(state.isOpenBoardCard(22));
        assertFalse(state.isOpenBoardCard(20));
        assertFalse(state.isOpenBoardCard(28));
        assertFalse(state.isOpenBoardCard(-1));
    }

    @Test
    void test_isOpenAt() {
        assertTrue(state.isOpenAt(27));
        assertTrue(state.isOpenAt(21));
        assertFalse(state.isOpenAt(20));

        range(21, LAST_BOARD)
                .forEach(i -> state.cards()[i] = null);

        assertTrue(state.isOpenAt(20));
        assertTrue(state.isOpenAt(19));
        assertTrue(state.isOpenAt(18));
        assertTrue(state.isOpenAt(17));
        assertTrue(state.isOpenAt(16));
        assertTrue(state.isOpenAt(15));
        assertFalse(state.isOpenAt(14));

        range(19, 21)
                .forEach(i -> state.cards()[i] = null);

        assertTrue(state.isOpenAt(14));
    }

}