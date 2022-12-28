package org.solitaire.pyramid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Card;
import org.solitaire.model.CardHelper;
import org.solitaire.model.GameSolver;
import org.solitaire.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.solitaire.model.CardHelper.VALUES;
import static org.solitaire.model.CardHelper.cloneArray;
import static org.solitaire.model.CardHelper.cloneStack;
import static org.solitaire.model.CardHelper.isCleared;
import static org.solitaire.model.CardHelper.resizeArray;
import static org.solitaire.util.SolitaireHelper.incTotal;

@SuppressWarnings("rawtypes")
@Data
@Builder
public class Pyramid implements GameSolver {
    public static final int LAST_BOARD = 28;
    public static final int LAST_DECK = 52;
    public static final String KING = "K";
    public static final String ACE = "A";
    private static final int[] ROW_SCORES = new int[]{500, 250, 150, 100, 75, 50, 25};
    private static final IntUnaryOperator reverse = i -> LAST_BOARD - i - 1;
    private Card[] cards;
    private Stack<Card> deck;
    private Stack<Card> flippedDeck;
    private List<Card[]> path;
    private int recycleCount;

    public static Pyramid build(String[] cards) {
        return Optional.of(cards)
                .map(CardHelper::toCards)
                .map(Pyramid::buildBoard)
                .map(Pyramid::buildDeck)
                .orElseThrow();
    }

    private static Pyramid buildBoard(Card[] cards) {
        assert cards.length == 52 : "Invalid # of cards: " + cards.length;

        return Pyramid.builder()
                .cards(cards)
                .recycleCount(3)
                .path(new ArrayList<>())
                .flippedDeck(new Stack<>())
                .build();
    }

    private Pyramid buildDeck() {
        deck = new Stack<>();
        stream(cards, LAST_BOARD, LAST_DECK).forEach(card -> deck.push(card));

        cards = resizeArray(cards, LAST_BOARD);
        return this;
    }

    @Override
    public List<List> solve() {
        if (isCleared(cards)) {
            return singletonList(path);
        }
        incTotal();
        return Optional.of(findCardsOf13())
                .filter(CollectionUtil::isNotEmpty)
                .map(this::clickCards)
                .orElseGet(this::clickDeck);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return results.stream()
                .map(it -> (List<Card[]>) it)
                .map(this::getScore)
                .reduce(Pair.of(0, null), (a, b) -> a.getLeft() >= b.getLeft() ? a : b);
    }

    private List<List> clickDeck() {
        checkDeck();
        return drawCard()
                .map(this::clickCard)
                .orElseGet(Collections::emptyList);
    }

    protected void checkDeck() {
        if (deck.isEmpty() && getRecycleCount() > 1) {
            recycleDeck();
        }
    }

    private void recycleDeck() {
        while (!flippedDeck.isEmpty()) deck.push(flippedDeck.pop());
        recycleCount--;
    }

    protected Optional<Card[]> drawCard() {
        return Optional.of(deck)
                .filter(CollectionUtil::isNotEmpty)
                .map(Stack::peek)
                .map(it -> new Card[]{it});
    }

    private List<List> clickCards(List<Card[]> clickable) {
        return clickable.stream()
                .map(this::clickCard)
                .filter(CollectionUtil::isNotEmpty)
                .flatMap(List::stream)
                .toList();
    }

    public List<List> clickCard(Card[] cards) {
        return cloneBoard()
                .click(cards)
                .solve();
    }

    protected Pyramid click(Card[] clickable) {
        Card card = clickable[0];
        if (clickable.length == 1 && isDeckCard(card)) {
            handleDrawedCard(card);
        } else {
            handlePairedCards(clickable);
        }
        path.add(clickable);
        return this;
    }

    private void handleDrawedCard(Card card) {
        if (isDeckCard(card)) {
            if (card.equals(deck.peek())) {
                deck.pop();
                if (!card.isKing()) {
                    flippedDeck.push(card);
                }
            }
        }
    }

    private void handlePairedCards(Card[] clickable) {
        stream(clickable).forEach(card -> {
            if (isBoardCard(card)) {
                cards[card.at()] = null;
            } else if (isDeckCard(card)) {
                if (!deck.isEmpty() && card.equals(deck.peek())) {
                    deck.pop();
                } else if (!flippedDeck.isEmpty()) {
                    flippedDeck.pop();
                }
            }
        });
    }

    protected Pyramid cloneBoard() {
        return Pyramid.builder()
                .cards(cloneArray(cards))
                .path(new ArrayList<>(path))
                .flippedDeck(cloneStack(flippedDeck))
                .deck(cloneStack(deck))
                .recycleCount(recycleCount)
                .build();
    }

    protected List<Card[]> findCardsOf13() {
        var collect = new LinkedList<Card[]>();
        var openCards = findOpenCards();

        IntStream.range(0, openCards.length - 1)
                .peek(i -> checkKing(collect, openCards[i]))
                .forEach(i -> findPairsOf13(collect, openCards, i));

        checkKing(collect, openCards[openCards.length - 1]);
        return collect;
    }

