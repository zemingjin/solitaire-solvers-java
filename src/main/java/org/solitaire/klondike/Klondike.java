package org.solitaire.klondike;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Stack;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
public class Klondike extends SolveExecutor<KlondikeBoard> {
    protected static final int SOLUTION_LIMIT = 1000;

    public Klondike(Columns columns,
                    Deck deck,
                    List<Stack<Card>> foundations) {
        super(new KlondikeBoard(columns, new Path<>(), 0, deck, new Stack<>(), foundations, true),
                KlondikeBoard::new);
        solveBoard(singleSolution() ? this::solveByHSD : this::solveByDFS);
    }

    @Override
    public boolean isContinuing() {
        return super.isContinuing() && solutions().size() < SOLUTION_LIMIT;
    }

    protected void solveByDFS(KlondikeBoard board) {
        Optional.of(board.findCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .map(Stream::toList)
                .filter(ObjectUtils::isNotEmpty)
                .ifPresent(this::addBoards);
    }

    protected void solveByHSD(KlondikeBoard board) {
        var boards = List.of(board);

        for (int i = 1; i <= hsdDepth() && isNotEmpty(boards); i++) {
            boards = boards.stream().flatMap(this::search).toList();
        }
        Optional.of(boards)
                .filter(ObjectUtils::isNotEmpty)
                .map(List::stream)
                .map(it -> it.sorted(comparingInt(KlondikeBoard::score)))
                .map(this::getBestBoard)
                .ifPresent(super::addBoard);
    }

    private Stream<KlondikeBoard> search(KlondikeBoard board) {
        return Optional.of(board)
                .map(KlondikeBoard::findCandidates)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .stream()
                .flatMap(it -> it);
    }

    protected Stream<KlondikeBoard> applyCandidates(List<Candidate> candidates, KlondikeBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return null;
    }
}