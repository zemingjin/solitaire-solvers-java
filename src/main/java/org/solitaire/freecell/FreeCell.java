package org.solitaire.freecell;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.List;

/**
 * G. Heineman’s Staged Deepening (HSD)
 * 1. Performing a depth-first search with a depth-bound of six.
 * 2. Apply the heuristic to evaluate all the board states exactly six moves away from the initial board state.
 * 3. Take the board state with the best score and do another depth-first search with a depth-bound of six from
 * that state.
 * 4. Repeat steps 2-3 and throw away the rest until a solution or some limit is reached.
 */
public class FreeCell extends SolveExecutor<String, Candidate, FreeCellBoard> {
    public FreeCell(Columns columns) {
        super(new FreeCellBoard(columns, new Path<>(), new Card[4], new Card[4]), FreeCellBoard::new);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }
}