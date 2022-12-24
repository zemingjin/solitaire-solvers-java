package org.solitaire.pyramid;

import org.apache.commons.lang3.time.StopWatch;
import org.solitaire.io.IOHelper;
import org.solitaire.model.CardHelper;
import org.solitaire.model.GameSolver;

import java.util.Optional;
import java.util.stream.Stream;

public class PyramidApp {
    public static final String PATH_MISSING = "Error: Missing board source file path.";

    public void run(String path) {
        var stopWatch = new StopWatch();

        stopWatch.start();
        var results = Optional.of(path)
                .map(IOHelper::loadFile)
                .map(PyramidBoard::build)
                .map(GameSolver::solve)
                .stream()
                .filter(it -> !it.isEmpty())
                .toList();
        stopWatch.stop();
        System.out.printf("Found %d solves in %s\n", results.size(), stopWatch.formatTime());
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            checkArgs(args);
            new PyramidApp().run(args[0]);
        } else {
            throw new RuntimeException(PATH_MISSING);
        }
    }

    protected static void checkArgs(String[] args) {
        Stream.of(args).filter(it -> it.equals("-n")).findFirst().ifPresent(it -> CardHelper.useSuit = false);
    }
}