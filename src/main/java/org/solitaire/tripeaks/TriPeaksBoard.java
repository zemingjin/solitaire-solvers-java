package org.solitaire.tripeaks;

import lombok.Builder;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static org.solitaire.model.CardHelper.cloneArray;
import static org.solitaire.model.CardHelper.cloneList;
import static org.solitaire.tripeaks.TriPeaksHelper.INI_COVERED;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_DECK;

@Builder
public class TriPeaksBoard implements GameSolver<List<Card>> {
    private static int limit = MAX_VALUE;
    private static int count;

    public static int getLimit() {
        return limit;
    }

    public static void setLimit(int limit) {
        TriPeaksBoard.limit = limit;
    }

    private Card[] cards;
    private List<Card> wastePile;

    public boolean isCleared() {
        return stream(cards, 0, min(cards.length, LAST_BOARD))
                .filter(Objects::nonNull)
                .findAny()
                .isEmpty();
    }

    public List<List<Card>> solve() {
        if (isCleared()) {
            count++;
            return Collections.singletonList(wastePile);
        }
        if (count < limit) {
            return Optional.of(findBoardCards())
                    .filter(it -> !it.isEmpty())
                    .map(this::clickBoardCards)
                    .orElseGet(this::clickDeckCard);
        }
        return Collections.emptyList();
    }

    private List<List<Card>> clickDeckCard() {
        return Optional.ofNullable(getTopDeckCard())
                .map(this::clickCard)
                .orElseGet(Collections::emptyList);
    }

    private List<List<Card>> clickBoardCards(List<Card> cards) {
        return cards.stream()
                .map(this::clickCard)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    protected List<Card> findBoardCards() {
        return Optional.of(wastePile.get(wastePile.size() - 1))
                .map(this::findAdjacentCardsFromBoard)
                .orElseGet(Collections::emptyList);
    }

    private List<Card> findAdjacentCardsFromBoard(Card target) {
        return stream(cards, 0, min(cards.length, LAST_BOARD))
                .filter(Objects::nonNull)
                .filter(this::isOpenCard)
                .filter(target::isAdjacent)
                .collect(Collectors.toList());
    }

    private Card getTopDeckCard() {
        return IntStream.range(LAST_BOARD, LAST_DECK)
                .map(i -> LAST_BOARD + (LAST_DECK - i - 1))
                .mapToObj(i -> cards[i])
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<List<Card>> clickCard(Card target) {
        return cloneBoard()
                .moveCardToWastePile(target)
                .solve();
    }

    private TriPeaksBoard cloneBoard() {
        return TriPeaksBoard.builder()
                .cards(cloneArray(cards))
                .wastePile(cloneList(wastePile))
                .build();
    }

    private TriPeaksBoard moveCardToWastePile(Card card) {
        wastePile.add(card);
        cards[card.getAt()] = null;
        return this;
    }

    protected boolean isOpenCard(Card card) {
        var at = card.getAt();

        return switch (toRow(at)) {
            case 4 -> true;
            case 3 -> checkBoard(at + 9);
            case 2 -> checkBoard(at + (at - 3) / 2 + 6);
            case 1 -> checkBoard(at * 2 + 3);
            default -> throw new RuntimeException("Invalid card: " + card);
        };
    }

    private int toRow(int at) {
        if (INI_COVERED <= at && at < LAST_BOARD)
            return 4;
        else if (9 <= at && at < INI_COVERED)
            return 3;
        else if (3 <= at && at < 9)
            return 2;
        else if (0 <= at && at < 3)
            return 1;
        return 0;
    }

    private boolean checkBoard(int at) {
        return cards[at] == null && cards[at + 1] == null;
    }
}
