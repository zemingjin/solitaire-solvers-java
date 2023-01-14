package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Heineman’s Staged Deepening (HSD)
 * Properties:
 * - there are usually multiple ways to arrive at the solution for a given FreeCell deal
 * - there are many cases where moving a card is irreversible, such as a card is moved to the foundation
 */
@SuppressWarnings("rawtypes")
public class FreeCell implements GameSolver {
    private final List<List> solutions = new ArrayList<>();
    private FreeCellState initState;
    private int totalScenarios;
    private Function<FreeCellState, FreeCellState> cloner = FreeCellState::new;

    public FreeCell(Columns columns) {
        initState = new FreeCellState(columns, new Path<>(), new Card[4], new Card[4]);
    }

    @Override
    public List<List> solve() {
        solve(initState);
        return solutions();
    }

    protected void solve(FreeCellState state) {
        if (state.isCleared()) {
            solutions.add(state.path());
        } else {
            totalScenarios++;
            Optional.of(state)
                    .map(FreeCellState::findCandidates)
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresent(it -> applyCandidates(it, state));
        }
    }

    protected void applyCandidates(List<Candidate> candidates, FreeCellState state) {
        candidates.stream()
                .map(it -> cloner.apply(state).updateState(it))
                .filter(Objects::nonNull)
                .forEach(this::solve);
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }

    @Override
    public int totalScenarios() {
        return totalScenarios;
    }

    public FreeCellState initState() {
        return initState;
    }

    public FreeCell initState(FreeCellState initState) {
        this.initState = initState;
        return this;
    }

    public List<List> solutions() {
        return solutions;
    }

    public void cloner(Function<FreeCellState, FreeCellState> cloner) {
        this.cloner = cloner;
    }
}