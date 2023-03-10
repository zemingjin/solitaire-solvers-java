package org.solitaire.spider;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class Spider extends SolveExecutor<SpiderBoard> {
    protected static final int SOLUTION_LIMIT = 1000;

    public Spider(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(new SpiderBoard(columns, path, totalScore, deck), SpiderBoard::new);
        solveBoard(singleSolution() ? this::solveByHSD : this::solveByDSF);
    }

    protected void solveByDSF(SpiderBoard board) {
        Optional.of(board.findCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .map(Stream::toList)
                .filter(ObjectUtils::isNotEmpty)
                .ifPresentOrElse(this::addBoards, () -> drawDeck(board));
    }

    protected void solveByHSD(SpiderBoard board) {
        var boards = List.of(board);

        for (int i = 1; i <= hsdDepth() && isNotEmpty(boards); i++) {
            boards = boards.stream().flatMap(this::search).toList();
        }
        Optional.of(boards)
                .filter(ObjectUtils::isNotEmpty)
                .map(List::stream)
                .map(it -> it.sorted(comparingInt(SpiderBoard::score)))
                .map(this::getBestBoard)
                .ifPresentOrElse(this::addBoard, () -> drawDeck(board));
    }

    private Stream<SpiderBoard> search(SpiderBoard board) {
        return Optional.of(board)
                .map(SpiderBoard::findCandidates)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .stream()
                .flatMap(it -> it)
                .filter(this::checkBoard);
    }

    @Override
    public boolean isContinuing() {
        return super.isContinuing() && solutions().size() < SOLUTION_LIMIT;
    }

    protected Stream<SpiderBoard> applyCandidates(List<Candidate> candidates, SpiderBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull);
    }

    protected void drawDeck(SpiderBoard board) {
        if (board.drawDeck()) {
            addBoard(board);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return Pair.of(0, new Path<>());
    }
}
