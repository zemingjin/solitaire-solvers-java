package org.solitaire.klondike;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
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

    protected transient final IntPredicate isNotImmediateFoundationable =
            i -> !peek(i).isHigherOfSameColor(foundationCard(peek(i)));

    protected final Deck deck;
    protected final Stack<Card> deckPile;
    protected final List<Stack<Card>> foundations;
    protected boolean stateChanged;

    KlondikeBoard(Columns columns,
                  Path<String> path,
                  int totalScore,
                  Deck deck,
                  Stack<Card> deckPile,
                  List<Stack<Card>> foundations,
                  boolean stateChanged) {
        super(columns, path, totalScore);
        this.deck = deck;
        this.deckPile = deckPile;
        this.foundations = foundations;
        this.stateChanged = stateChanged;
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

    /***************************************************************************************************************
     * Search
     **************************************************************************************************************/
    @Override
    public List<Candidate> findCandidates() {
        var candidates = Stream.concat(
                        findFoundationCandidates(),
                        findMovableCandidates()).collect(Collectors.toList());

        if (candidates.isEmpty() || !stateChanged) {
            candidates.addAll(findFoundationToColumnCandidates().toList());
        }
        if (candidates.isEmpty()) {
            return drawDeck();
        }
        return optimizeCandidates(candidates);
    }

    protected Stream<Candidate> findFoundationToColumnCandidates() {
        return foundations().stream()
                .filter(ObjectUtils::isNotEmpty)
                .map(Stack::peek)
                .map(this::fromFoundationToColumn)
                .filter(Objects::nonNull)
                .map(it -> new Candidate(List.of(it.getRight()), FOUNDATION, suitCode(it.getRight()), COLUMN, it.getLeft()));
    }

    protected Pair<Integer, Card> fromFoundationToColumn(Card card) {
        int targetCol = range(0, columns().size())
                .filter(isNotEmpty.and(isNotImmediateFoundationable))
                .filter(i -> card.isHigherWithDifferentColor(peek(i)))
                .findFirst()
                .orElse(-1);
        if (targetCol >= 0) {
            targetCol = range(0, columns().size())
                    .filter(isNotEmpty)
                    .filter(i -> peek(i).isHigherWithDifferentColor(card))
                    .findFirst()
                    .orElse(-1);
            if (targetCol >= 0) {
                return Pair.of(targetCol, card);
            }
        }
        return null;
    }

    protected Stream<Candidate> findFoundationCandidates() {
        return concat(findFoundationCandidatesFromColumns(), findFoundationCandidateFromDeck());
    }

    private Stream<Candidate> findFoundationCandidatesFromColumns() {
        return range(0, columns().size())
                .filter(i -> isNotEmpty(columns.get(i)))
                .filter(i -> isFoundationCandidate(columns.get(i).peek()))
                .mapToObj(i -> buildCandidate(i, COLUMN, FOUNDATION, columns.get(i).peek()));
    }

    protected Stream<Candidate> findFoundationCandidateFromDeck() {
        return Optional.of(deckPile)
                .filter(ObjectUtils::isNotEmpty)
                .map(Stack::peek)
                .filter(this::isFoundationCandidate)
                .map(it -> buildCandidate(-1, DECKPILE, FOUNDATION, it))
                .stream();
    }

    protected boolean isFoundationCandidate(Card card) {
        var foundationCard = foundations.get(suitCode(card));

        return Optional.of(foundationCard)
                .filter(Stack::isEmpty)
                .map(it -> card.isAce())
                .orElseGet(() -> foundationCard.peek().isLowerWithSameSuit(card) && isImmediateToFoundation(card));
    }

    protected Stream<Candidate> findMovableCandidates() {
        return Optional.of(findOpenCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .stream()
                .flatMap(List::stream)
                .map(this::findTarget)
                .flatMap(List::stream);
    }

    protected List<Candidate> findTarget(Candidate candidate) {
        return Optional.of(checkColumnsForAppendables(candidate))
                .orElseThrow();
    }

    private LinkedList<Candidate> checkColumnsForAppendables(Candidate candidate) {
        return range(0, columns.size())
                .filter(i -> isMatchingColumn(i, candidate))
                .mapToObj(i -> Candidate.buildColumnCandidate(candidate, i))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private boolean isMatchingColumn(int colNum, Candidate candidate) {
        if (isNotSameColumn(colNum, candidate)) {
            return isAppendable(columns.get(colNum), candidate);
        }
        return false;
    }

    private boolean isAppendable(Column column, Candidate candidate) {
        return Optional.of(column)
                .filter(Column::isNotEmpty)
                .map(it -> it.peek().isHigherWithDifferentColor(candidate.peek()))
                .orElseGet(() -> isMovableToEmptyColumn(candidate));
    }

    private boolean isMovableToEmptyColumn(Candidate candidate) {
        return candidate.peek().isKing() && isMovableToEmptyColumn.test(candidate);
    }

    protected boolean isMovable(Candidate candidate) {
        if (candidate.isFromColumn()) {
            return Optional.of(candidate.from())
                    .map(columns::get)
                    .filter(it -> it.indexOf(candidate.peek()) > 0)
                    .isPresent();
        }
        return DECKPILE.equals(candidate.origin());
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
                .filter(Objects::nonNull);
    }

    private Stream<Candidate> findCandidateAtDeck() {
        return Optional.of(deckPile)
                .filter(ObjectUtils::isNotEmpty)
                .map(Stack::peek)
                .map(it -> buildCandidate(-1, DECKPILE, it))
                .stream();
    }

    protected Candidate findCandidateAtColumn(int colAt) {
        return Optional.of(colAt)
                .map(columns::get)
                .filter(ObjectUtils::isNotEmpty)
                .map(this::getOrderedCards)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> buildCandidate(colAt, COLUMN, it))
                .orElse(null);
    }

    protected List<Card> getOrderedCards(Column column) {
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
    }

    protected boolean isMovable(Card card, Column column) {
        return !card.isKing() || column.indexOf(card) > 0;
    }

    public boolean isSolved() {
        return foundations.stream().allMatch(it -> it.size() == 13);
    }

    protected List<Candidate> optimizeCandidates(List<Candidate> candidates) {
        return Optional.of(candidates)
                .map(this::removeDuplicateKings)
                .orElse(candidates);
    }

    private List<Candidate> removeDuplicateKings(List<Candidate> candidates) {
        var collect = new LinkedList<Candidate>();

        range(0, candidates.size() - 1).forEach(i -> {
            var a = candidates.get(i);

            if (a.peek().isKing()) {
                range(i + 1, candidates.size()).forEach(j -> {
                    var b = candidates.get(j);

                    if (b.peek().isKing()) {
                        if (isDuplicate(a, b)) {
                            collect.add(b);
                        }
                    }
                });
            }
        });
        collect.forEach(candidates::remove);
        return candidates;
    }

    protected boolean isDuplicate(Candidate a, Candidate b) {
        return a.origin() == b.origin() && a.from() == b.from() && a.target() == b.target() && a.to() != b.to();
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
                .filter(ObjectUtils::isNotEmpty)
                .filter(it -> it.contains(card))
                .map(it -> calcBlocker(it, card))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Failed to find next card: " + card));
    }

    private static int calcBlocker(List<Card> cards, Card target) {
        return cards.size() - cards.lastIndexOf(target) - 1;
    }

    /*************************************************************************************************************
     * Accessors
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

    public Columns columns() {
        return columns;
    }

    public Stack<Card> deckPile() {
        return deckPile;
    }

    public List<Stack<Card>> foundations() {
        return foundations;
    }

    public Card foundationCard(Card card) {
        return foundationCard(suitCode(card));
    }

    public Card foundationCard(int suitCode) {
        return Optional.of(suitCode)
                .map(foundations()::get)
                .filter(ObjectUtils::isNotEmpty)
                .map(Stack::peek)
                .orElse(null);
    }

    @Override
    public List<String> verify() {
        return verifyBoard(columns, deck);
    }
}
