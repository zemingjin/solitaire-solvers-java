package org.solitaire.spider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelper.suitCode;

@Slf4j
public class SpiderBoard extends GameBoard {
    protected final Deck deck;
    protected transient final IntPredicate isNotEmpty = i -> !columns().get(i).isEmpty();
    private transient final IntPredicate isLongEnoughForRun = i -> 13 <= columns().get(i).size();
    private transient final IntPredicate isThereARun = i -> isThereARun(columns().get(i));

    public SpiderBoard(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(columns, path, totalScore);
        this.deck = deck;
    }

    public SpiderBoard(SpiderBoard that) {
        this(new Columns(that.columns()), new Path<>(that.path()), that.totalScore(), new Deck(that.deck()));
    }

    public Deck deck() {
        return deck;
    }

    @Override
    public boolean isSolved() {
        return super.isSolved() && deck.isEmpty();
    }

    /**************************************************************************************************************
     * Find/Match/Sort Candidates
     *************************************************************************************************************/
    @Override
    public List<Candidate> findCandidates() {
        return Stream.of(findCandidatesOfSameSuit(),
                        findCandidatesOfDifferentColors(),
                        drawDeck())
                .flatMap(List::stream)
                .toList();
    }

    protected List<Candidate> findCandidatesOfSameSuit() {
        return range(0, columns().size())
                .mapToObj(this::findCandidatesOfSameSuit)
                .flatMap(List::stream)
                .filter(this::isMovable)
                .toList();
    }

    private List<Candidate> findCandidatesOfSameSuit(Integer i) {
        return Optional.of(columns().get(i))
                .filter(ObjectUtils::isNotEmpty)
                .map(this::getOrderedCardsAtColumn)
                .map(it -> findCandidatesOfSameSuit(i, it))
                .orElseGet(Collections::emptyList);
    }

    private List<Candidate> findCandidatesOfSameSuit(Integer i, List<Card> cards) {
        return range(0, columns().size())
                .mapToObj(j -> findCandidateOfSameSuit(i, j, cards))
                .filter(Objects::nonNull)
                .toList();
    }

    private Candidate findCandidateOfSameSuit(Integer i, Integer j, List<Card> cards) {
        var column = columns().get(j);

        if (column.isEmpty() || column.peek().isHigherOfSameColor(cards.get(0))) {
            return new Candidate(cards, COLUMN, i, COLUMN, j);
        }
        var card = column.peek();
        if (card.isSameSuit(cards.get(0))) {
            for (int k = 0; k < cards.size(); k++) {
                if (card.isHigherOfSameColor(cards.get(k))) {
                    return new Candidate(cards.subList(k, cards.size()), COLUMN, i, COLUMN, j);
                }
            }
        }
        return null;
    }

    private List<Candidate> findCandidatesOfDifferentColors() {
        return range(0, columns().size())
                .mapToObj(this::findCandidatesOfDifferentColors)
                .flatMap(List::stream)
                .filter(this::isMovable)
                .toList();
    }

    private List<Candidate> findCandidatesOfDifferentColors(Integer i) {
        return Optional.of(columns().get(i))
                .filter(ObjectUtils::isNotEmpty)
                .map(this::getOrderedCardsAtColumn)
                .filter(it -> it.size() == 1)
                .map(it -> findCandidatesOfDifferentColors(i, it.get(0)))
                .orElseGet(Collections::emptyList);
    }

    private List<Candidate> findCandidatesOfDifferentColors(Integer i, Card card) {
        return range(0, columns().size())
                .mapToObj(j -> findCandidateOfDifferentColors(i, j, card))
                .filter(Objects::nonNull)
                .toList();
    }

    private Candidate findCandidateOfDifferentColors(Integer i, Integer j, Card card) {
        var column = columns().get(j);

        if (column.isEmpty() || column.peek().isHigherWithDifferentColor(card)) {
            return new Candidate(List.of(card), COLUMN, i, COLUMN, j);
        }
        return null;
    }

    protected boolean isMovable(Candidate candidate) {
        return Optional.of(candidate)
                .filter(this::isNotRepeatingCandidate)
                .filter(this::isLongerTargetSequence)
                .map(it -> !(it.isKing() && isAtTop(it)))
                .orElse(false);
    }

    private boolean isAtTop(Candidate candidate) {
        return columns.get(candidate.from()).indexOf(candidate.peek()) == 0;
    }

    private boolean isLongerTargetSequence(Candidate candidate) {
        var column =  columns().get(candidate.to());

        if (column.isNotEmpty()) {
            var target = column.peek();

            if (target.isSameSuit(candidate.peek())) {
                return targetSize(candidate) > getOrderedCardsAtColumn(columns().get(candidate.from())).size();
            }
        }
        return true;
    }

