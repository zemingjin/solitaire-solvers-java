package org.solitaire.tripeaks;

import org.solitaire.model.Board;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.util.CardHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_BOARD;
import static org.solitaire.tripeaks.TriPeaksHelper.LAST_DECK;
import static org.solitaire.tripeaks.TriPeaksHelper.calcCoveredAt;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.cloneArray;

public class TriPeaksBoard implements Board<Card, Card> {
    private static final int C = LAST_BOARD + LAST_DECK - 1;
    private static final IntUnaryOperator reverse = i -> C - i;
    private static final IntUnaryOperator reverseBoard = i -> LAST_BOARD - i - 1;

    private Card[] cards;
    private Column wastePile;
    private transient int score = MIN_VALUE;

    public TriPeaksBoard(Card[] cards, Column wastePile) {
        cards(cards);
        wastePile(wastePile);
    }

    protected TriPeaksBoard(TriPeaksBoard that) {
        this(cloneArray(that.cards), that.wastePile);
    }

    @Override
    public boolean isSolved() {
        return CardHelper.isCleared(cards, 0, LAST_BOARD);
    }

    @Override
    public List<Card> path() {
        return wastePile;
    }

    /************************************************************************************************************
     * Search
     ***********************************************************************************************************/
    @Override
    public List<Card> findCandidates() {
        return Optional.of(getCandidatesViaWastePile())
                .filter(listIsNotEmpty)
                .orElseGet(this::getCandidatesFromDeck);
    }

    private List<Card> getCandidatesFromDeck() {
        return Optional.ofNullable(getTopDeckCard())
                .map(List::of)
                .orElseGet(Collections::emptyList);
    }

    private List<Card> getCandidatesViaWastePile() {
        return Optional.of(wastePile.peek())
                .map(this::findAdjacentCardsFromBoard)
                .orElseGet(Collections::emptyList);
    }

    private List<Card> findAdjacentCardsFromBoard(Card target) {
        return range(0, min(cards.length, LAST_BOARD))
                .map(reverseBoard)
                .mapToObj(i -> cards[i])
                .filter(isNotNull)
                .filter(this::isOpenCard)
                .filter(target::isAdjacent)
                .toList();
    }

    private Card getTopDeckCard() {
        return rangeClosed(LAST_BOARD, LAST_DECK - 1)
                .map(reverse)
                .mapToObj(i -> cards[i])
                .filter(isNotNull)
                .findFirst()
                .orElse(null);
    }

    /************************************************************************************************************
     * Update
     ***********************************************************************************************************/
    @Override
    public TriPeaksBoard updateBoard(Card card) {
        if (nonNull(card)) {
            cards[card.at()] = null;
            wastePile(new Column(wastePile));
            wastePile.push(card);
            return this;
        }
        return null;
    }

    private boolean isNotCovered(int at) {
        return isNull(cards[at]) && isNull(cards[at + 1]);
    }

    public Card[] cards() {
        return cards;
    }

    public void cards(Card[] cards) {
        this.cards = cards;
    }

    public Column wastePile() {
        return wastePile;
    }

    public void wastePile(Column wastePile) {
        this.wastePile = wastePile;
    }

    @Override
    public List<String> verify() {
        return verifyBoard(allCards());
    }

    private Card[] allCards() {
        return Optional.of(Stream.concat(Stream.of(cards), wastePile.stream()))
                .map(CardHelper::toArray)
                .orElseThrow();
    }

    /***************************************************************************************************************
     * Score
     **************************************************************************************************************/
    @Override
    public int score() {
        if (isScoreNotSet()) {
            score(-calcBlockers());
        }
        return score;
    }

    public void score(int score) {
        this.score = score;
    }

    protected boolean isScoreNotSet() {
        return score == MIN_VALUE;
    }

    protected void resetScore() {
        score(MIN_VALUE);
    }

    private int calcBlockers() {
        return concat(findOpenBoardCards(), Stream.of(wastePile.peek()))
                .mapToInt(this::calcCardBlockers)
                .sum();
    }

    protected int calcCardBlockers(Card card) {
        return findBoardBlockers(card) + findBlockersInDeck(card);
    }

    private int findBoardBlockers(Card card) {
        return Arrays.stream(cards, 0, LAST_BOARD)
                .filter(isNotNull)
                .filter(it -> it.isAdjacent(card))
                .mapToInt(this::calcBlockerCards)
                .reduce(Integer.MAX_VALUE, Math::min);
    }

    private int calcBlockerCards(Card card) {
        var stack = new Stack<Card>();
        var count = new AtomicInteger(0);
        stack.push(card);

        while (isNotEmpty(stack)) {
            var next = stack.pop();

            if (!isOpenCard(next)) {
                getCoveringCards(next).forEach(c -> {
                    stack.push(c);
                    count.set(count.get() + 1);
                });
            }
        }
        return count.get();
    }

    protected Stream<Card> getCoveringCards(Card card) {
        var at = calcCoveredAt(card);

        if (at <= 0) {
            return Stream.empty();
        }
        return rangeClosed(at, at + 1)
                .mapToObj(i -> cards[i])
                .filter(isNotNull);
    }

    protected int findBlockersInDeck(Card card) {
        for (int i = LAST_DECK - 1; i >= LAST_BOARD; i--) {
            var comp = cards[i];

            if (nonNull(comp) && comp.isAdjacent(card)) {
                return calcDeckBlockers(i + 1);
            }
        }
        return 0;
    }

    private int calcDeckBlockers(int from) {
        return (int) Arrays.stream(cards, from, LAST_DECK)
                .filter(isNotNull)
                .count();
    }

    private Stream<Card> findOpenBoardCards() {
        return range(0, LAST_BOARD)
                .mapToObj(this::getOpenBoardCard)
                .filter(isNotNull);
    }

    private Card getOpenBoardCard(int i) {
        return Optional.ofNullable(cards[i])
                .filter(this::isOpenCard)
                .filter(Card::isNotKing)
                .orElse(null);
    }

    protected boolean isOpenCard(Card card) {
        var at = calcCoveredAt(card);

        return at == 0 || isNotCovered(at);
    }
}
