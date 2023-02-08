package org.solitaire.util;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;

@SuppressWarnings("rawtypes")
public class CardHelper {
    public static final String DIAMOND = "♦";   // \u2666
    public static final String SPADE = "♠";     // \u2660
    public static final String CLUB = "♣";      // \u2663
    public static final String HEART = "♥";     // \u2665
    public static final String VALUES = "A23456789TJQK";
    private static final Map<String, String> SUITS_MAP = new HashMap<>() {{
        put("d", DIAMOND);
        put("s", SPADE);
        put("c", CLUB);
        put("h", HEART);
    }};
    public static boolean useSuit = true;

    public static int diffOfValues(Card a, Card b) {
        assert nonNull(a);
        var aValue = VALUES.indexOf(a.value());

        if (nonNull(b)) {
            return aValue - VALUES.indexOf(b.value());
        }
        return aValue + 1;
    }

    public static int suitCode(Card card) {
        return switch (card.suit().toLowerCase()) {
            case "d" -> 0;
            case "h" -> 1;
            case "c" -> 2;
            default -> 3;
        };
    }

    public static String suit(int code) {
        return switch (code % 4) {
            case 0 -> useSuit ? CLUB : "C";
            case 1 -> useSuit ? DIAMOND : "D";
            case 2 -> useSuit ? HEART : "H";
            default -> useSuit ? SPADE : "S";
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

        clone.addAll(stack);
        return clone;
    }

    public static Card[] toCards(String[] cards) {
        requireNonNull(cards);
        return range(0, cards.length)
                .mapToObj(i -> buildCard(i, cards[i]))
                .toArray(Card[]::new);
    }

    public static Card buildCard(int at, String value) {
        assert nonNull(value) && value.length() == 2 && VALUES.indexOf(value.charAt(0)) >= 0
                : "Invalid card value/suit: " + value;

        return new Card(at, value.substring(0, 1), value.substring(1),
                useSuit ? value.charAt(0) + getSuit(value.substring(1)) : value);
    }

    public static boolean isCleared(Card[] cards) {
        return isCleared(cards, 0, cards.length);
    }

    public static boolean isCleared(Card[] cards, int startInclusive, int endExclusive) {
        return stream(cards, startInclusive, endExclusive).allMatch(Objects::isNull);
    }

    public static String string(List<?> cards) {
        return cards.stream()
                .map(CardHelper::stringOfRaws)
                .collect(Collectors.joining(" "));
    }

    protected static String stringOfRaws(Object obj) {
        return obj instanceof Card
                ? ((Card) obj).raw()
                : obj instanceof Card[] ? stringOfRaws((Card[]) obj) : (String) obj;
    }

    public static String stringOfRaws(Card[] cards) {
        return Optional.of(cards)
                .filter(it -> it.length == 1)
                .map(it -> it[0].raw())
                .orElseGet(() -> Arrays.toString(Stream.of(cards).map(Card::raw).toArray()));
    }

    public static String stringOfRaws(List<Card> cards) {
        return stringOfRaws(cards.toArray(Card[]::new));
    }

    public static void checkShortestPath(List<List> results) {
        checkPath(results, (a, b) -> a.size() <= b.size() ? a : b, "Shortest");
    }

    public static void checkLongestPath(List<List> results) {
        checkPath(results, (a, b) -> a.size() >= b.size() ? a : b, "Longest");
    }

    public static void checkMaxScore(Pair<GameSolver, List<List>> pair) {
        Optional.ofNullable(pair.getLeft().getMaxScore(pair.getRight()))
                .filter(p -> p.getLeft() > 0)
                .ifPresent(p -> System.out.printf("Max Score(%,d): %s\n", p.getLeft(), string(p.getRight())));
    }

    private static void checkPath(List<List> results, BinaryOperator<List> accumulator, String type) {
        requireNonNull(results);

        if (!results.isEmpty()) {
            Optional.of(results.stream().reduce(results.get(0), accumulator))
                    .ifPresent(it -> System.out.printf("%s Path(%d): %s\n", type, it.size(), string(it)));
        }
    }

    public static void checkDuplicates(String[] cards) {
        assert nonNull(cards);

        range(0, cards.length)
                .forEach(i -> checkDuplicates(cards, i));
    }

    private static void checkDuplicates(String[] cards, int at) {
        var list = Arrays.asList(cards);
        var card = cards[at];

        if (list.indexOf(card) != list.lastIndexOf(card)) {
            throw new RuntimeException("Duplicated cards: " + card);
        }
    }
}
