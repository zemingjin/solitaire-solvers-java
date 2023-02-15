package org.solitaire.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class BoardStack<T extends Board<?>> extends Stack<T> {
    public BoardStack(T board) {
        this(List.of(board));
    }

    public BoardStack(Collection<T> boards) {
        addAll(boards.stream().sorted(Comparator.comparingDouble(Board::score)).toList());
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
