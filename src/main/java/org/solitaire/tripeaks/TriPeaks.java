package org.solitaire.tripeaks;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.execution.SolveExecutor;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.util.MaxScore;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.isFromDeck;

public class TriPeaks extends SolveExecutor<Card, Card, TriPeaksBoard> {
    private static final int BOARD_BONUS = 5000;

    private final MaxScore maxScore = new MaxScore(this::getScore);

    public TriPeaks(Card[] cards, Column wastePile) {
        super(new TriPeaksBoard(cards, wastePile), TriPeaksBoard::new);
        addSolutionConsumer(this::solutionConsumer);
    }

    protected void solutionConsumer(List<Card> path) {
        maxScore.score(path);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> maxScore() {
        return maxScore.maxScore();
    }

    @SuppressWarnings("rawtypes unchecked")
    @Override
    public String pathString(List path) {
        return ((List<Card>) path).stream()
                .map(it -> (LAST_BOARD <= it.at() ? "^" : "") + it.raw())
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("rawtypes")
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

            return (num < 3) ? 500 * num : BOARD_BONUS;
        }
        return 0;
    }

    private int numOfPeeksCleared(Card card, List<Card> list) {
        return (int) rangeClosed(0, list.indexOf(card))
                .mapToObj(list::get)
                .filter(this::isPeakCard)
                .count();
    }

    private boolean isPeakCard(Card card) {
        var at = card.at();
        return 0 <= at && at < 3;
    }
}
