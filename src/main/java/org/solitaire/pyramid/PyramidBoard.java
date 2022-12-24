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
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.solitaire.model.CardHelper.VALUES;
import static org.solitaire.model.CardHelper.cloneArray;
import static org.solitaire.model.CardHelper.cloneList;
import static org.solitaire.model.CardHelper.isCleared;

@Getter
@Builder
public class PyramidBoard implements GameSolver<Card[]> {
    public static final int LAST_BOARD = 28;
    public static final int LAST_DECK = 52;

    private Card[] cards;
    private List<Card[]> wastePile;
    private int recycleCount;

    @Override
    public List<List<Card[]>> solve() {
        if (isCleared(cards, LAST_BOARD)) {
            return singletonList(wastePile);
        }
        return Optional.of(findClickableCards())
                .filter(it -> !it.isEmpty())
                .map(this::clickCards)
                .orElseGet(this::clickDeck);
    }

    private List<List<Card[]>> clickDeck() {
        if (cards[LAST_BOARD] == null && recycleCount > 0) {
            recycle();
        }
        return drawCard()
                .map(this::clickCard)
                .orElseGet(Collections::emptyList);
    }

    private void recycle() {
        wastePile.stream()
                .flatMap(Stream::of)
                .filter(it -> it.getAt() >= LAST_BOARD)
                .forEach(it -> cards[it.getAt()] = it);
        recycleCount--;
    }

    protected Optional<Card[]> drawCard() {
        return IntStream.range(LAST_BOARD, LAST_DECK)
                .map(i -> LAST_BOARD + (LAST_DECK - i - 1))
                .filter(this::isOpenDeckCard)
                .mapToObj(i -> cards[i])
                .map(it -> new Card[]{it})
                .findFirst();
    }

    private List<List<Card[]>> clickCards(List<Card[]> clickable) {
        return clickable.stream()
                .map(this::clickCard)
                .filter(it -> !it.isEmpty())
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public List<List<Card[]>> clickCard(Card[] cards) {
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

    protected List<Card[]> findClickableCards() {
        var collect = new LinkedList<Card[]>();
        Consumer<Card> checkSingleCard = card -> {
            if (isK(card)) {
                if (collect.isEmpty()) {
                    collect.add(new Card[]{card});
                } else if (isK(collect.get(0)[0])) {
                    collect.set(0, combine(collect.get(0), card));
                } else {
                    collect.add(0, new Card[]{card});
                }
            }
        };
        var openCards = findOpenCards();

        for (int i = 0; i < openCards.length - 1; i++) {
            for (int j = i + 1; j < openCards.length; j++) {
                if (isMatchingPair(openCards[i], openCards[j])) {
                    collect.add(new Card[]{openCards[i], openCards[j]});
                }
            }
            checkSingleCard.accept(openCards[i]);
        }
        checkSingleCard.accept(openCards[openCards.length - 1]);
        return collect;
    }

    private boolean isK(Card card) {
        return card.getValue() == 'K';
    }

    protected Card[] combine(Card[] cards, Card card) {
        var buf = new Card[cards.length + 1];
        System.arraycopy(cards, 0, buf, 0, cards.length);
        buf[cards.length] = card;
        return buf;
    }

    private boolean isMatchingPair(Card a, Card b) {
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

        return row == 7 || (cards[coveredBy] == null && cards[coveredBy + 1] == null);
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
        return LAST_BOARD <= at && at < LAST_DECK && cards[at] != null && (at == LAST_DECK - 1 || cards[at + 1] == null);
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
                .isPresent();
    }
}