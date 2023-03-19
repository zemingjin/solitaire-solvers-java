package org.solitaire.klondike;

import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.solitaire.util.CardHelper.buildCard;

public class KlondikeHelper {
    protected static final int LAST_DECK = 24;
    protected static final int NUM_COLUMNS = 7;

    public static Klondike build(String[] cards) {
        return new Klondike(
                buildColumns(cards),
                buildDeck(cards),
                buildFoundation());
    }

    private static List<Stack<Card>> buildFoundation() {
        return range(0, 4)
                .mapToObj(i -> new Stack<Card>())
                .toList();
    }

    private static Deck buildDeck(String[] cards) {
        return range(0, LAST_DECK)
                .mapToObj(i -> buildCard(i, cards[i]))
                .collect(Collectors.toCollection(Deck::new));
    }

    private static Columns buildColumns(String[] cards) {
        return range(0, NUM_COLUMNS)
                .mapToObj(i -> buildColumnCards(i, cards))
                .collect(Collectors.toCollection(Columns::new));
    }

    private static Column buildColumnCards(int col, String[] cards) {
        var colEnd = colEnd(col) - 1;
        var column = rangeClosed(colStart(col), colEnd)
                .mapToObj(i -> buildCard(i, cards[i]))
                .collect(Collectors.toCollection(Column::new));
        column.openAt(column.size() - 1);
        return column;
    }

    protected static int colStart(int col) {
        return LAST_DECK + calcStart(col);
    }

    private static int calcStart(int col) {
        return col == 0 ? 0 : rangeClosed(0, col).reduce(0, Integer::sum);
    }

    protected static int colEnd(int col) {
        return colStart(col) + col + 1;
    }

    private KlondikeHelper() {}

    public static KlondikeHelper of() {
        return new KlondikeHelper();
    }
}
