package org.solitaire.klondike;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;

@Slf4j
@SuppressWarnings("rawtypes")
public class Klondike implements GameSolver {
    protected static final int LIMIT_SOLUTIONS = 1000;
    private final List<List> solutions = new ArrayList<>();
    private int totalScenarios;
    private KlondikeState initState;
    private Function<KlondikeState, KlondikeState> cloner = KlondikeState::new;

    public Klondike(Columns columns,
                    Deck deck,
                    List<Stack<Card>> foundations) {
        initState = new KlondikeState(columns, new Path<>(), 0, deck, new Stack<>(), foundations, true);
    }

    @Override
    public List<List> solve() {
        solve(initState);
        return solutions;
    }

    protected void solve(KlondikeState state) {
        if (state.isCleared()) {
            solutions.add(state.path());
        } else if (solutions.size() < LIMIT_SOLUTIONS) {
            totalScenarios++;
            Optional.of(state.findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresentOrElse(it -> applyCandidates(it, state), () -> drawDeck(state));
        }
    }

    protected void drawDeck(KlondikeState state) {
        Optional.ofNullable(state.drawDeckCards())
                .ifPresent(this::solve);
    }

    protected void applyCandidates(List<Candidate> candidates, KlondikeState state) {
        candidates.stream()
                .map(it -> cloner.apply(state).updateStates(it))
                .filter(Objects::nonNull)
                .forEach(this::solve);
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
//        results.forEach(System.out::println);
        return null;
    }

    @Override
    public int totalScenarios() {
        return totalScenarios;
    }

    protected Klondike initState(KlondikeState initState) {
        this.initState = initState;
        return this;
    }

    public KlondikeState initState() {
        return initState;
    }

    public void cloner(Function<KlondikeState, KlondikeState> cloner) {
        this.cloner = cloner;
    }
}