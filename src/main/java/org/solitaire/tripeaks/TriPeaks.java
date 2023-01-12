package org.solitaire.tripeaks;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.rangeClosed;
import static org.solitaire.tripeaks.TriPeaksHelper.isFromDeck;

@SuppressWarnings("rawtypes")
public class TriPeaks implements GameSolver {
    private static final int BOARD_BONUS = 5000;
    private final List<List> solutions = new ArrayList<>();
    private int totalScenarios;
    private final TriPeaksState initState;

    public TriPeaks(Card[] cards, Stack<Card> wastePile) {
        initState = new TriPeaksState(cards, wastePile);
    }

    private static void removeDeckCardsAtEnd(Stack<Card> cards) {
        while (isFromDeck(cards.peek())) {
            cards.pop();
        }
    }

    @Override
    public List<List> solve() {
        solve(initState);
        return solutions;
    }

    private void solve(TriPeaksState state) {
        if (state.isCleared()) {
            removeDeckCardsAtEnd(state.wastePile);
            solutions.add(state.wastePile);
        } else {
            totalScenarios++;
            Optional.of(state.findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresentOrElse(it -> applyCandidates(it, state), () -> drawDeck(state));
        }
    }

    private void drawDeck(TriPeaksState state) {
        Optional.ofNullable(state.getTopDeckCard())
                .map(state::updateState)
                .ifPresent(this::solve);
    }

    private void applyCandidates(List<Card> cards, TriPeaksState state) {
        cards.stream()
                .map(it -> new TriPeaksState(state).updateState(it))
                .forEach(this::solve);
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

    @Override
    public int totalScenarios() {
        return totalScenarios;
    }

    public TriPeaksState initState() {
        return initState;
    }
}
