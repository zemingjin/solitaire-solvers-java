package org.solitaire.pyramid;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.execution.SolveExecutor;
import org.solitaire.util.MaxScore;

import java.util.List;

public class Pyramid extends SolveExecutor<Card[], Candidate, PyramidBoard> {
    public static final String KING = "K";
    public static final String ACE = "A";

    private final MaxScore maxScore = new MaxScore(PyramidHelper::getScore);

    public Pyramid(PyramidBoard board) {
        super(board, PyramidBoard::new);
        addSolutionConsumer(this::solutionConsumer);
        board.updateBoard(board.drawDeckCard());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> maxScore() {
        return maxScore.maxScore();
    }

    @SuppressWarnings("rawtypes")
    private void solutionConsumer(List path) {
        maxScore.score(path);
    }
}