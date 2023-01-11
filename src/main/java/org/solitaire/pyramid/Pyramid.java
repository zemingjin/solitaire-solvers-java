package org.solitaire.pyramid;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class Pyramid implements GameSolver {
    public static final String KING = "K";
    public static final String ACE = "A";
    private static int totalScenarios;
    private final List<List> solutions = new ArrayList<>();
    private PyramidState initState;
    private Function<PyramidState, PyramidState> cloner = PyramidState::new;

    public Pyramid(Card[] cards, Stack<Card> deck) {
        initState = new PyramidState(cards, deck, new Stack<>(), new Path<>(), 3);
        totalScenarios = 0;
    }

    @Override
    public List<List> solve() {
        solve(initState);
        return solutions();
    }

    protected void solve(PyramidState state) {
        if (state.isCleared()) {
            solutions.add(state.path());
        } else {
            totalScenarios++;
            Optional.of(state.findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresentOrElse(it -> applyCandidates(it, state), () -> drawDeck(state));
        }
    }

    protected void applyCandidates(List<Card[]> candidates, PyramidState state) {
        candidates.stream()
                .map(it -> cloner.apply(state).updateState(it))
                .filter(Objects::nonNull)
                .forEach(this::solve);
    }

    protected void drawDeck(PyramidState state) {
        Optional.ofNullable(state.drawDeckCards())
                .ifPresent(this::solve);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return results.stream()
                .map(it -> (List<Card[]>) it)
                .map(PyramidHelper::getScore)
                .reduce(Pair.of(0, null), (a, b) -> a.getLeft() >= b.getLeft() ? a : b);
    }

    protected PyramidState initState() {
        return initState;
    }

    protected void initState(PyramidState initState) {
        this.initState = initState;
    }

    public List<List> solutions() {
        return solutions;
    }

    @Override
    public int totalScenarios() {
        return totalScenarios;
    }

    public Pyramid cloner(Function<PyramidState, PyramidState> cloner) {
        this.cloner = cloner;
        return this;
    }
}