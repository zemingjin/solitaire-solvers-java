package org.solitaire.model;

import java.util.List;

import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.Origin.FREECELL;

public record Candidate(List<Card> cards, Origin origin, int from, Origin target, int to) {
    public static Candidate buildCandidate(int from, Origin origin, Origin target, Card card) {
        return buildCandidate(from, origin, target, List.of(card));
    }

    public static Candidate buildCandidate(int from, Origin origin, Origin target, List<Card> cards) {
        return new Candidate(cards, origin, from, target, -1);
    }

    public static Candidate buildCandidate(int from, Origin origin, Card card) {
        return buildCandidate(from, origin, List.of(card));
    }

    public static Candidate buildCandidate(int from, Origin origin, List<Card> cards) {
        return buildCandidate(from, origin, cards, -1);
    }

    public static Candidate buildCandidate(int from, Origin origin, List<Card> cards, int to) {
        return new Candidate(cards, origin, from, origin, to);
    }

    public static Candidate buildCandidate(Candidate that, int to) {
        return new Candidate(that.cards, that.origin, that.from, that.target, to);
    }

    public Card peek() {
        return cards.get(0);
    }

    public boolean isKing() {
        return peek().isKing();
    }

    public boolean isFromColumn() {
        return origin == COLUMN;
    }

    public boolean isToColumn() {
        return target == COLUMN;
    }

    public boolean isToFreeCell() {
        return FREECELL == target;
    }

    public boolean isToFoundation() {
        return FOUNDATION == target;
    }
}
