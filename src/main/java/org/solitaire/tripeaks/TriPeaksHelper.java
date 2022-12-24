package org.solitaire.tripeaks;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.model.CardHelper.string;

public class TriPeaksHelper {
    public static final int LAST_BOARD = 28;
    public static final int INI_COVERED = 18;
    public static final int LAST_DECK = 51;

    public static GameSolver<Card> build(String[] cards) {
        return TriPeaksBoard.builder()
                .cards(toCards(cards))
                .wastePile(toWastePile(cards))
                .build();
    }

    protected static List<Card> toWastePile(String[] cards) {
        assert 0 < cards.length && cards.length <= 52;

        return Stream.of(buildCard(51, cards[cards.length - 1])).toList();
    }

    protected static Card[] toCards(String[] cards) {
        requireNonNull(cards);
        return IntStream.range(0, min(cards.length, LAST_DECK))
                .mapToObj(i -> buildCard(i, cards[i]))
                .toArray(Card[]::new);
    }

    public static boolean isFromDeck(Card card) {
        return 28 <= card.getAt();
    }

    public static void checkMaxScore(List<List<Card>> results) {
        requireNonNull(results);

        Optional.of(results.stream()
                        .map(TriPeaksHelper::getScore)
                        .reduce(Pair.of(0, null), (a, b) -> a.getLeft() > b.getLeft() ? a : b))
                .ifPresent(it -> System.out.printf("Max Score(%d): %s\n", it.getLeft(), string(it.getRight())));
    }

    public static void checkShortestPath(List<List<Card>> results) {
        requireNonNull(results);

        Optional.of(results.stream().reduce(results.get(0), (a, b) -> a.size() < b.size() ? a : b))
                .ifPresent(it -> System.out.printf("Shortest Path(%d): %s\n", it.size(), string(it)));
    }

    protected static Pair<Integer, List<Card>> getScore(List<Card> cards) {
        int score = 0;
        int sequenceCount = 0;

        for (Card card : cards) {
            if (isFromDeck(card)) {
                sequenceCount = 0;
            } else {
                sequenceCount++;
                score += (sequenceCount * 2 - 1) * 100;
            }
        }
        return Pair.of(score, cards);
    }
}
