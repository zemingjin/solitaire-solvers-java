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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.klondike.KlondikeHelper.build;
import static org.solitaire.klondike.KlondikeHelperTest.CARDS;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.buildCard;

class KlondikeStateTest {
    private KlondikeState state;

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
        state = build(CARDS).initState();
    }

    @Test
    public void test_optimizeCandidates_removeDuplicates() {
        var candidates = new LinkedList<Candidate>();
        candidates.add(buildCandidate(0, COLUMN, List.of(buildCard(0, "Ks")), 0));
        candidates.add(buildCandidate(0, COLUMN, List.of(buildCard(0, "Ks")), 1));

        var result = state.optimizeCandidates(candidates);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void test_findCandidates() {
        var result = state.findCandidates();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Candidate(cards=[26:9s], origin=COLUMN, from=1, target=0)", result.get(0).toString());
    }

    @Test
    public void test_drawDeck_stateChanged_false() {
        while (isNotEmpty(state.deck)) state.drawDeckCards();
        state.stateChanged(false);

        state.drawDeckCards();

        assertEquals(24, state.deckPile().size());
        assertTrue(state.deck().isEmpty());
    }

    @Test
    public void test_updateBoard() {
        var candidate = state.findCandidateAtColumn(6).setTarget(2);

        assertEquals(7, state.columns().get(6).size());
        assertEquals(3, state.columns().get(2).size());

        state = state.updateStates(candidate);

        assertEquals(6, state.columns().get(6).size());
        assertEquals(4, state.columns().get(2).size());
    }

    @Test
    public void test_drawCardsFromDeck() {
        assertEquals(24, state.deck().size());
        assertEquals(0, state.deckPile().size());
        assertEquals("21:7d", state.deck().get(21).toString());

        state.drawDeckCards();

        assertEquals(21, state.deck().size());
        assertEquals(3, state.deckPile().size());
        assertEquals("21:7d", state.deckPile().peek().toString());

        state.deck().clear();
        assertTrue(state.isDeckCardsAvailable());

        state.drawDeckCards();
        assertEquals(0, state.deck().size());
        assertEquals(3, state.deckPile().size());
        assertEquals("21:7d", state.deckPile().peek().toString());

        state.deckPile().clear();

        assertFalse(state.isDeckCardsAvailable());
    }

    @Test
    public void test_checkColumnsForFoundation() {
        var column = state.columns().get(5);
        var foundation = state.foundations().get(0);

        column.set(5, buildCard(11, "Ad"));

        assertEquals(6, column.size());
        assertEquals(5, column.getOpenAt());
        assertTrue(foundation.isEmpty());
        assertEquals("11:Ad", column.peek().toString());
    }

    @Test
    public void test_appendToTarget() {
        var candidate = state.findCandidateAtColumn(6).setTarget(2);
        var column = state.columns().get(2);

        assertEquals(3, column.size());
        assertEquals("29:Jh", column.peek().toString());
        state.moveToTarget(candidate);

        assertEquals(4, column.size());
        assertEquals("51:Tc", column.peek().toString());
        assertEquals(5, state.totalScore());
    }

    @Test
    public void test_removeFromSource_column() {
        var column = state.columns().get(6);
        assertEquals(7, column.size());
        assertEquals(6, column.getOpenAt());

        var candidate = state.findCandidateAtColumn(6);

        state.removeFromSource(candidate);
        assertEquals(6, column.size());
        assertEquals(5, column.getOpenAt());
        assertEquals(0, state.totalScore());

        state.drawDeckCards();
        assertFalse(state.deckPile().isEmpty());
        assertEquals(3, state.deckPile().size());
        candidate = buildCandidate(-1, DECKPILE, new LinkedList<>());

        state.removeFromSource(candidate);

        assertEquals(2, state.deckPile().size());
    }


    @Test
    public void test_findFoundationCandidates() {
        state.drawDeckCards();
        var results = state.findFoundationCandidates();

        assertNotNull(results);
        assertTrue(results.isEmpty());

        state.columns().get(0).add(buildCard(0, "Ad"));

        results = state.findFoundationCandidates();

        assertNotNull(results);
        assertEquals(1, results.size());

        state.moveToTarget(results.get(0));
        assertEquals(1, state.foundations().get(0).size());
        assertEquals("[0:Ad]", state.foundations().get(0).toString());
    }

    @Test
    public void test_findTargets() {
        var targets = state.findMovableCandidates();

        assertNotNull(targets);
        assertEquals(3, targets.size());
        assertEquals("Candidate(cards=[26:9s], origin=COLUMN, from=1, target=0)", targets.get(0).toString());
        assertEquals("Candidate(cards=[38:5c], origin=COLUMN, from=4, target=5)", targets.get(1).toString());
    }

    @Test
    public void test_findTarget() {
        var card = buildCard(34, "9h");
        var candidate = buildCandidate(0, COLUMN, List.of(card));

        var result = state.findTarget(candidate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(6, result.get(0).getTarget());

        state.columns().get(6).clear();

        result = state.findTarget(candidate);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findTarget_king() {
        var card = buildCard(34, "Kh");
        var candidate = buildCandidate(1, COLUMN, List.of(card));

        state.columns().get(1).add(card);
        state.columns().get(0).clear();

        var result = state.findTarget(candidate);

        assertNotNull(result);
        assertEquals("Candidate(cards=[34:Kh], origin=COLUMN, from=1, target=0)", result.get(0).toString());

        candidate = buildCandidate(-1, DECKPILE, List.of(card));
        state.deckPile().add(card);

        result = state.findTarget(candidate);

        assertNotNull(result);
        assertEquals("Candidate(cards=[34:Kh], origin=DECKPILE, from=-1, target=0)", result.get(0).toString());
    }

    @Test
    public void test_findOpenCandidates() {
        var result = state.findOpenCandidates();

        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals("Candidate(cards=[24:Th], origin=COLUMN, from=0, target=-1)", result.get(0).toString());

        state.drawDeckCards();
        result = state.findOpenCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertEquals("Candidate(cards=[24:Th], origin=COLUMN, from=0, target=-1)", result.get(0).toString());
        assertEquals("Candidate(cards=[21:7d], origin=DECKPILE, from=-1, target=-1)", result.get(7).toString());
    }

    @Test
    public void test_findCandidate() {
        var result = state.findCandidateAtColumn(0);

        assertEquals(1, result.getCards().size());
        assertEquals(0, result.getFrom());
        assertEquals(COLUMN, result.getOrigin());
        assertEquals("24:Th", result.getCards().get(0).toString());

        result = state.findCandidateAtColumn(6);
        assertEquals(1, result.getCards().size());
        assertEquals(6, result.getFrom());
        assertEquals(COLUMN, result.getOrigin());
        assertEquals("[51:Tc]", result.getCards().toString());
    }

    @Test
    public void test_getOrderedCards() {
        var result = state.getOrderedCards(state.columns().get(0));

        assertEquals(1, result.size());
        assertEquals("24:Th", result.get(0).toString());

        result = state.getOrderedCards(state.columns().get(6));
        assertEquals(1, result.size());
        assertEquals("[51:Tc]", result.toString());

        state.columns().get(5).add(state.columns().get(4).peek());
        result = state.getOrderedCards(state.columns().get(5));

        assertEquals(2, result.size());

        state.columns().get(0).add(state.columns().get(1).pop());
        state.columns().get(0).add(state.columns().get(1).pop());
        result = state.getOrderedCards(state.columns().get(0));

        assertEquals(3, result.size());

    }

    @Test
    public void test_isCleared() {
        assertFalse(state.isCleared());

        mockFullFoundations(state.foundations());

        assertTrue(state.isCleared());
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