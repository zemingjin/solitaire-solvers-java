package org.solitaire.model;

import java.util.List;

public interface Board<R, U> {
    boolean isSolved();

    List<R> path();

    List<String> verify();

    int score();

    Board<R, U> updateBoard(U candidate);
}
