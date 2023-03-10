package org.solitaire.model;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

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
        return super.equals(obj) && openAt == ((Column) obj).openAt;
    }

    @Override
    public Card remove(int at) {
        checkOpenAt(at);

        return super.remove(at);
    }

    @Override
    public List<Card> subList(int fromIndex, int toIndex) {
        checkOpenAt(fromIndex);

        return super.subList(fromIndex, toIndex);
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
        return openAt;
    }

    public Column openAt(int openAt) {
        this.openAt = openAt;
        return this;
    }

    private void checkOpenAt(int at) {
        openAt(openAt >= at ? at - 1 : openAt);
    }
}
