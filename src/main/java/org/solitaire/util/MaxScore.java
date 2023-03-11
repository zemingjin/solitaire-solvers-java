package org.solitaire.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.isNull;

@SuppressWarnings("rawtypes")
public class MaxScore {
    private Pair<Integer, List> maxScore;
    private Function<List, Pair<Integer, List>> scorer;

    public MaxScore(Function<List, Pair<Integer, List>> scorer) {
        scorer(scorer);
    }

    public Pair<Integer, List> maxScore() {
        return maxScore;
    }

    private void maxScore(Pair<Integer, List> maxScore) {
        this.maxScore = maxScore;
    }

    public Function<List, Pair<Integer, List>> scorer() {
        return scorer;
    }

    public void scorer(Function<List, Pair<Integer, List>> scorer) {
        this.scorer = scorer;
    }

    public Pair<Integer, List> score(List list) {
        Optional.ofNullable(list)
                .map(scorer())
                .filter(it -> isNull(maxScore()) || maxScore().getLeft() < it.getLeft())
                .ifPresent(this::maxScore);
        return maxScore();
    }

}
