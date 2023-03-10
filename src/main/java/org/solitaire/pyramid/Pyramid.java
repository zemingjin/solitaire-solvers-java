package org.solitaire.pyramid;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.SolveExecutor;

import java.util.List;

public class Pyramid extends SolveExecutor<Card[], Candidate, PyramidBoard> {
    public static final String KING = "K";
    public static final String ACE = "A";

    public Pyramid(PyramidBoard board) {
        super(board, PyramidBoard::new);
    }

    @SuppressWarnings("unchecked rawtypes")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return results.stream()
                .map(it -> (List<Card[]>) it)
                .map(PyramidHelper::getScore)
                .reduce(Pair.of(0, null), (a, b) -> a.getLeft() >= b.getLeft() ? a : b);
    }
}