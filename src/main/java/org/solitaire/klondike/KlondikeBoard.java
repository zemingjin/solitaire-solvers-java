package org.solitaire.klondike;

import org.solitaire.execution.GameBoard;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.Path;
import org.solitaire.util.BoardHelper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.candidate;
import static org.solitaire.model.Candidate.columnToColumn;
import static org.solitaire.model.Candidate.columnToFoundation;
import static org.solitaire.model.Candidate.foundationToColumn;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.nextCard;
import static org.solitaire.util.CardHelper.rankDifference;
import static org.solitaire.util.CardHelper.suitCode;
import static org.solitaire.util.CardHelper.toArray;

/**
 * <a href="http://www.chessandpoker.com/solitaire_strategy.html">Solitaire Strategy Guide</a>
 * <a href="http://www.somethinkodd.com/oddthinking/2005/06/25/the-finer-points-of-klondike/">The Finer Points of Klondike</a>
 * <a href="https://gambiter.com/solitaire/Klondike_solitaire.html">Klondike - solitaire</a>
 * <a href="https://solitaired.com/turn-3">Turn 3 Solitaire Strategy</a>
 */
class KlondikeBoard extends GameBoard {
    private static int drawNumber = 3;

    private boolean stateChanged;
    private Deck deck;
    private Deck deckPile;
    private Columns foundations;

    KlondikeBoard(Columns columns,
                  Path<String> path,
                  int totalScore,
                  Deck deck,
                  Deck deckPile,
                  Columns foundations,
                  boolean stateChanged) {
        super(columns, path, totalScore);
        deck(deck);
        deckPile(deckPile);
        foundations(foundations);
        stateChanged(stateChanged);
        isInSequence(Card::isHigherWithDifferentColor);
    }


    KlondikeBoard(KlondikeBoard that) {
        this(new Columns(that.columns()),
                new Path<>(that.path()),
                that.totalScore(),
                new Deck(that.deck()),
                new Deck(that.deckPile()),
                new Columns(that.foundations()),
                that.stateChanged);
    }

    /***************************************************************************************************************
     * Search
     **************************************************************************************************************/
    @Override
    public List<Candidate> findCandidates() {
        return Optional.of(findCandidatesFromBoard())
                .filter(listIsNotEmpty)
                .orElseGet(this::drawDeck);
    }

    private List<Candidate> findCandidatesFromBoard() {
        return Stream.of(findColumnToFoundationCandidates(),
                        findDeckToFoundationCandidates(),
                        findColumnToColumnCandidates(),
                        findDeckToColumnCandidates(),
                        findFoundationToColumnCandidates())
                .flatMap(flattenStream)
                .toList();
    }

    protected Stream<Candidate> findFoundationToColumnCandidates() {
        return foundations().stream()
                .filter(BoardHelper.isNotEmpty)
                .map(Column::peek)
                .map(this::fromFoundationToColumn)
                .filter(isNotNull)
                .findFirst().stream();
    }

    protected Candidate fromFoundationToColumn(Card card) {
        return Optional.of(card)
                .filter(this::isOneToUncover)
                .map(this::findOneToReceive)
                .orElse(null);
    }

    private boolean isOneToUncover(Card card) {
        return range(0, columns().size())
                .filter(isNotEmpty.and(this::isNotFoundationable))
                .filter(i -> card.isHigherWithDifferentColor(peek(i)))
                .findFirst()
                .isPresent();
    }

    protected boolean isNotFoundationable(int i) {
        var card = peek(i);

        return !card.isHigherOfSameSuit(foundationCard(card));
    }

    private Candidate findOneToReceive(Card card) {
        return range(0, columns().size())
                .filter(isNotEmpty)
                .filter(i -> peek(i).isHigherWithDifferentColor(card))
                .mapToObj(i -> foundationToColumn(card, i))
                .findFirst()
                .orElse(null);
    }

    protected Stream<Candidate> findColumnToFoundationCandidates() {
        return range(0, columns().size())
                .filter(i -> isNotEmpty(column(i)))
                .filter(i -> isFoundationCandidate(peek(i)))
                .mapToObj(i -> columnToFoundation(column(i).peek(), i));
    }

    protected Stream<Candidate> findDeckToFoundationCandidates() {
        return Optional.of(deckPile)
                .filter(BoardHelper.isNotEmpty)
                .map(Stack::peek)
                .filter(this::isFoundationCandidate)
                .map(Candidate::deckToFoundation)
                .stream();
    }

