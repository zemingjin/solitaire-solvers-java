package org.solitaire.spider;

import org.solitaire.model.Candidate;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.execution.SolveExecutor;

public class Spider extends SolveExecutor<String, Candidate, SpiderBoard> {
    protected static final int SOLUTION_LIMIT = 1000;

    public Spider(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(new SpiderBoard(columns, path, totalScore, deck), SpiderBoard::new);
        singleSolution(true);
        hsdDepth(6);
    }

    @Override
    public boolean isContinuing() {
        return super.isContinuing() && totalSolutions() < SOLUTION_LIMIT;
    }
}
