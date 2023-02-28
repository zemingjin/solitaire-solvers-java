package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.Origin.FREECELL;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.suit;
import static org.solitaire.util.CardHelper.suitCode;

public class FreeCellBoard extends GameBoard<String> {
    private static final Function<List<Card>, Consumer<Card>> check = collector -> card -> {
        if (collector.isEmpty() || card.isHigherWithDifferentColor(collector.get(0))) {
            collector.add(0, card);
        }
    };
    private final Card[] freeCells;
    private final Card[] foundations;

    public FreeCellBoard(Columns columns, Path<String> path, Card[] freeCells, Card[] foundations) {
        super(columns, path);
        this.freeCells = freeCells;
        this.foundations = foundations;
    }

    protected FreeCellBoard(@Nonnull FreeCellBoard that) {
        this(new Columns(that.columns), new Path<>(that.path), cloneArray(that.freeCells), cloneArray(that.foundations));
    }

    /*****************************************************************************************************************
     * Find/Match Candidates
     ****************************************************************************************************************/
    protected List<Candidate> findCandidates() {
        return Stream.concat(findToColumnCandidates(), findColumnToFreeCellCandidates())
                .toList();
    }

    private Stream<Candidate> findToColumnCandidates() {
        return Stream.concat(findColumnToColumnCandidates(), findFreeCellToColumnCandidates())
                .flatMap(this::getTargetCandidates);
    }

    private Stream<Candidate> findColumnToFreeCellCandidates() {
        if (countFreeCells() > 0) {
            return range(0, columns.size())
                    .filter(i -> columns.get(i).isNotEmpty())
                    .filter(this::isNotInSequence)
                    .mapToObj(i -> buildCandidate(i, COLUMN, FREECELL, columns.get(i).peek()));
        }
        return Stream.empty();
    }

    private boolean isNotInSequence(int i) {
        var column = columns.get(i);
        var next = column.size() - 2;

        return column.size() <= 1 || !column.get(next).isHigherWithDifferentColor(column.peek());
    }

    protected boolean isFoundationable(Card card) {
        var foundationCard = foundations[suitCode(card)];

        return card.isAce() || (nonNull(foundationCard) && card.isHigherOfSameSuit(foundationCard));

    }

    protected Stream<Candidate> getTargetCandidates(Candidate candidate) {
        return range(0, columns.size())
                .filter(i -> isAppendableToColumn(i, candidate))
                .filter(i -> isMovable(candidate))
                .mapToObj(i -> Candidate.buildColumnCandidate(candidate, i));
    }

    protected boolean isAppendableToColumn(int i, Candidate candidate) {
        return Optional.of(columns.get(i))
                .filter(ObjectUtils::isNotEmpty)
                .map(Column::peek)
                .map(card -> card.isHigherWithDifferentColor(candidate.peek()))
                .orElse(true);
    }

    protected boolean isMovable(Candidate candidate) {
        return Optional.of(candidate)
                .map(Candidate::cards)
                .map(it -> it.size() <= maxCardsToMove())
                .orElse(false);
    }

    protected Stream<Candidate> findFreeCellToColumnCandidates() {
        return range(0, freeCells.length)
                .filter(i -> nonNull(freeCells[i]))
                .mapToObj(i -> buildCandidate(i, FREECELL, COLUMN, freeCells[i]));
    }

    protected Stream<Candidate> findColumnToColumnCandidates() {
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
                .map(it -> buildCandidate(col, COLUMN, COLUMN, it))
                .orElse(null);
    }

    protected List<Card> findCandidateAtColumn(Column column) {
        var ceiling = column.size() - 1;
        var collector = new LinkedList<Card>();
        var checkCandidateCard = check.apply(collector);

        rangeClosed(0, ceiling)
                .map(i -> ceiling - i)
                .mapToObj(column::get)
                .forEach(checkCandidateCard);
        return collector;
    }

    protected int getCardsInSequence(Column column) {
        if (column.isNotEmpty()) {
            return 1 + (int) range(1, column.size())
                    .filter(i -> column.get(i - 1).isHigherWithDifferentColor(column.get(i)))
                    .count();
        }
        return 0;
    }

