package org.solitaire.pyramid;

import org.apache.commons.lang3.ObjectUtils;
import org.solitaire.model.Card;
import org.solitaire.model.Path;
import org.solitaire.model.Board;
import org.solitaire.util.CardHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.pyramid.PyramidHelper.LAST_BOARD;
import static org.solitaire.pyramid.PyramidHelper.LAST_BOARD_INDEX;
import static org.solitaire.pyramid.PyramidHelper.isBoardCard;
import static org.solitaire.pyramid.PyramidHelper.row;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.cloneStack;

public class PyramidBoard implements Board<Card[]> {
    private final Card[] cards;
    private final Stack<Card> deck;
    private final Stack<Card> flippedDeck;
    private final Path<Card[]> path;
    private int recycleCount;
    private double score = 0;

    public PyramidBoard(Card[] cards, Stack<Card> deck, Stack<Card> flippedDeck, Path<Card[]> path, int recycleCount) {
        this.cards = cards;
        this.deck = deck;
        this.flippedDeck = flippedDeck;
        this.path = path;
        this.recycleCount = recycleCount;
    }

    public PyramidBoard(PyramidBoard that) {
        this(cloneArray(that.cards),
                cloneStack(that.deck),
                cloneStack(that.flippedDeck),
                new Path<>(that.path),
                that.recycleCount);
    }

    public Card[] cards() {
        return cards;
    }

    public Stack<Card> deck() {
        return deck;
    }

    public Stack<Card> flippedDeck() {
        return flippedDeck;
    }

    public Path<Card[]> path() {
        return path;
    }

    public int recycleCount() {
        return recycleCount;
    }

    public void recycleCount(int recycleCount) {
        this.recycleCount = recycleCount;
    }

    public boolean isCleared() {
        return CardHelper.isCleared(cards);
    }

    /***************************************************************************************************************
     * Find Candidates
     **************************************************************************************************************/
    protected List<Card[]> findCandidates() {
        var collect = new LinkedList<Card[]>();
        var openCards = findOpenCards();

        range(0, openCards.size() - 1)
                .peek(i -> checkKing(collect, openCards.get(i)))
                .forEach(i -> findPairsOf13(collect, openCards, i));

        if (ObjectUtils.isNotEmpty(openCards)) {
            checkKing(collect, openCards.get(openCards.size() - 1));
        }
        return collect;
    }

    private void findPairsOf13(LinkedList<Card[]> collect, List<Card> openCards, int i) {
        range(i + 1, openCards.size())
                .mapToObj(j -> new Card[]{openCards.get(i), openCards.get(j)})
                .filter(it -> isAddingTo13(it[0], it[1]))
                .forEach(collect::add);
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

    protected List<Card> findOpenCards() {
        return Optional.of(getBoardOpenCards())
                .map(this::getDeckCards)
                .orElseThrow();
    }

    private List<Card> getDeckCards(List<Card> list) {
        if (!deck.isEmpty()) list.add(deck.peek());
        if (!flippedDeck.isEmpty()) list.add(flippedDeck.peek());
        return list;
    }

    private List<Card> getBoardOpenCards() {
        return rangeClosed(0, LAST_BOARD_INDEX)
                .map(i -> LAST_BOARD_INDEX - i)
                .mapToObj(i -> cards[i])
                .filter(Objects::nonNull)
                .filter(this::isOpen)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected boolean isOpen(Card card) {
        return isOpenBoardCard(card.at()) || isOpenDeckCard(card);
    }

    protected boolean isOpenDeckCard(@Nonnull Card card) {
        return !deck.isEmpty() && card.equals(deck.peek());
    }

    protected boolean isOpenBoardCard(int at) {
        return 0 <= at && at < LAST_BOARD && nonNull(cards[at]) && isOpenAt(at);
    }

    protected boolean isOpenAt(int at) {
        var row = row(at);
        var coveredBy = at + row;

        return row == 7 || (isNull(cards[coveredBy]) && isNull(cards[coveredBy + 1]));
    }

    /***************************************************************************************************************
     * Update State
     **************************************************************************************************************/
    public PyramidBoard drawDeckCards() {
        if (checkDeck()) {
            flippedDeck.push(deck.pop());
            return this;
        }
        return null;
    }

    protected boolean checkDeck() {
        if (deck.isEmpty() && recycleCount() > 1) {
            while (!flippedDeck.isEmpty()) deck.push(flippedDeck.pop());
            recycleCount--;
            return true;
        }
        return isNotEmpty(deck);
    }

    protected PyramidBoard updateBoard(Card[] candidate) {
        stream(candidate).forEach(this::removeCardFromBoard);
        path.add(candidate);
        return this;
    }

    private void removeCardFromBoard(Card card) {
        if (isBoardCard(card)) {
            cards[card.at()] = null;
        } else if (!surePop(card, deck)) {
            surePop(card, flippedDeck);
        }
    }

    private boolean surePop(Card card, Stack<Card> stack) {
        if (isNotEmpty(stack) && stack.peek().equals(card)) {
            stack.pop();
            return true;
        }
        return false;
    }

    public double score() {
        if (score == 0) {
            score = findCandidates().size();
        }
        return score;
    }
}
