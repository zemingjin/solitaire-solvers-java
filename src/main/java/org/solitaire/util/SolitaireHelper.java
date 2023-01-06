package org.solitaire.util;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SolitaireHelper {
    @SuppressWarnings("rawtypes")
    public static final Supplier<List<List>> noSolution = () -> singletonList(emptyList());
    private static int totalScenarios = 0;

    public static void incTotal() {
        totalScenarios++;
    }

    public static int getTotalScenarios() {
        return totalScenarios;
    }
}
