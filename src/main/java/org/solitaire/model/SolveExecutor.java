package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.listNotEmpty;
import static org.solitaire.util.CardHelper.string;

public class SolveExecutor<S, U, T extends Board<S, U>> implements GameSolver {
    private static boolean singleSolution = false;
    private static int hsdDepth = 6;
    private static boolean isPrint = false;

    private final Stack<BoardStack<T>> stack = new Stack<>();
    private final List<Consumer<List<S>>> solutionConsumers = new LinkedList<>();
    private final Function<Stream<T>, Stream<T>> sortBoards = it -> it.sorted(comparingInt(T::score));
    private final Consumer<Collection<T>> push = boards -> stack().add(new BoardStack<>(boards));
    private final Consumer<Collection<T>> addBoards =
            boards -> Optional.of(boards)
                    .filter(listNotEmpty)
                    .ifPresent(push);
    private final Consumer<T> addBoard =
            board -> Optional.ofNullable(board)
                    .map(List::of)
                    .ifPresent(addBoards());
    private final Function<Stream<T>, T> getBestBoard =
            boards -> boards.reduce((a, b) -> b.score() >= a.score() ? b : a).orElseThrow();
    private final Function<BoardStack<T>, T> getBoard = boards -> {
        var board = boards.pop();

        if (boards.isEmpty()) {
            stack.pop();
        }
        return board;
    };
    private int totalScenarios = 0;
    private int totalSolutions = 0;
    private final Predicate<T> isUnsolvedBoard = board -> {
        if (nonNull(board) && board.isSolved() && isContinuing()) {
            solutionConsumers.forEach(it -> it.accept(board.path()));
            return false;
        }
        return true;
    };
    private Integer maxDepth = 0;
    private Function<T, T> cloner;
    @SuppressWarnings("unchecked")
    private final Function<Pair<T, U>, T> clone_Update = pair -> (T) clone(pair.getLeft()).updateBoard(pair.getRight());
    private final Function<Pair<List<U>, T>, Stream<T>> applyCandidates =
            pair -> pair.getLeft().parallelStream()
                    .map(it -> Pair.of(pair.getRight(), it))
                    .map(clone_Update)
                    .filter(isUnsolvedBoard)
                    .filter(isNotNull)
                    .peek(Board::score);
    private final Function<T, Stream<T>> searchBoard = board -> {
        totalScenarios++;
        return Optional.of(board)
                .map(T::findCandidates)
                .filter(listNotEmpty)
                .map(it -> Pair.of(it, board))
                .map(applyCandidates)
                .stream()
                .flatMap(it -> it);
    };
    private List<S> shortestPath;
    private List<S> longestPath;
    protected final Consumer<List<S>> defaultSolutionConsumer = path -> {
        totalSolutions(totalSolutions() + 1);
        if (nonNull(path) && isNotEmpty(path)) {
            checkShortestPath(path);
            checkLongestPath(path);
            if (isPrint()) {
                System.out.printf("%d: %s\n", path.size(), string(path));
            }
        }
    };

    public SolveExecutor(T initialBoard) {
        addBoard.accept(initialBoard);
        addSolutionConsumer(defaultSolutionConsumer);
    }

    public SolveExecutor(T initialBoard, Function<T, T> cloner) {
        this(initialBoard);
        cloner(cloner);
    }

    /**************************************************************************************************************
     * Accessors
     *************************************************************************************************************/
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
        var verify = board().verify();

        if (verify.isEmpty()) {
            while (isContinuing() && !stack.isEmpty()) {
                checkMaxDepth();

                Optional.ofNullable(stack.peek())
                        .filter(listNotEmpty)
                        .map(getBoard)
                        .filter(isUnsolvedBoard)
                        .ifPresent(solveBoard());
            }
        } else {
            throw new RuntimeException(verify.toString());
        }
    }

    public void solveByDFS(T board) {
        Optional.of(board)
                .map(searchBoard)
                .map(sortBoards)
                .map(Stream::toList)
                .filter(listNotEmpty)
                .ifPresent(addBoards());
    }

    public void solveByHSD(T board) {
        var boards = List.of(board);

        for (int i = 1; i <= hsdDepth() && isNotEmpty(boards); i++) {
            boards = boards.stream().flatMap(searchBoard).toList();
        }
        Optional.of(boards)
                .filter(listNotEmpty)
                .map(List::stream)
                .map(getBestBoard)
                .ifPresent(addBoard);
    }

    /**************************************************************************************************************
     * Helper routines
     *************************************************************************************************************/
    public boolean isContinuing() {
        return !singleSolution() || totalSolutions() == 0;
    }

    public Function<Pair<List<U>, T>, Stream<T>> applyCandidates() {
        return applyCandidates;
    }

    public Consumer<Collection<T>> addBoards() {
        return addBoards;
    }

    public Consumer<T> addBoard() {
        return addBoard;
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
