package org.solitaire.spider;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.solitaire.util.SolitaireHelper.incTotal;

@SuppressWarnings("rawtypes")
public class Spider extends SpiderState implements GameSolver {
    protected static final int SOLUTION_LIMIT = 1000;

    protected final List<List> solutions = new ArrayList<>();
    protected final SpiderState state;
    private final Function<SpiderState, SpiderState> cloner = SpiderState::new;

    public Spider(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(columns, path, totalScore, deck);
        state = this;
    }

    @Override
    public List<List> solve() {
        solve(state);
        return solutions;
    }

    protected void solve(SpiderState state) {
        if (solutions.size() < SOLUTION_LIMIT) {
            if (state.isCleared()) {
                solutions.add(state.getPath());
            } else {
                incTotal();
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
}
