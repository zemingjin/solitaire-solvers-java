package org.solitaire.klondike;

import org.solitaire.model.Card;
import org.solitaire.model.Column;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.model.CardHelper.cloneColumns;
import static org.solitaire.model.CardHelper.cloneList;
import static org.solitaire.model.CardHelper.cloneStack;
import static org.solitaire.model.CardHelper.cloneStacks;

public class KlondikeHelper {
    protected static final int LAST_DECK = 24;
    protected static final int NUM_COLUMNS = 7;

    public static Klondike build(String[] cards) {
        return Klondike.builder()
                .deck(buildDeck(cards))
                .deckPile(new Stack<>())
                .columns(buildColumns(cards))
                .path(new LinkedList<>())
                .foundations(buildFoundation())
                .build();
    }

    public static Klondike clone(Klondike klondike) {
        return Klondike.builder()
                .deck(cloneStack(klondike.getDeck()))
                .deckPile(cloneStack(klondike.getDeckPile()))
                .columns(cloneColumns(klondike.getColumns()))
                .path(cloneList(klondike.getPath()))
                .foundations(cloneStacks(klondike.getFoundations()))
                .totalScore(klondike.getTotalScore())
                .build();
    }

    private static List<Stack<Card>> buildFoundation() {
        return IntStream.range(0, 4)
                .mapToObj(i -> new Stack<Card>())
                .toList();
    }

    private static Stack<Card> buildDeck(String[] cards) {
        return IntStream.range(0, LAST_DECK)
                .mapToObj(i -> buildCard(i, cards[i]))
                .collect(Collectors.toCollection(Stack::new));
    }

    private static List<Column> buildColumns(String[] cards) {
        return IntStream.range(0, NUM_COLUMNS)
                .mapToObj(i -> buildColumnCards(i, cards))
                .toList();
    }

    private static Column buildColumnCards(int col, String[] cards) {
        var colEnd = colEnd(col) - 1;
        var column = IntStream.rangeClosed(colStart(col), colEnd)
                .mapToObj(i -> buildCard(i, cards[i]))
                .collect(Collectors.toCollection(Column::new));
        column.setOpenAt(column.size() - 1);
        return column;
    }

    protected static int colStart(int col) {
        return LAST_DECK + calcStart(col);
    }

    private static int calcStart(int col) {
        return col == 0 ? 0 : IntStream.rangeClosed(0, col).reduce(0, Integer::sum);
    }

    protected static int colEnd(int col) {
        return colStart(col) + col + 1;
    }

    public static Stack<Card> toStack(Card card) {
        var stack = new Stack<Card>();

        stack.push(card);
        return stack;
    }

    protected static void drawDeckCards(Stack<Card> deck, Stack<Card> deckPile, int drawNumber) {
        IntStream.range(0, drawNumber)
                .filter(i -> !deck.isEmpty())
                .forEach(i -> deckPile.push(deck.pop()));
    }
}
