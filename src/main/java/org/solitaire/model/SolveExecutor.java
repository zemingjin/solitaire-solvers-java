package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public class SolveExecutor<T> implements GameSolver {
    private final Stack<StateQueue<T>> stack = new Stack<>();
    private final List<List> solutions = new ArrayList<>();
    private int totalScenarios = 0;
    private int maxStack = 0;

    private Consumer<T> stateConsumer;

    public SolveExecutor(T state) {
        stack.add(new StateQueue<>(state));
    }

    public Stack<StateQueue<T>> stack() {
        return this.stack;
    }

    public void stateConsumer(Consumer<T> stateConsumer) {
        this.stateConsumer = stateConsumer;
    }

    @Override
    public List<List> solve() {
        while (!stack.isEmpty()) {
            totalScenarios++;
            checkMaxStack();

            Optional.ofNullable(stack.peek())
                    .map(this::getState)
                    .ifPresent(stateConsumer);
        }
        return solutions();
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not implemented");
    }

    private T getState(StateQueue<T> queue) {
        if (queue.isNotEmpty()) {

            T state = queue.poll();

            if (queue.isEmpty()) {
                stack.pop();
            }
            return state;
        }
        return null;
    }

    public boolean addAll(Collection<T> states) {
        if (!states.isEmpty()) {
            return stack().add(new StateQueue<>(states));
        }
        return false;
    }

    public boolean add(T state) {
        return stack().add(new StateQueue<>(state));
    }

    private void checkMaxStack() {
        if (stack.size() > maxStack()) {
            maxStack = stack.size();
        }
    }

    public List<List> solutions() {
        return this.solutions;
    }

    public int maxStack() {
        return this.maxStack;
    }

    @Override
    public int totalScenarios() {
        return this.totalScenarios;
    }
}
