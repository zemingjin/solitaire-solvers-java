package org.solitaire.pyramid;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.SolveExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class Pyramid extends SolveExecutor<PyramidState> {
    public static final String KING = "K";
    public static final String ACE = "A";
    private Function<PyramidState, PyramidState> cloner = PyramidState::new;

    public Pyramid(PyramidState state) {
        super(state);
        stateConsumer(this::solve);
    }

    protected void solve(PyramidState state) {
        if (state.isCleared()) {
            solutions().add(state.path());
        } else {
            Optional.of(state.findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .map(it -> applyCandidates(it, state))
                    .map(super::addAll)
                    .orElseGet(() -> drawDeck(state));
        }
    }

    protected List<PyramidState> applyCandidates(List<Card[]> candidates, PyramidState state) {
        return candidates.stream()
                .map(it -> cloner.apply(state).updateState(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected boolean drawDeck(PyramidState state) {
        return Optional.ofNullable(state.drawDeckCards())
                .map(super::add)
                .orElse(false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return results.stream()
                .map(it -> (List<Card[]>) it)
                .map(PyramidHelper::getScore)
                .reduce(Pair.of(0, null), (a, b) -> a.getLeft() >= b.getLeft() ? a : b);
    }

    public Pyramid cloner(Function<PyramidState, PyramidState> cloner) {
        this.cloner = cloner;
        return this;
    }
}