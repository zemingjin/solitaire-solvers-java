package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.math.NumberUtils.min;

@SuppressWarnings("rawtypes")
public class CardHelper {
    public static final String DIAMOND = "♦";
    public static final String SPADE = "♠";
    public static final String CLUB = "♣";
    public static final String HEART = "♥";
    public static final String VALUES = "A23456789TJQK";
    private static final Map<String, String> SUITS_MAP = new HashMap<>() {{
        put("d", DIAMOND);
        put("s", SPADE);
        put("c", CLUB);
        put("h", HEART);
    }};
    public static boolean useSuit = true;

    public static int suitCode(Card card) {
        return switch (card.suit().toLowerCase()) {
            case "d" -> 0;
            case "h" -> 1;
            case "c" -> 2;
            default -> 3;
        };
    }

    public static String getSuit(String c) {
        return SUITS_MAP.get(c);
    }

    public static Card[] cloneArray(Card[] array) {
        requireNonNull(array);
        var buf = new Card[array.length];

        System.arraycopy(array, 0, buf, 0, array.length);
        return buf;
    }

    public static <R> List<Stack<R>> cloneStacks(List<Stack<R>> stacks) {
        return stacks.stream()
                .map(CardHelper::cloneStack)
                .toList();
    }

    public static <R> Stack<R> cloneStack(Stack<R> stack) {
        var clone = new Stack<R>();

        if (nonNull(stack) && !stack.isEmpty()) {
            for (R item : stack) {
                clone.push(item);
            }
        }
        return clone;
    }

    public static List<Column> cloneColumns(List<Column> lists) {
        return lists.stream()
                .map(CardHelper::cloneColumn)
                .toList();
    }

    public static Card[] resizeArray(Card[] origin, int newSize) {
        var target = new Card[newSize];

        System.arraycopy(origin, 0, target, 0, min(origin.length, newSize));
        return target;
    }

    public static <T> List<T> cloneList(List<T> list) {
        return Optional.ofNullable(list)
                .map(ArrayList::new)
                .orElse(null);
    }

    public static Column cloneColumn(Column column) {
        return Optional.ofNullable(column)
                .map(Column::new)
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
                .raw(useSuit ? value.charAt(0) + getSuit(value.substring(1)) : value)
                .value(value.substring(0, 1))
                .suit(value.substring(1))
                .build();
    }

    public static boolean isCleared(Card[] cards) {
        return stream(cards, 0, cards.length)
                .filter(Objects::nonNull)
                .findAny()
                .isEmpty();
    }

    @SuppressWarnings("unchecked")
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

    public static void checkShortestPath(List<List> results) {
        checkPath(results, (a, b) -> a.size() <= b.size() ? a : b, "Shortest");
    }

    public static void checkLongestPath(List<List> results) {
        checkPath(results, (a, b) -> a.size() >= b.size() ? a : b, "Longest");
    }

    public static void checkMaxScore(Pair<GameSolver, List<List>> pair) {
        Optional.of(pair.getLeft().getMaxScore(pair.getRight()))
                .filter(p -> p.getLeft() > 0)
                .ifPresent(p -> System.out.printf("Max Score(%d): %s\n", p.getLeft(), string(p.getRight())));
    }

    private static void checkPath(List<List> results, BinaryOperator<List> accumulator, String type) {
        requireNonNull(results);

        Optional.of(results.stream().reduce(results.get(0), accumulator))
                .ifPresent(it -> System.out.printf("%s Path(%d): %s\n", type, it.size(), string(it)));
    }
}
