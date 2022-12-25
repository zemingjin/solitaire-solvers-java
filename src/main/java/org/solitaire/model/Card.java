package org.solitaire.model;

import lombok.Builder;

import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static org.solitaire.model.CardHelper.VALUES;

@Builder
public record Card(int at, char value, String raw) {
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
                .map(Card::value)
                .map(this::isAdjacentValue)
                .orElse(false);
    }

    private boolean isAdjacentValue(char value) {
        var diff = abs(VALUES.indexOf(this.value) - VALUES.indexOf(value));

        return diff == 1 || diff == 12;
    }
}
