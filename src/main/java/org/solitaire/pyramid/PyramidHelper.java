package org.solitaire.pyramid;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

public class PyramidHelper {
    protected static final int LAST_BOARD = 28;
    protected static final int LAST_DECK = 52;
    protected static final int LAST_BOARD_INDEX = LAST_BOARD - 1;
    protected static final int[] ROW_SCORES = new int[]{500, 250, 150, 100, 75, 50, 25};

    public static Pyramid build(String[] cards) {
        return Optional.of(cards)
                .map(CardHelper::toCards)
                .map(PyramidHelper::buildPyramid)
                .orElseThrow();
    }

    private static Pyramid buildPyramid(Card[] cards) {
        assert cards.length == 52 : "Invalid # of cards: " + cards.length;

        return new Pyramid(copyOf(cards, LAST_BOARD), buildDeck(cards));
    }

    private static Stack<Card> buildDeck(Card[] cards) {
        var deck = new Stack<Card>();

        deck.addAll(stream(cards, LAST_BOARD, LAST_DECK).toList());
        return deck;
    }

    protected static int row(int at) {
        assert 0 <= at && at < LAST_BOARD : "Invalid board card index: " + at;

        final var rowMax = new RowMax(LAST_BOARD);

        return rangeClosed(1, 7)
                .map(i -> 7 - i + 1)
                .peek(i -> rowMax.rowMax(rowMax.rowMax - i))
                .filter(i -> rowMax.rowMax() <= at)
                .findFirst()
                .orElseThrow();
    }

    @SuppressWarnings("rawtypes")
    protected static Pair<Integer, List> getScore(List<?> list) {
        return Pair.of(
                range(0, list.size())
                        .map(i -> getClickScore(i, list))
                        .reduce(0, Integer::sum),
                list);
    }

    /*
     * Score Rules:
     * - 5: each pair
     * - 25 for row 7
     * - 50 for row 6
     * - 75 for row 5
     * - 100 for row 4
     * - 150 for row 3
     * - 250 for row 2
     * - 500 for clear board
     */
    @SuppressWarnings("unchecked")
    protected static int getClickScore(int at, List<?> list) {
        var item = list.get(at);
        return Optional.of(item)
                .map(it -> (Card[]) it)
                .filter(it -> it.length == 1)
                .map(it -> it[0])
                .map(it -> it.isKing() ? 5 : 0)
                .orElseGet(() -> getRowClearingScore(at, (List<Card[]>) list));
    }

    protected static int getRowClearingScore(int at, List<Card[]> list) {
        return Optional.ofNullable(cardAt(list.get(at)))
                .filter(PyramidHelper::isBoardCard)
                .map(it -> row(it.at()))
                .map(row -> getScore(row, at, list))
                .orElse(5);
    }

    protected static int getScore(int row, int at, List<Card[]> list) {
        return 5 + (isRowCleared(row, at, list) ? scoreByRow(row) : 0);
    }

    protected static Card cardAt(Card[] cards) {
        var a = cards[0];
        var b = cards[1];

        if (isBoardCard(a) == isBoardCard(b)) {
            return a.at() >= b.at() ? a : b;
        } else if (isBoardCard(a)) {
            return a;
        }
        return b;
    }

    protected static boolean isBoardCard(Card card) {
        return isBoardCard(card.at());
    }

    protected static boolean isBoardCard(int at) {
        return 0 <= at && at < LAST_BOARD;
    }

    protected static int scoreByRow(int row) {
        assert 0 < row && row <= ROW_SCORES.length : "Invalid row number: " + row;
        return ROW_SCORES[row - 1];
    }

    protected static boolean isRowCleared(int row, int at, List<Card[]> list) {
        return rangeClosed(0, at)
                .mapToObj(list::get)
                .flatMap(Stream::of)
                .filter(PyramidHelper::isBoardCard)
                .filter(it -> row(it.at()) == row)
                .count() == row;
    }

    @AllArgsConstructor
    static class RowMax {
        private int rowMax;

        int rowMax() {
            return rowMax;
        }

        void rowMax(int rowMax) {
            this.rowMax = rowMax;
        }
    }

}
