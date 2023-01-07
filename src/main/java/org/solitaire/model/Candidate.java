package org.solitaire.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
@Getter
public class Candidate {
    private List<Card> cards;
    private Origin origin;
    private int from;
    private int target;

    public Candidate(Candidate candidate) {
        cards = candidate.getCards();
        from = candidate.getFrom();
        origin = candidate.getOrigin();
        target = candidate.getTarget();
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
}
