package org.solitaire.freecell;

import org.solitaire.model.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static org.solitaire.model.CardHelper.buildCard;
import static org.solitaire.model.CardHelper.cloneArray;
import static org.solitaire.model.CardHelper.cloneList;

public class FreeCellHelper {
    private static final int LAST_LONG = 28;
    private static final int LAST_BOARD = 52;
    private static final int LEN_LONG = 7;
    private static final int LEN_SHORT = 6;

    public static FreeCell cloneGame(FreeCell game) {
        return FreeCell.builder()
                .board(cloneBoard(game.getBoard()))
                .foundation(cloneArray(game.getFoundation()))
                .freeCells(cloneArray(game.getFreeCells()))
                .path(cloneList(game.getPath()))
                .build();
    }

    private static List<List<Card>> cloneBoard(List<List<Card>> board) {
        return board.stream()
                .map(it -> (List<Card>) new LinkedList<>(it))
                .toList();
    }

    public static FreeCell build(String[] cards) {
        return FreeCell.builder()
                .board(buildBoard(cards))
                .freeCells(new Card[4])
                .foundation(new Card[4])
                .build();
    }

    private static List<List<Card>> buildBoard(String[] cards) {
        assert cards != null && cards.length == LAST_BOARD : "Invalid source cards: " + Arrays.toString(cards);

        var board = new ArrayList<List<Card>>(8);
        IntStream.range(0, 8).forEach(i -> board.add(i, new LinkedList<>()));

        IntStream.range(0, cards.length)
                .mapToObj(i -> buildCard(i, cards[i]))
                .forEach(it -> setCardToBoard(board, it));
        return board;
    }

    private static void setCardToBoard(List<List<Card>> board, Card card) {
        var column = getColumn(card.at());
        var col = board.get(column);

        if (col == null) {
            col = new LinkedList<>();
            board.set(column, col);
        }
        col.add(card);
    }

    private static int getColumn(int at) {
        return (at < LAST_LONG)
                ? at / LEN_LONG
                : (at - LAST_LONG) / LEN_SHORT + 4;
    }

}
