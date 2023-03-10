package org.solitaire.model;

import java.util.List;

public interface Board<R, U> {
    boolean isCleared();

    List<R> path();

    List<String> verify();

    int score();

    Board<R, U> updateBoard(U candidate);
}
