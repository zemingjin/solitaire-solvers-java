package org.solitaire.klondike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;
import org.solitaire.model.CardHelper;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.klondike.KlondikeHelper.toStack;
import static org.solitaire.klondike.KlondikeHelperTest.CARDS;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.model.Origin.COLUMN;

class KlondikeTest {
    private Klondike klondike;

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
        klondike = KlondikeHelper.build(CARDS);
    }

    @Test
    public void test_getMaxScore() {
        var result = klondike.getMaxScore(null);

        assertNull(result);
    }

//    @Test
//    public void test_solve() {
//        var result = klondike.solve();
//
//        assertNotNull(result);
//        assertEquals(0, result.size());
//    }

//    @Test
//    public void test_moveCards() {
//        var candidates = singletonList(klondike.findCandidateAtColumn(6).setTarget(2));
//        var result = klondike.moveCards(candidates);
//
//        assertNotNull(result);
//    }

    @Test
    public void test_updateBoard() {
        var candidate = klondike.findCandidateAtColumn(6).setTarget(2);

        assertEquals(7, klondike.getColumns().get(6).size());
        assertEquals(3, klondike.getColumns().get(2).size());

        klondike = klondike.updateBoard(candidate);

        assertEquals(6, klondike.getColumns().get(6).size());
        assertEquals(4, klondike.getColumns().get(2).size());
    }

    @Test
    public void test_drawCardsFromDeck() {
        assertEquals(24, klondike.getDeck().size());
        assertEquals(0, klondike.getDeckPile().size());
        assertEquals("21:Ts", klondike.getDeck().get(21).toString());

        klondike.drawCardsFromDeck();

        assertEquals(21, klondike.getDeck().size());
        assertEquals(3, klondike.getDeckPile().size());
        assertEquals("21:Ts", klondike.getDeckPile().peek().toString());

        klondike.getDeck().clear();
        assertTrue(klondike.isDeckCardsAvailable());

        klondike.drawCardsFromDeck();
        assertEquals(0, klondike.getDeck().size());
        assertEquals(3, klondike.getDeckPile().size());
        assertEquals("21:Ts", klondike.getDeckPile().peek().toString());

        klondike.getDeckPile().clear();

        assertFalse(klondike.isDeckCardsAvailable());
    }

    @Test
    public void test_checkColumnsForFoundation() {
        var column = klondike.getColumns().get(5);
        var foundation = klondike.getFoundations().get(0);

        column.set(5, buildCard(11, "Ad"));

        assertEquals(6, column.size());
        assertEquals(5, column.getOpenAt());
        assertTrue(foundation.isEmpty());
        assertEquals("11:Ad", column.peek().toString());

        klondike.checkColumnsForFoundation();

        assertEquals(5, column.size());
        assertEquals(4, column.getOpenAt());
        assertFalse(foundation.isEmpty());
        assertEquals("11:Ad", foundation.peek().toString());
        assertEquals("43:Qh", column.peek().toString());
        assertEquals(10, klondike.getTotalScore());
    }

    @Test
    public void test_appendToTarget() {
        var candidate = klondike.findCandidateAtColumn(6).setTarget(2);
        var column = klondike.getColumns().get(2);

        assertEquals(3, column.size());
        assertEquals("29:Jh", column.peek().toString());
        klondike.appendToTarget(candidate);

        assertEquals(4, column.size());
        assertEquals("51:Tc", column.peek().toString());
        assertEquals(5, klondike.getTotalScore());
    }

    @Test
    public void test_removeFromSource_column() {
        var column = klondike.getColumns().get(6);
        assertEquals(7, column.size());
        assertEquals(6, column.getOpenAt());

        var candidate = klondike.findCandidateAtColumn(6);

        klondike.removeFromSource(candidate);
        assertEquals(6, column.size());
        assertEquals(5, column.getOpenAt());
        assertEquals(0, klondike.getTotalScore());
    }

    @Test
    public void test_findTargets() {
        var candidates = klondike.findCandidates();
        var targets = klondike.findTargets(candidates);

        assertNotNull(targets);
        assertEquals(3, targets.size());
        assertEquals("Candidate(cards=[26:9s], origin=COLUMN, from=1, target=0)", targets.get(0).toString());
        assertEquals("Candidate(cards=[38:5c], origin=COLUMN, from=4, target=5)", targets.get(1).toString());
    }

    @Test
    public void test_findTarget() {
        var card = buildCard(34, "9h");
        var candidate = Candidate.builder().origin(COLUMN).from(0).cards(toStack(card)).build();

        var result = klondike.findTarget(candidate);

        assertNotNull(result);
        assertEquals(6, result.getTarget());

        klondike.getColumns().get(6).clear();
        assertNull(klondike.findTarget(candidate));

        card = buildCard(34, "Kh");
        candidate = Candidate.builder().origin(COLUMN).from(0).cards(toStack(card)).build();

        result = klondike.findTarget(candidate);
        assertNotNull(result);
        assertEquals(6, result.getTarget());
    }

    @Test
    public void test_findCandidates() {
        var result = klondike.findCandidates();

        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals("Candidate(cards=[24:Th], origin=COLUMN, from=0, target=0)", result.get(0).toString());

        klondike.drawCardsFromDeck();
        result = klondike.findCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertEquals("Candidate(cards=[24:Th], origin=COLUMN, from=0, target=0)", result.get(0).toString());
        assertEquals("Candidate(cards=[21:Ts], origin=DECKPILE, from=-1, target=0)", result.get(7).toString());
    }

    @Test
    public void test_findCandidate() {
        var result = klondike.findCandidateAtColumn(0);

        assertEquals(1, result.getCards().size());
        assertEquals(0, result.getFrom());
        assertEquals(COLUMN, result.getOrigin());
        assertEquals("24:Th", result.getCards().peek().toString());

        result = klondike.findCandidateAtColumn(6);
        assertEquals(1, result.getCards().size());
        assertEquals(6, result.getFrom());
        assertEquals(COLUMN, result.getOrigin());
        assertEquals("[51:Tc]", result.getCards().toString());
    }

    @Test
    public void test_getOrderedCards() {
        var result = klondike.getOrderedCards(klondike.getColumns().get(0));

        assertEquals(1, result.size());
        assertEquals("24:Th", result.peek().toString());

        result = klondike.getOrderedCards(klondike.getColumns().get(6));
        assertEquals(1, result.size());
        assertEquals("[51:Tc]", result.toString());
    }

    @Test
    public void test_isCleared() {
        assertFalse(klondike.isCleared());

        klondike.getDeck().clear();
        IntStream.range(0, 7)
                .mapToObj(i -> klondike.getColumns().get(i))
                .forEach(List::clear);

        assertTrue(klondike.isCleared());
    }
}