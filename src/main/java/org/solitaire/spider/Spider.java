package org.solitaire.spider;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("rawtypes")
@Getter
@Builder
public class Spider implements GameSolver {
    private List<Card[]> path;
    private List<List<Card>> board;
    private List<Card> deck;

    @Override
    public List<List> solve() {
        return null;
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return Pair.of(0, Collections.emptyList());
    }
}
