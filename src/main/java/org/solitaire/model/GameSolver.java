package org.solitaire.model;

import java.util.List;

public interface GameSolver<R> {
    List<List<R>> solve();

    List<List<R>> clickCard(R r);
}
