package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("rawtypes")
public class SolveExecutor<T extends Board> implements GameSolver {
    private final Stack<BoardQueue<T>> stack = new Stack<>();
    private final List<List> solutions = new ArrayList<>();
    private int totalScenarios = 0;
    private int maxStack = 0;

    private Consumer<T> solveBoard;

    public SolveExecutor(T board) {
        stack.add(new BoardQueue<>(board));
    }

    public Stack<BoardQueue<T>> stack() {
        return this.stack;
    }

    public void solveBoard(Consumer<T> solveBoard) {
        this.solveBoard = solveBoard;
    }

    @Override
    public List<List> solve() {
        while (!stack.isEmpty()) {
            checkMaxStack();

            Optional.ofNullable(stack.peek())
                    .map(this::getBoard)
                    .ifPresent(this::processBoard);
        }
        return solutions();
    }

    private void processBoard(T board) {
        if (board.isCleared()) {
            solutions.add(board.path());
        } else {
            totalScenarios++;
            requireNonNull(solveBoard).accept(board);
        }
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int totalScenarios() {
        return this.totalScenarios;
    }

    @Override
    public int maxDepth() {
        return maxStack;
    }

    private T getBoard(BoardQueue<T> queue) {
        try {
            if (queue.isNotEmpty()) {
                return queue.poll();
            }
            return null;
        } finally {
            if (queue.isEmpty()) {
                stack.pop();
            }
        }
    }

    public boolean addAll(Collection<T> states) {
        if (!states.isEmpty()) {
            return stack().add(new BoardQueue<>(states));
        }
        return false;
    }

    public boolean add(T state) {
        return stack().add(new BoardQueue<>(state));
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
}
