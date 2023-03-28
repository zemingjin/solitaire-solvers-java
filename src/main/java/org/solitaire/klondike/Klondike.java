package org.solitaire.klondike;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.execution.SolveExecutor;
import org.solitaire.model.Candidate;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;

import java.util.List;
import java.util.Optional;

import static org.solitaire.model.Board.listIsNotEmpty;

@Slf4j
public class Klondike extends SolveExecutor<String, Candidate, KlondikeBoard> {
    protected static final int SOLUTION_LIMIT = 1000;
    @SuppressWarnings("rawtypes")
    private Pair<Integer, List> maxScore;

    public Klondike(Columns columns,
                    Deck deck,
                    Columns foundations) {
        super(new KlondikeBoard(columns, new Path<>(), 0, deck, new Deck(), foundations, true),
                KlondikeBoard::new);
        Optional.of(board().drawDeck())
                .filter(listIsNotEmpty)
                .map(it -> it.get(0))
                .ifPresent(it -> board().updateBoard(it));
    }

    @Override
    public boolean isContinuing() {
        return super.isContinuing() && totalSolutions() < SOLUTION_LIMIT;
    }

    @SuppressWarnings("rawtypes")
    public Pair<Integer, List> maxScore() {
        throw new RuntimeException("Not ready yet.");
    }

}