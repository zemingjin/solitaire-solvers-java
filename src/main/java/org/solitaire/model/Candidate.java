package org.solitaire.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static org.solitaire.util.CardHelper.toList;

@ToString
@AllArgsConstructor
@Builder
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

    public static Candidate buildCandidate(int at, Origin origin, Card card) {
        return buildCandidate(at, origin, toList(card));
    }

    public static Candidate buildCandidate(int at, Origin origin, List<Card> cards) {
        return buildCandidate(at, origin, cards, -1);
    }

    public static Candidate buildCandidate(int at, Origin origin, List<Card> cards, int target) {
        return Candidate.builder().from(at).origin(origin).cards(cards).target(target).build();
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
