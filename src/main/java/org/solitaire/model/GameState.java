package org.solitaire.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Getter
public class GameState<R> {
    protected final Columns columns;
    protected final Path<R> path;
    protected int totalScore;

    public GameState(Columns columns, Path<R> path, int totalScore) {
        this.columns = columns;
        this.path = path;
        this.totalScore = totalScore;
    }

    protected void removeFromColumn(Candidate candidate) {
        Optional.of(candidate)
                .map(Candidate::getFrom)
                .map(columns::get)
                .ifPresent(it -> removeFromColumn(candidate, it));
    }

    private void removeFromColumn(Candidate candidate, Column column) {
        var colSize = column.size();

        Optional.of(candidate)
                .map(Candidate::getCards)
                .filter(it -> column.contains(it.get(0)))
                .ifPresent(it -> column.subList(colSize - it.size(), colSize).clear());
    }

    protected void appendToTargetColumn(Candidate candidate) {
        var cards = candidate.getCards();

        columns.get(candidate.getTarget()).addAll(cards);
    }

    protected boolean isCleared() {
        return columns.stream().allMatch(Column::isEmpty);
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
