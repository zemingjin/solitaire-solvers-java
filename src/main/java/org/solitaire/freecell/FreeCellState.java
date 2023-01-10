package org.solitaire.freecell;

import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.GameState;
import org.solitaire.model.Path;

public class FreeCellState extends GameState<Card> {
    public FreeCellState(Columns columns, Path<Card> path) {
        super(columns, path, 0);
    }
}
