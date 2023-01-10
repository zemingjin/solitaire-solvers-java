package org.solitaire.pyramid;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Card;
import org.solitaire.util.IOHelper;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.pyramid.Pyramid.LAST_BOARD;
import static org.solitaire.pyramid.Pyramid.build;

class PyramidTest {
    private static final String TEST_FILE = "games/pyramid/pyramid-121922-medium.txt";

    private Pyramid pyramid;

    @BeforeEach
    public void setup() {
        pyramid = build(IOHelper.loadFile(TEST_FILE));
    }

    @Test
    public void test_solve() {
        pyramid = build(IOHelper.loadFile("games/pyramid/pyramid-121122-expert.txt"));
        var result = pyramid.solve();

        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_getMaxScore() {
        var result = pyramid.getMaxScore(pyramid.solve());
        var counts = getItemCounts((List<Card[]>) result.getRight());

        assertEquals(23, counts.size());
        assertNotNull(result);
        assertEquals(1265, result.getLeft());
    }

    private List<String> getItemCounts(List<Card[]> list) {
        return IntStream.range(0, list.size())
                .filter(i -> list.get(i).length > 1 || list.get(i)[0].isKing())
                .mapToObj(i -> Pair.of(pyramid.getClickScore(i, list), list.get(i)))
                .map(it -> Pair.of(it.getLeft(), stream(it.getRight()).map(Card::raw).collect(joining(","))))
                .map(it -> it.getRight() + ": " + it.getLeft())
                .toList();
    }

    @Test
    public void test_getCardAt() {
        var a = pyramid.getCards()[27];
        var b = pyramid.getDeck().peek();
        var card = pyramid.getCardAt(new Card[]{a, b});

        assertSame(a, card);

        card = pyramid.getCardAt(new Card[]{b, a});
        assertSame(a, card);
    }

    @Test
    public void test_clickCard() {
        var result = pyramid.clickCard(new Card[]{pyramid.getCards()[24]});

        assertNotNull(result);
    }

    @Test
    public void test_click() {
        var card = pyramid.getDeck().peek();
        var c = new Card[]{card};

        assertEquals(51, card.at());
        assertFalse(card.isKing());

        pyramid.click(c);

        assertFalse(pyramid.isOpen(card));
        assertEquals(1, pyramid.getFlippedDeck().size());
        assertTrue(pyramid.getPath().contains(c));
        assertEquals(1, pyramid.getPath().size());

        card = pyramid.getDeck().peek();
        c = new Card[]{card, pyramid.getCards()[LAST_BOARD - 1]};

        assertEquals(50, card.at());
        assertFalse(card.isKing());

        pyramid.click(c);

        assertFalse(pyramid.isOpen(card));
        assertEquals(1, pyramid.getFlippedDeck().size());
        assertTrue(pyramid.getPath().contains(c));
        assertEquals(2, pyramid.getPath().size());
    }

    @Test
    public void test_recycle() {
        while (!pyramid.getDeck().isEmpty()) {
            pyramid.getFlippedDeck().push(pyramid.getDeck().pop());
        }
        assertFalse(pyramid.drawCard().isPresent());

        pyramid.checkDeck();
        assertTrue(pyramid.drawCard().isPresent());
    }

    @Test
    public void test_cloneBoard() {
        var cloned = pyramid.cloneBoard();
        assertEquals(pyramid, cloned);
    }

    @Test
    public void test_findCardsAddingTo13() {
        var cards = pyramid.findCardsOf13();

        assertNotNull(cards);
        assertEquals(3, cards.size());
    }

    @Test
    public void test_findOpenCards() {
        var cards = pyramid.findOpenCards();

        assertNotNull(cards);
        assertEquals(8, cards.length);
    }

    @Test
    void test_isOpenDeckCard() {
        assertTrue(pyramid.isOpenDeckCard(pyramid.getDeck().peek()));
        assertFalse(pyramid.isOpenDeckCard(pyramid.getDeck().get(0)));
    }

    @Test
    void test_isOpenAt() {
        assertTrue(pyramid.isOpenAt(27));
        assertTrue(pyramid.isOpenAt(21));
        assertFalse(pyramid.isOpenAt(20));

        IntStream.range(21, LAST_BOARD)
                .forEach(i -> pyramid.getCards()[i] = null);

        assertTrue(pyramid.isOpenAt(20));
        assertTrue(pyramid.isOpenAt(19));
        assertTrue(pyramid.isOpenAt(18));
        assertTrue(pyramid.isOpenAt(17));
        assertTrue(pyramid.isOpenAt(16));
        assertTrue(pyramid.isOpenAt(15));
        assertFalse(pyramid.isOpenAt(14));

        IntStream.range(19, 21)
                .forEach(i -> pyramid.getCards()[i] = null);

        assertTrue(pyramid.isOpenAt(14));
    }

    @Test
    void test_getRow() {
        assertEquals(7, pyramid.getRow(27));
        assertEquals(6, pyramid.getRow(19));
        assertEquals(5, pyramid.getRow(12));
        assertEquals(4, pyramid.getRow(7));
        assertEquals(3, pyramid.getRow(3));
        assertEquals(2, pyramid.getRow(1));
        assertEquals(1, pyramid.getRow(0));
    }

}