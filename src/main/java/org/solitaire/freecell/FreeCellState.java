package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.GameState;
import org.solitaire.model.Path;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.FREECELL;
import static org.solitaire.util.CardHelper.cloneArray;

public class FreeCellState extends GameState<Card[]> {
    private final Card[] freeCells;
    private final Card[] foundations;

    public FreeCellState(Columns columns, Path<Card[]> path, Card[] freeCells, Card[] foundations) {
        super(columns, path);
        this.freeCells = freeCells;
        this.foundations = foundations;
    }

    protected FreeCellState(@Nonnull FreeCellState that) {
        this(new Columns(that.columns), new Path<>(that.path), cloneArray(that.freeCells), cloneArray(that.foundations));
    }

    /*****************************************************************************************************************
     * Find/Match Candidates
     ****************************************************************************************************************/
    protected List<Candidate> findCandidates() {
        return Stream.concat(findColumnCandidates(), findFreeCellCandidates())
                .map(this::getTargetCandidates)
                .flatMap(List::stream)
                .toList();
    }

    protected List<Candidate> getTargetCandidates(Candidate candidate) {
        return range(0, columns.size())
                .mapToObj(i -> Pair.of(i, candidate))
                .filter(this::isAppendableToColumn)
                .filter(this::isMovable)
                .map(it -> buildCandidate(it.getRight(), it.getLeft()))
                .toList();
    }

    private boolean isAppendableToColumn(Pair<Integer, Candidate> pair) {
        return Optional.of(pair.getLeft())
                .map(columns::get)
                .map(Column::peek)
                .filter(it -> it.isHigherWithDifferentColor(pair.getRight().peek()))
                .isPresent();
    }

    private boolean isMovable(Pair<Integer, Candidate> pair) {
        return pair.getRight().cards().size() <= maxCardsToMove();
    }

    protected Stream<Candidate> findFreeCellCandidates() {
        return range(0, freeCells.length)
                .filter(i -> nonNull(freeCells[i]))
                .mapToObj(i -> buildCandidate(i, FREECELL, freeCells[i]));
    }

    protected Stream<Candidate> findColumnCandidates() {
        return range(0, columns.size())
                .mapToObj(this::findCandidateAtColumn)
                .filter(Objects::nonNull);
    }

    protected Candidate findCandidateAtColumn(int col) {
        return Optional.of(col)
                .map(columns::get)
                .filter(ObjectUtils::isNotEmpty)
                .map(this::findCandidateAtColumn)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> buildCandidate(col, COLUMN, it))
                .orElse(null);
    }

    protected List<Card> findCandidateAtColumn(Column column) {
        var ceiling = column.size() - 1;
        var collector = new LinkedList<Card>();

        rangeClosed(0, ceiling)
                .map(i -> ceiling - i)
                .mapToObj(column::get)
                .map(it -> Pair.of(it, (List<Card>) collector))
                .forEach(this::checkCandidateCard);
        return collector;
    }

    private void checkCandidateCard(Pair<Card, List<Card>> pair) {
        var collector = pair.getRight();
        var card = pair.getLeft();

        if (collector.isEmpty() || card.isHigherWithDifferentColor(collector.get(0))) {
            collector.add(0, card);
        }
    }

    /*****************************************************************************************************************
     * Apply candidate
     ****************************************************************************************************************/
    protected FreeCellState updateState(Candidate candidate) {
        return Optional.of(candidate)
                .map(this::removeFromOrigin)
                .map(this::moveToTarget)
                .orElse(null);
    }

    protected Candidate removeFromOrigin(Candidate candidate) {
        switch (candidate.origin()) {
            case COLUMN -> {
                var column = columns.get(candidate.from());

                column.subList(column.size() - candidate.cards().size(), column.size()).clear();
            }
            case FREECELL -> freeCells[candidate.from()] = null;
        }
        return candidate;
    }

    private FreeCellState moveToTarget(Candidate candidate) {
        path.add(candidate.cards().toArray(Card[]::new));
        return Optional.of(candidate)
                .filter(it -> it.target() >= 0)
                .map(this::moveToColumn)
                .orElseGet(() -> toFreeCell(candidate.peek()));
    }

    private FreeCellState moveToColumn(Candidate candidate) {
        return Optional.of(candidate)
                .map(Candidate::target)
                .map(columns::get)
                .map(it -> { it.addAll(candidate.cards()); return this; })
                .orElse(null);
    }

    private FreeCellState toFreeCell(Card card) {
        return range(0, freeCells.length)
                .filter(i -> isNull(freeCells[i]))
                .mapToObj(i -> Pair.of(i, card))
                .map(this::toFreeCell)
                .findFirst()
                .orElse(null);

    }

    private FreeCellState toFreeCell(Pair<Integer, Card> pair) {
        freeCells[pair.getLeft()] = pair.getRight();
        return this;
    }

    protected Card[] freeCells() {
        return freeCells;
    }

    protected int maxCardsToMove() {
        var emptyColumns = columns.stream().filter(ObjectUtils::isEmpty).count();
        var emptyFreeCells = stream(freeCells).filter(Objects::isNull).count();

        return (int)((emptyFreeCells + 1) * (emptyColumns + 1));
    }
}
