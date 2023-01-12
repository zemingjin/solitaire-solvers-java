package org.solitaire.klondike;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.util.CardHelper.diffOfValues;
import static org.solitaire.util.CardHelper.suitCode;
import static org.solitaire.util.CollectionUtil.add;

@Slf4j
@SuppressWarnings("rawtypes")
@Getter
public class Klondike extends KlondikeState implements GameSolver {
    private static final List<List> solutions = new ArrayList<>();
    private static final int drawNumber = 3;
    private static final int LIMIT_SOLUTIONS = 1000;
    private static int totalScenarios;

    public Klondike(Columns columns,
                    Path<String> path,
                    int totalScore,
                    Deck deck,
                    Stack<Card> deckPile,
                    List<Stack<Card>> foundations,
                    boolean stateChanged) {
        super(columns, path, totalScore, deck, deckPile, foundations, stateChanged);
    }

    public Klondike(Klondike that) {
        super(that);
    }

    @Override
    public List<List> solve() {
        if (isCleared()) {
            solutions.add(path);
        } else if (solutions.size() < LIMIT_SOLUTIONS) {
            totalScenarios++;
            Optional.of(findCandidates())
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresentOrElse(this::applyCandidates, this::drawDeck);
        }
        return solutions;
    }

    protected List<Candidate> findCandidates() {
        var candidates = Optional.of(findFoundationCandidates())
                .filter(ObjectUtils::isNotEmpty)
                .orElseGet(this::findMovableCandidates);
        return optimizeCandidates(candidates);
    }

    protected List<Candidate> findFoundationCandidates() {
        return Optional.of(findFoundationCandidatesFromColumns())
                .map(this::findFoundationCandidateFromDeck)
                .orElseThrow();
    }