    /*****************************************************************************************************************
     * Apply candidate
     ****************************************************************************************************************/
    protected FreeCellBoard updateBoard(Candidate candidate) {
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
            default -> throw new RuntimeException("Invalid candidate origin: " + candidate);
        }
        return candidate;
    }

    protected FreeCellBoard moveToTarget(Candidate candidate) {
        path.add(candidate.notation());
        switch (candidate.target()) {
            case COLUMN -> moveToColumn(candidate);
            case FREECELL -> toFreeCell(candidate.peek());
            case FOUNDATION -> toFoundation(candidate.peek());
            case DECKPILE -> throw new RuntimeException("Invalid candidate target: " + candidate);
        }
        return this;
    }

    private void toFoundation(Card card) {
        foundations[suitCode(card)] = card;
    }

    private void moveToColumn(Candidate candidate) {
        Optional.of(candidate)
                .map(Candidate::to)
                .map(columns::get)
                .ifPresent(it -> it.addAll(candidate.cards()));
    }

    private void toFreeCell(Card card) {
        range(0, freeCells.length)
                .filter(i -> isNull(freeCells[i]))
                .mapToObj(i -> Pair.of(i, card))
                .findFirst()
                .map(it -> freeCells[it.getLeft()] = it.getRight())
                .orElseThrow();
    }

    protected Card[] freeCells() {
        return freeCells;
    }

    protected Card[] foundations() {
        return foundations;
    }

    protected int countFreeCells() {
        return (int) stream(freeCells).filter(Objects::isNull).count();
    }

    protected int maxCardsToMove() {
        return countFreeCells() + countEmptyColumns() + 1;
    }

    protected FreeCellBoard checkFoundationCandidates() {
        checkFreeCellToFoundation();
        checkColumnToFoundation();
        return this;
    }

    protected void checkColumnToFoundation() {
        range(0, columns.size())
                .filter(i -> ObjectUtils.isNotEmpty(columns.get(i)))
                .filter(i -> isFoundationable(columns.get(i).peek()))
                .mapToObj(i -> buildCandidate(i, COLUMN, FOUNDATION, columns.get(i).peek()))
                .map(this::updateBoard)
                .forEach(it -> checkFoundationCandidates());
    }

    protected void checkFreeCellToFoundation() {
        range(0, freeCells.length)
                .filter(i -> nonNull(freeCells[i]))
                .filter(i -> isFoundationable(freeCells[i]))
                .mapToObj(i -> buildCandidate(i, FREECELL, FOUNDATION, freeCells[i]))
                .map(this::updateBoard)
                .forEach(it -> checkFoundationCandidates());
    }

    /*****************************************************************************************************************
     * HSD's heuristic: for each foundation pile, locate within the columns the next card that should be placed there,
     * and count the cards found on top of it. The sum of this count for each foundation is what the heuristic
     * returns. This number is multiplied by 2 if there are no available FreeCells or there are empty foundation piles.
     ****************************************************************************************************************/
    @Override
    public int score() {
        if (super.score() == 0) {
            score(calcHsdHeuristic());
        }
        return super.score();
    }

    private int calcHsdHeuristic() {
       return Optional.of(range(0, foundations.length).map(this::calcHsdHeuristic).sum())
               .map(it -> it * heuristicMultiplier())
               .orElse(0);
    }

    private int heuristicMultiplier() {
        return (countFreeCells() > 0 || countEmptyColumns() > 0) ? 1 : 2;
    }

    private int calcHsdHeuristic(int at) {
        return calcHeuristicByNextCard(nextCard(foundations[at], at));
    }

    private int calcHeuristicByNextCard(Card card) {
        return columns.stream()
                .filter(it -> it.contains(card))
                .map(it -> it.size() - it.indexOf(card) - 1)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Failed to find next card: " + card));
    }

    private Card nextCard(Card card, int suitCode) {
        return nonNull(card) && !card.isKing() ? CardHelper.nextCard(card) : card("A" + suit(suitCode));
    }

}
