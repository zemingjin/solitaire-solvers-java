package org.solitaire.spider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.TriFunction;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.listNotEmpty;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelper.suitCode;
import static org.solitaire.util.CardHelper.toArray;

@Slf4j
public class SpiderBoard extends GameBoard {
    protected final Deck deck;
    private transient final IntPredicate isLongEnoughForRun = i -> 13 <= column(i).size();
    private transient final IntPredicate isThereARun = i -> isThereARun(column(i));

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
        return Optional.of(findBoardCandidates())
                .map(Stream::toList)
                .filter(listNotEmpty)
                .orElseGet(this::drawDeck);
    }

    private Stream<Candidate> findBoardCandidates() {
        return concat(findCandidates(this::findCandidateOfSameSuit),
                findCandidates(this::findCandidateOfDifferentColors));
    }

    protected Stream<Candidate> findCandidates(TriFunction<Integer, Integer, List<Card>, Candidate> finder) {
        return range(0, columns().size())
                .filter(isNotEmpty)
                .mapToObj(i -> findCandidates(i, getOrderedCardsAtColumn(column(i)), finder))
                .flatMap(flattenStream)
                .filter(this::isMovable);
    }

    private Stream<Candidate> findCandidates(int i, List<Card> cards,
                                             TriFunction<Integer, Integer, List<Card>, Candidate> finder) {
        return range(0, columns().size())
                .mapToObj(j -> finder.apply(i, j, cards))
                .filter(isNotNull);
    }

    protected Candidate findCandidateOfSameSuit(Integer i, Integer j, List<Card> cards) {
        var column = column(j);

        if (column.isEmpty() || column.peek().isHigherOfSameColor(cards.get(0))) {
            return new Candidate(cards.toArray(Card[]::new), COLUMN, i, COLUMN, j);
        }
        var card = column.peek();
        if (card.isSameSuit(cards.get(0))) {
            for (int k = 0; k < cards.size(); k++) {
                if (card.isHigherOfSameColor(cards.get(k))) {
                    return new Candidate(cards.subList(k, cards.size()).toArray(Card[]::new), COLUMN, i, COLUMN, j);
                }
            }
        }
        return null;
    }

    private Candidate findCandidateOfDifferentColors(Integer i, Integer j, List<Card> cards) {
        if (cards.size() == 1) {
            var column = column(j);
            var card = cards.get(0);

            if (column.isEmpty() || column.peek().isHigherWithDifferentColor(card)) {
                return new Candidate(toArray(card), COLUMN, i, COLUMN, j);
            }
        }
        return null;
    }

    protected boolean isMovable(Candidate candidate) {
        return Optional.of(candidate)
                .filter(this::isNotRepeatingCandidate)
                .filter(this::isLongerTargetSequence)
                .filter(isMovableToEmptyColumn)
                .map(it -> !(it.isKing() && isAtTop(it)))
                .orElse(false);
    }

    private boolean isAtTop(Candidate candidate) {
        return columns.get(candidate.from()).indexOf(candidate.peek()) == 0;
    }

    private boolean isLongerTargetSequence(Candidate candidate) {
        var column = column(candidate.to());

        if (column.isNotEmpty()) {
            var target = column.peek();

            if (target.isSameSuit(candidate.peek())) {
                return targetSize(candidate) > getOrderedCardsAtColumn(column(candidate.from())).size();
            }
        }
        return true;
    }

    private int targetSize(Candidate candidate) {
        return getOrderedCardsAtColumn(columns.get(candidate.to())).size() + candidate.cards().length;
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
     * Update Board
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
        Stream.of(candidate.cards()).forEach(it -> deck().remove(it));
    }

    protected SpiderBoard appendToTarget(Candidate candidate) {
        path.add(candidate.notation());

        switch (candidate.origin()) {
            case DECKPILE -> range(0, columns().size()).forEach(i -> column(i).add(candidate.cards()[i]));
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
        var column = column(i);
        assert nonNull(column) && 13 <= column.size();

        var run = column.subList(column.size() - 13, column.size());
        var candidate = new Candidate(run.toArray(Card[]::new), COLUMN, i, FOUNDATION, suitCode(run.get(0)));

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

            return List.of(new Candidate(cards.toArray(Card[]::new), DECKPILE, 0, COLUMN, 0));
        }
        return emptyList();
    }

    protected boolean isNoEmptyColumn() {
        return columns().stream().allMatch(Column::isNotEmpty);
    }

    /*****************************************************************************************************************
     * Scoring board
     ****************************************************************************************************************/
    @Override
    public int score() {
        if (super.score() == 0) {
            // The smaller, the better.
            var boardCards = columns().stream().mapToInt(Column::size).sum() * 3;
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
        var column = column(col);

        if (column.isEmpty()) {
            return 0;
        }
        var cards = getOrderedCardsAtColumn(column);
        var card = cards.get(0);

        if (card.isNotKing()) {
            var next = card.next();
            var value = valueFromColumns(col, next);

            if (value == MAX_VALUE) {
                return deck.indexOf(next) / columns().size();
            }
            return value;
        }
        return 0;
    }

    private int valueFromColumns(int col, Card next) {
        return range(0, columns().size())
                .filter(i -> i != col)
                .mapToObj(this::column)
                .filter(listNotEmpty)
                .filter(it -> it.contains(next))
                .mapToInt(it -> it.size() - it.lastIndexOf(next) - 1)
                .reduce(MAX_VALUE, Math::min);
    }

    // The bigger, the better
    protected int calcSequences() {
        return range(0, columns().size())
                .filter(isNotEmpty)
                .map(i -> calcSequenceScore(i, getOrderedCardsAtColumn(column(i))))
                .sum();
    }

    protected int calcSequenceScore(int i, List<Card> cards) {
        var size = cards.size();

        if (size == 1) {
            return 1;
        } else if (isBestSequence(cards, i)) {
            return size * size;
        }
        return size * 2;
    }

    boolean isBestSequence(List<Card> cards, int col) {
        return cards.get(0).isKing() && cards.size() == columns.get(col).size();
    }

    @Override
    public List<String> verify() {
        return verifyBoard(columns(), deck());
    }
}
