package org.solitaire.model;

import java.util.LinkedList;

public class Path<T> extends LinkedList<T> {
    public Path() {
        super();
    }

    public Path(Path<T> that) {
        super(that);
    }

    @Override
    public T peek() {
        assert size() > 0;

        return get(size() - 1);
    }
}
