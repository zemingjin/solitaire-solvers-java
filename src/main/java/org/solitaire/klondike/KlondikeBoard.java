package org.solitaire.klondike;

import org.apache.commons.lang3.ObjectUtils;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.cloneStack;
import static org.solitaire.util.CardHelper.cloneStacks;
import static org.solitaire.util.CardHelper.diffOfValues;
import static org.solitaire.util.CardHelper.suit;
import static org.solitaire.util.CardHelper.suitCode;

class KlondikeBoard extends GameBoard {
    private static int drawNumber = 3;
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
        var candidates = Optional.of(findFoundationCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .orElseGet(this::findMovableCandidates);
        optimizeCandidates(candidates);
        if (candidates.isEmpty()) {
            candidates = drawDeck();
        }
        return candidates;
    }

    protected List<Candidate> findFoundationCandidates() {
        return Stream.of(findFoundationCandidatesFromColumns(), findFoundationCandidateFromDeck())
                .flatMap(it -> it)
                .toList();
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
        var foundation = foundations.get(suitCode(card));

        return Optional.of(foundation)
                .filter(Stack::isEmpty)
                .map(it -> card.isAce())
                .orElseGet(() -> foundation.peek().isLowerWithSameSuit(card) && isImmediateToFoundation(card));
    }

    protected List<Candidate> findMovableCandidates() {
        return Optional.of(findOpenCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .stream()
                .flatMap(List::stream)
                .map(this::findTarget)
                .flatMap(List::stream)
                .collect(Collectors.toCollection(LinkedList::new));
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
        var cards = candidate.cards();
        var card = cards.get(0);

        if (column.isEmpty()) {
            return card.isKing() && isMovable(candidate);
        }
        return column.peek().isHigherWithDifferentColor(card);
    }

    protected boolean isMovable(Candidate candidate) {
        if (COLUMN.equals(candidate.origin())) {
            return Optional.of(candidate.from())
                    .map(columns::get)
                    .filter(it -> it.indexOf(candidate.peek()) > 0)
                    .isPresent();
        }
        return DECKPILE.equals(candidate.origin());
    }

    protected boolean isNotSameColumn(int colNum, Candidate candidate) {
        return !(COLUMN.equals(candidate.origin()) && candidate.from() == colNum);
    }

    protected List<Candidate> findOpenCandidates() {
        var candidates = findCandidatesFromColumns();

        return Optional.ofNullable(findCandidateAtDeck())
                .map(it -> add(candidates, it))
                .orElse(candidates);
    }

    protected List<Candidate> findCandidatesFromColumns() {
        return range(0, 7)
                .mapToObj(this::findCandidateAtColumn)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private Candidate findCandidateAtDeck() {
        return Optional.of(deckPile)
                .filter(ObjectUtils::isNotEmpty)
                .map(Stack::peek)
                .map(it -> buildCandidate(-1, DECKPILE, it))
                .orElse(null);
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

            if (a.cards().get(0).isKing()) {
                range(i + 1, candidates.size()).forEach(j -> {
                    var b = candidates.get(j);

                    if (b.cards().get(0).isKing()) {
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
                .filter(it -> stateChanged)
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
        stateChanged(true);

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
        var card = candidate.cards().get(0);

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
            super.score(-calcHsdHeuristic());
        }
        return super.score();
    }

    // The smaller, the better
    private int calcHsdHeuristic() {
        return Optional.of(range(0, foundations.size()).map(this::calcHsdHeuristic).sum())
                .orElse(0);
    }

    private int calcHsdHeuristic(int at) {
        var foundationCard = foundations.get(at).isEmpty() ? null : foundations.get(at).peek();

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

    private static Card nextCard(Card card, int suitCode) {
        return nonNull(card) ? CardHelper.nextCard(card) : card("A" + suit(suitCode));
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
}
