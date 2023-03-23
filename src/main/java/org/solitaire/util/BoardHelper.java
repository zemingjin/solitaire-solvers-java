package org.solitaire.util;

import org.apache.commons.lang3.ObjectUtils;
import org.solitaire.model.Card;
import org.solitaire.model.Column;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static org.solitaire.util.BoardErrors.Extra;
import static org.solitaire.util.BoardErrors.Missing;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.suit;
import static org.solitaire.util.CardHelper.suitCode;

public class BoardHelper {
    public static final Predicate<Object> isNull = Objects::isNull;
    public static final Predicate<Object> isNotNull = Objects::nonNull;
    public static final Predicate<Object> listNotEmpty = ObjectUtils::isNotEmpty;

    private BoardHelper() {
    }

    public static BoardHelper of() {
        return new BoardHelper();
    }

    @SafeVarargs
    public static List<String> verifyBoard(List<Column> columns, List<Card>... decks) {
        return Optional.of(concat(toStream(columns), toStream(decks)))
                .map(CardHelper::toArray)
                .map(BoardHelper::verifyBoard)
                .orElseThrow();
    }

    public static List<String> verifyBoard(List<Column> columns) {
        return Optional.of(columns)
                .map(BoardHelper::toStream)
                .map(CardHelper::toArray)
                .map(BoardHelper::verifyBoard)
                .orElseThrow();
    }

    public static List<String> verifyBoard(Card[] cards) {
        var maps = mapCards(cards);
        var numberOfEachCards = numberOfEachCard(maps);

        return Stream.concat(check(maps, it -> it > numberOfEachCards, Extra),
                check(maps, it -> it < numberOfEachCards, Missing)).toList();
    }

    protected static int numberOfEachCard(int[][] maps) {
        var numberOfSuits = (int) Stream.of(maps).filter(it -> it[0] > 0).count();

        return switch (numberOfSuits) {
            case 4 -> 1;
            case 2 -> 4;
            case 1 -> 8;
            default -> throw new RuntimeException("Invalid number of suit: " + numberOfSuits);
        };
    }

    private static int[][] mapCards(Card[] cards) {
        var maps = new int[4][14];

        Stream.of(cards)
                .filter(isNotNull)
                .forEach(it -> {
                    var at = suitCode(it);

                    maps[at][0]++;
                    maps[at][it.rank()]++;
                });
        return maps;
    }

    protected static Stream<String> check(int[][] maps, IntPredicate test, BoardErrors type) {
        return range(0, maps.length)
                .filter(i -> maps[i][0] > 0)
                .mapToObj(i -> checkCard(test, type, maps, i))
                .flatMap(it -> it);
    }

    private static Stream<String> checkCard(IntPredicate test, BoardErrors type, int[][] maps, int i) {
        return range(1, maps[i].length)
                .filter(j -> test.test(maps[i][j]))
                .mapToObj(j -> format("%s card: %s%s", type, VALUES.charAt(j - 1), suit(i).toLowerCase()));
    }

    private static Stream<Card> toStream(List<Column> lists) {
        return lists.stream().flatMap(List::stream);
    }

    private static Stream<Card> toStream(List<Card>[] lists) {
        return Stream.of(lists).flatMap(List::stream);
    }
}
