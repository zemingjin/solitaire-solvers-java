package org.solitaire.spider;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class Spider extends SolveExecutor<SpiderBoard> {
    protected static final int SOLUTION_LIMIT = 1000;

    private final Function<SpiderBoard, SpiderBoard> cloner = SpiderBoard::new;

    public Spider(Columns columns, Path<Card[]> path, int totalScore, Deck deck) {
        super(new SpiderBoard(columns, path, totalScore, deck));
        solveBoard(this::solve);
    }

    protected void solve(SpiderBoard board) {
        if (solutions().size() < SOLUTION_LIMIT) {
            Optional.of(board.findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .map(it -> applyCandidates(it, board))
                    .filter(ObjectUtils::isNotEmpty)
                    .map(this::scoreStates)
                    .ifPresentOrElse(super::addAll, () -> drawDeck(board));
        }
    }

    private List<SpiderBoard> scoreStates(List<SpiderBoard> boards) {
        boards.forEach(SpiderBoard::score);
        boards.sort((a, b) -> Double.compare(b.score(), a.score()));
        return boards;
    }

    protected List<SpiderBoard> applyCandidates(List<Candidate> candidates, SpiderBoard board) {
        return candidates.stream()
                .map(it -> cloner.apply(board).updateBoard(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected void drawDeck(SpiderBoard board) {
        if (board.drawDeck()) {
            add(board);
        }
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return Pair.of(0, new Path<>());
    }
}
