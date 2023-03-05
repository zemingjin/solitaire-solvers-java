package org.solitaire.util;

import org.solitaire.model.Card;
import org.solitaire.model.Column;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.IntStream.range;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.suit;
import static org.solitaire.util.CardHelper.suitCode;

public class BoardHelper {
    public static List<String> verifyBoard(List<Column> columns) {
        return verifyBoard(toArray(columns));
    }

    public static List<String> verifyBoard(Card[] cards) {
        var maps = new Card[4][14];
        var results = checkDuplidates(cards, maps);

        results.addAll(checkMissing(maps));
        return results;
    }

    protected static List<String> checkDuplidates(Card[] cards, Card[][] maps) {
        return Stream.of(cards)
                .map(it -> checkDuplicates(it, maps))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static String checkDuplicates(Card card, Card[][] maps) {
        if (isNull(card)) {
            return null;
        } else if (isNull(maps[suitCode(card)][card.rank()])) {
            maps[suitCode(card)][card.rank()] = card;
            return null;
        }
        return format("Duplicated card: %s", card.raw());
    }

    protected static List<String> checkMissing(Card[][] cards) {
        return range(0, cards.length)
                .mapToObj(i -> range(1, cards[i].length)
                        .filter(j -> isNull(cards[i][j]))
                        .mapToObj(j -> format("Missing card: %s%s", VALUES.charAt(j - 1), suit(i).toLowerCase())))
                .flatMap(it -> it)
                .collect(Collectors.toList());
    }

    private static Card[] toArray(List<Column> columns) {
        return columns.stream().flatMap(Column::stream).map(Card.class::cast).toArray(Card[]::new);
    }
}