    private int targetSize(Candidate candidate) {
        return getOrderedCardsAtColumn(columns.get(candidate.to())).size() + candidate.cards().size();
    }

    private boolean isNotRepeatingCandidate(Candidate candidate) {
        var prev = candidate.notation();
        return path.stream().noneMatch(it -> it.equals(prev));
    }

    protected List<Card> getOrderedCardsAtColumn(Column column) {
        var collector = new LinkedList<Card>();

        for (int i = column.size(), floor = max(column.openAt(), 0); i-- > floor; ) {
            var card = column.get(i);

            if (collector.isEmpty() || card.isHigherOfSameColor(collector.get(0))) {
                collector.add(0, card);
            } else {
                break;
            }
        }
        return collector;
    }

    /**************************************************************************************************************
     * Update State
     * ***********************************************************************************************************/
    @Override
    public SpiderBoard updateBoard(Candidate candidate) {
        return removeFromSource(candidate)
                .appendToTarget(candidate)
                .checkForRun(candidate);
    }

    protected SpiderBoard removeFromSource(Candidate candidate) {
        switch (candidate.origin()) {
            case COLUMN -> removeFromColumn(candidate);
            case DECKPILE -> removeFromDeck(candidate);
        }
        return this;
    }

    private void removeFromDeck(Candidate candidate) {
        candidate.cards().forEach(it -> deck().remove(it));
    }

    protected SpiderBoard appendToTarget(Candidate candidate) {
        path.add(candidate.notation());

        switch (candidate.origin()) {
            case DECKPILE -> range(0, columns().size()).forEach(i -> columns().get(i).add(candidate.cards().get(i)));
            case COLUMN -> {
                addToTargetColumn(candidate);
                totalScore--;
            }
        }
        return this;
    }

    protected SpiderBoard checkForRun(Candidate candidate) {
        rangeClosed(candidate.to(), candidate.to())
                .filter(isNotEmpty.and(isLongEnoughForRun).and(isThereARun))
                .forEach(this::removeTheRun);
        return this;
    }

    private boolean isThereARun(Column column) {
        assert nonNull(column) && 13 <= column.size();

        for (int i = column.size() - 1, floor = column.size() - 13; i > floor; i--) {
            var a = column.get(i);
            var b = column.get(i - 1);

            if (!b.isHigherOfSameColor(a)) {
                return false;
            }
        }
        return true;
    }

    private void removeTheRun(int i) {
        var column = columns().get(i);
        assert nonNull(column) && 13 <= column.size();

        var run = column.subList(column.size() - 13, column.size());
        var candidate = new Candidate(run, COLUMN, i, FOUNDATION, suitCode(run.get(0)));

        path().add(candidate.notation());
        if (isPrint()) {
            System.out.printf("Run: %s\n", stringOfRaws(run));
        }
        totalScore += 100;
        run.clear();
    }

    protected List<Candidate> drawDeck() {
        if (isNoEmptyColumn() && isNotEmpty(deck)) {
            var cards = deck().subList(0, columns.size());

            return List.of(new Candidate(new ArrayList<>(cards), DECKPILE, 0, COLUMN, 0));
        }
        return emptyList();
    }

    private boolean isNoEmptyColumn() {
        return columns().stream().allMatch(Column::isNotEmpty);
    }

    /*****************************************************************************************************************
     * Scoring board
     ****************************************************************************************************************/
    @Override
    public int score() {
        if (super.score() == 0) {
            // The smaller, the better.
            var boardCards = columns().stream().mapToInt(Column::size).sum() * 2;
            var blockerCount = countBlockers();
            // The larger, the better.
            var sequences = calcSequences();
            super.score(sequences - boardCards - blockerCount);
        }
        return super.score();
    }

    private int countBlockers() {
        return range(0, columns().size())
                .map(this::countBlockers)
                .sum();
    }

    protected int countBlockers(int col) {
        var column = columns().get(col);

        if (column.isEmpty()) {
            return 0;
        }
        var cards = getOrderedCardsAtColumn(column);
        var card = cards.get(0);

        if (card.isNotKing()) {
            var next = card(VALUES.charAt(card.rank()) + card.suit());
            var value = range(0, columns().size())
                    .filter(i -> i != col)
                    .mapToObj(columns()::get)
                    .filter(ObjectUtils::isNotEmpty)
                    .filter(it -> it.contains(next))
                    .mapToInt(it -> it.size() - it.lastIndexOf(next) - 1)
                    .reduce(MAX_VALUE, Math::min);
            if (value == MAX_VALUE) {
                return deck.indexOf(next) / columns().size();
            }
            return value;
        }
        return 0;
    }

    // The bigger, the better
    protected int calcSequences() {
        return columns.stream()
                .map(this::getOrderedCardsAtColumn)
                .mapToInt(List::size)
                .filter(i -> i > 1)
                .map(it -> it * it)
                .sum();
    }

}
