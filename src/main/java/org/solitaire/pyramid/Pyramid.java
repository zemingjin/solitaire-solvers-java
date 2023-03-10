package org.solitaire.pyramid;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.SolveExecutor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public class Pyramid extends SolveExecutor<PyramidBoard> {
    public static final String KING = "K";
    public static final String ACE = "A";

    public Pyramid(PyramidBoard board) {
        super(board, PyramidBoard::new);
        solveBoard(this::solve);
    }

    protected void solve(PyramidBoard board) {
        Optional.of(board.findCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .filter(ObjectUtils::isNotEmpty)
                .ifPresent(super::addBoards);
    }

    protected List<PyramidBoard> applyCandidates(List<Candidate> candidates, PyramidBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull)
                .toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return results.stream()
                .map(it -> (List<Card[]>) it)
                .map(PyramidHelper::getScore)
                .reduce(Pair.of(0, null), (a, b) -> a.getLeft() >= b.getLeft() ? a : b);
    }
}