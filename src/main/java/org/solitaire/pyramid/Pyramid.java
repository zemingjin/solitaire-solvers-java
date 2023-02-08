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
public class Pyramid extends SolveExecutor<PyramidBoard> {
    public static final String KING = "K";
    public static final String ACE = "A";
    private final Function<PyramidBoard, PyramidBoard> cloner = PyramidBoard::new;

    public Pyramid(PyramidBoard board) {
        super(board);
        solveBoard(this::solve);
    }

    protected void solve(PyramidBoard board) {
        Optional.of(board.findCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .filter(ObjectUtils::isNotEmpty)
                .map(this::scoreStates)
                .ifPresentOrElse(super::addAll, () -> drawDeck(board));
    }

    private List<PyramidBoard> scoreStates(List<PyramidBoard> boards) {
        boards.forEach(PyramidBoard::score);
        boards.sort((a, b) -> Double.compare(b.score(), a.score()));
        return boards;
    }

    protected List<PyramidBoard> applyCandidates(List<Card[]> candidates, PyramidBoard board) {
        return candidates.stream()
                .map(it -> cloner.apply(board).updateBoard(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected void drawDeck(PyramidBoard board) {
        Optional.ofNullable(board.drawDeckCards())
                .ifPresent(super::add);
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