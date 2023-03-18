package org.solitaire.model;

import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.function.Predicate;

public interface Board<R, U> {
    Predicate<List<?>> listIsNotEmpty = ObjectUtils::isNotEmpty;

    boolean isSolved();

    List<R> path();

    List<String> verify();

    int score();

    List<U> findCandidates();

    Board<R, U> updateBoard(U candidate);
}
