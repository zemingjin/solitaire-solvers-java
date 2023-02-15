package org.solitaire.spider;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public class Spider extends SolveExecutor<SpiderBoard> {
    protected static final int SOLUTION_LIMIT = 1000;

    public Spider(Columns columns, Path<Card[]> path, int totalScore, Deck deck) {
        super(new SpiderBoard(columns, path, totalScore, deck));
        solveBoard(this::solve);
        cloner(SpiderBoard::new);
    }

    protected void solve(SpiderBoard board) {
        if (solutions().size() < SOLUTION_LIMIT) {
            Optional.of(board.findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .map(it -> applyCandidates(it, board))
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresentOrElse(super::addBoards, () -> drawDeck(board));
        }
    }

    protected List<SpiderBoard> applyCandidates(List<Candidate> candidates, SpiderBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull)
                .toList();
    }

    protected void drawDeck(SpiderBoard board) {
        if (board.drawDeck()) {
            addBoard(board);
        }
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return Pair.of(0, new Path<>());
    }
}
