package org.solitaire.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@SuppressWarnings("rawtypes")
public interface GameSolver {
    void solve();

    int maxDepth();

    Pair<Integer, List> maxScore();

    int totalScenarios();

    int totalSolutions();

    List shortestPath();

    List longestPath();
}
