package org.solitaire.pyramid;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
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
        super(board);
        solveBoard(this::solve);
        cloner(PyramidBoard::new);
    }

    protected void solve(PyramidBoard board) {
        Optional.of(board.findCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .filter(ObjectUtils::isNotEmpty)
                .ifPresentOrElse(super::addBoards, () -> drawDeck(board));
    }

    protected List<PyramidBoard> applyCandidates(List<Card[]> candidates, PyramidBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull)
                .toList();
    }

    protected void drawDeck(PyramidBoard board) {
        Optional.ofNullable(board.drawDeckCards())
                .ifPresent(super::addBoard);
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