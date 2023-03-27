package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.lang.Integer.MIN_VALUE;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.candidate;
import static org.solitaire.model.Candidate.columnToColumn;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.useSuit;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.THREE;
import static org.solitaire.util.CardHelperTest.ZERO;

public class GameBoardTest {
    protected static final String TEST_FILE = "games/spider/spider-expert-122922.txt";
    public static final String[] cards = IOHelper.loadFile(TEST_FILE);

    private GameBoard board;

    private static GameBoard mockState(GameBoard board) {
        var mock = new GameBoard(new Columns(board.columns), new Path<>(board.path), board.totalScore());

        mock.isInSequence(board.isInSequence());
        return mock;
    }

    @BeforeEach
    void setup() {
        useSuit(false);
        board = mockState(Objects.requireNonNull(build(cards).board()));
    }

    @Test
    void test_add() {
        var result = board.add(new LinkedList<>(), candidate(card("Ad"), COLUMN, 0, COLUMN, 1));

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
        var candidate = columnToColumn(board.peek(0), 0, 1);

        board.removeFromColumn(candidate);
        assertEquals(5, board.columns.get(0).size());
    }

    @Test
    void test_removeFromColumn_skip() {
        var candidate = columnToColumn(board.peek(0), 1, 0);

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
        var result = board.getOrderedCards(board.column(ZERO));

        assertEquals(ONE, result.length);

        board.column(ZERO).addAll(List.of(card("9h"), card("8h")));
        result = board.getOrderedCards(board.column(ZERO));

        assertEquals(THREE, result.length);
    }

    @Test
    void test_isNotScored() {
        assertTrue(board.isNotScored());

        board.score(0);
        assertFalse(board.isNotScored());
    }

    @Test
    void test_resetCache() {
        assertEquals(1, board.getOrderedCards(0).length);
        assertNotNull(board.orderedCards(0));
        board.score(123);

        board.resetCache();
        assertNull(board.orderedCards(0));
        assertEquals(MIN_VALUE, board.score());
    }

}