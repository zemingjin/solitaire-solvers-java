package org.solitaire.spider;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class Spider extends SolveExecutor<SpiderState> {
    protected static final int SOLUTION_LIMIT = 1000;

    private final Function<SpiderState, SpiderState> cloner = SpiderState::new;

    public Spider(Columns columns, Path<Card[]> path, int totalScore, Deck deck) {
        super(new SpiderState(columns, path, totalScore, deck));
        stateConsumer(this::solve);
    }

    protected void solve(SpiderState state) {
        if (solutions().size() < SOLUTION_LIMIT) {
            if (state.isCleared()) {
                solutions().add(state.path());
            } else {
                Optional.of(state.findCandidates())
                        .filter(ObjectUtils::isNotEmpty)
                        .map(it -> applyCandidates(it, state))
                        .filter(it -> !it.isEmpty())
                        .map(super::addAll)
                        .orElseGet(() -> drawDeck(state));
            }
        }
    }

    protected List<SpiderState> applyCandidates(List<Candidate> candidates, SpiderState state) {
        return candidates.stream()
                .map(it -> cloneState(state).updateState(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected boolean drawDeck(SpiderState state) {
        if (state.drawDeck()) {
            return add(state);
        }
        return false;
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return Pair.of(0, new Path<>());
    }

    private SpiderState cloneState(SpiderState state) {
        return cloner.apply(state);
    }
}
