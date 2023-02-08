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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("rawtypes")
public class Klondike extends SolveExecutor<KlondikeBoard> {
    protected static final int LIMIT_SOLUTIONS = 1000;
    private Function<KlondikeBoard, KlondikeBoard> cloner = KlondikeBoard::new;

    public Klondike(Columns columns,
                    Deck deck,
                    List<Stack<Card>> foundations) {
        super(new KlondikeBoard(columns, new Path<>(), 0, deck, new Stack<>(), foundations, true));
        solveBoard(this::solve);
    }

    protected void solve(KlondikeBoard board) {
        if (solutions().size() < LIMIT_SOLUTIONS) {
            Optional.of(board.findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .map(it -> applyCandidates(it, board))
                    .filter(it -> !it.isEmpty())
                    .ifPresentOrElse(super::addAll, () -> drawDeck(board));
        }
    }

    protected void drawDeck(KlondikeBoard state) {
        Optional.ofNullable(state.drawDeckCards())
                .ifPresent(super::add);
    }

    protected List<KlondikeBoard> applyCandidates(List<Candidate> candidates, KlondikeBoard board) {
        return candidates.stream()
                .map(it -> cloner.apply(board).updateBoard(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return null;
    }

    public void cloner(Function<KlondikeBoard, KlondikeBoard> cloner) {
        this.cloner = cloner;
    }
}