    private void findPairsOf13(LinkedList<Card[]> collect, Card[] openCards, int i) {
        IntStream.range(i + 1, openCards.length)
                .filter(j -> isAddingTo13(openCards[i], openCards[j]))
                .forEach(j -> collect.add(new Card[]{openCards[i], openCards[j]}));
    }

    private void checkKing(List<Card[]> collect, Card card) {
        if (card.isKing()) {
            collect.add(0, new Card[]{card});
        }
    }

    private boolean isAddingTo13(Card a, Card b) {
        requireNonNull(a);
        requireNonNull(b);

        return VALUES.indexOf(a.value()) + VALUES.indexOf(b.value()) == 11;
    }

    protected Card[] findOpenCards() {
        return Optional.of(getBoardOpenCards())
                .map(this::checkDeckCards)
                .map(it -> it.toArray(Card[]::new))
                .orElseThrow();
    }

    private List<Card> checkDeckCards(List<Card> list) {
        if (!deck.isEmpty()) list.add(deck.peek());
        if (!flippedDeck.isEmpty()) list.add(flippedDeck.peek());
        return list;
    }

    private List<Card> getBoardOpenCards() {
        return IntStream.range(0, cards.length)
                .map(reverse)
                .mapToObj(i -> cards[i])
                .filter(Objects::nonNull)
                .filter(this::isOpen)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected boolean isOpen(Card card) {
        return isOpenBoardCard(card.at()) || isOpenDeckCard(card);
    }

    protected boolean isOpenDeckCard(Card card) {
        assert nonNull(card) : "Null is invalid value for card";

        return !deck.isEmpty() && card.equals(deck.peek());
    }

    protected boolean isOpenBoardCard(int at) {
        return 0 <= at && at < LAST_BOARD && nonNull(cards[at]) && isOpenAt(at);
    }

    protected boolean isOpenAt(int at) {
        var row = getRow(at);
        var coveredBy = at + row;

        return row == 7 || (isNull(cards[coveredBy]) && isNull(cards[coveredBy + 1]));
    }

    protected int getRow(int at) {
        assert 0 <= at && at < LAST_BOARD : "Invalid board card index: " + at;

        final var rowMax = new RowMax(LAST_BOARD);

        return IntStream.rangeClosed(1, 7)
                .map(i -> 7 - i + 1)
                .peek(i -> rowMax.setRowMax(rowMax.rowMax - i))
                .filter(i -> rowMax.getRowMax() <= at)
                .findFirst()
                .orElseThrow();
    }

    private Pair<Integer, List> getScore(List<Card[]> list) {
        return Pair.of(
                IntStream.range(0, list.size())
                        .map(i -> getClickScore(i, list))
                        .reduce(0, Integer::sum),
                list);
    }

    /*
     * Score Rules:
     * - 5: each pair
     * - 25 for row 7
     * - 50 for row 6
     * - 75 for row 5
     * - 100 for row 4
     * - 150 for row 3
     * - 250 for row 2
     * - 500 for clear board
     */
    protected int getClickScore(int at, List<Card[]> list) {
        var item = list.get(at);
        return Optional.of(item)
                .filter(it -> it.length == 1)
                .map(it -> it[0].isKing() ? 5 : 0)
                .orElseGet(() -> getRowClearingScore(at, list));
    }

    private int getRowClearingScore(int at, List<Card[]> list) {
        return Optional.ofNullable(getCardAt(list.get(at)))
                .filter(this::isBoardCard)
                .map(it -> getRow(it.at()))
                .map(row -> getScore(row, at, list))
                .orElse(5);
    }

    private int getScore(int row, int at, List<Card[]> list) {
        return 5 + (isRowCleared(row, at, list) ? scoreByRow(row) : 0);
    }

    private boolean isRowCleared(int row, int at, List<Card[]> list) {
        return IntStream.rangeClosed(0, at)
                .mapToObj(list::get)
                .flatMap(Stream::of)
                .filter(this::isBoardCard)
                .filter(it -> getRow(it.at()) == row)
                .count() == row;
    }

    protected Card getCardAt(Card[] cards) {
        var a = cards[0];
        var b = cards[1];
        return isBoardCard(a)
                ? isBoardCard(b)
                ? a.at() >= b.at() ? a : b
                : a
                : isBoardCard(b)
                ? b
                : a.at() >= b.at() ? a : b;
    }

    private boolean isBoardCard(Card card) {
        return isBoardCard(card.at());
    }

    private boolean isBoardCard(int at) {
        return 0 <= at && at < LAST_BOARD;
    }

    private boolean isDeckCard(Card card) {
        return !isBoardCard(card);
    }

    private int scoreByRow(int row) {
        assert 0 < row && row <= ROW_SCORES.length : "Invalid row number: " + row;
        return ROW_SCORES[row - 1];
    }

    @Data
    @AllArgsConstructor
    static class RowMax {
        private int rowMax;
    }
}