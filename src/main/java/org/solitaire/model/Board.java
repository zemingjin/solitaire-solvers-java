package org.solitaire.model;

import java.util.List;

public interface Board<R> {
    boolean isCleared();

    List<R> path();

    double score();

}
