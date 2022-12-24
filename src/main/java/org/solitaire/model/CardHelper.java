package org.solitaire.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

public class CardHelper {
    private static final Map<Character, String> SUITS_MAP = new HashMap<>() {{
        put('d', "♦");
        put('s', "♠");
        put('c', "♣");
        put('h', "♥");
    }};

    public static String getSuit(char c) {
        return SUITS_MAP.get(c);
    }

    public static Card[] cloneArray(Card[] array) {
        requireNonNull(array);
        var buf = new Card[array.length];

        System.arraycopy(array, 0, buf, 0, array.length);
        return buf;
    }

    public static <T> List<T> cloneList(List<T> list) {
        return Optional.ofNullable(list)
                .map(ArrayList::new)
                .orElse(null);
    }

    public static final String VALUES = "A23456789TJQK";
    public static boolean useSuit = true;

    public static Card[] toCards(String[] cards) {
        requireNonNull(cards);
        return IntStream.range(0, cards.length)
                .mapToObj(i -> buildCard(i, cards[i]))
                .toArray(Card[]::new);
    }

    public static Card buildCard(int at, String value) {
        assert VALUES.indexOf(value.charAt(0)) >= 0;

        if (useSuit) {
            value = value.charAt(0) + SUITS_MAP.get(value.charAt(1));
        }
        return Card.builder()
                .at(at)
                .raw(value)
                .value(value.charAt(0))
                .build();
    }

    public static boolean isCleared(Card[] cards, int endExclusive) {
        return stream(cards, 0, min(cards.length, endExclusive))
                .filter(Objects::nonNull)
                .findAny()
                .isEmpty();
    }

    public static String string(List<Card> cards) {
        return cards.stream().map(Card::getRaw).collect(Collectors.joining(" "));
    }
}
