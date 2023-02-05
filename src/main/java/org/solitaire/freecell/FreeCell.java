package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Path;
import org.solitaire.model.StateQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Heineman’s Staged Deepening (HSD)
 * Properties:
 * - there are usually multiple ways to arrive at the solution for a given FreeCell deal
 * - there are many cases where moving a card is irreversible, such as a card is moved to the foundation
 */
@SuppressWarnings("rawtypes")
public class FreeCell implements GameSolver {
    private final List<List> solutions = new ArrayList<>();
    private int totalScenarios;
    private Function<FreeCellState, FreeCellState> cloner = FreeCellState::new;
    private Stack<StateQueue<FreeCellState>> stack = new Stack<>();
    private int maxStack = 0;

    public FreeCell(Columns columns) {
        stack.add(new StateQueue<>(new FreeCellState(columns, new Path<>(), new Card[4], new Card[4])));
    }

    public int maxStack() {
        return this.maxStack;
    }

    public void maxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    protected Stack<StateQueue<FreeCellState>> stack() {
        return this.stack;
    }

    protected void stack(Stack<StateQueue<FreeCellState>> stack) {
        this.stack = stack;
    }

    @Override
    public List<List> solve() {
        while (!stack.isEmpty()) {
            checkMaxStack();
            Optional.of(stack.peek())
                    .filter(StateQueue::isNotEmpty)
                    .map(StateQueue::poll)
                    .ifPresentOrElse(this::solve, stack::pop);
        }
        return solutions();
    }

    protected void solve(FreeCellState state) {
        if (state.isCleared()) {
            solutions.add(state.path());
        } else {
            totalScenarios++;
            Optional.of(state)
                    .map(FreeCellState::findCandidates)
                    .filter(ObjectUtils::isNotEmpty)
                    .map(it -> toStateQueue(it, state))
                    .map(this::scoreStates)
                    .map(this::sortStates)
                    .ifPresent(stack::add);
        }
    }

    protected StateQueue<FreeCellState> toStateQueue(List<Candidate> candidates, FreeCellState state) {
        return candidates.stream()
                .map(it -> cloner.apply(state).updateState(it))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(StateQueue<FreeCellState>::new));
    }

    protected StateQueue<FreeCellState> scoreStates(StateQueue<FreeCellState> queue) {
        queue.forEach(FreeCellState::score);
        return queue;
    }

    protected StateQueue<FreeCellState> sortStates(StateQueue<FreeCellState> queue) {
        return queue.stream()
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .collect(Collectors.toCollection(StateQueue<FreeCellState>::new));
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }

    @Override
    public int totalScenarios() {
        return totalScenarios;
    }

    public List<List> solutions() {
        return solutions;
    }

    public void cloner(Function<FreeCellState, FreeCellState> cloner) {
        this.cloner = cloner;
    }

    private void checkMaxStack() {
        if (stack.size() > maxStack()) {
            maxStack(stack.size());
        }
    }
}