package org.solitaire.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static org.solitaire.model.CardHelper.VALUES;

@Getter
@Builder
public class Card {
    private int at;
    private char value;
    private String raw;

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return Optional.ofNullable(obj)
                .filter(it -> it instanceof Card)
                .map(Card.class::cast)
                .map(it -> it.raw.equals(raw))
                .orElse(false);
    }

    @Override
    public String toString() {
        return format("%d:%s", at, raw);
    }

    public boolean isAdjacent(Card card) {
        return Optional.ofNullable(card)
                .map(Card::getValue)
                .map(this::isAdjacentValue)
                .orElse(false);
    }

    private boolean isAdjacentValue(char value) {
        var diff = abs(VALUES.indexOf(this.value) - VALUES.indexOf(value));

        return diff == 1 || diff == 12;
    }
}
