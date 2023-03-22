package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.toArray;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.THREE;
import static org.solitaire.util.CardHelperTest.ZERO;

public class GameBoardTest {
    protected static final String TEST_FILE = "games/spider/spider-122922-expert.txt";

    public static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private GameBoard board;

    private static GameBoard mockState(GameBoard board) {
        return new GameBoard(new Columns(board.columns), new Path<>(board.path), board.totalScore);
    }

    @BeforeEach
    void setup() {
        useSuit(false);
        board = mockState(Objects.requireNonNull(build(cards).board()));
    }

    @Test
    void test_add() {
        var result = board.add(new LinkedList<>(), new Candidate(toArray(card("Ad")), COLUMN, 0, COLUMN, 1));

        assertEquals(1, result.size());
        assertEquals("01:Ad", result.get(0).notation());
    }

    @Test
    void test_verify() {
        var ex = assertThrows(RuntimeException.class, () -> board.verify());
        assertEquals("'verify' not implemented", ex.getMessage());
    }

    @Test
    void test_findCandidates() {
        assertTrue(board.findCandidates().isEmpty());
    }

    @Test
    void test_updateBoard() {
        assertNull(board.updateBoard(null));
    }

    @Test
    void test_equals() {
        var other = mockState(board);

        assertTrue(reflectionEquals(other, board));
    }

    @Test
    void test_removeFromColumn_success() {
        var candidate = buildCandidate(0, COLUMN, board.columns.get(0).peek());

        board.removeFromColumn(candidate);
        assertEquals(5, board.columns.get(0).size());
    }

    @Test
    void test_removeFromColumn_skip() {
        var candidate = buildCandidate(1, COLUMN, board.columns.get(0).peek());

        board.removeFromColumn(candidate);
        assertEquals(6, board.columns.get(0).size());
        assertEquals(6, board.columns.get(1).size());
    }

    @Test
    void test_candidateToEmptyColumn() {
        assertThrows(RuntimeException.class, () -> board.candidateToEmptyColumn(null, ZERO, ZERO));
    }

    @Test
    void test_getOrderedCards() {
        board.isInSequence(Card::isHigherOfSameSuit);
        var result = board.getOrderedCards().apply(board.column(ZERO));

        assertEquals(ONE, result.length);

        board.column(ZERO).addAll(List.of(card("9h"), card("8h")));
        result = board.getOrderedCards().apply(board.column(ZERO));

        assertEquals(THREE, result.length);
    }

}