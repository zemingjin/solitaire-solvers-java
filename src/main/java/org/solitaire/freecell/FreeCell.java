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

/**
 * Heineman’s Staged Deepening (HSD)
 * Properties:
 * - there are usually multiple ways to arrive at the solution for a given FreeCell deal
 * - there are many cases where moving a card is irreversible, such as a card is moved to the foundation
 */
@SuppressWarnings("rawtypes")
public class FreeCell extends SolveExecutor<FreeCellBoard> {
    public FreeCell(Columns columns) {
        super(new FreeCellBoard(columns, new Path<>(), new Card[4], new Card[4]), FreeCellBoard::new);
        solveBoard(this::solve);
    }

    protected void solve(FreeCellBoard board) {
        Optional.of(board)
                .map(FreeCellBoard::findCandidates)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .filter(ObjectUtils::isNotEmpty)
                .ifPresent(this::addBoards);
    }

    protected List<FreeCellBoard> applyCandidates(List<Candidate> candidates, FreeCellBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull)
                .map(FreeCellBoard::checkFoundationCandidates)
                .toList();
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }
}