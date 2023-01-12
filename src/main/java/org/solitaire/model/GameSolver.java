package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@SuppressWarnings("rawtypes")
public interface GameSolver {
    List<List> solve();

    Pair<Integer, List> getMaxScore(List<List> results);

    int totalScenarios();
}
