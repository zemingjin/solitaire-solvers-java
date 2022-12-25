package org.solitaire.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

public class CardHelper {
    public static final String DIAMOND = "♦";
    public static final String SPADE = "♠";
    public static final String CLUB = "♣";
    public static final String HEART = "♥";
    public static final String VALUES = "A23456789TJQK";
    private static final Map<Character, String> SUITS_MAP = new HashMap<>() {{
        put('d', DIAMOND);
        put('s', SPADE);
        put('c', CLUB);
        put('h', HEART);
    }};
    public static boolean useSuit = true;

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

    public static Card[] toCards(String[] cards) {
        requireNonNull(cards);
        return IntStream.range(0, cards.length)
                .mapToObj(i -> buildCard(i, cards[i]))
                .toArray(Card[]::new);
    }

    public static Card buildCard(int at, String value) {
        assert VALUES.indexOf(value.charAt(0)) >= 0;

        return Card.builder()
                .at(at)
                .raw(useSuit ? value.charAt(0) + getSuit(value.charAt(1)) : value)
                .value(value.charAt(0))
                .build();
    }

    public static boolean isCleared(Card[] cards, int endExclusive) {
        return stream(cards, 0, min(cards.length, endExclusive))
                .filter(Objects::nonNull)
                .findAny()
                .isEmpty();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String string(List cards) {
        return ((List<Object>) cards).stream()
                .map(CardHelper::toString)
                .collect(Collectors.joining(" "));
    }

    protected static String toString(Object obj) {
        return obj instanceof Card ? ((Card) obj).getRaw() : toString((Card[]) obj);
    }

    private static String toString(Card[] cards) {
        return Optional.of(cards)
                .filter(it -> it.length > 1)
                .map(it -> Stream.of(cards).map(Card::getRaw).collect(Collectors.joining(":")))
                .orElseGet(cards[0]::getRaw);
    }

    @SuppressWarnings("rawtypes")
    public static void checkShortestPath(List<List> results) {
        requireNonNull(results);

        Optional.of(results.stream().reduce(results.get(0), (a, b) -> a.size() < b.size() ? a : b))
                .ifPresent(it -> System.out.printf("Shortest Path(%d): %s\n", it.size(), string(it)));
    }

}
