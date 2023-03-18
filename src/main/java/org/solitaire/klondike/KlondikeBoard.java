package org.solitaire.klondike;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Candidate.buildFoundationToColumn;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.listNotEmpty;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.cloneStack;
import static org.solitaire.util.CardHelper.cloneStacks;
import static org.solitaire.util.CardHelper.diffOfValues;
import static org.solitaire.util.CardHelper.nextCard;
import static org.solitaire.util.CardHelper.suitCode;

/**
 * <a href="http://www.chessandpoker.com/solitaire_strategy.html">Solitaire Strategy Guide</a>
 * <a href="http://www.somethinkodd.com/oddthinking/2005/06/25/the-finer-points-of-klondike/">The Finer Points of Klondike</a>
 * <a href="https://gambiter.com/solitaire/Klondike_solitaire.html">Klondike - solitaire</a>
 * <a href="https://solitaired.com/turn-3">Turn 3 Solitaire Strategy</a>
 */
class KlondikeBoard extends GameBoard {
    private static int drawNumber = 3;
    protected Deck deck;
    protected Stack<Card> deckPile;
    protected List<Stack<Card>> foundations;
    protected transient final IntPredicate isNotImmediateFoundationable =
            i -> !peek(i).isHigherOfSameColor(foundationCard(peek(i)));
    protected boolean stateChanged;

    KlondikeBoard(Columns columns,
                  Path<String> path,
                  int totalScore,
                  Deck deck,
                  Stack<Card> deckPile,
                  List<Stack<Card>> foundations,
                  boolean stateChanged) {
        super(columns, path, totalScore);
        deck(deck);
        deckPile(deckPile);
        foundations(foundations);
        stateChanged(stateChanged);
    }

    KlondikeBoard(KlondikeBoard that) {
        this(new Columns(that.columns()),
                new Path<>(that.path()),
                that.totalScore(),
                new Deck(that.deck()),
                cloneStack(that.deckPile()),
                cloneStacks(that.foundations()),
                that.stateChanged);
    }

    private static int calcBlocker(List<Card> cards, Card target) {
        return cards.size() - cards.lastIndexOf(target) - 1;
    }

    public static int drawNumber() {
        return drawNumber;
    }

    public static void drawNumber(int drawNumber) {
        KlondikeBoard.drawNumber = drawNumber;
    }

