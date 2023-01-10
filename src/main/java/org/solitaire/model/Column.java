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
        setOpenAt(column.getOpenAt());
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

    public Column subList(Card card) {
        assert contains(card);

        var sub = new Column();

        sub.addAll(super.subList(0, lastIndexOf(card)));
        return sub;
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

    public int getOpenAt() {
        return openAt;
    }

    public void setOpenAt(int openAt) {
        this.openAt = openAt;
    }

    private void checkOpenAt(int at) {
        setOpenAt(openAt >= at ? at - 1 : openAt);
    }
}
