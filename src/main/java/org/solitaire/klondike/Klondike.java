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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("rawtypes")
public class Klondike extends SolveExecutor<KlondikeState> {
    protected static final int LIMIT_SOLUTIONS = 1000;
    private Function<KlondikeState, KlondikeState> cloner = KlondikeState::new;

    public Klondike(Columns columns,
                    Deck deck,
                    List<Stack<Card>> foundations) {
        super(new KlondikeState(columns, new Path<>(), 0, deck, new Stack<>(), foundations, true));
        stateConsumer(this::solve);
    }

    protected void solve(KlondikeState state) {
        if (state.isCleared()) {
            solutions().add(state.path());
        } else if (solutions().size() < LIMIT_SOLUTIONS) {
            Optional.of(state.findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .map(it -> applyCandidates(it, state))
                    .filter(it -> !it.isEmpty())
                    .map(super::addAll)
                    .orElseGet(() -> drawDeck(state));
        }
    }

    protected boolean drawDeck(KlondikeState state) {
        return Optional.ofNullable(state.drawDeckCards())
                .map(super::add)
                .orElse(false);
    }

    protected List<KlondikeState> applyCandidates(List<Candidate> candidates, KlondikeState state) {
        return candidates.stream()
                .map(it -> cloner.apply(state).updateStates(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return null;
    }

    public void cloner(Function<KlondikeState, KlondikeState> cloner) {
        this.cloner = cloner;
    }
}