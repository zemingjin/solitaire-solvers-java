package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;
import org.solitaire.util.BoardHelper;
import org.solitaire.util.CardHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Candidate.buildFoundationCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.FREECELL;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.suit;
import static org.solitaire.util.CardHelper.suitCode;

public class FreeCellBoard extends GameBoard<String> {
    protected static final Function<FreeCellBoard, List<Candidate>> findCandidates = FreeCellBoard::findCandidates;
    private final Card[] freeCells;
    private final Card[] foundations;
    protected final IntPredicate isNotFoundationable = i -> !isFoundationable(columns.get(i).peek());
    private final IntPredicate hasMultipleCards = i -> columns.get(i).size() > 1;
    private final IntPredicate isNotInSequence = i -> {
        var column = columns.get(i);
        return !column.get(column.size() - 2).isHigherWithDifferentColor(column.peek());
    };

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
        return Optional.of(findToColumnCandidates())
                .map(it -> concat(it.stream(), findColumnToFreeCellCandidates(it)))
                .map(it -> concat(it, getFoundationCandidates()))
                .map(it -> it.collect(Collectors.toList()))
                .map(this::cleanupCandidates)
                .orElseThrow();
    }

    private List<Candidate> findToColumnCandidates() {
        return concat(findColumnToColumnCandidates(), findFreeCellToColumnCandidates())
                .flatMap(this::getTargetCandidates)
                .toList();
    }

    protected Stream<Candidate> findColumnToFreeCellCandidates(List<Candidate> toColumns) {
        final IntPredicate isNotInToColumns = i -> toColumns.stream().allMatch(c -> c.from() != i);

        if (countFreeCells() > 0) {
            return range(0, columns.size())
                    .filter(hasMultipleCards.and(isNotInSequence).and(isNotFoundationable).and(isNotInToColumns))
                    .mapToObj(i -> buildCandidate(i, COLUMN, FREECELL, columns.get(i).peek()));
        }
        return Stream.empty();
    }

    protected boolean isFoundationable(Card card) {
        if (card.isAce()) {
            return true;
        }
        var foundationCard = foundations[suitCode(card)];

        return nonNull(foundationCard) && card.isHigherOfSameSuit(foundationCard);
    }

    protected Stream<Candidate> getTargetCandidates(Candidate candidate) {
        return range(0, columns.size())
                .filter(i -> isAppendableToColumn(i, candidate))
                .filter(i -> isMovable(candidate, i))
                .mapToObj(i -> Candidate.buildColumnCandidate(candidate, i));
    }

    protected boolean isAppendableToColumn(int i, Candidate candidate) {
        var column = columns.get(i);

        if (column.isEmpty()) {
            return isNotAtBottom(candidate);
        }
        return Optional.of(column)
                .map(Column::peek)
                .map(card -> card.isHigherWithDifferentColor(candidate.peek()))
                .orElse(true);
    }

    private boolean isNotAtBottom(Candidate candidate) {
        return columns.get(candidate.from()).size() > candidate.cards().size();
    }

    protected boolean isMovable(Candidate candidate, int to) {
        return Optional.of(candidate)
                .map(Candidate::cards)
                .map(it -> it.size() <= maxCardsToMove(to))
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
                .filter(it -> !isFoundationable(it.peek()))
                .map(this::findCandidateAtColumn)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> buildCandidate(col, COLUMN, it))
                .orElse(null);
    }

    protected List<Card> findCandidateAtColumn(Column column) {
        var ceiling = column.size() - 1;
        var collector = new LinkedList<Card>();

        for (int i = ceiling; i >= 0; i--) {
            var card = column.get(i);

            if (collector.isEmpty() || card.isHigherWithDifferentColor(collector.get(0))) {
                collector.add(0, card);
            } else {
                break;
            }
        }
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

    private List<Candidate> cleanupCandidates(List<Candidate> candidates) {
        for (int i = 0; i < candidates.size() - 1; i++) {
            var a = candidates.get(i);
            for (int j = i + 1; j < candidates.size(); j++) {
                var b = candidates.get(j);

                if (a.peek().equals(b.peek())) {
                    if (a.isToFoundation()) {
                        candidates.set(j, null);
                    } else if (b.isToFoundation()) {
                        candidates.set(i, null);
                    }
                }
            }
        }
        return candidates.stream().filter(Objects::nonNull).toList();
    }

    protected Stream<Candidate> getFoundationCandidates() {
        return concat(getFreeCellToFoundation(), getColumnToFoundation());
    }

    protected Stream<Candidate> getColumnToFoundation() {
        return range(0, columns.size())
                .filter(i -> ObjectUtils.isNotEmpty(columns.get(i)))
                .filter(i -> isFoundationable(columns.get(i).peek()))
                .mapToObj(i -> buildFoundationCandidate(columns.get(i).peek(), COLUMN, i));
    }

    protected Stream<Candidate> getFreeCellToFoundation() {
        return range(0, freeCells.length)
                .filter(i -> nonNull(freeCells[i]))
                .filter(i -> isFoundationable(freeCells[i]))
                .mapToObj(i -> buildFoundationCandidate(freeCells[i], FREECELL, i));
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

    protected int maxCardsToMove(int to) {
        return countFreeCells() + countEmptyColumns() + (columns().get(to).isEmpty() ? 0 : 1);
    }

    /*****************************************************************************************************************
     * HSD's heuristic: for each foundation pile, locate within the columns the next card that should be placed there,
     * and count the cards found on top of it. The sum of this count for each foundation is what the heuristic
     * returns. This number is multiplied by 2 if there are no available FreeCells or there are empty foundation piles.
     ****************************************************************************************************************/
    @Override
    public int score() {
        if (super.score() == 0) {
            var hsdHeuristic = calcHsdHeuristic();
            var foundationScore = Stream.of(foundations).mapToInt(CardHelper::rank).sum();

            score(hsdHeuristic + foundationScore);
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
        var foundationCard = foundations[at];

        if (nonNull(foundationCard) && foundationCard.isKing()) {
            return 0;
        }
        return calcHeuristicByNextCard(nextCard(foundationCard, at));
    }

    private int calcHeuristicByNextCard(Card card) {
        if (Arrays.asList(freeCells).contains(card)) {
            return 0;
        }
        return columns.stream()
                .filter(ObjectUtils::isNotEmpty)
                .filter(it -> it.contains(card))
                .map(it -> -(it.size() - it.indexOf(card) - 1))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Failed to find next card: " + card));
    }

    private Card nextCard(Card card, int suitCode) {
        return nonNull(card) ? CardHelper.nextCard(card) : card("A" + suit(suitCode));
    }

    @Override
    public List<String> verify() {
        return BoardHelper.verifyBoard(columns());
    }
}
