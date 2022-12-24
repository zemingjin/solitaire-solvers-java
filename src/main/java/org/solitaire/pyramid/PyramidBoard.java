package org.solitaire.pyramid;

import lombok.Builder;
import lombok.Getter;
import org.solitaire.model.Card;
import org.solitaire.model.CardHelper;
import org.solitaire.model.GameSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.solitaire.model.CardHelper.VALUES;
import static org.solitaire.model.CardHelper.cloneArray;
import static org.solitaire.model.CardHelper.cloneList;
import static org.solitaire.model.CardHelper.isCleared;

@Getter
@Builder
public class PyramidBoard implements GameSolver {
    public static final int LAST_BOARD = 28;
    public static final int LAST_DECK = 52;
    public static final char KING = 'K';

    private Card[] cards;
    private List<Card[]> wastePile;
    private int recycleCount;

    @SuppressWarnings("rawtypes")
    @Override
    public List<List> solve() {
        if (isCleared(cards, LAST_BOARD)) {
            return singletonList(wastePile);
        }
        return Optional.of(findCardsOf13())
                .filter(it -> !it.isEmpty())
                .map(this::clickCards)
                .orElseGet(this::clickDeck);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<List> showDetails(List<List> results) {
        CardHelper.checkShortestPath(results);
        return results;
    }

    @SuppressWarnings("rawtypes")
    private List<List> clickDeck() {
        checkDeck();
        return drawCard()
                .map(this::clickCard)
                .orElseGet(Collections::emptyList);
    }

    protected void checkDeck() {
        if (isNull(cards[LAST_BOARD]) && getRecycleCount() > 0) {
            recycleDeck();
        }
    }

    private void recycleDeck() {
        wastePile.stream()
                .flatMap(Stream::of)
                .filter(it -> it.getAt() >= LAST_BOARD)
                .forEach(it -> cards[it.getAt()] = it);
        recycleCount--;
    }

    private final IntUnaryOperator reverse = i -> LAST_BOARD + LAST_DECK - i - 1;

    protected Optional<Card[]> drawCard() {
        return IntStream.range(LAST_BOARD, LAST_DECK)
                .map(reverse)
                .filter(this::isOpenDeckCard)
                .mapToObj(i -> new Card[]{cards[i]})
                .findFirst();
    }

    @SuppressWarnings("rawtypes")
    private List<List> clickCards(List<Card[]> clickable) {
        return clickable.stream()
                .map(this::clickCard)
                .filter(it -> !it.isEmpty())
                .flatMap(List::stream)
                .toList();
    }

    @SuppressWarnings("rawtypes")
    public List<List> clickCard(Card[] cards) {
        return cloneBoard()
                .click(cards)
                .solve();
    }

    protected PyramidBoard click(Card[] clickable) {
        Stream.of(clickable).forEach(it -> cards[it.getAt()] = null);
        wastePile.add(clickable);
        return this;
    }

    protected PyramidBoard cloneBoard() {
        return PyramidBoard.builder()
                .cards(cloneArray(cards))
                .wastePile(cloneList(wastePile))
                .recycleCount(recycleCount)
                .build();
    }

    protected List<Card[]> findCardsOf13() {
        var collect = new LinkedList<Card[]>();
        var openCards = findOpenCards();

        IntStream.range(0, openCards.length - 1)
                .peek(i -> checkKing(openCards[i], collect))
                .forEach(i -> findPairsOf13(collect, openCards, i));

        checkKing(openCards[openCards.length - 1], collect);
        return collect;
    }

    private void findPairsOf13(LinkedList<Card[]> collect, Card[] openCards, int i) {
        IntStream.range(i + 1, openCards.length)
                .filter(j -> isAddingTo13(openCards[i], openCards[j]))
                .forEach(j -> collect.add(new Card[]{openCards[i], openCards[j]}));
    }

    private void checkKing(Card card, List<Card[]> collect) {
        if (isKing(card)) {
            if (collect.isEmpty()) {
                collect.add(new Card[]{card});
            } else if (isKing(collect.get(0)[0])) {
                collect.set(0, merge(collect.get(0), card));
            } else {
                collect.add(0, new Card[]{card});
            }
        }
    }

    private boolean isKing(Card card) {
        return card.getValue() == KING;
    }

    protected Card[] merge(Card[] mergeTo, Card toMerge) {
        var buf = new Card[mergeTo.length + 1];
        System.arraycopy(mergeTo, 0, buf, 0, mergeTo.length);
        buf[mergeTo.length] = toMerge;
        return buf;
    }

    private boolean isAddingTo13(Card a, Card b) {
        requireNonNull(a);
        requireNonNull(b);

        return VALUES.indexOf(a.getValue()) + VALUES.indexOf(b.getValue()) == 11;
    }

    protected Card[] findOpenCards() {
        return stream(cards)
                .filter(Objects::nonNull)
                .filter(this::isOpen)
                .toArray(Card[]::new);
    }

    protected boolean isOpen(Card card) {
        var at = card.getAt();
        return isOpenBoardCard(at) || isOpenDeckCard(at);
    }

    protected boolean isOpenBoardCard(int at) {
        return 0 <= at && at < LAST_BOARD && isOpenAt(at);
    }

    protected boolean isOpenAt(int at) {
        var row = getRow(at);
        var coveredBy = at + row;

        return row == 7 || (isNull(cards[coveredBy]) && isNull(cards[coveredBy + 1]));
    }

    protected int getRow(int at) {
        assert 0 <= at && at < LAST_BOARD : "Invalid board card index: " + at;

        var rowMax = LAST_BOARD;

        for (int row = 7; row > 0; row--) {
            if (rowMax - row <= at) {
                return row;
            } else {
                rowMax -= row;
            }
        }
        return 0;
    }

    protected boolean isOpenDeckCard(int at) {
        return LAST_BOARD <= at && at < LAST_DECK && nonNull(cards[at]) &&
                (at == LAST_DECK - 1 || isNull(cards[at + 1]));
    }

    public static PyramidBoard build(String[] cards) {
        return Optional.of(cards)
                .map(CardHelper::toCards)
                .map(PyramidBoard::buildBoard)
                .orElseThrow();
    }

    private static PyramidBoard buildBoard(Card[] cards) {
        return PyramidBoard.builder().cards(cards).recycleCount(3).wastePile(new ArrayList<>()).build();
    }

    @Override
    public boolean equals(Object obj) {
        return Optional.ofNullable(obj)
                .filter(it -> it instanceof PyramidBoard)
                .map(PyramidBoard.class::cast)
                .filter(it -> Arrays.equals(cards, it.cards))
                .filter(it -> wastePile.equals(it.wastePile))
                .filter(it -> recycleCount == it.recycleCount)
                .isPresent();
    }
}