package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

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
public class FreeCell extends SolveExecutor<FreeCellBoard> {
    private Function<FreeCellBoard, FreeCellBoard> cloner = FreeCellBoard::new;

    public FreeCell(Columns columns) {
        super(new FreeCellBoard(columns, new Path<>(), new Card[4], new Card[4]));
        solveBoard(this::solve);
    }

    protected void solve(FreeCellBoard state) {
        Optional.of(state)
                .map(FreeCellBoard::findCandidates)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, state))
                .filter(ObjectUtils::isNotEmpty)
                .map(this::scoreStates)
                .ifPresent(super::addAll);
    }

    protected List<FreeCellBoard> applyCandidates(List<Candidate> candidates, FreeCellBoard state) {
        return candidates.stream()
                .map(it -> cloner.apply(state).updateBoard(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @return the list of States sorted by their scores in descending order.
     */
    protected List<FreeCellBoard> scoreStates(List<FreeCellBoard> list) {
        list.forEach(FreeCellBoard::score);
        list.sort((a, b) -> Double.compare(b.score(), a.score()));
        return list;
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }

    public void cloner(Function<FreeCellBoard, FreeCellBoard> cloner) {
        this.cloner = cloner;
    }
}