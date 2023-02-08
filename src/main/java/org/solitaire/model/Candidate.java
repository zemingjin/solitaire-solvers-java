package org.solitaire.model;

import java.util.List;

public record Candidate(List<Card> cards, Origin origin, int from, int target) {
    public static Candidate buildCandidate(int from, Origin origin, Card card) {
        return buildCandidate(from, origin, List.of(card));
    }

    public static Candidate buildCandidate(int from, Origin origin, List<Card> cards) {
        return buildCandidate(from, origin, cards, -1);
    }

    public static Candidate buildCandidate(int from, Origin origin, List<Card> cards, int target) {
        return new Candidate(cards, origin, from, target);
    }

    public static Candidate buildCandidate(Candidate that, int target) {
        return new Candidate(that.cards, that.origin, that.from, target);
    }

    public Card peek() {
        return cards.get(0);
    }

    public boolean isKing() {
        return peek().isKing();
    }

    public boolean isToColumn() {
        return !isToFoundation();
    }

    public boolean isToFoundation() {
        return -1 == target;
    }
}
