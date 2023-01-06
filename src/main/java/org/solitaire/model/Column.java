package org.solitaire.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
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

    private void checkOpenAt(int at) {
        setOpenAt(openAt >= at ? openAt - 1 : openAt);
    }
}
