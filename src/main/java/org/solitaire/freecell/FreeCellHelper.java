package org.solitaire.freecell;

import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;

import java.util.Arrays;

import static java.util.stream.IntStream.range;
import static org.solitaire.util.CardHelper.buildCard;

public class FreeCellHelper {
    protected static final int LAST_LONG = 28;
    protected static final int LAST_BOARD = 52;
    protected static final int LEN_LONG = 7;
    protected static final int LEN_SHORT = 6;
    protected static final int COLUMNS = 8;

    public static FreeCell build(String[] cards) {
        return new FreeCell(buildBoard(cards));
    }

    public static Columns buildBoard(String[] cards) {
        assert cards != null && cards.length == LAST_BOARD : "Invalid source cards: " + Arrays.toString(cards);

        var columns = new Columns(COLUMNS);
        range(0, COLUMNS).forEach(i -> columns.add(i, new Column()));

        range(0, cards.length)
                .mapToObj(i -> buildCard(i, cards[i]))
                .forEach(it -> setCardsToColumns(columns, it));
        return columns;
    }

    private static void setCardsToColumns(Columns columns, Card card) {
        var column = getColumn(card.at());
        var col = columns.get(column);

        col.add(card);
    }

    protected static int getColumn(int at) {
        return (at < LAST_LONG + LEN_SHORT)
                ? at / LEN_LONG
                : 4 + (at - LAST_LONG) / LEN_SHORT;
    }
}
