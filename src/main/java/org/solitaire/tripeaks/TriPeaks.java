package org.solitaire.tripeaks;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.SolveExecutor;
import org.solitaire.util.MaxScore;

import java.util.List;
import java.util.Stack;

import static java.util.stream.IntStream.rangeClosed;
import static org.solitaire.tripeaks.TriPeaksHelper.isFromDeck;

public class TriPeaks extends SolveExecutor<Card, Card, TriPeaksBoard> {
    private static final int BOARD_BONUS = 5000;

    private final MaxScore maxScore = new MaxScore(this::getScore);

    public TriPeaks(Card[] cards, Stack<Card> wastePile) {
        super(new TriPeaksBoard(cards, wastePile), TriPeaksBoard::new);
        addSolutionConsumer(this::solutionConsumer);
    }

    protected void solutionConsumer(List<Card> path) {
        maxScore.score(path);
    }

    @Override
    public void solve() {
        var verify = board().verify();

        if (verify.isEmpty()) {
            super.solve();
        } else {
            throw new RuntimeException(verify.toString());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> maxScore() {
        return maxScore.maxScore();
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
