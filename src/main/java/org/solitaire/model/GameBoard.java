package org.solitaire.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.util.BoardHelper.listNotEmpty;

@Slf4j
public class GameBoard implements Board<String, Candidate> {
    public transient final IntPredicate isNotEmpty = i -> isNotEmpty(columns().get(i));
    public transient final Predicate<Candidate> isMovableToEmptyColumn
            = c -> !c.isFromColumn() || (c.cards().size() < columns().get(c.from()).size() || isNotEmpty.test(c.to()));
    protected final Columns columns;
    protected final Path<String> path;
    protected int totalScore;
    private transient int score = 0;

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
                .filter(it -> column.contains(it.get(0)))
                .ifPresent(it -> column.subList(colSize - it.size(), colSize).clear());
    }

    protected void addToTargetColumn(Candidate candidate) {
        var cards = candidate.cards();

        columns.get(candidate.to()).addAll(cards);
    }

    @Override
    public boolean isSolved() {
        return columns.isCleared();
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
}
