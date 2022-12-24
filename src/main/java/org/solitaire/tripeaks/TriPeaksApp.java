package org.solitaire.tripeaks;

import org.apache.commons.lang3.time.StopWatch;
import org.solitaire.io.IOHelper;
import org.solitaire.model.CardHelper;
import org.solitaire.model.GameSolver;

import java.util.List;
import java.util.Optional;

import static org.solitaire.tripeaks.TriPeaksHelper.checkMaxScore;
import static org.solitaire.tripeaks.TriPeaksHelper.checkShortestPath;

public class TriPeaksApp {
    public static final String PATH_MISSING = "Error: Missing board source file path.";

    public void run(String path) {
        var stopWatch = new StopWatch();

        stopWatch.start();
        var results = Optional.of(path)
                .map(IOHelper::loadFile)
                .map(TriPeaksHelper::build)
                .map(GameSolver::solve)
                .stream()
                .flatMap(List::stream)
                .filter(it -> !it.isEmpty())
                .toList();
        stopWatch.stop();
        System.out.printf("Found %d solves in %s\n", results.size(), stopWatch.formatTime());
        checkMaxScore(results);
        checkShortestPath(results);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            checkArgs(args);
            new TriPeaksApp().run(args[0]);
        } else {
            throw new RuntimeException(PATH_MISSING);
        }
    }

    protected static void checkArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-n")) {
                CardHelper.useSuit = false;
                break;
            }
        }
    }
}