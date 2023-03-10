package org.solitaire.model;

import java.util.Collection;
import java.util.List;
import java.util.Stack;

import static java.util.Comparator.comparingInt;

public class BoardStack<T extends Board<?, ?>> extends Stack<T> {
    public BoardStack(T board) {
        this(List.of(board));
    }

    public BoardStack(Collection<T> boards) {
        addAll(boards.stream().sorted(comparingInt(Board::score)).toList());
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
