package org.solitaire.klondike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.klondike.KlondikeHelper.build;
import static org.solitaire.klondike.KlondikeHelperTest.CARDS;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.suitCode;
import static org.solitaire.util.CardHelper.useSuit;

class KlondikeBoardTest {
    private KlondikeBoard board;

    @BeforeEach
    public void setup() {
        useSuit(false);
        board = build(CARDS).board();
    }

    @Test
    public void test_optimizeCandidates_removeDuplicates() {
        var candidates = new LinkedList<Candidate>();
        candidates.add(buildCandidate(0, COLUMN, List.of(card("Ks")), 0));
        candidates.add(buildCandidate(0, COLUMN, List.of(card("Ks")), 1));

        var result = board.optimizeCandidates(candidates);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void test_findCandidates() {
        var result = board.findCandidates();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Candidate[cards=[26:9s], origin=COLUMN, from=1, target=COLUMN, to=0]", result.get(0).toString());
    }

    @Test
    public void test_drawDeck_stateChanged_false() {
        while (isNotEmpty(board.deck)) board.drawDeckCards();
        board.stateChanged(false);

        board.drawDeckCards();

        assertEquals(24, board.deckPile().size());
        assertTrue(board.deck().isEmpty());
    }

    @Test
    public void test_updateStates() {
        var candidate = Candidate.buildColumnCandidate(board.findCandidateAtColumn(6), 2);

        assertEquals(7, board.columns().get(6).size());
        assertEquals(3, board.columns().get(2).size());
        board.stateChanged = false;

        board = board.updateBoard(candidate);

        assertEquals(6, board.columns().get(6).size());
        assertEquals(4, board.columns().get(2).size());
        assertTrue(board.stateChanged);
    }

    @Test
    public void test_isScorable() {
        var card = board.columns().get(1).peek();

        assertTrue(board.isScorable(buildCandidate(1, COLUMN, card)));
        assertTrue(board.isScorable(buildCandidate(-1, DECKPILE, card)));

        board.columns().get(1).clear();
        assertFalse(board.isScorable(buildCandidate(1, COLUMN, card)));
    }

    @Test
    public void test_moveToFoundation() {
        var card = card("Ad");
        var foundation = board.foundations().get(suitCode(card));
        board.moveToFoundation(buildCandidate(0, COLUMN, card));
        assertTrue(foundation.contains(card));
        assertEquals(15, board.totalScore());

        card = card("2d");
        board.moveToFoundation(buildCandidate(0, DECKPILE, card));
        assertEquals(2, foundation.size());
        assertEquals(25, board.totalScore());

        card = card("3d");
        board.moveToFoundation(buildCandidate(0, COLUMN, card));
        assertEquals(3, foundation.size());
        assertEquals(30, board.totalScore());
    }

    @Test
    public void test_drawDeckCards() {
        assertEquals(24, board.deck().size());
        assertEquals(0, board.deckPile().size());
        assertEquals("21:7d", board.deck().get(21).toString());

        assertNotNull(board.drawDeckCards());

        assertEquals(21, board.deck().size());
        assertEquals(3, board.deckPile().size());
        assertEquals("21:7d", board.deckPile().peek().toString());

        var card = board.deck().peek();
        board.deck().clear();
        board.deck().add(card);
        assertTrue(board.isDeckCardsAvailable());

        assertNotNull(board.drawDeckCards());
        assertEquals(0, board.deck().size());
        assertEquals(4, board.deckPile().size());
        assertEquals(card.toString(), board.deckPile().peek().toString());

        board.stateChanged(true);
        board.deckPile().pop();
        assertNotNull(board.drawDeckCards());
        assertEquals(0, board.deck().size());
        assertEquals(3, board.deckPile().size());
        assertEquals("21:7d", board.deckPile().peek().toString());
        assertFalse(board.stateChanged);

        board.deckPile().clear();
        board.stateChanged(true);

        assertNull(board.drawDeckCards());
        assertFalse(board.isDeckCardsAvailable());
        assertFalse(board.stateChanged);
    }

    @Test
    public void test_checkColumnsForFoundation() {
        var column = board.columns().get(5);
        var foundation = board.foundations().get(0);

        column.set(5, buildCard(11, "Ad"));

        assertEquals(6, column.size());
        assertEquals(5, column.getOpenAt());
        assertTrue(foundation.isEmpty());
        assertEquals("11:Ad", column.peek().toString());
    }

    @Test
    public void test_appendToTarget() {
        var candidate = Candidate.buildColumnCandidate(board.findCandidateAtColumn(6), 2);
        var column = board.columns().get(2);

        assertEquals(3, column.size());
        assertEquals("29:Jh", column.peek().toString());
        board.moveToTarget(candidate);

        assertEquals(4, column.size());
        assertEquals("51:Tc", column.peek().toString());
        assertEquals(5, board.totalScore());
    }

    @Test
    public void test_removeFromSource_column() {
        var column = board.columns().get(6);
        assertEquals(7, column.size());
        assertEquals(6, column.getOpenAt());

        var candidate = board.findCandidateAtColumn(6);

        board.removeFromSource(candidate);
        assertEquals(6, column.size());
        assertEquals(5, column.getOpenAt());
        assertEquals(0, board.totalScore());

        board.drawDeckCards();
        assertFalse(board.deckPile().isEmpty());
        assertEquals(3, board.deckPile().size());
        candidate = buildCandidate(-1, DECKPILE, new LinkedList<>());

        board.removeFromSource(candidate);

        assertEquals(2, board.deckPile().size());
    }


    @Test
    void test_removeFromSource_foundation() {
        var card = card("Ad");
        var suitCode = suitCode(card);

        assertTrue(board.foundations().get(suitCode).isEmpty());
        board.foundations().get(suitCode).add(card);
        assertEquals(card.toString(), board.foundations().get(suitCode).peek().toString());

        var result = board.removeFromSource(buildCandidate(suitCode, FOUNDATION, card));

        assertTrue(board.foundations().get(suitCode).isEmpty());
        assertSame(board, result);
    }

    @Test
    public void test_findFoundationCandidates() {
        board.drawDeckCards();
        var results = board.findFoundationCandidates();

        assertNotNull(results);
        assertTrue(results.isEmpty());

        var card = card("Ad");
        board.columns().get(0).clear();
        board.columns().get(6).add(card);

        results = board.findFoundationCandidates();

        assertNotNull(results);
        assertEquals(1, results.size());

        board.moveToTarget(results.get(0));
        assertEquals(1, board.foundations().get(suitCode(card)).size());
        assertEquals("[0:Ad]", board.foundations().get(suitCode(card)).toString());
    }

    @Test
    public void test_findFoundationCandidateFromDeck() {
        var result = board.findFoundationCandidateFromDeck().toList();

        assertEquals(0, result.size());

        board.deckPile.push(card("Ad"));
        result = board.findFoundationCandidateFromDeck().toList();

        assertEquals(1, result.size());
        assertEquals("Candidate[cards=[0:Ad], origin=DECKPILE, from=-1, target=FOUNDATION, to=-1]",
                result.get(0).toString());
    }

    @Test
    public void test_isFoundationCandidate() {
        var a = card("Ad");
        var b = card("2d");

        assertTrue(board.isFoundationCandidate(a));
        assertFalse(board.isFoundationCandidate(b));

        board.foundations().get(suitCode(a)).push(a);

        assertTrue(board.isFoundationCandidate(b));
        assertFalse(board.isFoundationCandidate(card("3d")));
    }

    @Test
    public void test_isMovable() {
        var column = board.columns().get(3);

        assertTrue(board.isMovable(buildCandidate(3, COLUMN, column.peek())));

        assertFalse(board.isMovable(buildCandidate(3, COLUMN, column.get(0))));

        board.drawDeckCards();
        assertTrue(board.isMovable(buildCandidate(-1, DECKPILE, board.deckPile.peek())));

        assertFalse(board.isMovable(buildCandidate(-1, FOUNDATION, column.get(0))));

        var card = card("Kd");

        assertFalse(board.isMovable(card, column));

        column.clear();
        column.add(card);
        assertFalse(board.isMovable(card, column));

        card = card("Qd");
        column.add(card);
        assertTrue(board.isMovable(card, column));
    }

    @Test
    public void test_isNotSameColumn() {
        var card = board.columns().get(3).peek();

        assertTrue(board.isNotSameColumn(0, buildCandidate(3, COLUMN, card)));
        assertFalse(board.isNotSameColumn(3, buildCandidate(3, COLUMN, card)));
        assertTrue(board.isNotSameColumn(3, buildCandidate(3, DECKPILE, card)));
    }

    @Test
    public void test_findTargets() {
        var targets = board.findMovableCandidates();

        assertNotNull(targets);
        assertEquals(3, targets.size());
        assertEquals("Candidate[cards=[26:9s], origin=COLUMN, from=1, target=COLUMN, to=0]", targets.get(0).toString());
        assertEquals("Candidate[cards=[38:5c], origin=COLUMN, from=4, target=COLUMN, to=5]", targets.get(1).toString());
    }

    @Test
    public void test_findTarget() {
        var card = buildCard(34, "9h");
        var candidate = buildCandidate(0, COLUMN, List.of(card));

        var result = board.findTarget(candidate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(6, result.get(0).to());

        board.columns().get(6).clear();

        result = board.findTarget(candidate);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findTarget_king() {
        var card = buildCard(34, "Kh");
        var candidate = buildCandidate(1, COLUMN, List.of(card));

        board.columns().get(1).add(card);
        board.columns().get(0).clear();

        var result = board.findTarget(candidate);

        assertNotNull(result);
        assertEquals("Candidate[cards=[34:Kh], origin=COLUMN, from=1, target=COLUMN, to=0]", result.get(0).toString());

        candidate = buildCandidate(-1, DECKPILE, List.of(card));
        board.deckPile().add(card);

        result = board.findTarget(candidate);

        assertNotNull(result);
        assertEquals("Candidate[cards=[34:Kh], origin=DECKPILE, from=-1, target=COLUMN, to=0]", result.get(0).toString());
    }

    @Test
    public void test_findOpenCandidates() {
        var result = board.findOpenCandidates();

        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals("Candidate[cards=[24:Th], origin=COLUMN, from=0, target=null, to=-1]", result.get(0).toString());

        board.drawDeckCards();
        result = board.findOpenCandidates();

        assertNotNull(result);
        assertEquals(8, result.size());
        assertEquals("Candidate[cards=[24:Th], origin=COLUMN, from=0, target=null, to=-1]", result.get(0).toString());
        assertEquals("Candidate[cards=[21:7d], origin=DECKPILE, from=-1, target=null, to=-1]", result.get(7).toString());
    }

    @Test
    public void test_findCandidate() {
        var result = board.findCandidateAtColumn(0);

        assertEquals(1, result.cards().size());
        assertEquals(0, result.from());
        assertEquals(COLUMN, result.origin());
        assertEquals("24:Th", result.cards().get(0).toString());

        result = board.findCandidateAtColumn(6);
        assertEquals(1, result.cards().size());
        assertEquals(6, result.from());
        assertEquals(COLUMN, result.origin());
        assertEquals("[51:Tc]", result.cards().toString());
    }

    @Test
    public void test_getOrderedCards() {
        var result = board.getOrderedCards(board.columns().get(0));

        assertEquals(1, result.size());
        assertEquals("24:Th", result.get(0).toString());

        result = board.getOrderedCards(board.columns().get(6));
        assertEquals(1, result.size());
        assertEquals("[51:Tc]", result.toString());

        board.columns().get(5).add(board.columns().get(4).peek());
        result = board.getOrderedCards(board.columns().get(5));

        assertEquals(2, result.size());

        board.columns().get(0).add(board.columns().get(1).pop());
        board.columns().get(0).add(board.columns().get(1).pop());
        result = board.getOrderedCards(board.columns().get(0));

        assertEquals(3, result.size());

        board.columns().get(0).clear();
        assertTrue(board.getOrderedCards(board.columns().get(0)).isEmpty());
    }

    @Test
    public void test_isDuplicate() {
        var cards = List.of(board.columns().get(0).peek());
        var a = buildCandidate(0, COLUMN, cards, 1);

        assertTrue(board.isDuplicate(a, buildCandidate(0, COLUMN, cards, 2)));

        assertFalse(board.isDuplicate(a, buildCandidate(3, COLUMN, cards, 2)));
        assertFalse(board.isDuplicate(a, buildCandidate(0, DECKPILE, cards, 2)));
    }

    @Test
    public void test_isImmediateToFoundation() {
        var card = card("Ad");

        assertTrue(board.isImmediateToFoundation(card));

        var foundation = board.foundations().get(suitCode(card));
        foundation.add(card);
        card = card("2d");

        assertTrue(board.isImmediateToFoundation(card));

        foundation.add(card);
        assertTrue(board.isImmediateToFoundation(card));

        card = card("3d");
        assertFalse(board.isImmediateToFoundation(card));
    }

    @Test
    public void test_isCleared() {
        assertFalse(board.isCleared());

        mockFullFoundations(board.foundations());

        assertTrue(board.isCleared());
    }

    private void mockFullFoundations(List<Stack<Card>> foundations) {
        range(0, 4)
                .forEach(i -> range(0, 13)
                        .forEach(j -> foundations.get(i).add(card(VALUES.charAt(i) + toSuit(i)))));
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