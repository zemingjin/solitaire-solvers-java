package org.solitaire.model;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@SuppressWarnings("rawtypes")
public class SolveExecutor<T extends Board<?, ?>> implements GameSolver {
    private static boolean singleSolution = false;
    private static int hsdDepth = 6;

    private final Stack<BoardStack<T>> stack = new Stack<>();
    private final List<List> solutions = new ArrayList<>();
    private int totalScenarios = 0;
    private int maxStack = 0;
    private Function<T, T> cloner;
    private Consumer<T> solveBoard;

    public SolveExecutor(T board) {
        addBoard(board);
    }

    public SolveExecutor(T board, Function<T, T> cloner) {
        this(board);
        cloner(cloner);
    }

    public static boolean singleSolution() {
        return singleSolution;
    }

    public static void singleSolution(boolean singleSolution) {
        SolveExecutor.singleSolution = singleSolution;
    }


    public static int hsdDepth() {
        return hsdDepth;
    }

    public static void hsdDepth(int hsdDepth) {
        SolveExecutor.hsdDepth = hsdDepth;
    }

    public Stack<BoardStack<T>> stack() {
        return this.stack;
    }

    public void solveBoard(Consumer<T> solveBoard) {
        this.solveBoard = solveBoard;
    }

    @Override
    public List<List> solve() {
        var verify = board().verify();

        if (verify.isEmpty()) {
            while (isContinuing() && !stack.isEmpty()) {
                checkMaxStack();

                Optional.ofNullable(stack.peek())
                        .filter(ObjectUtils::isNotEmpty)
                        .map(this::getBoard)
                        .ifPresent(this::processBoard);
            }
            return solutions();
        }
        throw new RuntimeException(verify.toString());
    }

    public boolean isContinuing() {
        return !singleSolution || solutions.size() != 1;
    }

    public boolean checkBoard(T board) {
        if (board.isSolved()) {
            solutions.add(board.path());
            if (nonNull(board.path()) && isNotEmpty(board.path())) {
                System.out.printf("%d: %s\n", board.path().size(), board.path());
            }
            return false;
        }
        return true;
    }

    private void processBoard(T board) {
        if (checkBoard(board)) {
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
        boards = boards.stream().filter(Objects::nonNull).filter(this::checkBoard).toList();

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

    protected T getBestBoard(Stream<T> boards) {
        return boards.reduce(null, (a, b) -> isNull(a) || b.score() >= a.score() ? b : a);
    }
}
