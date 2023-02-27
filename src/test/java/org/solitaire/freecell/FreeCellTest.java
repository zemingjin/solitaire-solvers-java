package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Columns;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.freecell.FreeCellHelper.buildBoard;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
class FreeCellTest {
    @Mock private FreeCellBoard board;

    private MockFreeCell freeCell;

    @BeforeEach
    public void setup() {
        board = spy(board);
        freeCell = new MockFreeCell(buildBoard(FreeCellHelperTest.cards));
        freeCell.stack().clear();
        freeCell.addBoard(board);
        freeCell.cloner(it -> board);
    }

    @Test
    public void test_solve() {
        var candidate = buildCandidate(0, COLUMN, buildCard(0, "Ad"));

        when(board.isCleared()).thenReturn(false);
        when(board.findCandidates()).thenReturn(List.of(candidate));
        when(board.updateBoard(eq(candidate))).thenReturn(board);
        when(board.checkFoundationCandidates()).thenReturn(board);

        var result = freeCell.solve();

        assertNotNull(result);
        assertEquals(ZERO, result.size());
        assertEquals(ONE, freeCell.totalScenarios());
        assertTrue(freeCell.first);
        verify(board, times(ONE)).isCleared();
        verify(board, times(ONE)).findCandidates();
        verify(board, times(ONE)).updateBoard(eq(candidate));
    }

    @Test
    public void test_solve_cleared() {
        when(board.isCleared()).thenReturn(true);

        var result = freeCell.solve();

        verify(board, times(ONE)).isCleared();
        assertNotNull(result);
        assertEquals(ONE, result.size());
        assertEquals(ZERO, freeCell.totalScenarios());
    }

    @Test
    public void test_getMaxScore() {
        assertThrows(RuntimeException.class, () -> freeCell.getMaxScore(null));
    }

    static class MockFreeCell extends FreeCell {
        boolean first = true;

        MockFreeCell(Columns columns) {
            super(columns);
        }

        @Override
        public boolean addBoards(Collection<FreeCellBoard> boards) {
            if (first) {
                first = false;
                return super.addBoards(boards);
            }
            first = true;
            return false;
        }
    }
}