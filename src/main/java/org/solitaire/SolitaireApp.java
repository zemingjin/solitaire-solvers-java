package org.solitaire;

import org.apache.commons.lang3.time.StopWatch;
import org.solitaire.freecell.FreeCellHelper;
import org.solitaire.klondike.KlondikeHelper;
import org.solitaire.model.GameBuilder;
import org.solitaire.model.GameSolver;
import org.solitaire.model.SolutionType;
import org.solitaire.pyramid.PyramidHelper;
import org.solitaire.spider.SpiderHelper;
import org.solitaire.tripeaks.TriPeaksHelper;
import org.solitaire.util.CardHelper;
import org.solitaire.util.IOHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.IntStream.range;
import static org.solitaire.model.SolutionType.Longest;
import static org.solitaire.model.SolutionType.One;
import static org.solitaire.model.SolutionType.Shortest;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.util.BoardHelper.listNotEmpty;
import static org.solitaire.util.CardHelper.string;

public class SolitaireApp {
    public static final String TRIPEAKS = "-t";
    public static final String SPIDER = "-s";
    public static final String PYRAMID = "-p";
    public static final String KLONDIKE = "-k";
    public static final String FREECELL = "-f";
    public static final String USE_SUITS = "-suits";
    public static final String SINGLE_SOLUTION = "-single";
    public static final String PRINT = "-print";

    private static final Map<String, GameBuilder> BUILDERS = new HashMap<>() {{
        put(TRIPEAKS, TriPeaksHelper::build);
        put(PYRAMID, PyramidHelper::build);
        put(SPIDER, SpiderHelper::build);
        put(KLONDIKE, KlondikeHelper::build);
        put(FREECELL, FreeCellHelper::build);
    }};

    private static final SolitaireApp app = new SolitaireApp();
    private GameSolver solver;
    @SuppressWarnings("rawtypes")
    private final Function<SolutionType, Supplier<List>> pathSupplier = type ->
            switch (type) {
                case One, Shortest -> solver::shortestPath;
                case Longest -> solver::longestPath;
            };
    private StopWatch stopWatch;

    public static SolitaireApp app() {
        return SolitaireApp.app;
    }

    public static void main(String[] args) {
        app().run(args);
    }

    public static void checkUseSuits(String[] args) {
        CardHelper.useSuit(checkParam(args, USE_SUITS));
    }

    public static void checkSingleSolution(String[] args) {
        singleSolution(checkParam(args, SINGLE_SOLUTION));
    }

    public static void checkPrint(String[] args) {
        isPrint(checkParam(args, PRINT));
    }

    private static boolean checkParam(String[] args, String target) {
        return range(1, args.length).anyMatch(i -> target.equalsIgnoreCase(args[i]));
    }

    public StopWatch stopWatch() {
        return stopWatch;
    }

    public void stopWatch(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    public GameSolver solver() {
        return solver;
    }

    public void solver(GameSolver solver) {
        this.solver = solver;
    }

    public void run(String[] args) {
        Function<String[], GameSolver> buildSolver = it -> getGameBuilder(args).apply(it);

        stopWatch(new StopWatch());

        stopWatch().start();
        checkUseSuits(args);
        checkSingleSolution(args);
        checkPrint(args);
        solver(Optional.of(getPath(args))
                .map(IOHelper::loadFile)
                .map(buildSolver)
                .orElseThrow());
        solver().solve();

        stopWatch().stop();

        System.out.printf("Found %,d solutions in %,d scenarios - total time: %s with maximum depth of %d.\n",
                solver().totalSolutions(), solver().totalScenarios(), stopWatch.formatTime(), solver().maxDepth());
        checkPath(solver, singleSolution() ? One : Shortest);
        if (!singleSolution()) {
            checkPath(solver, Longest);
            checkMaxScore(solver());
        }
    }

    private String getPath(String[] args) {
        return Optional.of(args).filter(it -> it.length > 0).map(it -> it[0]).orElseThrow();
    }

    private GameBuilder getGameBuilder(String[] args) {
        return Optional.ofNullable(getSolverType(args))
                .map(BUILDERS::get)
                .orElseThrow();
    }

    public String getSolverType(String[] args) {
        return Arrays.stream(args, 1, args.length)
                .filter(BUILDERS::containsKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing solver type; '-t', '-p', '-k', '-f', or '-s'"));
    }

    public void checkPath(GameSolver solver, SolutionType type) {
        Optional.of(type)
                .map(pathSupplier)
                .map(Supplier::get)
                .filter(listNotEmpty)
                .ifPresent(it -> System.out.printf("%s Path(%d): %s\n", type, it.size(), solver.pathString(it)));
    }

    public void checkMaxScore(GameSolver solver) {
        try {
            Optional.of(solver)
                    .map(GameSolver::maxScore)
                    .ifPresent(it -> System.out.printf("Max Score(%,d): %s\n", it.getLeft(), string(it.getRight())));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}