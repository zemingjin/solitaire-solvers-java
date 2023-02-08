package org.solitaire.tripeaks;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.SolveExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.rangeClosed;
import static org.solitaire.tripeaks.TriPeaksHelper.isFromDeck;

@SuppressWarnings("rawtypes")
public class TriPeaks extends SolveExecutor<TriPeaksBoard> {
    private static final int BOARD_BONUS = 5000;
    private final Function<TriPeaksBoard, TriPeaksBoard> cloner = TriPeaksBoard::new;

    public TriPeaks(Card[] cards, Stack<Card> wastePile) {
        super(new TriPeaksBoard(cards, wastePile));
        solveBoard(this::solve);
    }

    private void solve(TriPeaksBoard board) {
        Optional.of(board.findCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .filter(ObjectUtils::isNotEmpty)
                .map(this::scoreStates)
                .ifPresentOrElse(super::addAll, () -> drawDeck(board));
    }

    private List<TriPeaksBoard> scoreStates(List<TriPeaksBoard> boards) {
        boards.forEach(TriPeaksBoard::score);
        boards.sort((a, b) -> Double.compare(b.score(), a.score()));
        return boards;
    }

    private List<TriPeaksBoard> applyCandidates(List<Card> candidates, TriPeaksBoard board) {
        return candidates.stream()
                .map(it -> cloner.apply(board).updateBoard(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void drawDeck(TriPeaksBoard board) {
        Optional.ofNullable(board.getTopDeckCard())
                .map(board::updateBoard)
                .ifPresent(super::add);
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
            if (isFromDeck(card)) {
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
                return BOARD_BONUS;
            }
        }
        return 0;
    }

    private int numOfPeeksCleared(Card card, List<Card> list) {
        return (int) rangeClosed(0, list.indexOf(card))
                .mapToObj(list::get)
                .filter(it -> it.at() < 3)
                .count();
    }

    private boolean isPeakCard(Card card) {
        var at = card.at();
        return 0 <= at && at < 3;
    }
}
