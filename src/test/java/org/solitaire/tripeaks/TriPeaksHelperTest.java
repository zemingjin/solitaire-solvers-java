package org.solitaire.tripeaks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;
import org.solitaire.util.IOHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.tripeaks.TriPeaksHelper.build;
import static org.solitaire.tripeaks.TriPeaksHelper.isFromDeck;
import static org.solitaire.tripeaks.TriPeaksHelper.toCards;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.toArray;

class TriPeaksHelperTest {
    private static final String TEST_FILE = "games/tripeaks/tripeaks-120822-expert.txt";
    private Card[] cards;

    @BeforeEach
    void setup() {
        cards = toArray(buildCard(29, "Ac"), buildCard(2, "Ac"));
    }

    @Test
    void test_build_checkDuplicates() {
        var data = IOHelper.loadFile(TEST_FILE);

        data[0] = data[data.length - 1];
        assertThrows(RuntimeException.class, () -> build(data));
    }

    @Test
    void test_toCards() {
        cards = toCards(new String[]{"Ad", "As"});

        assertEquals(2, cards.length);
        assertEquals("0:" + cards[0].raw(), cards[0].toString());
        assertEquals("1:" + cards[1].raw(), cards[1].toString());
    }

    @Test
    void test_isFromDeck() {
        assertTrue(isFromDeck(buildCard(28, "Ac")));
        assertTrue(isFromDeck(buildCard(29, "Ac")));
        assertFalse(isFromDeck(buildCard(2, "Ac")));
    }
}