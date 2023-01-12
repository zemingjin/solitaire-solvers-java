package org.solitaire.tripeaks;

import lombok.Builder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.solitaire.tripeaks.TriPeaksHelper.INI_COVERED;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_DECK;
import static org.solitaire.tripeaks.TriPeaksHelper.isFromDeck;
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.cloneStack;
import static org.solitaire.util.CardHelper.isCleared;
import static org.solitaire.util.SolitaireHelper.incTotal;

@SuppressWarnings("rawtypes")
@Builder
public class TriPeaks implements GameSolver {
    private static final int BOARD_BONUS = 5000;
    private static final int C = LAST_BOARD + LAST_DECK - 1;
    private static int totalScenarios;
    private final IntUnaryOperator reverse = i -> C - i;
    private final IntUnaryOperator reverseBoard = i -> LAST_BOARD - i - 1;
    private Card[] cards;
    private Stack<Card> wastePile;

    private static void removeDeckCardsAtEnd(Stack<Card> cards) {
        while (isFromDeck(cards.peek())) {
            cards.pop();
        }
    }

    @Override
    public List<List> solve() {
        if (isCleared(cards)) {
            removeDeckCardsAtEnd(wastePile);
            return List.of(wastePile);
        }
        incTotal();
        return Optional.of(findBoardCards())
                .filter(ObjectUtils::isNotEmpty)
                .map(this::clickBoardCards)
                .orElseGet(this::clickDeckCard);
    }

    private List<List> clickDeckCard() {
        return Optional.ofNullable(getTopDeckCard())
                .map(this::click)
                .map(TriPeaks::solve)
                .orElseGet(Collections::emptyList);
    }

    private List<List> clickBoardCards(List<Card> cards) {
        return cards.stream()
                .map(this::clickCard)
                .flatMap(List::stream)
                .toList();
    }

    protected List findBoardCards() {
        return Optional.of(wastePile.peek())
                .map(this::findAdjacentCardsFromBoard)
                .orElseGet(Collections::emptyList);
    }

    private List<Card> findAdjacentCardsFromBoard(Card target) {
        return IntStream.range(0, min(cards.length, LAST_BOARD))
                .map(reverseBoard)
                .mapToObj(i -> cards[i])
                .filter(Objects::nonNull)
                .filter(this::isOpenCard)
                .filter(target::isAdjacent)
                .toList();
    }

    private Card getTopDeckCard() {
        return IntStream.rangeClosed(LAST_BOARD, LAST_DECK - 1)
                .map(reverse)
                .mapToObj(i -> cards[i])
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public List<List> clickCard(Card target) {
        return cloneBoard()
                .click(target)
                .solve();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        requireNonNull(results);

        return results.stream()
                .map(it -> (List<Card>) it)
                .map(this::getScore)
                .reduce(Pair.of(0, null), (a, b) -> a.getLeft() >= b.getLeft() ? a : b);
    }

    protected Pair<Integer, List> getScore(List<Card> cards) {
        int score = 0;
        int sequenceCount = 0;

        for (Card card : cards) {
            if (TriPeaksHelper.isFromDeck(card)) {
                sequenceCount = 0;
            } else {
                sequenceCount++;
                score += (sequenceCount * 2 - 1) * 100 + checkPeakBonus(card, cards);
            }
        }
        return Pair.of(score, cards);
    }

    protected int checkPeakBonus(Card card, List<Card> list) {
        if (isPeakCard(card)) {
            var num = numOfPeeksCleared(card, list);
            if (num < 3) {
                return 500 * num;
            } else {
                return 5000;
            }
        }
        return 0;
    }

    private int numOfPeeksCleared(Card card, List<Card> list) {
        return (int) IntStream.rangeClosed(0, list.indexOf(card))
                .mapToObj(list::get)
                .filter(it -> it.at() < 3)
                .count();
    }

    private boolean isPeakCard(Card card) {
        var at = card.at();
        return 0 <= at && at < 3;
    }

    private TriPeaks cloneBoard() {
        return TriPeaks.builder()
                .cards(cloneArray(cards))
                .wastePile(cloneStack(wastePile))
                .build();
    }

    private TriPeaks click(Card card) {
        cards[card.at()] = null;
        wastePile.push(card);
        return this;
    }

    protected boolean isOpenCard(Card card) {
        var at = card.at();

        return switch (toRow(at)) {
            case 4 -> true;
            case 3 -> isNotCovered(at + 9);
            case 2 -> isNotCovered(at + (at - 3) / 2 + 6);
            case 1 -> isNotCovered(at * 2 + 3);
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

    private boolean isNotCovered(int at) {
        return isNull(cards[at]) && isNull(cards[at + 1]);
    }

    @Override
    public int totalScenarios() {
        return totalScenarios;
    }
}
