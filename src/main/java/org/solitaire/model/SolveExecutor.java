package org.solitaire.model;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.LinkedList;
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
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@SuppressWarnings("rawtypes")
public class SolveExecutor<S, U, T extends Board<S, U>> implements GameSolver {
    //        private final Function<List<T>, List<T>> checkReducingBoards = boards ->
//            isReducingBoards()
//                    ? range(boards.size() * 3 / 5, boards.size()).mapToObj(boards::get).toList()
//                    : boards;
    private static boolean singleSolution = false;
    private static int hsdDepth = 6;
    private static boolean isPrint = true;

    private final Stack<BoardStack<T>> stack = new Stack<>();
    private final List<Consumer<List<S>>> solutionConsumers = new LinkedList<>();
    private int totalScenarios = 0;
    private int totalSolutions = 0;
    private int maxStack = 0;
    private Function<T, T> cloner;
    private List<S> shortestPath;
    private List<S> longestPath;

    public SolveExecutor(T board) {
        addBoard(board);
        addSolutionConsumer(this::defaultSolutionConsumer);
    }

    public SolveExecutor(T board, Function<T, T> cloner) {
        this(board);
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
                checkMaxStack();

                Optional.ofNullable(stack.peek())
                        .filter(ObjectUtils::isNotEmpty)
                        .map(this::getBoard)
                        .ifPresent(this::processBoard);
            }
        } else {
            throw new RuntimeException(verify.toString());
        }
    }

    public void solveByDFS(T board) {
        Optional.of(board)
                .map(this::searchBoard)
                .map(Stream::toList)
                .filter(ObjectUtils::isNotEmpty)
//                .map(checkReducingBoards)
                .ifPresent(this::addBoards);
    }

    public void solveByHSD(T board) {
        var boards = List.of(board);

        for (int i = 1; i <= hsdDepth() && isNotEmpty(boards); i++) {
            boards = boards.stream().flatMap(this::searchBoard).toList();
        }
        Optional.of(boards)
                .filter(ObjectUtils::isNotEmpty)
                .map(List::stream)
                .map(it -> it.sorted(comparingInt(T::score)))
                .map(this::getBestBoard)
                .ifPresent(this::addBoard);
    }

    private Stream<T> searchBoard(T board) {
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
                .peek(it -> totalScenarios++)
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull);
    }

    /**************************************************************************************************************
     * Helper routines
     *************************************************************************************************************/
    public boolean isContinuing() {
        return !singleSolution() || totalSolutions() != 1;
    }

    public boolean checkBoard(T board) {
        if (board.isSolved()) {
            solutionConsumers.forEach(it -> it.accept(board.path()));
            return false;
        }
        return true;
    }

    private void processBoard(T board) {
        Optional.of(board)
                .filter(this::checkBoard)
                .ifPresent(it -> solveBoard().accept(it));
    }

    protected void defaultSolutionConsumer(List<S> path) {
        totalSolutions(totalSolutions() + 1);
        if (nonNull(path) && isNotEmpty(path)) {
            if (isNull(shortestPath()) || shortestPath().size() > path.size()) {
                shortestPath(path);
            }
            if (isNull(longestPath()) || longestPath().size() < path.size()) {
                longestPath(path);
            }
            if (isPrint()) {
                System.out.printf("%d: %s\n", path.size(), path);
            }
        }
    }

    @Override
    public int totalScenarios() {
        return this.totalScenarios;
    }

    @Override
    public int maxDepth() {
        return maxStack;
    }

    @Override
    public Pair<Integer, List> maxScore() {
        throw new RuntimeException("Not supported!");
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
