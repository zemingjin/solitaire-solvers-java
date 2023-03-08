package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.util.IOHelper;

import java.util.Objects;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.util.CardHelper.useSuit;

public class GameBoardTest {
    protected static final String TEST_FILE = "games/spider/spider-122922-expert.txt";

    public final static String[] cards = IOHelper.loadFile(TEST_FILE);

    private GameBoard<String> board;

    private static GameBoard<String> mockState(GameBoard<String> board) {
        return new GameBoard<>(new Columns(board.columns), new Path<>(board.path), board.totalScore);
    }

    @BeforeEach
    public void setup() {
        useSuit(false);
        board = mockState(Objects.requireNonNull(build(cards).board()));
    }

    @Test
    public void test_equals() {
        var other = mockState(board);

        assertTrue(reflectionEquals(other, board));
    }

    @Test
    public void test_removeFromColumn_success() {
        var candidate = buildCandidate(0, COLUMN, board.columns.get(0).peek());

        board.removeFromColumn(candidate);
        assertEquals(5, board.columns.get(0).size());
    }

    @Test
    public void test_removeFromColumn_skip() {
        var candidate = buildCandidate(1, COLUMN, board.columns.get(0).peek());

        board.removeFromColumn(candidate);
        assertEquals(6, board.columns.get(0).size());
        assertEquals(6, board.columns.get(1).size());
    }
}