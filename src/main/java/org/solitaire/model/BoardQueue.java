package org.solitaire.model;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BoardQueue<T> extends ConcurrentLinkedQueue<T> {
    public BoardQueue(T board) {
        add(board);
    }

    public BoardQueue(Collection<T> boards) {
        addAll(boards);
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
