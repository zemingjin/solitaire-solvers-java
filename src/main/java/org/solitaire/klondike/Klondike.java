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

@Slf4j
@SuppressWarnings("rawtypes")
public class Klondike extends SolveExecutor<KlondikeBoard> {
    protected static final int SOLUTION_LIMIT = 1000;

    public Klondike(Columns columns,
                    Deck deck,
                    List<Stack<Card>> foundations) {
        super(new KlondikeBoard(columns, new Path<>(), 0, deck, new Stack<>(), foundations, true),
                KlondikeBoard::new);
        solveBoard(this::solve);
    }

    @Override
    public boolean isContinuing() {
        return super.isContinuing() && solutions().size() < SOLUTION_LIMIT;
    }

    protected void solve(KlondikeBoard board) {
        Optional.of(board.findCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .filter(it -> !it.isEmpty())
                .ifPresentOrElse(super::addBoards, () -> drawDeck(board));
    }

    protected void drawDeck(KlondikeBoard board) {
        Optional.ofNullable(board.drawDeckCards())
                .ifPresent(super::addBoard);
    }

    protected List<KlondikeBoard> applyCandidates(List<Candidate> candidates, KlondikeBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return null;
    }
}