    /***************************************************************************************************************
     * Search
     **************************************************************************************************************/
    @Override
    public List<Candidate> findCandidates() {
        var candidates = Stream.concat(findFoundationCandidates(), findMovableCandidates())
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            candidates.addAll(findFoundationToColumnCandidates());
        }
        if (candidates.isEmpty()) {
            return drawDeck();
        }
        return candidates;
    }

    protected List<Candidate> findFoundationToColumnCandidates() {
        return foundations().stream()
                .filter(listNotEmpty)
                .map(Stack::peek)
                .map(fromFoundationToColumn)
                .filter(isNotNull)
                .map(buildFoundationToColumn)
                .findFirst()
                .map(List::of)
                .orElseGet(Collections::emptyList);
    }

    private transient final Predicate<Card> isOneToUncover =
            card -> range(0, columns().size())
                    .filter(isNotEmpty.and(isNotImmediateFoundationable))
                    .filter(i -> card.isHigherWithDifferentColor(peek(i)))
                    .findFirst()
                    .isPresent();

    private transient final Function<Card, Pair<Integer, Card>> findOneToReceive =
            card -> range(0, columns().size())
                    .filter(isNotEmpty)
                    .filter(i -> peek(i).isHigherWithDifferentColor(card))
                    .mapToObj(i -> Pair.of(i, card))
                    .findFirst()
                    .orElse(null);

    protected transient final Function<Card, Pair<Integer, Card>> fromFoundationToColumn =
            card -> Optional.of(card)
                    .filter(isOneToUncover)
                    .map(findOneToReceive)
                    .orElse(null);


    protected Stream<Candidate> findFoundationCandidates() {
        return concat(findFoundationCandidatesFromColumns(), findFoundationCandidateFromDeck());
    }

    private Stream<Candidate> findFoundationCandidatesFromColumns() {
        return range(0, columns().size())
                .filter(i -> isNotEmpty(columns.get(i)))
                .filter(isFoundationCandidateFromColumn)
                .mapToObj(i -> buildCandidate(i, COLUMN, FOUNDATION, columns.get(i).peek()));
    }

    protected Stream<Candidate> findFoundationCandidateFromDeck() {
        return Optional.of(deckPile)
                .filter(listNotEmpty)
                .map(Stack::peek)
                .filter(isFoundationCandidate)
                .map(it -> buildCandidate(-1, DECKPILE, FOUNDATION, it))
                .stream();
    }

    protected transient final Predicate<Card> isFoundationCandidate = card -> {
        var foundationCard = foundations.get(suitCode(card));

        return Optional.of(foundationCard)
                .filter(Stack::isEmpty)
                .map(it -> card.isAce())
                .orElseGet(() -> foundationCard.peek().isLowerWithSameSuit(card) && isImmediateToFoundation(card));
    };

    private transient final IntPredicate isFoundationCandidateFromColumn = i -> isFoundationCandidate.test(peek(i));

    protected Stream<Candidate> findMovableCandidates() {
        return Optional.of(findOpenCandidates())
                .filter(listNotEmpty)
                .stream()
                .flatMap(List::stream)
                .map(findTarget)
                .flatMap(List::stream);
    }

    private transient final Predicate<Pair<Integer, Candidate>> isAppendable =
            pair -> Optional.of(columns().get(pair.getLeft()))
                    .filter(Column::isNotEmpty)
                    .map(it -> it.peek().isHigherWithDifferentColor(pair.getRight().peek()))
                    .orElseGet(() -> isMovableToEmptyColumn(pair.getRight()));

    private transient final Function<Candidate, LinkedList<Candidate>> checkColumnsForAppendable =
            candidate -> range(0, columns.size())
                    .mapToObj(i -> Pair.of(i, candidate))
                    .filter(isAppendable)
                    .map(it -> Candidate.buildColumnCandidate(it.getRight(), it.getLeft()))
                    .collect(Collectors.toCollection(LinkedList::new));

    protected transient final Function<Candidate, List<Candidate>> findTarget =
            candidate -> Optional.of(candidate)
                    .map(checkColumnsForAppendable)
                    .orElseThrow();

    private boolean isMovableToEmptyColumn(Candidate candidate) {
        return candidate.peek().isKing() && isMovableToEmptyColumn.test(candidate);
    }

    protected boolean isNotSameColumn(int colNum, Candidate candidate) {
        return !(candidate.isFromColumn() && candidate.from() == colNum);
    }

    protected List<Candidate> findOpenCandidates() {
        return concat(findCandidatesFromColumns(), findCandidateAtDeck()).toList();
    }

    protected Stream<Candidate> findCandidatesFromColumns() {
        return range(0, 7)
                .mapToObj(this::findCandidateAtColumn)
                .filter(isNotNull);
    }

    private Stream<Candidate> findCandidateAtDeck() {
        return Optional.of(deckPile)
                .filter(listNotEmpty)
                .map(Stack::peek)
                .map(it -> buildCandidate(-1, DECKPILE, it))
                .stream();
    }

    protected Candidate findCandidateAtColumn(int colAt) {
        return Optional.of(colAt)
                .map(columns::get)
                .filter(listNotEmpty)
                .map(getOrderedCards)
                .filter(listNotEmpty)
                .map(it -> buildCandidate(colAt, COLUMN, it))
                .orElse(null);
    }

    protected transient final Function<Column, List<Card>> getOrderedCards = column -> {
        var collector = new LinkedList<Card>();

        for (int i = column.size() - 1; i >= max(column.openAt(), 0); i--) {
            var card = column.get(i);

            if (collector.isEmpty() || card.isHigherWithDifferentColor(collector.get(0))) {
                collector.add(0, card);
            }
        }
        if (isNotEmpty(collector) && isMovable(collector.get(0), column)) {
            return collector;
        }
        return emptyList();
    };

    protected boolean isMovable(Card card, Column column) {
        return !card.isKing() || column.indexOf(card) > 0;
    }

    public boolean isSolved() {
        return foundations.stream().allMatch(it -> it.size() == 13);
    }

    protected boolean isImmediateToFoundation(Card card) {
        if (!stateChanged()) {
            return true;
        }
        return foundations.stream()
                .allMatch(it -> diffOfValues(card, it.isEmpty() ? null : it.peek()) <= 2);
    }

    protected List<Candidate> drawDeck() {
        if (checkRecycleDeck()) {
            var floor = max(0, deck().size() - drawNumber);
            var ceiling = Math.min(floor + drawNumber, deck().size());
            var cards = deck().subList(floor, ceiling);

            return List.of(new Candidate(new LinkedList<>(cards), DECKPILE, 0, DECKPILE, 0));
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
            case FOUNDATION -> foundations.get(candidate.from()).pop();
        }
        return this;
    }

    private void removeFromDeck(Candidate candidate) {
        if (candidate.target() == DECKPILE) {
            candidate.cards().forEach(it -> deck.pop());
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
        range(0, candidate.cards().size())
                .map(i -> candidate.cards().size() - i - 1)
                .mapToObj(i -> candidate.cards().get(i))
                .forEach(deckPile::add);
    }

    @Override
    protected void addToTargetColumn(Candidate candidate) {
        super.addToTargetColumn(candidate);
        if (isScorable(candidate)) {
            totalScore += 5;
        }
    }

    protected boolean isScorable(Candidate candidate) {
        return DECKPILE.equals(candidate.origin()) ||
                (COLUMN.equals(candidate.origin()) && isNotEmpty(columns.get(candidate.from())));
    }

    protected void moveToFoundation(Candidate candidate) {
        var card = candidate.peek();

        Optional.of(suitCode(card))
                .map(foundations::get)
                .ifPresent(it -> it.push(card));
        totalScore += isFirstCardToFoundation(candidate) ? 15 : DECKPILE.equals(candidate.origin()) ? 10 : 5;
    }

    private boolean isFirstCardToFoundation(Candidate candidate) {
        if (COLUMN.equals(candidate.origin())) {
            var card = candidate.cards().get(0);

            return foundations.get(suitCode(card)).size() == 1;
        }
        return false;
    }

    /*************************************************************************************************************
     * Score board
     ************************************************************************************************************/

    @Override
    public int score() {
        if (super.score() == 0) {
            super.score(-calcBlockers());
        }
        return super.score();
    }

    // The smaller, the better
    private int calcBlockers() {
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
                .filter(listNotEmpty)
                .filter(it -> it.contains(card))
                .map(it -> calcBlocker(it, card))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Failed to find next card: " + card));
    }

    /*************************************************************************************************************
     * Accessors/Helpers
     ************************************************************************************************************/

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

    public Stack<Card> deckPile() {
        return deckPile;
    }

    public void deckPile(Stack<Card> deckPile) {
        this.deckPile = deckPile;
    }

    public List<Stack<Card>> foundations() {
        return foundations;
    }

    public void foundations(List<Stack<Card>> foundations) {
        this.foundations = foundations;
    }

    public Card foundationCard(Card card) {
        return foundationCard(suitCode(card));
    }

    public Card foundationCard(int suitCode) {
        return Optional.of(suitCode)
                .map(foundations()::get)
                .filter(listNotEmpty)
                .map(Stack::peek)
                .orElse(null);
    }

    @Override
    public List<String> verify() {
        return verifyBoard(columns, deck);
    }

}
