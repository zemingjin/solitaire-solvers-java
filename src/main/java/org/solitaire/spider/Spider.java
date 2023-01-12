package org.solitaire.spider;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class Spider implements GameSolver {
    protected static final int SOLUTION_LIMIT = 1000;
    private static int totalScenarios;

    protected final List<List> solutions = new ArrayList<>();
    private final Function<SpiderState, SpiderState> cloner = SpiderState::new;
    private final SpiderState initState;

    public Spider(Columns columns, Path<Card[]> path, int totalScore, Deck deck) {
        initState = new SpiderState(columns, path, totalScore, deck);
    }

    @Override
    public List<List> solve() {
        solve(initState);
        return solutions;
    }

    protected void solve(SpiderState state) {
        if (solutions.size() < SOLUTION_LIMIT) {
            if (state.isCleared()) {
                solutions.add(state.path());
            } else {
                totalScenarios++;
                Optional.of(state.findCandidates())
                        .filter(ObjectUtils::isNotEmpty)
                        .ifPresentOrElse(it -> applyCandidates(it, state), () -> drawDeck(state));
            }
        }
    }

    protected void applyCandidates(List<Candidate> candidates, SpiderState state) {
        candidates.stream()
                .map(it -> cloneState(state).updateState(it))
                .filter(Objects::nonNull)
                .forEach(this::solve);
    }

    protected void drawDeck(SpiderState state) {
        if (state.drawDeck()) {
            solve(state);
        }
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return Pair.of(0, new Path<>());
    }

    private SpiderState cloneState(SpiderState state) {
        return cloner.apply(state);
    }

    public SpiderState getInitState() {
        return initState;
    }

    @Override
    public int totalScenarios() {
        return totalScenarios;
    }

}
