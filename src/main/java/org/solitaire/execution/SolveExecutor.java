package org.solitaire.execution;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Board;
import org.solitaire.model.BoardStack;
import org.solitaire.model.GameSolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.util.BoardHelper.isNotEmpty;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.CardHelper.string;

public class SolveExecutor<S, U, T extends Board<S, U>> implements GameSolver {
    private static boolean singleSolution = false;
    private static int hsdDepth = 6;
    private static boolean isPrint = false;

    private final Stack<BoardStack<T>> stack = new Stack<>();
    private final List<Consumer<List<S>>> solutionConsumers = new LinkedList<>();
    private int totalScenarios = 0;
    private int totalSolutions = 0;
    private Integer maxDepth = 0;
    private Function<T, T> cloner;
    private List<S> shortestPath;
    private List<S> longestPath;

    public SolveExecutor(T initialBoard) {
        addBoard(initialBoard);
        addSolutionConsumer(this::defaultSolutionConsumer);
    }

    public SolveExecutor(T initialBoard, Function<T, T> cloner) {
        this(initialBoard);
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

    public static boolean isPrint() {
        return isPrint;
    }

    public static void isPrint(boolean isPrint) {
        SolveExecutor.isPrint = isPrint;
    }

    /**************************************************************************************************************
     * Execution routines
     *************************************************************************************************************/
    @Override
    public void solve() {
        verifyBoard();

        while (isContinuing() && !stack.isEmpty()) {
            checkMaxDepth();

            Optional.ofNullable(stack.peek())
                    .filter(isNotEmpty)
                    .map(this::getBoard)
                    .filter(this::isUnsolvedBoard)
                    .ifPresent(solveBoard());
        }
    }

    public void solveByDFS(T board) {
        Optional.of(board)
                .map(this::searchBoard)
                .map(it -> it.sorted(comparingInt(T::score)).toList())
                .filter(isNotEmpty)
                .ifPresent(this::addBoards);
    }

    public void solveByHSD(T board) {
        var boards = List.of(board);

        for (int i = 1; i <= hsdDepth() && isNotEmpty(boards); i++) {
            boards = boards.stream().flatMap(this::searchBoard).toList();
        }
        Optional.of(boards)
                .filter(isNotEmpty)
                .map(List::stream)
                .map(this::getBestBoard)
                .ifPresent(this::addBoard);
    }

    private T getBestBoard(Stream<T> boards) {
        return boards.reduce((a, b) -> b.score() >= a.score() ? b : a).orElseThrow();
    }

    private Stream<T> searchBoard(T board) {
        totalScenarios++;
        return Optional.of(board)
                .map(T::findCandidates)
                .filter(isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .stream()
                .flatMap(it -> it);
    }

    @SuppressWarnings("unchecked")
    public Stream<T> applyCandidates(List<U> list, T board) {
        return list.parallelStream()
                .map(it -> (T) clone(board).updateBoard(it))
                .filter(this::isUnsolvedBoard)
                .filter(isNotNull)
                .peek(Board::score);
    }

    private boolean isUnsolvedBoard(T board) {
        if (nonNull(board) && board.isSolved() && isContinuing()) {
            solutionConsumers.forEach(it -> it.accept(board.path()));
            return false;
        }
        return true;
    }

    protected void defaultSolutionConsumer(List<S> path) {
        totalSolutions(totalSolutions() + 1);
        if (nonNull(path) && isNotEmpty(path)) {
            checkShortestPath(path);
            checkLongestPath(path);
            if (isPrint()) {
                System.out.printf("%d: %s\n", path.size(), string(path));
            }
        }
    }

    /**************************************************************************************************************
     * Helper routines
     *************************************************************************************************************/
    public T getBoard() {
        return getBoard(stack.peek());
    }

    private T getBoard(BoardStack<T> boards) {
        var board = boards.pop();

        if (boards.isEmpty()) {
            stack.pop();
        }
        return board;
    }

    public boolean isContinuing() {
        return !singleSolution() || totalSolutions() == 0;
    }

    private void checkLongestPath(List<S> path) {
        if (!singleSolution() && (isNull(longestPath()) || longestPath().size() < path.size())) {
            longestPath(path);
        }
    }

    private void checkShortestPath(List<S> path) {
        if (isNull(shortestPath()) || shortestPath().size() > path.size()) {
            shortestPath(path);
        }
    }

    public void addBoards(Collection<T> boards) {
        Optional.of(boards)
                .filter(isNotEmpty)
                .ifPresent(it -> stack.add(new BoardStack<>(it)));
    }

    public void addBoard(T board) {
        Optional.ofNullable(board)
                .map(List::of)
                .ifPresent(this::addBoards);
    }

    private void verifyBoard() {
        var verify = board().verify();

        if (!verify.isEmpty()) {
            throw new RuntimeException(verify.toString());
        }
    }

    /**************************************************************************************************************
     * Accessors
     *************************************************************************************************************/
    @Override
    public int totalScenarios() {
        return this.totalScenarios;
    }

    @Override
    public Integer maxDepth() {
        return maxDepth;
    }

    protected void maxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> maxScore() {
        throw new RuntimeException("Maximum score is not supported!");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String pathString(List path) {
        return string(path);
    }

    protected void checkMaxDepth() {
        if (stack.size() > maxDepth()) {
            maxDepth(stack.size());
        }
    }

    public T board() {
        return stack.isEmpty() ? null : stack.peek().peek();
    }

    public T clone(T board) {
        return requireNonNull(cloner).apply(board);
    }

    public void cloner(Function<T, T> cloner) {
        this.cloner = cloner;
    }

    public Stack<BoardStack<T>> stack() {
        return this.stack;
    }

    public Consumer<T> solveBoard() {
        return singleSolution() ? this::solveByHSD : this::solveByDFS;
    }

    public List<Consumer<List<S>>> solutionConsumers() {
        return solutionConsumers;
    }

    public void addSolutionConsumer(Consumer<List<S>> consumer) {
        solutionConsumers().add(consumer);
    }

    public boolean removeSolutionConsumer(Consumer<List<S>> consumer) {
        return solutionConsumers().remove(consumer);
    }

    @Override
    public List<S> shortestPath() {
        return shortestPath;
    }

    public void shortestPath(List<S> shortestPath) {
        this.shortestPath = shortestPath;
    }

    @Override
    public List<S> longestPath() {
        return longestPath;
    }

    public void longestPath(List<S> longestPath) {
        this.longestPath = longestPath;
    }

    @Override
    public int totalSolutions() {
        return totalSolutions;
    }

    public void totalSolutions(int totalSolutions) {
        this.totalSolutions = totalSolutions;
    }

}
