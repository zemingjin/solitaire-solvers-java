package org.solitaire.pyramid;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.Card;
import org.solitaire.model.Path;
import org.solitaire.util.IOHelper;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.solitaire.pyramid.PyramidHelper.build;
import static org.solitaire.pyramid.PyramidHelper.getClickScore;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.ZERO;

@ExtendWith(MockitoExtension.class)
public class PyramidTest {
    protected static final String TEST_FILE = "games/pyramid/pyramid-121122-expert.txt";
    public static final String[] cards = IOHelper.loadFile(TEST_FILE);

    @Mock private PyramidBoard board;

    private Pyramid pyramid;

    @BeforeEach
    public void setup() {
        board = spy(board);
        pyramid = build(cards);
        pyramid.cloner(it -> board);
        pyramid.stack().clear();
        pyramid.addBoard(board);
    }

    @Test
    public void test_solve() {
        when(board.path()).thenReturn(new Path<>());
        when(board.isCleared()).thenReturn(true);

        var result = pyramid.solve();

        assertNotNull(result);
        assertEquals(ONE, result.size());
        assertEquals("[]", result.get(0).toString());
        assertEquals(ZERO, pyramid.totalScenarios());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_getMaxScore() {
        pyramid = build(cards);
        var result = pyramid.getMaxScore(pyramid.solve());
        var counts = getItemCounts((List<Card[]>) result.getRight());

        assertEquals(28, counts.size());
        assertNotNull(result);
        assertEquals(1290, result.getLeft());
        assertEquals(4956, pyramid.totalScenarios());
    }

    private List<String> getItemCounts(List<Card[]> list) {
        return range(0, list.size())
                .filter(i -> list.get(i).length > 1 || list.get(i)[0].isKing())
                .mapToObj(i -> Pair.of(getClickScore(i, list), list.get(i)))
                .map(it -> Pair.of(it.getLeft(), stream(it.getRight()).map(Card::raw).collect(joining(","))))
                .map(it -> it.getRight() + ": " + it.getLeft())
                .toList();
    }

}