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

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@SuppressWarnings("rawtypes")
public class SolveExecutor<S, U, T extends Board<S, U>> implements GameSolver {
    private final Function<List<T>, List<T>> checkReducingBoards = boards ->
            isReducingBoards()
                    ? range(boards.size() * 3 / 5, boards.size()).mapToObj(boards::get).toList()
                    : boards;
    private static boolean singleSolution = false;
    private static int hsdDepth = 6;

    private final Stack<BoardStack<T>> stack = new Stack<>();
    private final List<List> solutions = new ArrayList<>();
    private int totalScenarios = 0;
    private int maxStack = 0;
    private boolean isPrint = true;
    private Function<T, T> cloner;
    private Consumer<T> solveBoard;
    private boolean isReducingBoards = false;

    public SolveExecutor(T board) {
        addBoard(board);
        solveBoard(singleSolution() ? this::solveByHSD : this::solveByDFS);
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

    public boolean isPrint() {
        return isPrint;
    }

    public void isPrint(boolean isPrint) {
        this.isPrint = isPrint;
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

    public boolean isReducingBoards() {
        return isReducingBoards;
    }

    public void isReducingBoards(boolean isReducingBoards) {
        this.isReducingBoards = isReducingBoards;
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

    public void solveByDFS(T board) {
        Optional.of(board.findCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .map(Stream::toList)
                .filter(ObjectUtils::isNotEmpty)
                .map(checkReducingBoards)
                .ifPresent(this::addBoards);
    }

    public void solveByHSD(T board) {
        var boards = List.of(board);

        for (int i = 1; i <= hsdDepth() && isNotEmpty(boards); i++) {
            boards = boards.stream().flatMap(this::search).toList();
        }
        Optional.of(boards)
                .filter(ObjectUtils::isNotEmpty)
                .map(List::stream)
                .map(it -> it.sorted(comparingInt(T::score)))
                .map(this::getBestBoard)
                .ifPresent(this::addBoard);
    }

    private Stream<T> search(T board) {
        return Optional.of(board)
                .map(T::findCandidates)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .stream()
                .flatMap(it -> it);
    }

    @SuppressWarnings("unchecked")
    public Stream<T> applyCandidates(List<U> candidates, T board) {
        return (Stream<T>) candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull);
    }

    public boolean isContinuing() {
        return !singleSolution || solutions.size() != 1;
    }

    public boolean checkBoard(T board) {
        if (board.isSolved()) {
            solutions.add(board.path());
            if (nonNull(board.path()) && isNotEmpty(board.path()) && isPrint()) {
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