    private List<Candidate> findFoundationCandidatesFromColumns() {
        return IntStream.range(0, columns.size())
                .filter(i -> isNotEmpty(columns.get(i)))
                .filter(i -> isFoundationCandidate(columns.get(i).peek()))
                .mapToObj(i -> buildCandidate(i, COLUMN, columns.get(i).peek()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private List<Candidate> findFoundationCandidateFromDeck(List<Candidate> collector) {
        if (isNotEmpty(deckPile)) {
            Optional.of(deckPile.peek())
                    .filter(this::isFoundationCandidate)
                    .map(it -> buildCandidate(-1, DECKPILE, it))
                    .ifPresent(collector::add);
        }
        return collector;
    }

    protected void drawDeck() {
        if (isDeckCardsAvailable() && drawDeckCards()) {
            solve();
        }
    }

    protected boolean drawDeckCards() {
        if (checkRecycleDeck()) {
            IntStream.range(0, drawNumber)
                    .filter(i -> isNotEmpty(deck))
                    .forEach(i -> deckPile.push(deck.pop()));
            return true;
        }
        return false;
    }

    private boolean checkRecycleDeck() {
        if (deck.isEmpty()) {
            if (stateChanged) {
                while (isNotEmpty(deckPile)) {
                    deck.push(deckPile.pop());
                }
                setStateChanged(false);
            } else {
                return false;
            }
        }
        return true;
    }

    protected void applyCandidates(List<Candidate> candidates) {
        candidates.stream()
                .map(this::updateStates)
                .forEach(Klondike::solve);
    }

    protected Klondike updateStates(Candidate candidate) {
        setStateChanged(true);

        return new Klondike(this)
                .removeFromSource(candidate)
                .moveToTarget(candidate);
    }

    private boolean isFoundationCandidate(Card card) {
        var foundation = foundations.get(suitCode(card));

        return Optional.of(foundation)
                .filter(Stack::isEmpty)
                .map(it -> card.isAce())
                .orElseGet(() -> foundation.peek().isLowerWithSameSuit(card) && isImmediateToFoundation(card));
    }

    protected Klondike moveToTarget(Candidate candidate) {
        path.add(CardHelper.stringOfRaws(candidate.getCards()));

        if (candidate.isToColumn()) {
            appendToTargetColumn(candidate);
            if (isScorable(candidate)) {
                totalScore += 5;
            }
        } else if (candidate.isToFoundation()) {
            moveToFoundation(candidate);
        }
        return this;
    }

    private boolean isScorable(Candidate candidate) {
        return DECKPILE.equals(candidate.getOrigin()) ||
                (COLUMN.equals(candidate.getOrigin()) && isNotEmpty(columns.get(candidate.getFrom())));
    }

    private void moveToFoundation(Candidate candidate) {
        var card = candidate.getCards().get(0);

        Optional.of(suitCode(card))
                .map(foundations::get)
                .ifPresent(it -> it.push(card));
        totalScore += isFirstCardToFoundation(candidate) ? 15 : DECKPILE.equals(candidate.getOrigin()) ? 10 : 5;
    }

    private boolean isFirstCardToFoundation(Candidate candidate) {
        if (COLUMN.equals(candidate.getOrigin())) {
            var card = candidate.getCards().get(0);

            return foundations.get(suitCode(card)).size() == 1;
        }
        return false;
    }

    protected Klondike removeFromSource(Candidate candidate) {
        switch (candidate.getOrigin()) {
            case COLUMN -> removeFromColumn(candidate);
            case DECKPILE -> deckPile.pop();
            case FOUNDATION -> foundations.get(candidate.getFrom()).pop();
        }
        return this;
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
        return IntStream.range(0, columns.size())
                .filter(i -> isMatchingColumn(i, candidate))
                .mapToObj(i -> new Candidate(candidate).setTarget(i))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private boolean isMatchingColumn(int colNum, Candidate candidate) {
        if (isNotSameColumn(colNum, candidate)) {
            return isAppendable(columns.get(colNum), candidate);
        }
        return false;
    }

    private boolean isAppendable(Column column, Candidate candidate) {
        var cards = candidate.getCards();
        var card = cards.get(0);

        if (column.isEmpty()) {
            return card.isKing() && isMovable(candidate);
        }
        return column.peek().isHigherWithDifferentColor(card);
    }

    private boolean isMovable(Candidate candidate) {
        if (COLUMN.equals(candidate.getOrigin())) {
            var column = getColumns().get(candidate.getFrom());
            var card = candidate.getCards().get(0);

            return column.indexOf(card) > 0;
        }
        return DECKPILE.equals(candidate.getOrigin());
    }

    private boolean isNotSameColumn(int colNum, Candidate candidate) {
        return !(COLUMN.equals(candidate.getOrigin()) && candidate.getFrom() == colNum);
    }

    protected List<Candidate> findOpenCandidates() {
        var candidates = findCandidatesFromColumns();

        return Optional.ofNullable(findCandidateAtDeck())
                .map(it -> add(candidates, it))
                .orElse(candidates);
    }

    private List<Candidate> findCandidatesFromColumns() {
        return IntStream.range(0, 7)
                .filter(i -> isNotEmpty(columns.get(i)))
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
        return Optional.of(getOrderedCards(columns.get(colAt)))
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> buildCandidate(colAt, COLUMN, it))
                .orElse(null);
    }

    protected List<Card> getOrderedCards(Column column) {
        var collector = new LinkedList<Card>();

        for (int i = column.size() - 1; max(column.getOpenAt(), 0) <= i; i--) {
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

    private boolean isMovable(Card card, Column column) {
        return !card.isKing() || column.indexOf(card) > 0;
    }

    protected boolean isDeckCardsAvailable() {
        return !(deck.isEmpty() && deckPile.isEmpty());
    }

    protected boolean isCleared() {
        return foundations.stream().allMatch(it -> it.size() == 13);
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
//        results.forEach(System.out::println);
        return null;
    }

    protected List<Candidate> optimizeCandidates(List<Candidate> candidates) {
        return Optional.of(candidates)
                .map(this::removeDuplicateKings)
                .orElse(candidates);
    }

    private List<Candidate> removeDuplicateKings(List<Candidate> candidates) {
        var collect = new LinkedList<Candidate>();

        for (int i = 0; i < candidates.size() - 1; i++) {
            var a = candidates.get(i);

            if (a.getCards().get(0).isKing()) {
                for (int j = i + 1; j < candidates.size(); j++) {
                    var b = candidates.get(j);

                    if (b.getCards().get(0).isKing()) {
                        if (isDuplicate(a, b)) {
                            collect.add(b);
                        }
                    }
                }
            }
        }
        collect.forEach(candidates::remove);
        return candidates;
    }

    private boolean isDuplicate(Candidate a, Candidate b) {
        return a.getOrigin() == b.getOrigin() && a.getFrom() == b.getFrom() && a.getTarget() != b.getTarget();
    }

    private boolean isImmediateToFoundation(Card card) {
        return foundations.stream()
                .allMatch(it -> diffOfValues(card, it.isEmpty() ? null : it.peek()) <= 2);
    }

    @Override
    public int totalScenarios() {
        return totalScenarios;
    }

}