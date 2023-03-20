package org.solitaire.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.util.Arrays.copyOfRange;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.IntStream.range;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.listNotEmpty;

@Slf4j
public class GameBoard implements Board<String, Candidate> {
    public transient final Function<Stream<Candidate>, Stream<Candidate>> flattenStream = it -> it;
    public transient final IntPredicate isNotEmpty = i -> column(i).isNotEmpty();
    public transient final Predicate<Candidate> isMovableToEmptyColumn = c ->
            !c.isFromColumn() || (c.cards().length < column(c.from()).size() || isNotEmpty.test(c.to()));
    public transient final Predicate<Candidate> isNotRepeatingCandidate = it -> !path().contains(it.notation());

    protected transient final Function<Column, Card[]> getOrderedCards = column -> {
        var floor = max(column.openAt(), 0);

        for (int i = column.size() - 1; i >= floor; ) {
            var card = column.get(i);

            if (i > floor && isInSequence().test(column.get(i - 1), card)) {
                i--;
            } else if (card.isNotKing() || i > 0) {
                return column.subList(i, column.size()).toArray(Card[]::new);
            } else {
                break;
            }
        }
        return new Card[]{};
    };

    private transient BiPredicate<Card, Card> isInSequence;
    protected final Path<String> path;
    protected int totalScore;
    private transient int score = 0;
    protected final Columns columns;

    public GameBoard(Columns columns, Path<String> path) {
        this(columns, path, 0);
    }

    public GameBoard(Columns columns, Path<String> path, int totalScore) {
        this.columns = columns;
        this.path = path;
        this.totalScore = totalScore;
    }

    protected void removeFromColumn(Candidate candidate) {
        Optional.of(candidate)
                .map(Candidate::from)
                .map(columns::get)
                .ifPresent(it -> removeFromColumn(candidate, it));
    }

    private void removeFromColumn(Candidate candidate, Column column) {
        var colSize = column.size();

        Optional.of(candidate)
                .map(Candidate::cards)
                .filter(it -> column.contains(it[0]))
                .ifPresent(it -> column.subList(colSize - it.length, colSize).clear());
    }

    protected void addToTargetColumn(Candidate candidate) {
        var cards = candidate.cards();

        column(candidate.to()).addAll(List.of(cards));
    }

    @Override
    public boolean isSolved() {
        return columns.isCleared();
    }

    public Stream<Candidate> findColumnToColumnCandidates() {
        return range(0, columns().size())
                .mapToObj(i -> toColumnCandidates(i, peek(i)))
                .flatMap(flattenStream)
                .filter(isNotNull);
    }

    public Stream<Candidate> toColumnCandidates(int to, Card card) {
        return range(0, columns().size())
                .mapToObj(i -> toColumnCandidate(i, to, card))
                .filter(isNotNull)
                .filter(isNotRepeatingCandidate);
    }

    public Candidate toColumnCandidate(int from, int to, Card card) {
        return Optional.of(from)
                .filter(it -> it != to)
                .map(this::column)
                .map(getOrderedCards)
                .filter(listNotEmpty)
                .map(it -> toColumnCandidate(it, from, to, card))
                .orElse(null);
    }

    public Candidate toColumnCandidate(Card[] cards, int from, int to, Card card) {
        if (isNull(card)) {
            return candidateToEmptyColumn(cards, from, to);
        }
        return range(0, cards.length)
                .filter(i -> card.isHigherWithDifferentColor(cards[i]))
                .mapToObj(i -> new Candidate(copyOfRange(cards, i, cards.length), COLUMN, from, COLUMN, to))
                .findFirst()
                .orElse(null);
    }

    protected Candidate candidateToEmptyColumn(Card[] cards, int from, int to) {
        throw new RuntimeException("candidateToEmptyColumn was not implemented!");
    }

    @Override
    public Path<String> path() {
        return path;
    }

    @Override
    public List<Candidate> findCandidates() {
        return emptyList();
    }

    public Columns columns() {
        return columns;
    }

    public int totalScore() {
        return totalScore;
    }

    @Override
    public List<String> verify() {
        throw new RuntimeException("'verify' not implemented");
    }

    @Override
    public int score() {
        return score;
    }

    @Override
    public Board<String, Candidate> updateBoard(Candidate candidate) {
        return null;
    }

    public void score(int score) {
        this.score = score;
    }

    public List<Candidate> add(List<Candidate> collector, Candidate item) {
        collector.add(item);
        return collector;
    }

    public int countEmptyColumns() {
        return (int) columns.stream().filter(ObjectUtils::isEmpty).count();
    }

    public Card peek(int colId) {
        return Optional.of(colId)
                .map(this::column)
                .filter(listNotEmpty)
                .map(Column::peek)
                .orElse(null);
    }

    public Column column(int colId) {
        return columns().get(colId);
    }

    protected BiPredicate<Card, Card> isInSequence() {
        return isInSequence;
    }

    public void isInSequence(BiPredicate<Card, Card> isInSequence) {
        this.isInSequence = isInSequence;
    }

    public Function<Column, Card[]> getOrderedCards() {
        return getOrderedCards;
    }

}
