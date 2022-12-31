package org.solitaire;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.klondike.KlondikeHelper;
import org.solitaire.model.GameBuilder;
import org.solitaire.model.GameSolver;
import org.solitaire.pyramid.Pyramid;
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

import static org.solitaire.util.CardHelper.checkLongestPath;
import static org.solitaire.util.CardHelper.checkMaxScore;
import static org.solitaire.util.CardHelper.checkShortestPath;
import static org.solitaire.util.SolitaireHelper.getTotalScenarios;

@SuppressWarnings("rawtypes")
public class SolitaireApp {
    public static final String TRIPEAKS = "-t";
    public static final String SPIDER = "-s";
    public static final String PYRAMID = "-p";
    public static final String KLONDIKE = "-k";
    public static final String NOSUITS = "-n";
    private static final Function<GameSolver, Pair<GameSolver, List<List>>> solveIt = it -> Pair.of(it, it.solve());

    private static final Map<String, GameBuilder> BUILDERS = new HashMap<>() {{
        put(TRIPEAKS, TriPeaksHelper::build);
        put(PYRAMID, Pyramid::build);
        put(SPIDER, SpiderHelper::build);
        put(KLONDIKE, KlondikeHelper::build);
    }};

    public static void main(String[] args) {
        new SolitaireApp().run(args);
    }

    public List<List> run(String[] args) {
        Function<String[], GameSolver> buildSolver = it -> getGameBuilder(args).apply(it);

        var stopWatch = new StopWatch();

        stopWatch.start();
        checkUseSuits(args);
        var results = Optional.of(getPath(args))
                .map(IOHelper::loadFile)
                .map(buildSolver)
                .map(solveIt)
                .orElseThrow();
        stopWatch.stop();

        System.out.printf("Found %,d solutions in %,d scenarios - total time: %s.\n",
                results.getRight().size(), getTotalScenarios(), stopWatch.formatTime());
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

    private String getSolverType(String[] args) {
        return Arrays.stream(args, 1, args.length)
                .filter(BUILDERS::containsKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing solver type; '-t', '-p', or '-s'"));
    }

    protected void checkUseSuits(String[] args) {
        CardHelper.useSuit = Arrays.stream(args, 1, args.length)
                .noneMatch(it -> it.equalsIgnoreCase(NOSUITS));
    }
}