package org.solitaire.model;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StateQueue<T> extends ConcurrentLinkedQueue<T> {
    public StateQueue(T state) {
        add(state);
    }

    public StateQueue(Collection<T> states) {
        addAll(states);
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
