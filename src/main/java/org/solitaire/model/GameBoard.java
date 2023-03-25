package org.solitaire.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.solitaire.util.BoardHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.copyOfRange;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.IntStream.range;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.CardHelper.toArray;

@Slf4j
public class GameBoard implements Board<String, Candidate> {
    public static final Function<Stream<Candidate>, Stream<Candidate>> flattenStream = it -> it;

    private transient BiPredicate<Card, Card> isInSequence;
    public transient final IntPredicate isNotEmpty = i -> column(i).isNotEmpty();
    private transient int score = MIN_VALUE;
    private int totalScore;
    protected final Columns columns;
    protected final Path<String> path;

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
        Optional.of(candidate)
                .map(Candidate::cards)
                .filter(it -> column.contains(it[0]))
                .ifPresent(it -> removeIt(candidate, column));
    }

    protected void removeIt(Candidate candidate, Column column) {
        var colSize = column.size();

        column.subList(colSize - candidate.cards().length, colSize).clear();
        column.openAt(min(column.openAt(), column.size() - 1));
    }

    protected void addToTargetColumn(Candidate candidate) {
        var cards = candidate.cards();
        var column = column(candidate.to());
        var openAt = column.openAt();

        column(candidate.to()).addAll(List.of(cards));
        column.openAt(openAt);
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
                .filter(this::isNotRepeatingCandidate)
                .filter(this::isLongerTargetSequence);
    }

    public boolean isNotRepeatingCandidate(Candidate candidate) {
        return !path().contains(candidate.notation());
    }

    public boolean isLongerTargetSequence(Candidate candidate) {
        var column = column(candidate.to());

        if (column.isNotEmpty()) {
            return targetSize(candidate) > getOrderedCards(column(candidate.from())).length;
        }
        return true;
    }

    private int targetSize(Candidate candidate) {
        return getOrderedCards(column(candidate.to())).length + candidate.cards().length;
    }

    public Candidate toColumnCandidate(int from, int to, Card card) {
        return Optional.of(from)
                .filter(it -> it != to)
                .map(this::column)
                .map(this::getOrderedCards)
                .filter(BoardHelper.isNotEmpty)
                .map(it -> toColumnCandidate(it, from, to, card))
                .orElse(null);
    }

    public Candidate toColumnCandidate(Card[] cards, int from, int to, Card card) {
        if (isMovable(cards, from, to)) {
            if (isNull(card)) {
                return candidateToEmptyColumn(cards, from, to);
            }
            return range(0, cards.length)
                    .filter(i -> isInSequence().test(card, cards[i]))
                    .mapToObj(i -> new Candidate(copyOfRange(cards, i, cards.length), COLUMN, from, COLUMN, to))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public Card[] getOrderedCards(Column column) {
        if (column.isEmpty()) {
            return toArray();
        }
        int at;
        var floor = max(column.openAt(), 0);

        for (at = column.size() - 1; at > floor; at--) {
            if (!isInSequence().test(column.get(at - 1), column.get(at))) {
                break;
            }
        }
        return toArray(column.subList(at, column.size()));
    }

    protected boolean isMovable(Card[] cards, int from, int to) {
        return cards.length < column(from).size() || column(to).isNotEmpty();
    }

    protected Candidate candidateToEmptyColumn(Card[] cards, int from, int to) {
        throw new RuntimeException("candidateToEmptyColumn was not implemented!");
    }

    public boolean isMovableToEmptyColumn(Candidate c) {
        return !c.isFromColumn() || (c.cards().length < column(c.from()).size() || isNotEmpty.test(c.to()));
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

    public void totalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public List<String> verify() {
        throw new RuntimeException("'verify' not implemented");
    }

    @Override
    public int score() {
        return score;
    }

    public boolean isNotScored() {
        return score == MIN_VALUE;
    }

    public void resetScore() {
        score(MIN_VALUE);
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
                .filter(BoardHelper.isNotEmpty)
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

}
