package org.solitaire.spider;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.List;

public class Spider extends SolveExecutor<String, Candidate, SpiderBoard> {
    protected static final int SOLUTION_LIMIT = 1000;

    public Spider(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(new SpiderBoard(columns, path, totalScore, deck), SpiderBoard::new);
        isReducingBoards(true);
    }

    @Override
    public boolean isContinuing() {
        return super.isContinuing() && solutions().size() < SOLUTION_LIMIT;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return Pair.of(0, new Path<>());
    }
}
