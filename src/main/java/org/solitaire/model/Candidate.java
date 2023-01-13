package org.solitaire.model;

import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
public class Candidate {
    private List<Card> cards;
    private Origin origin;
    private int from;
    private int target;

    public Candidate(Candidate candidate) {
        cards = candidate.cards();
        from = candidate.from();
        origin = candidate.origin();
        target = candidate.target();
    }

    public static Candidate buildCandidate(int from, Origin origin, Card card) {
        return buildCandidate(from, origin, List.of(card));
    }

    public static Candidate buildCandidate(int from, Origin origin, List<Card> cards) {
        return buildCandidate(from, origin, cards, -1);
    }

    public static Candidate buildCandidate(int from, Origin origin, List<Card> cards, int target) {
        return new Candidate(cards, origin, from, target);
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

    public Candidate setTarget(int target) {
        this.target = target;
        return this;
    }

    public List<Card> cards() {
        return cards;
    }

    public Origin origin() {
        return origin;
    }

    public int from() {
        return from;
    }

    public int target() {
        return target;
    }
}
