package org.solitaire.freecell;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.Collections;
import java.util.List;

/**
 * Heineman’s Staged Deepening (HSD)
 * Properties:
 * - there are usually multiple ways to arrive at the solution for a given FreeCell deal
 * - there are many cases where moving a card is irreversible, such as a card is moved to the foundation
 */
@SuppressWarnings("rawtypes")
@Data
@Builder
public class FreeCell implements GameSolver {
    private List<List<Card>> board;
    private Card[] freeCells;
    private Card[] foundation;
    private List<Card> path;

    @Override
    public List<List> solve() {
        if (isCleared()) {
            return Collections.singletonList(path);
        }
        return null;
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }

    private boolean isCleared() {
        return board.stream()
                .filter(List::isEmpty)
                .count() == board.size();
    }
}