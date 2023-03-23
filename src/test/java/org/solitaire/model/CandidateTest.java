package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.candidate;
import static org.solitaire.model.Candidate.columnToColumn;
import static org.solitaire.model.Origin.BOARD;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.Origin.FREECELL;
import static org.solitaire.model.Origin.REMOVE;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelper.useSuit;

class CandidateTest {
    @BeforeEach
    void setup() {
        useSuit(false);
    }

    @Test
    void test_isToDeck() {
        var candidate = candidate(card("Ad"), DECKPILE, -1, DECKPILE, -1);

        assertTrue(candidate.isToDeck());
        assertFalse(candidate.isNotToDeck());

        candidate = candidate(card("Ad"), DECKPILE, -1, COLUMN, 0);

        assertFalse(candidate.isToDeck());
        assertTrue(candidate.isNotToDeck());
    }

    @Test
    void test_notation() {
        var cards = toArray(card("Ks"));

        assertEquals("01:Ks", new Candidate(cards, COLUMN, 0, COLUMN, 1).notation());
        assertEquals("0f:Ks", new Candidate(cards, COLUMN, 0, FREECELL, -1).notation());
        assertEquals("0$:Ks", new Candidate(cards, COLUMN, 0, FOUNDATION, -1).notation());
        assertEquals("f$:Ks", new Candidate(cards, FREECELL, -1, FOUNDATION, -1).notation());
        assertEquals("f1:Ks", new Candidate(cards, FREECELL, -1, COLUMN, 1).notation());
        assertEquals("^1:Ks", new Candidate(cards, DECKPILE, -1, COLUMN, 1).notation());
        assertEquals("^^:Ks", new Candidate(cards, DECKPILE, -1, DECKPILE, -1).notation());
        assertEquals("bb:Ks", new Candidate(cards, BOARD, 1, BOARD, 0).notation());
        assertEquals("br:Ks", new Candidate(cards, BOARD, -1, REMOVE, -1).notation());
        assertEquals("01:[Ks, Qs]",
                new Candidate(toArray(card("Ks"), card("Qs")), COLUMN, 0, COLUMN, 1).notation());

        assertThrows(RuntimeException.class,
                () -> new Candidate(cards, REMOVE, -1, COLUMN, 1).notation());
    }

    @Test
    void test_isKing() {
        assertTrue(columnToColumn(card("Ks"), 1, 0).isKing());
        assertFalse(columnToColumn(card("Ad"), 0, 1).isKing());
    }

    @Test
    void test_isFrom() {
        var candidate = columnToColumn(card("Ks"), 0, 1);

        assertTrue(candidate.isToColumn());

        candidate = candidate(card("Ks"), FREECELL, 0, FREECELL, 0);
        assertFalse(candidate.isToFoundation());

        candidate = candidate(card("Ks"), FREECELL, 0, FOUNDATION, 0);
        assertTrue(candidate.isToFoundation());
        assertFalse(candidate.isToColumn());
    }

    @Test
    void test_toString() {
        assertEquals("Candidate(Ks, COLUMN, 0, COLUMN, 1)",
                columnToColumn(card("Ks"), 0, 1).toString());

    }

}