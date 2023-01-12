package org.solitaire.freecell;

import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.GameState;
import org.solitaire.model.Path;

import java.util.List;

public class FreeCellState extends GameState<Card> {
    public FreeCellState(Columns columns, Path<Card> path) {
        super(columns, path, 0);
    }

    public FreeCellState(FreeCellState that) {
        this(new Columns(that.columns), new Path<>(that.path));
    }

    public boolean isCleared() {
        return columns.stream().allMatch(List::isEmpty);
    }
}
