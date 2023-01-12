package org.solitaire.freecell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.solitaire.util.CardHelperTest.ONE;

@ExtendWith(MockitoExtension.class)
class FreeCellTest {
    private FreeCell freeCell;
    @Mock
    private FreeCellState state;

    @BeforeEach
    public void setup() {
        state = spy(state);
        freeCell = FreeCellHelper.build(FreeCellHelperTest.cards).initState(state);
        freeCell.cloner(it -> state);
    }

    @Test
    public void test_solve_cleared() {
        when(state.isCleared()).thenReturn(true);

        var result = freeCell.solve();

        verify(state, times(ONE)).isCleared();
        assertNotNull(result);
    }

    @Test
    public void test_getMaxScore() {
        assertThrows(RuntimeException.class, () -> freeCell.getMaxScore(null));
    }

}