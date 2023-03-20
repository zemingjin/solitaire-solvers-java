package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelper.suitCode;
import static org.solitaire.util.CardHelper.toArray;

public record Candidate(Card[] cards, Origin origin, int from, Origin target, int to) {
    public static final Function<Pair<Integer, Card>, Candidate> buildFoundationToColumn =
            pair -> new Candidate(toArray(pair.getRight()), FOUNDATION, suitCode(pair.getRight()),
                    COLUMN, pair.getLeft());

    public static Candidate buildCandidate(int from, Origin origin, Origin target, Card card) {
        return buildCandidate(from, origin, target, toArray(card));
    }

    public static Candidate buildCandidate(int from, Origin origin, Origin target, Card[] cards) {
        return new Candidate(cards, origin, from, target, -1);
    }

    public static Candidate buildCandidate(int from, Origin origin, Card card) {
        return buildCandidate(from, origin, toArray(card));
    }

    public static Candidate buildCandidate(int from, Origin origin, Card[] cards) {
        return buildCandidate(from, origin, cards, -1);
    }

    public static Candidate buildCandidate(Card[] cards, Origin origin, Origin target) {
        return new Candidate(cards, origin, cards[0].at(), target, 0);
    }

    public static Candidate buildCandidate(int from, Origin origin, Card[] cards, int to) {
        return new Candidate(cards, origin, from, null, to);
    }

    public static Candidate buildColumnCandidate(Candidate that, int to) {
        return new Candidate(that.cards, that.origin, that.from, COLUMN, to);
    }

    public static Candidate buildFoundationCandidate(Card card, Origin origin, int from) {
        return new Candidate(toArray(card), origin, from, FOUNDATION, suitCode(card));
    }

    public String notation() {
        return originNotation() + targetNotation() + ":" + valueNotation();
    }

    private String originNotation() {
        return switch (origin) {
            case COLUMN -> Integer.toString(from);
            case FREECELL -> "f";
            case DECKPILE -> "^";
            case BOARD -> "b";
            case FOUNDATION -> "$";
            default -> throw new RuntimeException("Invalid Origin: " + this);
        };
    }

    private String targetNotation() {
        return isNull(target)
                ? "_"
                : switch (target) {
            case COLUMN -> Integer.toString(to);
            case FREECELL -> "f";
            case FOUNDATION -> "$";
            case DECKPILE -> "^";
            case BOARD -> "b";
            case REMOVE -> "r";
        };
    }

    private String valueNotation() {
        return stringOfRaws(cards);
    }

    public Card peek() {
        return cards[0];
    }

    public boolean isKing() {
        return peek().isKing();
    }

    public boolean isToColumn() {
        return COLUMN == target();
    }

    public boolean isToFoundation() {
        return FOUNDATION == target();
    }

    public boolean isFromColumn() {
        return COLUMN == origin();
    }

    public boolean isToDeck() {
        return DECKPILE == target();
    }

    public boolean isNotToDeck() {
        return !isToDeck();
    }

    @Override
    public String toString() {
        return format("Candidate(%s, %s, %d, %s, %d)", stringOfRaws(cards), origin, from, target, to);
    }
}
