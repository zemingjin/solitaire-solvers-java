package org.solitaire.model;

import java.util.concurrent.ConcurrentLinkedQueue;

public class StateQueue<T> extends ConcurrentLinkedQueue<T> {
    public StateQueue() {
    }

    public StateQueue(T state) {
        add(state);
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
