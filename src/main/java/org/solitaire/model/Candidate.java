package org.solitaire.model;

import java.util.List;

import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelper.suitCode;

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
        return new Candidate(cards, origin, from, null, to);
    }

    public static Candidate buildColumnCandidate(Candidate that, int to) {
        return new Candidate(that.cards, that.origin, that.from, COLUMN, to);
    }

    public static Candidate buildFoundationCandidate(Card card, Origin origin, int from) {
        return new Candidate(List.of(card), origin, from, FOUNDATION, suitCode(card));
    }

    public String notation() {
        return originNotation() + targetNotation() + ":" + valueNotation();
    }

    private String originNotation() {
        return switch (origin) {
            case COLUMN -> Integer.toString(from);
            case FREECELL -> "f";
            case DECKPILE -> "d";
            default -> throw new RuntimeException("Invalid Origin: " + this);
        };
    }

    private String targetNotation() {
        return switch (target) {
            case COLUMN -> Integer.toString(to);
            case FREECELL -> "f";
            case FOUNDATION -> "F";
            case DECKPILE -> "D";
        };
    }

    private String valueNotation() {
        return stringOfRaws(cards().toArray(Card[]::new));
    }

    public Card peek() {
        return cards.get(0);
    }

    public boolean isKing() {
        return peek().isKing();
    }

    public boolean isToColumn() {
        return COLUMN == target;
    }

    public boolean isToFoundation() {
        return FOUNDATION == target;
    }
}
