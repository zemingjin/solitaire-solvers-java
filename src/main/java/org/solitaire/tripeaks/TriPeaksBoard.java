package org.solitaire.tripeaks;

import lombok.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.solitaire.model.CardHelper.cloneArray;
import static org.solitaire.model.CardHelper.cloneList;
import static org.solitaire.model.CardHelper.incTotal;
import static org.solitaire.model.CardHelper.isCleared;
import static org.solitaire.tripeaks.TriPeaksHelper.INI_COVERED;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_DECK;

@SuppressWarnings("rawtypes")
@Builder
public class TriPeaksBoard implements GameSolver {
    private final IntUnaryOperator reverse = i -> LAST_BOARD + LAST_DECK - i - 1;
    private Card[] cards;
    private List<Card> wastePile;

    public List<List> solve() {
        if (isCleared(cards, LAST_BOARD)) {
            return Collections.singletonList(wastePile);
        }
        incTotal();
        return Optional.of(findBoardCards())
                .filter(it -> !it.isEmpty())
                .map(this::clickBoardCards)
                .orElseGet(this::clickDeckCard);
    }

    private List<List> clickDeckCard() {
        return Optional.ofNullable(getTopDeckCard())
                .map(this::clickCard)
                .orElseGet(Collections::emptyList);
    }

    private List<List> clickBoardCards(List<Card> cards) {
        return cards.stream()
                .map(this::clickCard)
                .flatMap(List::stream)
                .toList();
    }

    protected List findBoardCards() {
        return Optional.of(wastePile.get(wastePile.size() - 1))
                .map(this::findAdjacentCardsFromBoard)
                .orElseGet(Collections::emptyList);
    }

    private List<Card> findAdjacentCardsFromBoard(Card target) {
        return stream(cards, 0, min(cards.length, LAST_BOARD))
                .filter(Objects::nonNull)
                .filter(this::isOpenCard)
                .filter(target::isAdjacent)
                .toList();
    }

    private Card getTopDeckCard() {
        return IntStream.range(LAST_BOARD, LAST_DECK)
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
                score += (sequenceCount * 2 - 1) * 100;
            }
        }
        return Pair.of(score, cards);
    }

    private TriPeaksBoard cloneBoard() {
        return TriPeaksBoard.builder()
                .cards(cloneArray(cards))
                .wastePile(cloneList(wastePile))
                .build();
    }

    private TriPeaksBoard click(Card card) {
        cards[card.at()] = null;
        wastePile.add(card);
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
}
