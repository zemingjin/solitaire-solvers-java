package org.solitaire;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.freecell.FreeCellHelper;
import org.solitaire.klondike.KlondikeHelper;
import org.solitaire.model.GameBuilder;
import org.solitaire.model.GameSolver;
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

import static java.util.stream.IntStream.range;
import static org.solitaire.model.SolveExecutor.singleSolution;
import static org.solitaire.util.CardHelper.checkLongestPath;
import static org.solitaire.util.CardHelper.checkMaxScore;
import static org.solitaire.util.CardHelper.checkShortestPath;

public class SolitaireApp {
    public static final String TRIPEAKS = "-t";
    public static final String SPIDER = "-s";
    public static final String PYRAMID = "-p";
    public static final String KLONDIKE = "-k";
    public static final String FREECELL = "-f";
    public static final String USE_SUITS = "-suits";
    public static final String SINGLE_SOLUTION = "-single";
    @SuppressWarnings("rawtypes")
    private static final Function<GameSolver, Pair<GameSolver, List<List>>> solveIt = it -> Pair.of(it, it.solve());

    private static final Map<String, GameBuilder> BUILDERS = new HashMap<>() {{
        put(TRIPEAKS, TriPeaksHelper::build);
        put(PYRAMID, PyramidHelper::build);
        put(SPIDER, SpiderHelper::build);
        put(KLONDIKE, KlondikeHelper::build);
        put(FREECELL, FreeCellHelper::build);
    }};

    public static void main(String[] args) {
        new SolitaireApp().run(args);
    }

    protected static void checkUseSuits(String[] args) {
        CardHelper.useSuit(checkParam(args, USE_SUITS));
    }

    protected static void checkSingleSolution(String[] args) {
        singleSolution(checkParam(args, SINGLE_SOLUTION));
    }

    private static boolean checkParam(String[] args, String target) {
        return range(1, args.length).anyMatch(i -> args[i].equalsIgnoreCase(target));
    }

    @SuppressWarnings("rawtypes")
    public List<List> run(String[] args) {
        Function<String[], GameSolver> buildSolver = it -> getGameBuilder(args).apply(it);

        var stopWatch = new StopWatch();

        stopWatch.start();
        checkUseSuits(args);
        checkSingleSolution(args);
        var solver = Optional.of(getPath(args))
                .map(IOHelper::loadFile)
                .map(buildSolver)
                .orElseThrow();
        var results = Optional.of(solver)
                .map(solveIt)
                .orElseThrow();
        stopWatch.stop();

        System.out.printf("Found %,d solutions in %,d scenarios - total time: %s with maximum depth of %d.\n",
                results.getRight().size(), solver.totalScenarios(), stopWatch.formatTime(), solver.maxDepth());
        checkShortestPath(results.getRight());
        checkLongestPath(results.getRight());
        checkMaxScore(results);
        return results.getRight();
    }

    private String getPath(String[] args) {
        return Optional.of(args).filter(it -> it.length > 0).map(it -> it[0]).orElseThrow();
    }

    private GameBuilder getGameBuilder(String[] args) {
        return Optional.ofNullable(getSolverType(args))
                .map(BUILDERS::get)
                .orElseThrow();
    }

    protected String getSolverType(String[] args) {
        return Arrays.stream(args, 1, args.length)
                .filter(BUILDERS::containsKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing solver type; '-t', '-p', '-k', '-f', or '-s'"));
    }
}