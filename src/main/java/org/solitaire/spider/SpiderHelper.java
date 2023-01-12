package org.solitaire.spider;

import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.solitaire.util.CardHelper.buildCard;

public class SpiderHelper {
    protected static final int LAST_BOARD = 54;
    protected static final int LAST_DECK = 50;
    protected static final int LAST_COLUMN = 10;
    protected static final int NUM_LONG = 4;
    protected static final int COL_LONG = 6;
    protected static final int LAST_LONG = 24;

    public static Spider build(String[] cards) {
        assert nonNull(cards) && cards.length == LAST_BOARD + LAST_DECK;

        return new Spider(buildColumns(cards), new Path<>(), 500, buildDeck(cards));
    }

    protected static Columns buildColumns(String[] cards) {
        var columns = new Columns(LAST_COLUMN);

        for (int i = 0; i < LAST_BOARD; i++) {
            var columnAt = calcColumn(i);
            var column = getColumn(columns, columnAt);

            column.add(buildCard(i, cards[i]));
            column.setOpenAt(column.size() - 1);
        }
        return columns;
    }

    private static Column getColumn(List<Column> board, int columnAt) {
        assert 0 <= columnAt && columnAt < LAST_COLUMN : "Invalid column: " + columnAt;

        if (board.isEmpty() || board.size() <= columnAt) {
            board.add(columnAt, new Column());
        }
        return board.get(columnAt);
    }

    protected static Deck buildDeck(String[] cards) {
        return range(LAST_BOARD, LAST_BOARD + LAST_DECK)
                .mapToObj(i -> buildCard(i, cards[i]))
                .collect(Collectors.toCollection(Deck::new));
    }

    protected static int calcColumn(int i) {
        if (0 <= i && i < LAST_LONG) {
            return i / 6;
        } else if (LAST_LONG <= i && i < LAST_BOARD) {
            return (i - LAST_LONG) / 5 + NUM_LONG;
        }
        throw new IndexOutOfBoundsException("Invalid index: " + i);
    }
}
