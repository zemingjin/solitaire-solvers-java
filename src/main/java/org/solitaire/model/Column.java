package org.solitaire.model;

import java.util.ArrayList;
import java.util.EmptyStackException;

public class Column extends ArrayList<Card> {
    private int openAt;

    public Column() {
        super(8);
    }

    public Column(Column column) {
        super(column);
        openAt(column.openAt());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && openAt() == ((Column) obj).openAt();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public Card peek() {
        if (isNotEmpty()) {
            return get(size() - 1);
        }
        throw new EmptyStackException();
    }

    public Card pop() {
        if (isNotEmpty()) {
            return remove(size() - 1);
        }
        throw new EmptyStackException();
    }

    public int openAt() {
        if (openAt >= size()) {
            openAt(size() - 1);
        }
        return openAt;
    }

    public Column openAt(int openAt) {
        this.openAt = openAt;
        return this;
    }
}