    protected boolean isFoundationCandidate(Card card) {
        var foundationCards = foundation(suitCode(card));

        return Optional.of(foundationCards)
                .filter(Column::isEmpty)
                .map(it -> card.isAce())
                .orElseGet(() -> foundationCards.peek().isLowerWithSameSuit(card) && isImmediateToFoundation(card));
    }

    @Override
    protected Candidate candidateToEmptyColumn(Card[] cards, int from, int to) {
        return Optional.of(cards)
                .filter(it -> it[0].isKing() && column(from).size() > it.length)
                .map(it -> columnToColumn(cards, from, to))
                .orElse(null);
    }

    protected Stream<Candidate> findDeckToColumnCandidates() {
        return Optional.of(deckPile)
                .filter(BoardHelper.isNotEmpty)
                .map(Stack::peek)
                .map(this::deckToColumnCandidates)
                .orElseGet(Stream::empty);
    }

    private Stream<Candidate> deckToColumnCandidates(Card card) {
        return range(0, columns().size())
                .mapToObj(i -> deckToColumnCandidate(i, card))
                .filter(isNotNull);
    }

    private Candidate deckToColumnCandidate(int colAt, Card card) {
        return Stream.of(column(colAt))
                .map(it -> it.isNotEmpty() ? it.peek() : null)
                .filter(it -> isDeckToColumnCandidate(card, it))
                .map(it -> candidate(card, DECKPILE, -1, COLUMN, colAt))
                .findFirst()
                .orElse(null);
    }

    private boolean isDeckToColumnCandidate(Card foundationCard, Card columnCard) {
        return isNull(columnCard)
                ? foundationCard.isKing()
                : columnCard.isHigherWithDifferentColor(foundationCard);
    }

    protected boolean isMovable(Card card, Column column) {
        return !card.isKing() || column.indexOf(card) > 0;
    }

    public boolean isSolved() {
        return foundations.stream().allMatch(it -> it.size() == 13);
    }

    protected boolean isImmediateToFoundation(Card card) {
        if (card.isHigherRank(foundationCard(card))) {
            if (!stateChanged()) {
                return true;
            }
            return foundations.stream()
                    .allMatch(it -> rankDifference(card, it.isEmpty() ? null : it.peek()) <= 2);
        }
        return false;
    }

    protected boolean helpOpenCard(Card card) {
        return columns().stream().anyMatch(it -> it.indexOf(card) == it.openAt());
    }

    protected List<Candidate> drawDeck() {
        if (checkRecycleDeck()) {
            var floor = max(0, deck().size() - drawNumber);
            var ceiling = min(floor + drawNumber, deck().size());
            var cards = toArray(deck().subList(floor, ceiling));

            return List.of(new Candidate(cards, DECKPILE, 0, DECKPILE, 0));
        }
        return emptyList();
    }

    private boolean checkRecycleDeck() {
        Optional.of(deck())
                .filter(Deck::isEmpty)
                .filter(it -> stateChanged())
                .ifPresent(it -> {
                    while (isNotEmpty(deckPile())) {
                        it.push(deckPile().pop());
                    }
                    stateChanged(false);
                });
        return isNotEmpty(deck());
    }

    /*************************************************************************************************************
     * Update board
     ************************************************************************************************************/
    @Override
    public KlondikeBoard updateBoard(Candidate candidate) {
        if (candidate.isNotToDeck()) {
            stateChanged(true);
        }
        return removeFromSource(candidate)
                .moveToTarget(candidate);
    }

    protected KlondikeBoard removeFromSource(Candidate candidate) {
        switch (candidate.origin()) {
            case COLUMN -> removeFromColumn(candidate);
            case DECKPILE -> removeFromDeck(candidate);
            case FOUNDATION -> foundation(candidate.from()).pop();
        }
        return this;
    }

    private void removeFromDeck(Candidate candidate) {
        if (candidate.target() == DECKPILE) {
            Stream.of(candidate.cards()).forEach(it -> deck.pop());
        } else {
            deckPile.pop();
        }
    }

    protected KlondikeBoard moveToTarget(Candidate candidate) {
        path.add(candidate.notation());

        switch (candidate.target()) {
            case COLUMN -> addToTargetColumn(candidate);
            case FOUNDATION -> moveToFoundation(candidate);
            case DECKPILE -> moveToDeskPile(candidate);
        }
        return this;
    }

    private void moveToDeskPile(Candidate candidate) {
        range(0, candidate.cards().length)
                .map(i -> candidate.cards().length - i - 1)
                .mapToObj(i -> candidate.cards()[i])
                .forEach(deckPile::add);
    }

