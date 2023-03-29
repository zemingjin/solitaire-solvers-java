package org.solitaire.pyramid;

import org.solitaire.model.Board;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Integer.MIN_VALUE;
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
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.toArray;

public class PyramidBoard implements Board<Card[], Candidate> {
    private final Card[] cards;
    private final Column deck;
    private final Column flippedDeck;
    private final Path<Card[]> path;
    private int recycleCount;
    private transient int score;

    public PyramidBoard(Card[] cards, Column deck, Column flippedDeck, Path<Card[]> path, int recycleCount) {
        this.cards = cards;
        this.deck = deck;
        this.flippedDeck = flippedDeck;
        this.path = path;
        this.recycleCount = recycleCount;
        resetScore();
    }

    public PyramidBoard(PyramidBoard that) {
        this(cloneArray(that.cards),
                new Column(that.deck),
                new Column(that.flippedDeck),
                new Path<>(that.path),
                that.recycleCount);
    }

    /***************************************************************************************************************
     * Find Candidates
     **************************************************************************************************************/
    @Override
    public List<Candidate> findCandidates() {
        return Optional.of(findOpenCards())
                .filter(listIsNotEmpty)
                .map(this::findCardsOf13s)
                .filter(listIsNotEmpty)
                .orElseGet(this::drawDeckCards);
    }

    private List<Candidate> drawDeckCards() {
        return Optional.ofNullable(drawDeckCard())
                .map(List::of)
                .orElseGet(Collections::emptyList);
    }

    private List<Candidate> findCardsOf13s(List<Card> openCards) {
        return range(0, openCards.size())
                .mapToObj(i -> cardsOf13s(openCards, i))
                .flatMap(it -> it)
                .toList();
    }

    private Stream<Candidate> cardsOf13s(List<Card> openCards, int i) {
        var card = openCards.get(i);

        if (card.isKing()) {
            return Stream.of(buildCandidate(toArray(card), BOARD, REMOVE));
        }
        return range(i + 1, openCards.size())
                .mapToObj(openCards::get)
                .filter(it -> card.rank() + it.rank() == 13)
                .map(it -> toArray(card, it))
                .map(it -> buildCandidate(it, BOARD, REMOVE));
    }

    protected List<Card> findOpenCards() {
        return Stream.concat(getBoardOpenCards(), getDeckCards()).toList();
    }

    private Stream<Card> getDeckCards() {
        return Stream.of(deck(), flippedDeck())
                .filter(listIsNotEmpty)
                .map(Column::peek);
    }

    private Stream<Card> getBoardOpenCards() {
        return rangeClosed(0, LAST_BOARD_INDEX)
                .map(i -> LAST_BOARD_INDEX - i)
                .mapToObj(i -> cards[i])
                .filter(isNotNull)
                .filter(this::isOpen);
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

    protected Candidate drawDeckCard() {
        if (checkDeck()) {
            return buildCandidate(toArray(deck.peek()), DECKPILE, DECKPILE);
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
        removeFromOrigin(candidate);
        updateTargets((candidate));
        return this;
    }

    private void updateTargets(Candidate candidate) {
        var cards = candidate.cards();
        if (candidate.target() == DECKPILE) {
            flippedDeck.push(cards[0]);
        } else {
            path.add(cards);
        }
    }

    private void removeFromOrigin(Candidate candidate) {
        Stream.of(candidate.cards()).forEach(this::removeCardFromBoard);
    }

    private void removeCardFromBoard(Card card) {
        if (isBoardCard(card)) {
            cards[card.at()] = null;
        } else if (!surePop(card, deck)) {
            surePop(card, flippedDeck);
        }
    }

    protected boolean surePop(Card card, Column stack) {
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
        if (notScored()) {
            score(-calcBlockers());
        }
        return score;
    }

    private int calcBlockers() {
        return (int) Stream.of(cards).filter(isNotNull).count();
    }

    protected void score(int score) {
        this.score = score;
    }

    public void resetScore() {
        score(MIN_VALUE);
    }

    public boolean notScored() {
        return score == MIN_VALUE;
    }

    /***************************************************************************************************************
     * Helpers/Accessors
     **************************************************************************************************************/
    @Override
    public List<String> verify() {
        return verifyBoard(allCards());
    }

    protected Card[] allCards() {
        return Stream.of(Stream.of(cards), deck.stream(), flippedDeck.stream()).flatMap(it -> it).toArray(Card[]::new);
    }

    public Card[] cards() {
        return cards;
    }

    public Column deck() {
        return deck;
    }

    public Column flippedDeck() {
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
