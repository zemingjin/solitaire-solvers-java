package org.solitaire.klondike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.klondike.KlondikeHelperTest.CARDS;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.buildCard;

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
//        var candidates = List.of(klondike.findCandidateAtColumn(6).setTarget(2));
//        var result = klondike.moveCards(candidates);
//
//        assertNotNull(result);
//    }

    @Test
    public void solve_cleared() {
        mockFullFoundations(klondike.getFoundations());

        var result = klondike.solve();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isEmpty());
    }

    @Test
    public void test_optimizeCandidates_removeDuplicates() {
        var candidates = new LinkedList<Candidate>();
        candidates.add(buildCandidate(0, COLUMN, List.of(buildCard(0, "Ks")), 0));
        candidates.add(buildCandidate(0, COLUMN, List.of(buildCard(0, "Ks")), 1));

        var result = klondike.optimizeCandidates(candidates);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void test_findCandidates() {
        var result = klondike.findCandidates();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Candidate(cards=[26:9s], origin=COLUMN, from=1, target=0)", result.get(0).toString());
    }

    @Test
    public void test_drawDeck_stateChanged_false() {
        while (isNotEmpty(klondike.deck)) klondike.drawDeckCards();
        klondike.setStateChanged(false);

        klondike.drawDeck();

        assertEquals(24, klondike.getDeckPile().size());
        assertTrue(klondike.getDeck().isEmpty());
    }

    @Test
    public void test_findFoundationCandidates() {
        klondike.drawDeckCards();
        var results = klondike.findFoundationCandidates();

        assertNotNull(results);
        assertTrue(results.isEmpty());

        klondike.getColumns().get(0).add(buildCard(0, "Ad"));

        results = klondike.findFoundationCandidates();

        assertNotNull(results);
        assertEquals(1, results.size());

        klondike.moveToTarget(results.get(0));
        assertEquals(1, klondike.getFoundations().get(0).size());
        assertEquals("[0:Ad]", klondike.getFoundations().get(0).toString());
    }

    @Test
    public void test_updateBoard() {
        var candidate = klondike.findCandidateAtColumn(6).setTarget(2);

        assertEquals(7, klondike.getColumns().get(6).size());
        assertEquals(3, klondike.getColumns().get(2).size());

        klondike = klondike.updateStates(candidate);

        assertEquals(6, klondike.getColumns().get(6).size());
        assertEquals(4, klondike.getColumns().get(2).size());
    }

    @Test
    public void test_drawCardsFromDeck() {
        assertEquals(24, klondike.getDeck().size());
        assertEquals(0, klondike.getDeckPile().size());
        assertEquals("21:7d", klondike.getDeck().get(21).toString());

        klondike.drawDeckCards();

        assertEquals(21, klondike.getDeck().size());
        assertEquals(3, klondike.getDeckPile().size());
        assertEquals("21:7d", klondike.getDeckPile().peek().toString());

        klondike.getDeck().clear();
        assertTrue(klondike.isDeckCardsAvailable());

        klondike.drawDeckCards();
        assertEquals(0, klondike.getDeck().size());
        assertEquals(3, klondike.getDeckPile().size());
        assertEquals("21:7d", klondike.getDeckPile().peek().toString());

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
    }

    @Test
    public void test_appendToTarget() {
        var candidate = klondike.findCandidateAtColumn(6).setTarget(2);
        var column = klondike.getColumns().get(2);

        assertEquals(3, column.size());
        assertEquals("29:Jh", column.peek().toString());
        klondike.moveToTarget(candidate);

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

        klondike.drawDeckCards();
        assertFalse(klondike.getDeckPile().isEmpty());
        assertEquals(3, klondike.getDeckPile().size());
        candidate = buildCandidate(-1, DECKPILE, new LinkedList<>());

        klondike.removeFromSource(candidate);

        assertEquals(2, klondike.getDeckPile().size());
    }

    @Test
    public void test_findTargets() {
        var targets = klondike.findMovableCandidates();

        assertNotNull(targets);
        assertEquals(3, targets.size());
        assertEquals("Candidate(cards=[26:9s], origin=COLUMN, from=1, target=0)", targets.get(0).toString());
        assertEquals("Candidate(cards=[38:5c], origin=COLUMN, from=4, target=5)", targets.get(1).toString());
    }

    @Test
    public void test_findTarget() {
        var card = buildCard(34, "9h");
        var candidate = buildCandidate(0, COLUMN, List.of(card));

        var result = klondike.findTarget(candidate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(6, result.get(0).getTarget());

        klondike.getColumns().get(6).clear();

        result = klondike.findTarget(candidate);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findTarget_king() {
        var card = buildCard(34, "Kh");
        var candidate = buildCandidate(1, COLUMN, List.of(card));

        klondike.getColumns().get(1).add(card);
        klondike.getColumns().get(0).clear();

        var result = klondike.findTarget(candidate);

        assertNotNull(result);
        assertEquals("Candidate(cards=[34:Kh], origin=COLUMN, from=1, target=0)", result.get(0).toString());

        candidate = buildCandidate(-1, DECKPILE, List.of(card));
        klondike.getDeckPile().add(card);

        result = klondike.findTarget(candidate);

        assertNotNull(result);
        assertEquals("Candidate(cards=[34:Kh], origin=DECKPILE, from=-1, target=0)", result.get(0).toString());
    }

    @Test
    public void test_findOpenCandidates() {
        var result = klondike.findOpenCandidates();

        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals("Candidate(cards=[24:Th], origin=COLUMN, from=0, target=-1)", result.get(0).toString());

        klondike.drawDeckCards();
        result = klondike.findOpenCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertEquals("Candidate(cards=[24:Th], origin=COLUMN, from=0, target=-1)", result.get(0).toString());
        assertEquals("Candidate(cards=[21:7d], origin=DECKPILE, from=-1, target=-1)", result.get(7).toString());
    }

    @Test
    public void test_findCandidate() {
        var result = klondike.findCandidateAtColumn(0);

        assertEquals(1, result.getCards().size());
        assertEquals(0, result.getFrom());
        assertEquals(COLUMN, result.getOrigin());
        assertEquals("24:Th", result.getCards().get(0).toString());

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
        assertEquals("24:Th", result.get(0).toString());

        result = klondike.getOrderedCards(klondike.getColumns().get(6));
        assertEquals(1, result.size());
        assertEquals("[51:Tc]", result.toString());

        klondike.getColumns().get(5).add(klondike.getColumns().get(4).peek());
        result = klondike.getOrderedCards(klondike.getColumns().get(5));

        assertEquals(2, result.size());

        klondike.getColumns().get(0).add(klondike.getColumns().get(1).pop());
        klondike.getColumns().get(0).add(klondike.getColumns().get(1).pop());
        result = klondike.getOrderedCards(klondike.getColumns().get(0));

        assertEquals(3, result.size());

    }

    @Test
    public void test_isCleared() {
        assertFalse(klondike.isCleared());

        mockFullFoundations(klondike.getFoundations());

        assertTrue(klondike.isCleared());
    }

    private void mockFullFoundations(List<Stack<Card>> foundations) {
        IntStream.range(0, 4)
                .forEach(i -> IntStream.range(0, 13)
                        .forEach(j -> foundations.get(i).add(buildCard(0, VALUES.charAt(i) + toSuit(i)))));
    }

    private String toSuit(int i) {
        return switch (i) {
            case 0 -> "d";
            case 1 -> "h";
            case 2 -> "c";
            default -> "s";
        };
    }
}