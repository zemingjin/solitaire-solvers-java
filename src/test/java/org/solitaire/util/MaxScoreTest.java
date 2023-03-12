package org.solitaire.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("rawtypes")
class MaxScoreTest {
    private MaxScore maxScore;
    private int value;

    @BeforeEach
    void setup() {
        maxScore = new MaxScore(this::mockScore);
    }

    @Test
    void test_score() {
        value = 6;
        var result = maxScore.score(List.of(1, 2, 3));
        assertEquals(6, result.getLeft());
        assertEquals("[1, 2, 3]", result.getRight().toString());

        value = 6;
        result = maxScore.score(List.of(6));
        assertEquals(6, result.getLeft());
        assertEquals("[1, 2, 3]", result.getRight().toString());

        value = 5;
        result = maxScore.score(List.of(5));
        assertEquals(6, result.getLeft());
        assertEquals("[1, 2, 3]", result.getRight().toString());

        value = 7;
        result = maxScore.score(List.of(7));
        assertEquals(7, result.getLeft());
        assertEquals("[7]", result.getRight().toString());
    }

    private Pair<Integer, List> mockScore(List list) {
        return Pair.of(value, list);
    }
}