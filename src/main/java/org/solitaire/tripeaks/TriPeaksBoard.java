package org.solitaire.tripeaks;

import org.solitaire.model.Board;
import org.solitaire.model.Card;
import org.solitaire.util.CardHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.solitaire.tripeaks.TriPeaksHelper.INI_COVERED;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_DECK;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.cloneStack;

public class TriPeaksBoard implements Board<Card, Card> {
    private static final int C = LAST_BOARD + LAST_DECK - 1;
    private final IntUnaryOperator reverse = i -> C - i;
    private final IntUnaryOperator reverseBoard = i -> LAST_BOARD - i - 1;

    private Card[] cards;
    private Stack<Card> wastePile;

    public TriPeaksBoard(Card[] cards, Stack<Card> wastePile) {
        cards(cards);
        wastePile(wastePile);
    }

    protected TriPeaksBoard(TriPeaksBoard that) {
        this(cloneArray(that.cards), cloneStack(that.wastePile));
    }

    @Override
    public boolean isSolved() {
        return CardHelper.isCleared(cards, 0, LAST_BOARD);
    }

    @Override
    public List<Card> path() {
        return wastePile;
    }

    /************************************************************************************************************
     * Search
     ***********************************************************************************************************/
    @Override
    public List<Card> findCandidates() {
        var candidates = Optional.of(wastePile.peek())
                .map(this::findAdjacentCardsFromBoard)
                .orElseGet(Collections::emptyList);
        if (candidates.isEmpty()) {
            var deckCard = getTopDeckCard();

            if (nonNull(deckCard)) {
                candidates.add(deckCard);
            }
        }
        return candidates;
    }

    private List<Card> findAdjacentCardsFromBoard(Card target) {
        return range(0, min(cards.length, LAST_BOARD))
                .map(reverseBoard)
                .mapToObj(i -> cards[i])
                .filter(Objects::nonNull)
                .filter(this::isOpenCard)
                .filter(target::isAdjacent)
                .collect(Collectors.toList());
    }

    private Card getTopDeckCard() {
        return rangeClosed(LAST_BOARD, LAST_DECK - 1)
                .map(reverse)
                .mapToObj(i -> cards[i])
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /************************************************************************************************************
     * Update
     ***********************************************************************************************************/
    @Override
    public TriPeaksBoard updateBoard(Card card) {
        if (nonNull(card)) {
            cards[card.at()] = null;
            wastePile.push(card);
            return this;
        }
        return null;
    }

    protected boolean isOpenCard(Card card) {
        var at = card.at();

        return switch (row(at)) {
            case 4 -> true;
            case 3 -> isNotCovered(at + 9);
            case 2 -> isNotCovered(at + (at - 3) / 2 + 6);
            case 1 -> isNotCovered(at * 2 + 3);
            default -> throw new RuntimeException("Invalid card: " + card);
        };
    }

    private int row(int at) {
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

    private boolean isNotCovered(int at) {
        return isNull(cards[at]) && isNull(cards[at + 1]);
    }

    public Card[] cards() {
        return cards;
    }

    public void cards(Card[] cards) {
        this.cards = cards;
    }

    public Stack<Card> wastePile() {
        return wastePile;
    }

    public void wastePile(Stack<Card> wastePile) {
        this.wastePile = wastePile;
    }

    @Override
    public List<String> verify() {
        return verifyBoard(allCards());
    }

    private Card[] allCards() {
        return Stream.concat(Stream.of(cards), wastePile.stream()).toArray(Card[]::new);
    }

    /***************************************************************************************************************
     * Score
     **************************************************************************************************************/
    @Override
    public int score() {
        return wastePile.size();
    }

}
