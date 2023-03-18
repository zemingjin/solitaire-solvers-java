package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@SuppressWarnings("rawtypes")
public interface GameSolver {
    void solve();

    Integer maxDepth();

    Pair<Integer, List> maxScore();

    int totalScenarios();

    int totalSolutions();

    List shortestPath();

    List longestPath();

    String pathString(List path);
}
