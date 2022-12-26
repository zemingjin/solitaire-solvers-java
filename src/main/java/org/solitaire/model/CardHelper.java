package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
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
    private static int totalScenarios;

    public static void incTotal() {
        totalScenarios++;
    }

    public static int getTotalScenarios() {
        return totalScenarios;
    }


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
        return obj instanceof Card ? ((Card) obj).raw() : toString((Card[]) obj);
    }

    private static String toString(Card[] cards) {
        return Optional.of(cards)
                .filter(it -> it.length > 1)
                .map(it -> Stream.of(cards).map(Card::raw).collect(Collectors.joining(":")))
                .orElseGet(cards[0]::raw);
    }

    @SuppressWarnings("rawtypes")
    public static void checkShortestPath(List<List> results) {
        checkPath(results, (a, b) -> a.size() <= b.size() ? a : b, "Shortest");
    }

    @SuppressWarnings("rawtypes")
    public static void checkLongestPath(List<List> results) {
        checkPath(results, (a, b) -> a.size() >= b.size() ? a : b, "Longest");
    }

    @SuppressWarnings("rawtypes")
    public static void checkMaxScore(Pair<GameSolver, List<List>> pair) {
        Optional.of(pair.getLeft().getMaxScore(pair.getRight()))
                .filter(p -> p.getLeft() > 0)
                .ifPresent(p -> System.out.printf("Max Score(%d): %s", p.getLeft(), string(p.getRight())));
    }

    @SuppressWarnings("rawtypes")
    private static void checkPath(List<List> results, BinaryOperator<List> accumulator, String type) {
        requireNonNull(results);

        Optional.of(results.stream().reduce(results.get(0), accumulator))
                .ifPresent(it -> System.out.printf("%s Path(%d): %s\n", type, it.size(), string(it)));
    }
}
