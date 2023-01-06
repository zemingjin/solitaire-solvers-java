package org.solitaire.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class GameState {
    protected final Columns columns;
    protected final Path path;
    protected int totalScore;

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
        return columns.stream().anyMatch(Column::isNotEmpty);
    }
}
