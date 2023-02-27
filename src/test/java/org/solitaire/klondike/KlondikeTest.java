package org.solitaire.klondike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.klondike.Klondike.SOLUTION_LIMIT;
import static org.solitaire.klondike.KlondikeHelper.build;
import static org.solitaire.klondike.KlondikeHelperTest.CARDS;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class KlondikeTest {
    @Mock private KlondikeBoard board;

    private Klondike klondike;

    @BeforeEach
    public void setup() {
        board = spy(board);
        CardHelper.useSuit = false;
        klondike = mockKlondike();
    }

    @Test
    void test_isContinuing() {
        assertTrue(klondike.isContinuing());
        range(0, SOLUTION_LIMIT).forEach(i -> klondike.solutions().add(List.of()));
        assertFalse(klondike.isContinuing());
    }

    @Test
    public void test_solve_cleared() {
        when(board.isCleared()).thenReturn(true);
        when(board.path()).thenReturn(new Path<>());
        var result = klondike.solve();

        assertNotNull(result);
        assertEquals(ONE, result.size());
        assertTrue(result.get(0).isEmpty());
        assertEquals(ZERO, klondike.totalScenarios());
    }

    @Test
    public void test_solve_uncleared() {
        var candidates = List.of(
                buildCandidate(4, COLUMN, buildCard(0, "Ac")),
                buildCandidate(3, COLUMN, buildCard(1, "3d")));
        when(board.isCleared()).thenReturn(false);
        when(board.findCandidates()).thenReturn(candidates);
        when(board.updateBoard(any())).thenReturn(null);
        when(board.drawDeckCards()).thenReturn(null);

        var result = klondike.solve();

        assertEquals("[]", result.toString());
        verify(board, times(ONE)).isCleared();
        verify(board, times(ONE)).findCandidates();
        verify(board, times(TWO)).updateBoard(any());
        assertEquals(1, klondike.totalScenarios());
    }

    @Test
    public void test_solve_drawDeck() {
        when(board.isCleared()).thenReturn(false);
        when(board.findCandidates()).thenReturn(emptyList());
        when(board.drawDeckCards()).thenReturn(null);

        klondike.solve();

        verify(board, times(ONE)).isCleared();
        verify(board, times(ONE)).findCandidates();
        verify(board, times(ONE)).drawDeckCards();
        assertEquals(1, klondike.totalScenarios());
    }

    @Test
    public void test_getMaxScore() {
        var result = klondike.getMaxScore(null);

        assertNull(result);
    }

    @Test
    public void test_solve() {
        klondike = build(CARDS);

        var result = klondike.solve();

        assertNotNull(result);
        assertEquals(SOLUTION_LIMIT, result.size());
    }

    private Klondike mockKlondike() {
        var klondike = new Klondike(new Columns(), new Deck(), new ArrayList<>());

        klondike.stack().clear();
        klondike.addBoard(board);
        klondike.cloner(it -> board);

        return klondike;
    }
}