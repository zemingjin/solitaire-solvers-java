package org.solitaire.model;

import java.util.List;

public interface GameSolver<R> {
    List<R> solve();
}
