package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("rawtypes")
public class SolveExecutor<T extends Board<?>> implements GameSolver {
    private final Stack<BoardStack<T>> stack = new Stack<>();
    private final List<List> solutions = new ArrayList<>();
    private int totalScenarios = 0;
    private int maxStack = 0;
    private Function<T, T> cloner;
    private Consumer<T> solveBoard;
    private boolean singleSolution = false;

    public SolveExecutor(T board) {
        addBoard(board);
    }

    public SolveExecutor(T board, Function<T, T> cloner) {
        this(board);
        cloner(cloner);
    }

    public SolveExecutor(T board, Function<T, T> cloner, boolean singleSolution) {
        this(board, cloner);
        singleSolution(singleSolution);
    }

    public Stack<BoardStack<T>> stack() {
        return this.stack;
    }

    public void solveBoard(Consumer<T> solveBoard) {
        this.solveBoard = solveBoard;
    }

    public boolean singleSolution() {
        return singleSolution;
    }

    public void singleSolution(boolean singleSolution) {
        this.singleSolution = singleSolution;
    }

    @Override
    public List<List> solve() {
        var verify = board().verify();

        if (verify.isEmpty()) {
            while (isContinuing() && !stack.isEmpty()) {
                checkMaxStack();

                Optional.ofNullable(stack.peek()).map(this::getBoard).ifPresent(this::processBoard);
            }
            return solutions();
        }
        throw new RuntimeException(verify.toString());
    }

    public boolean isContinuing() {
        return !singleSolution || solutions.size() != 1;
    }

    private void processBoard(T board) {
        if (board.isCleared()) {
            solutions.add(board.path());
            if (nonNull(board.path())) {
                System.out.printf("%d: %s\n", board.path().size(), board.path());
            }
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

    private T getBoard(BoardStack<T> boards) {
        var board = boards.pop();

        if (boards.isEmpty()) {
            stack.pop();
        }
        return board;
    }

    public boolean addBoards(Collection<T> boards) {
        if (!boards.isEmpty()) {
            return stack().add(new BoardStack<>(boards));
        }
        return false;
    }

    public boolean addBoard(T board) {
        return addBoards(new BoardStack<>(board));
    }

    private void checkMaxStack() {
        if (stack.size() > maxStack()) {
            maxStack = stack.size();
        }
    }

    public T board() {
        return stack.peek().peek();
    }

    public List<List> solutions() {
        return this.solutions;
    }

    public int maxStack() {
        return this.maxStack;
    }

    public T clone(T board) {
        return requireNonNull(cloner).apply(board);
    }

    public void cloner(Function<T, T> cloner) {
        this.cloner = cloner;
    }

    protected T getBestBoard(List<T> boards) {
        return boards.stream().reduce(null, (a, b) -> isNull(a) || b.score() >= a.score() ? b : a);
    }
}
