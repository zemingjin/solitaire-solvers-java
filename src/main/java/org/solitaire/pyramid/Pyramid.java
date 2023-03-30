package org.solitaire.pyramid;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.execution.SolveExecutor;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.util.MaxScore;

import java.util.List;

public class Pyramid extends SolveExecutor<Card[], Candidate, PyramidBoard> {
    public static final String KING = "K";
    public static final String ACE = "A";

    private MaxScore maxScore;

    public Pyramid(PyramidBoard board) {
        super(board, PyramidBoard::new);
        addSolutionConsumer(this::solutionConsumer);
        board.updateBoard(board.drawDeckCard());
    }

    private void solutionConsumer(List<Card[]> path) {
        if (maxScore == null) {
            maxScore = new MaxScore(PyramidHelper::getScore);
        }
        maxScore.score(path);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> maxScore() {
        return maxScore.maxScore();
    }

    @SuppressWarnings("rawtypes unchecked")
    @Override
    public String pathString(List path) {
        return PyramidBoard.toString((List<Card[]>) path);
    }
}