    @Override
    protected void addToTargetColumn(Candidate candidate) {
        super.addToTargetColumn(candidate);
        if (isScorable(candidate)) {
            totalScore(totalScore() + 5);
        }
    }

    protected boolean isScorable(Candidate candidate) {
        return candidate.isFromDeck() ||
                (candidate.isFromColumn() && column(candidate.from()).isNotEmpty());
    }

    protected void moveToFoundation(Candidate candidate) {
        var card = candidate.peek();

        Optional.of(suitCode(card))
                .map(foundations::get)
                .ifPresent(it -> it.add(card));
        totalScore(totalScore() + (isFirstCardToFoundation(candidate)
                ? 15
                : DECKPILE.equals(candidate.origin())
                ? 10 :
                5));
    }

    private boolean isFirstCardToFoundation(Candidate candidate) {
        if (COLUMN.equals(candidate.origin())) {
            var card = candidate.cards()[0];

            return foundation(suitCode(card)).size() == 1;
        }
        return false;
    }

    /*************************************************************************************************************
     * Score board
     ************************************************************************************************************/

    @Override
    public int score() {
        // the smaller, the better
        var blockScore = calcBlockers();
        var uncoveredCards = uncoveredCards();
        // the larger, the better
        var sequenceScore = calcSequenceScore();

        if (isNotScored()) {
            super.score(sequenceScore - blockScore - uncoveredCards);
        }
        return super.score();
    }

    protected int calcSequenceScore() {
        return range(0, columns().size())
                .filter(i -> column(i).isNotEmpty())
                .map(this::calcSequenceScore)
                .sum();
    }

    private int calcSequenceScore(int colAt) {
        return Optional.of(getOrderedCards(colAt))
                .map(it -> isPreferredSequence(colAt, it) ? it.length * 2 : it.length)
                .orElse(0);
    }

    private boolean isPreferredSequence(int colAt, Card[] cards) {
        return cards.length == column(colAt).size() && cards[0].isKing();
    }

    // The smaller, the better
    protected int calcBlockers() {
        return Optional.of(range(0, foundations.size()).map(this::calcBlockers).sum())
                .orElse(0);
    }

    private int calcBlockers(int at) {
        var foundationCard = foundationCard(at);

        if (nonNull(foundationCard) && foundationCard.isKing()) {
            return 0;
        }
        var card = nextCard(foundationCard, at);

        if (deck.contains(card)) {
            return calcBlocker(deck, card) / drawNumber;
        } else if (deckPile.contains(card)) {
            return calcBlocker(deckPile, card);
        }
        return columns.stream()
                .filter(listIsNotEmpty)
                .filter(it -> it.contains(card))
                .map(it -> calcBlocker(it, card))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Failed to find next card: " + card));
    }

    private static int calcBlocker(List<Card> cards, Card target) {
        return cards.size() - cards.lastIndexOf(target) - 1;
    }

    // The smaller, the better
    private int uncoveredCards() {
        return columns().stream().mapToInt(Column::openAt).sum();
    }

    /*************************************************************************************************************
     * Accessors/Helpers
     ************************************************************************************************************/

    public static int drawNumber() {
        return drawNumber;
    }

    public static void drawNumber(int drawNumber) {
        KlondikeBoard.drawNumber = drawNumber;
    }

    protected boolean stateChanged() {
        return stateChanged;
    }

    protected void stateChanged(boolean stateChanged) {
        this.stateChanged = stateChanged;
    }

    public Deck deck() {
        return deck;
    }

    public void deck(Deck deck) {
        this.deck = deck;
    }

    public Columns columns() {
        return columns;
    }

    public Deck deckPile() {
        return deckPile;
    }

    public void deckPile(Deck deckPile) {
        this.deckPile = deckPile;
    }

    public Columns foundations() {
        return foundations;
    }

    public void foundations(Columns foundations) {
        this.foundations = foundations;
    }

    protected Column foundation(int i) {
        return foundations.get(i);
    }

    public Card foundationCard(Card card) {
        return foundationCard(suitCode(card));
    }

    public Card foundationCard(int suitCode) {
        return Optional.of(suitCode)
                .map(foundations()::get)
                .filter(BoardHelper.isNotEmpty)
                .map(Column::peek)
                .orElse(null);
    }

    @Override
    public List<String> verify() {
        return verifyBoard(columns, deck, deckPile);
    }

}