package org.solitaire.spider;

import org.solitaire.model.Card;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;
import static org.solitaire.model.CardHelper.buildCard;

public class SpiderHelper {
    protected static final int LAST_BOARD = 54;
    protected static final int LAST_DECK = 50;
    protected static final int LAST_COLUMN = 10;
    protected static final int NUM_LONG = 4;
    protected static final int COL_LONG = 6;
    private static final int LAST_LONG = 24;

    public static Spider build(String[] cards) {
        assert nonNull(cards) && cards.length == LAST_BOARD + LAST_DECK;
        return Spider.builder()
                .board(buildBoard(cards))
                .deck(buildDeck(cards))
                .path(new LinkedList<>())
                .build();
    }

    protected static List<Column> buildBoard(String[] cards) {
        var board = new ArrayList<Column>(LAST_COLUMN);

        for (int i = 0; i < LAST_BOARD; i++) {
            var columnAt = calcColumn(i);
            var column = getColumn(board, columnAt);

            column.cards().add(0, buildCard(i, cards[i]));
        }
        return board;
    }

    private static Column getColumn(List<Column> board, int columnAt) {
        assert 0 <= columnAt && columnAt < LAST_COLUMN : "Invalid column: " + columnAt;
        if (board.isEmpty() || board.size() <= columnAt) {
            board.add(columnAt, new Column(columnAt < NUM_LONG ? COL_LONG - 1 : COL_LONG - 2, new LinkedList<>()));
        }
        return board.get(columnAt);
    }

    protected static List<Card> buildDeck(String[] cards) {
        return IntStream.range(LAST_BOARD, LAST_BOARD + LAST_DECK)
                .mapToObj(i -> buildCard(i, cards[i]))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static int calcColumn(int i) {
        return i < LAST_LONG ? i / 6 : (i - LAST_LONG) / 5 + NUM_LONG;
    }
}
