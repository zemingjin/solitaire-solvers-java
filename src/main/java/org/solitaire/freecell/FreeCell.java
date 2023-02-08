package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Heineman’s Staged Deepening (HSD)
 * Properties:
 * - there are usually multiple ways to arrive at the solution for a given FreeCell deal
 * - there are many cases where moving a card is irreversible, such as a card is moved to the foundation
 */
@SuppressWarnings("rawtypes")
public class FreeCell extends SolveExecutor<FreeCellState> {
    private Function<FreeCellState, FreeCellState> cloner = FreeCellState::new;

    public FreeCell(Columns columns) {
        super(new FreeCellState(columns, new Path<>(), new Card[4], new Card[4]));
        stateConsumer(this::solve);
    }

    protected void solve(FreeCellState state) {
        if (state.isCleared()) {
            solutions().add(state.path());
        } else {
            Optional.of(state)
                    .map(FreeCellState::findCandidates)
                    .filter(ObjectUtils::isNotEmpty)
                    .map(it -> applyCandidates(it, state))
                    .map(this::scoreStates)
                    .map(this::sortStates)
                    .ifPresent(super::addAll);
        }
    }

    protected List<FreeCellState> applyCandidates(List<Candidate> candidates, FreeCellState state) {
        return candidates.stream()
                .map(it -> cloner.apply(state).updateState(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected List<FreeCellState> scoreStates(List<FreeCellState> list) {
        list.forEach(FreeCellState::score);
        return list;
    }

    protected List<FreeCellState> sortStates(List<FreeCellState> list) {
        list.sort((a, b) -> Double.compare(b.score(), a.score()));
        return list;
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }

    public void cloner(Function<FreeCellState, FreeCellState> cloner) {
        this.cloner = cloner;
    }
}