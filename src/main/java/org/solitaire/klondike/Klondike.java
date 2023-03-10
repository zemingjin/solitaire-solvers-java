package org.solitaire.klondike;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.List;
import java.util.Stack;

@Slf4j
public class Klondike extends SolveExecutor<String, Candidate, KlondikeBoard> {
    protected static final int SOLUTION_LIMIT = 1000;

    public Klondike(Columns columns,
                    Deck deck,
                    List<Stack<Card>> foundations) {
        super(new KlondikeBoard(columns, new Path<>(), 0, deck, new Stack<>(), foundations, true),
                KlondikeBoard::new);
    }

    @Override
    public boolean isContinuing() {
        return super.isContinuing() && solutions().size() < SOLUTION_LIMIT;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return null;
    }
}