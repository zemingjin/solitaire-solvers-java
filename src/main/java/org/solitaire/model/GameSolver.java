package org.solitaire.model;

import java.util.List;

@SuppressWarnings("rawtypes")
public interface GameSolver {
    List<List> solve();

    List<List> showDetails(List<List> results);
}
