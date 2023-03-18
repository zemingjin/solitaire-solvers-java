package org.solitaire.util;

import org.solitaire.model.Card;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static org.solitaire.util.BoardHelper.isNull;

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
    private static boolean useSuit = true;

    public static void useSuit(boolean useSuit) {
        CardHelper.useSuit = useSuit;
    }

    public static boolean useSuit() {
        return useSuit;
    }

    public static int rank(Card card) {
        return Optional.ofNullable(card)
                .map(Card::rank)
                .orElse(0);
    }

    public static int rankDifference(Card a, Card b) {
        if (nonNull(b)) {
            return requireNonNull(a).rank() - b.rank();
        }
        return requireNonNull(a).rank();
    }

    public static int suitCode(Card card) {
        return switch (requireNonNull(card).suit().toLowerCase()) {
            case "c" -> 0;
            case "d" -> 1;
            case "h" -> 2;
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

    public static Card card(String value) {
        return buildCard(0, value);
    }

    public static Card nextCard(Card card, int suitCode) {
        return nonNull(card)
                ? card(nextValue(card.value()) + card.suit())
                : card("A" + suit(suitCode).toLowerCase());
    }

    public static Card nextCard(Card card) {
        return card(nextValue(card.value()) + card.suit());
    }

    public static String nextValue(String value) {
        var at = VALUES.indexOf(value);
        return VALUES.substring(at + 1, at + 2);
    }

    public static boolean isCleared(Card[] cards) {
        return isCleared(cards, 0, cards.length);
    }

    public static boolean isCleared(Card[] cards, int startInclusive, int endExclusive) {
        return stream(cards, startInclusive, endExclusive).allMatch(isNull);
    }

    public static String string(List<?> cards) {
        return cards.stream()
                .map(CardHelper::stringOfRaws)
                .collect(Collectors.joining(", "));
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

    @SafeVarargs
    public static <T> T[] toArray(T... items) {
        return items;
    }

    public static Card higherCardOfSameSuit(Card card) {
        return card.isKing() ? card : card(VALUES.charAt(card.rank()) + card.suit());
    }
}
