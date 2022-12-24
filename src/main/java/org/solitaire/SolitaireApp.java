package org.solitaire;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.io.IOHelper;
import org.solitaire.model.CardHelper;
import org.solitaire.model.GameBuilder;
import org.solitaire.model.GameSolver;
import org.solitaire.pyramid.PyramidBoard;
import org.solitaire.tripeaks.TriPeaksHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SolitaireApp {
    private static final String TRIPEAKS = "-t";
    private static final String PYRAMID = "-p";
    private static final Map<String, GameBuilder> BUILDERS = new HashMap<>() {{
        put(TRIPEAKS, TriPeaksHelper::build);
        put(PYRAMID, PyramidBoard::build);
    }};

    public static void main(String[] args) {
        new SolitaireApp().run(args);
    }

    @SuppressWarnings("rawtypes")
    public List<List> run(String[] args) {
        checkArgs(args);
        var stopWatch = new StopWatch();

        stopWatch.start();
        var results = Optional.of(getPath(args))
                .map(IOHelper::loadFile)
                .map(it -> getGameBuilder(args).apply(it))
                .map(this::solve)
                .orElseThrow();
        stopWatch.stop();
        System.out.printf("Found %d solutions in %s\n", results.getRight().size(), stopWatch.formatTime());
        results.getLeft().showDetails(results.getRight());
        return results.getRight();
    }

    @SuppressWarnings("rawtypes")
    private Pair<GameSolver, List<List>> solve(GameSolver solver) {
        return Pair.of(solver, solver.solve());
    }

    private String getPath(String[] args) {
        return Optional.of(args).filter(it -> it.length > 0).map(it -> it[0]).orElseThrow();
    }

    private GameBuilder getGameBuilder(String[] args) {
        return Optional.of(getType(args))
                .map(BUILDERS::get)
                .orElseThrow();
    }

    private static final List<String> TYPES = Arrays.asList(TRIPEAKS, PYRAMID);

    private String getType(String[] args) {
        return Optional.of(args)
                .filter(it -> it.length > 1)
                .stream()
                .flatMap(Stream::of)
                .filter(TYPES::contains)
                .findFirst()
                .orElse(PYRAMID);
    }

    protected void checkArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-n")) {
                CardHelper.useSuit = false;
                break;
            }
        }
    }
}