package org.solitaire.klondike;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.GameSolver;
import org.solitaire.model.Origin;
import org.solitaire.util.CollectionUtil;

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
import static java.util.Collections.singletonList;
import static org.solitaire.klondike.KlondikeHelper.drawDeckCards;
import static org.solitaire.klondike.KlondikeHelper.toStack;
import static org.solitaire.model.CardHelper.suitCode;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.util.CollectionUtil.add;
import static org.solitaire.util.CollectionUtil.isNotEmpty;

@SuppressWarnings("rawtypes")
@EqualsAndHashCode
@Getter
@Builder
public class Klondike implements GameSolver {
    private static int drawNumber = 3;
    private Stack<Card> deck;
    private Stack<Card> deckPile;
    private List<Stack<Card>> foundations;
    private List<Column> columns;
    private List<String[]> path;
    private int totalScore;

    @Override
    public List<List> solve() {
        if (isCleared()) {
            return singletonList(path);
        }
        return Optional.of(findCandidates())
                .filter(CollectionUtil::isNotEmpty)
                .map(this::findTargets)
                .filter(CollectionUtil::isNotEmpty)
                .map(this::moveCards)
                .orElseGet(this::drawDeck);
    }

    protected List<List> drawDeck() {
        if (isDeckCardsAvailable()) {
            return KlondikeHelper.clone(this)
                    .drawCardsFromDeck()
                    .solve();
        }
        return singletonList(emptyList());
    }

    protected Klondike drawCardsFromDeck() {
        checkDeckRecycle();
        drawDeckCards(deck, deckPile, drawNumber);
        return this;
    }

    private void checkDeckRecycle() {
        if (deck.isEmpty()) {
            while (!deckPile.isEmpty()) {
                deck.push(deckPile.pop());
            }
        }
    }

    protected List<List> moveCards(List<Candidate> candidates) {
        return candidates.stream()
                .map(this::updateBoard)
                .map(Klondike::solve)
                .flatMap(List::stream)
                .peek(it -> System.out.println("Solution #" + it.size()))
                .filter(CollectionUtil::isNotEmpty)
                .toList();
    }

    protected Klondike updateBoard(Candidate candidate) {
        return KlondikeHelper.clone(this)
                .removeFromSource(candidate)
                .appendToTarget(candidate)
                .checkColumnsForFoundation();
    }

    protected Klondike checkColumnsForFoundation() {
        columns.stream()
                .filter(Column::isNotEmpty)
                .filter(this::isMovableToFoundation)
                .forEach(this::moveToFoundation);
        return this;
    }

    protected void moveToFoundation(Column column) {
        var card = column.pop();

        totalScore += 10;
        Optional.of(suitCode(card))
                .map(foundations::get)
                .ifPresent(it -> it.push(card));
    }

    protected boolean isMovableToFoundation(Column column) {
        var card = column.peek();
        var stack = foundations.get(suitCode(card));

        return Optional.of(stack)
                .filter(Stack::isEmpty)
                .map(it -> card.isAce())
                .orElseGet(() -> stack.peek().isLowerWithSameSuit(card));
    }

    protected Klondike appendToTarget(Candidate candidate) {
        var column = columns.get(candidate.getTarget());
        var cards = candidate.getCards();
        var collect = new ArrayList<String>(cards.size());

        while (!cards.isEmpty()) {
            var card = cards.pop();

            column.add(card);
            collect.add(0, card.raw());
        }
        path.add(collect.toArray(String[]::new));
        if (COLUMN.equals(candidate.getOrigin()) && isNotEmpty(columns.get(candidate.getFrom()))) {
            totalScore += 5;
        }
        return this;
    }

    protected Klondike removeFromSource(Candidate candidate) {
        switch (candidate.getOrigin()) {
            case COLUMN -> removeFromColumn(candidate);
            case DECKPILE -> deckPile.pop();
            case FOUNDATION -> foundations.get(candidate.getFrom()).pop();
        }
        return this;
    }

    private void removeFromColumn(Candidate candidate) {
        var column = columns.get(candidate.getFrom());

        for (int i = column.size(), floor = column.size() - candidate.getCards().size(); --i >= floor; ) {
            column.remove(i);
        }
    }

    protected List<Candidate> findTargets(List<Candidate> candidates) {
        return candidates.stream()
                .map(this::findTarget)
                .filter(Objects::nonNull)
                .toList();
    }

    protected Candidate findTarget(Candidate candidate) {
        return IntStream.range(0, columns.size())
                .filter(i -> isMatchingColumn(i, candidate))
                .mapToObj(candidate::setTarget)
                .findFirst()
                .orElse(null);
    }

    private boolean isMatchingColumn(int colNum, Candidate candidate) {
        if (isNotSameColumn(colNum, candidate)) {
            return isAppendable(columns.get(colNum), candidate);
        }
        return false;
    }

    private boolean isAppendable(Column column, Candidate candidate) {
        var card = candidate.getCards().peek();

        if (column.isEmpty()) {
            return card.isKing();
        }
        return column.peek().isHigherWithDifferentColor(card);
    }

    private boolean isNotSameColumn(int colNum, Candidate candidate) {
        return !(COLUMN.equals(candidate.getOrigin()) && candidate.getFrom() == colNum);
    }

    protected List<Candidate> findCandidates() {
        var candidates = checkColumns();

        return Optional.ofNullable(checkDeck())
                .map(it -> add(candidates, it))
                .orElse(candidates);
    }

    private List<Candidate> checkColumns() {
        return IntStream.range(0, 7)
                .filter(i -> !columns.get(i).isEmpty())
                .mapToObj(this::findCandidateAtColumn)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private Candidate checkDeck() {
        return Optional.of(deckPile)
                .filter(CollectionUtil::isNotEmpty)
                .map(Stack::pop)
                .map(it -> buildCandidate(-1, DECKPILE, toStack(it)))
                .orElse(null);
    }

    protected Candidate findCandidateAtColumn(int colAt) {
        return Optional.of(getOrderedCards(columns.get(colAt)))
                .filter(CollectionUtil::isNotEmpty)
                .map(it -> buildCandidate(colAt, COLUMN, it))
                .orElse(null);
    }

    private Candidate buildCandidate(int at, Origin origin, Stack<Card> cards) {
        return Candidate.builder().from(at).origin(origin).cards(cards).build();
    }

    protected Stack<Card> getOrderedCards(Column column) {
        var collector = new Stack<Card>();

        for (int i = column.size() - 1; max(column.getOpenAt(), 0) <= i; i--) {
            var card = column.get(i);

            if (collector.isEmpty() || card.isHigherWithDifferentColor(collector.peek())) {
                collector.push(card);
            }
        }
        return collector;
    }

    protected boolean isDeckCardsAvailable() {
        return !(deck.isEmpty() && deckPile.isEmpty());
    }

    protected boolean isCleared() {
        return deck.isEmpty() && allColumnsEmpty(columns);
    }

    protected boolean allColumnsEmpty(List<Column> lists) {
        return columns.stream().filter(List::isEmpty).count() == lists.size();
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return null;
    }
}