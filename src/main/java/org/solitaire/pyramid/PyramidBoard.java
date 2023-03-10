package org.solitaire.pyramid;

import org.solitaire.model.Board;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.BOARD;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.REMOVE;
import static org.solitaire.pyramid.PyramidHelper.LAST_BOARD;
import static org.solitaire.pyramid.PyramidHelper.LAST_BOARD_INDEX;
import static org.solitaire.pyramid.PyramidHelper.isBoardCard;
import static org.solitaire.pyramid.PyramidHelper.row;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.cloneStack;

public class PyramidBoard implements Board<Card[], Candidate> {
    private final Card[] cards;
    private final Stack<Card> deck;
    private final Stack<Card> flippedDeck;
    private final Path<Card[]> path;
    private int recycleCount;
    private transient int score = 0;

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

    /***************************************************************************************************************
     * Find Candidates
     **************************************************************************************************************/
    @Override
    public List<Candidate> findCandidates() {
        var openCards = findOpenCards();

        var candidates = range(0, openCards.size())
                .mapToObj(i -> findPairsOf13(openCards, i))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return Optional.ofNullable(drawDeckCard()).map(List::of).orElseGet(Collections::emptyList);
        }
        return candidates;
    }


    private List<Candidate> findPairsOf13(List<Card> openCards, int i) {
        var card = openCards.get(i);

        if (card.isKing()) {
            return List.of(buildCandidate(List.of(card), BOARD, REMOVE));
        }
        return range(i + 1, openCards.size())
                .mapToObj(openCards::get)
                .filter(it -> it.isNotKing() && (card.rank() + it.rank()) == 13)
                .map(it -> List.of(card, it))
                .map(it -> buildCandidate(it, BOARD, REMOVE))
                .toList();
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

    public Candidate drawDeckCard() {
        if (checkDeck()) {
            return buildCandidate(List.of(deck.peek()), DECKPILE, DECKPILE);
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

    /***************************************************************************************************************
     * Update State
     **************************************************************************************************************/
    @Override
    public PyramidBoard updateBoard(Candidate candidate) {
        var cards = candidate.cards();
        if (candidate.target() == DECKPILE) {
            flippedDeck.push(cards.get(0));
        } else {
            path.add(cards.toArray(new Card[0]));
        }
        cards.forEach(this::removeCardFromBoard);
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

    /***************************************************************************************************************
     * Scoring
     **************************************************************************************************************/
    @Override
    public int score() {
        if (score == 0) {
            score(-calcBlockers());
        }
        return score;
    }

    private int calcBlockers() {
        return (int) Stream.of(cards).filter(Objects::nonNull).count();
    }

    protected void score(int score) {
        this.score = score;
    }

    /***************************************************************************************************************
     * Helpers/Accessors
     **************************************************************************************************************/
    @Override
    public List<String> verify() {
        return verifyBoard(allCards());
    }

    protected Card[] allCards() {
        return Stream.concat(Stream.of(cards), deck.stream()).toArray(Card[]::new);
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

    public boolean isSolved() {
        return CardHelper.isCleared(cards);
    }

}
