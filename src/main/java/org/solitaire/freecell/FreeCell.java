package org.solitaire.freecell;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Path;

import java.util.Arrays;
import java.util.List;

/**
 * Heineman’s Staged Deepening (HSD)
 * Properties:
 * - there are usually multiple ways to arrive at the solution for a given FreeCell deal
 * - there are many cases where moving a card is irreversible, such as a card is moved to the foundation
 */
@SuppressWarnings("rawtypes")
@Getter
public class FreeCell extends FreeCellState implements GameSolver {
    private final Card[] freeCells;
    private final Card[] foundations;

    @Builder
    public FreeCell(Columns columns, Path<Card> path, Card[] freeCells, Card[] foundations) {
        super(columns, path);
        this.freeCells = freeCells;
        this.foundations = foundations;
    }

    public FreeCell(FreeCell freeCell) {
        super(new Columns(freeCell.columns), new Path<>(freeCell.path));
        this.freeCells = Arrays.copyOf(freeCell.freeCells, freeCell.freeCells.length);
        this.foundations = Arrays.copyOf(freeCell.foundations, freeCell.foundations.length);
    }

    @Override
    public List<List> solve() {
        if (isCleared()) {
            return List.of(path);
        }
        return null;
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }

}