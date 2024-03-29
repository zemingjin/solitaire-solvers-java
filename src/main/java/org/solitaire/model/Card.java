package org.solitaire.model;

import java.awt.*;
import java.util.Optional;

import static java.awt.Color.BLACK;
import static java.awt.Color.RED;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.solitaire.pyramid.Pyramid.ACE;
import static org.solitaire.pyramid.Pyramid.KING;
import static org.solitaire.util.CardHelper.VALUES;

public record Card(int at, String value, String suit, String raw) {
    private static Color getColor(String suit) {
        return switch (suit.toLowerCase()) {
            case "d", "h", "♦", "♥" -> RED;
            case "c", "s", "♠", "♣" -> BLACK;
            default -> throw new RuntimeException("Invalid suit: " + suit);
        };
    }

    public int rank() {
        return VALUES.indexOf(value) + 1;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return Optional.ofNullable(obj)
                .filter(Card.class::isInstance)
                .map(Card.class::cast)
                .map(Card::raw)
                .map(raw::equalsIgnoreCase)
                .orElse(false);
    }

    @Override
    public String toString() {
        return format("%d:%s", at, raw);
    }

    public boolean isKing() {
        return KING.equals(value);
    }

    public boolean isNotKing() {
        return !isKing();
    }

    public boolean isAce() {
        return ACE.equals(value);
    }

    public boolean isAdjacent(Card card) {
        return Optional.ofNullable(card)
                .map(Card::value)
                .map(this::isAdjacentValue)
                .orElse(false);
    }

    private boolean isAdjacentValue(String value) {
        var diff = abs(VALUES.indexOf(this.value) - VALUES.indexOf(value));

        return diff == 1 || diff == 12;
    }

    public boolean isHigherOfSameSuit(Card other) {
        return isSameSuit(other) && isHigherRank(other);
    }

    public boolean isHigherWithDifferentColor(Card other) {
        return isDifferentColor(other) && isHigherRank(other);
    }

    public boolean isHigherRank(Card other) {
        return isNull(other) ? isAce() : (rank() - other.rank()) == 1;
    }

    public boolean isHigherOfSameColor(Card other) {
        return nonNull(other) && isSameColor(other) && isHigherRank(other);
    }

    public boolean isLowerWithSameSuit(Card other) {
        return isSameSuit(other) && isLowerOrder(other);
    }

    private boolean isLowerOrder(Card other) {
        return VALUES.indexOf(other.value) - VALUES.indexOf(value) == 1;
    }

    public boolean isSameSuit(Card other) {
        return isNull(other) || suit.equals(other.suit);
    }

    public boolean isSameColor(Card other) {
        return nonNull(other) && (suit.equals(other.suit) || getColor(suit) == getColor(other.suit));
    }

    public boolean isDifferentColor(Card other) {
        return !isSameColor(other);
    }
}
