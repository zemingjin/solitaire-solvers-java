package org.solitaire.model;

import java.util.function.Function;

public interface GameBuilder<T> extends Function<String[], GameSolver<T>> {
}
