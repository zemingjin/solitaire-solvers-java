package org.solitaire.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.EmptyStackException;

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
        setOpenAt(openAt >= at ? openAt - 1 : openAt);
        return super.remove(at);
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
            if (openAt == size() - 1) {
                openAt--;
            }
            return remove(size() - 1);
        }
        throw new EmptyStackException();
    }
}
