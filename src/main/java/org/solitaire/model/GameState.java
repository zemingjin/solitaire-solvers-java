package org.solitaire.model;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class GameState<R> {
    protected final Columns columns;
    protected final Path<R> path;
    protected int totalScore;
    private double score = 0;

    public GameState(Columns columns, Path<R> path) {
        this(columns, path, 0);
    }

    public GameState(Columns columns, Path<R> path, int totalScore) {
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

    protected void appendToTargetColumn(Candidate candidate) {
        var cards = candidate.cards();

        columns.get(candidate.target()).addAll(cards);
    }

    public boolean isCleared() {
        return columns.isCleared();
    }

    public Columns columns() {
        return columns;
    }

    public Path<R> path() {
        return path;
    }

    public int totalScore() {
        return totalScore;
    }

    public double score() {
        return score;
    }

    public void score(double score) {
        this.score = score;
    }
}
