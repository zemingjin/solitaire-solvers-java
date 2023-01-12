package org.solitaire.freecell;

import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;

import java.util.Arrays;

import static java.util.stream.IntStream.range;
import static org.solitaire.util.CardHelper.buildCard;

public class FreeCellHelper {
    private static final int LAST_LONG = 28;
    private static final int LAST_BOARD = 52;
    private static final int LEN_LONG = 7;
    private static final int LEN_SHORT = 6;

    public static FreeCell build(String[] cards) {
        return new FreeCell(buildBoard(cards));
    }

    private static Columns buildBoard(String[] cards) {
        assert cards != null && cards.length == LAST_BOARD : "Invalid source cards: " + Arrays.toString(cards);

        var columns = new Columns(8);
        range(0, 8).forEach(i -> columns.add(i, new Column()));

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

    private static int getColumn(int at) {
        return (at < LAST_LONG)
                ? at / LEN_LONG
                : (at - LAST_LONG) / LEN_SHORT + 4;
    }
}
