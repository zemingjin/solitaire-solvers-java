package org.solitaire.freecell;

import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.execution.GameBoard;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Path;
import org.solitaire.util.BoardHelper;
import org.solitaire.util.CardHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.solitaire.model.Candidate.candidate;
import static org.solitaire.model.Candidate.toFoundationCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.FREECELL;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.isNull;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.rank;
import static org.solitaire.util.CardHelper.suit;
import static org.solitaire.util.CardHelper.suitCode;

public class FreeCellBoard extends GameBoard {
    protected static final Function<FreeCellBoard, List<Candidate>> findCandidates = FreeCellBoard::findCandidates;
    protected final Card[] freeCells;
    protected final Card[] foundations;
    protected transient final IntPredicate isNotFoundationable = i -> !isFoundationable(peek(i));
    private transient final IntPredicate hasMultipleCards = i -> column(i).size() > 1;

    public FreeCellBoard(Columns columns, Path<String> path, Card[] freeCells, Card[] foundations) {
        super(columns, path);
        this.freeCells = freeCells;
        this.foundations = foundations;
        isInSequence(Card::isHigherWithDifferentColor);
    }

    protected FreeCellBoard(@Nonnull FreeCellBoard that) {
        this(new Columns(that.columns), new Path<>(that.path), cloneArray(that.freeCells), cloneArray(that.foundations));
    }

    /*****************************************************************************************************************
     * Find/Match Candidates
     ****************************************************************************************************************/
    @Override
    public List<Candidate> findCandidates() {
        var candidates = Stream.of(
                        findColumnToFoundationCandidates(),
                        findFreeCellToFoundationCandidates(),
                        findColumnToColumnCandidates(),
                        findFreeCellToColumnCandidates(),
                        findColumnToFreeCellCandidates()
                )
                .flatMap(flattenStream)
                .toList();
        return filterCandidates(candidates);
    }

    private List<Candidate> filterCandidates(List<Candidate> candidates) {
        return range(0, candidates.size())
                .mapToObj(i -> filterCandidate(i, candidates))
                .filter(isNotNull)
                .toList();
    }

    /**
     * For candidates from same origin, toFreeCell or not toFoundation would be filtered out.
     */
    private Candidate filterCandidate(int i, List<Candidate> candidates) {
        var candidate = candidates.get(i);

        return test(candidate, candidates, Candidate::isToFreeCell) ||
                test(candidate, candidates, Candidate::isNotToFoundation) ? null : candidate;
    }

    private boolean test(Candidate candidate, List<Candidate> candidates, Predicate<Candidate> toTarget) {
        return toTarget.test(candidate)
                && candidates.stream().anyMatch(it -> it.isSameOrigin(candidate) && !toTarget.test(it));
    }

    @Override
    protected Candidate candidateToEmptyColumn(Card[] cards, int from, int to) {
        if (column(from).size() > cards.length && maxCardsToMove(to) >= cards.length) {
            return new Candidate(cards, COLUMN, from, COLUMN, to);
        }
        return null;
    }

    @Override
    public boolean isMovable(Card[] cards, int from, int to) {
        return cards.length <= maxCardsToMove(to);
    }

    protected Stream<Candidate> findFreeCellToColumnCandidates() {
        return range(0, freeCells.length)
                .filter(i -> nonNull(freeCells[i]))
                .mapToObj(this::freeCellToColumnCandidates)
                .flatMap(flattenStream);
    }

    private Stream<Candidate> freeCellToColumnCandidates(int from) {
        return range(0, columns().size())
                .mapToObj(to -> freeCellToColumnCandidate(from, to))
                .filter(isNotNull);
    }

    private Candidate freeCellToColumnCandidate(int from, int to) {
        var column = column(to);

        if (column.isEmpty() || column.peek().isHigherWithDifferentColor(freeCells[from])) {
            return candidate(freeCells[from], FREECELL, from, COLUMN, to);
        }
        return null;
    }

    protected Stream<Candidate> findFreeCellToFoundationCandidates() {
        return range(0, freeCells.length)
                .filter(i -> nonNull(freeCells[i]))
                .filter(i -> isFoundationable(freeCells[i]))
                .mapToObj(from -> toFoundationCandidate(freeCells[from], FREECELL, from));
    }

    protected Stream<Candidate> findColumnToFoundationCandidates() {
        return range(0, columns().size())
                .mapToObj(this::columnToFoundationCandidate)
                .filter(isNotNull);
    }

    private Candidate columnToFoundationCandidate(int from) {
        return Optional.of(column(from))
                .filter(BoardHelper.isNotEmpty)
                .map(Column::peek)
                .filter(this::isFoundationable)
                .map(it -> toFoundationCandidate(it, COLUMN, from))
                .orElse(null);
    }

    protected Stream<Candidate> findColumnToFreeCellCandidates() {
        var freeCellAt = freeCellAt();

        if (freeCellAt >= 0) {
            return range(0, columns.size())
                    .filter(hasMultipleCards.and(this::isNotInSequence).and(isNotFoundationable))
                    .mapToObj(i -> candidate(peek(i), COLUMN, i, FREECELL, freeCellAt()));
        }
        return Stream.empty();
    }

    private boolean isNotInSequence(int i) {
        var column = column(i);
        return !column.get(column.size() - 2).isHigherWithDifferentColor(column.peek());
    }

    private int freeCellAt() {
        return range(0, freeCells.length).filter(i -> isNull(freeCells[i])).findFirst().orElse(-1);
    }

    protected boolean isFoundationable(Card card) {
        return card.isAce() || Optional.ofNullable(foundations[suitCode(card)])
                .filter(isNotNull)
                .filter(card::isHigherOfSameSuit)
                .isPresent();
    }

    /*****************************************************************************************************************
     * Apply candidate
     ****************************************************************************************************************/
    @Override
    public FreeCellBoard updateBoard(Candidate candidate) {
        return Optional.of(candidate)
                .map(this::removeFromOrigin)
                .map(this::moveToTarget)
                .orElse(null);
    }

    protected Candidate removeFromOrigin(Candidate candidate) {
        switch (candidate.origin()) {
            case COLUMN -> removeFromColumn(candidate);
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
            case DECKPILE -> throw new RuntimeException("Invalid candidate target: " + candidate.notation());
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
                .ifPresent(it -> it.addAll(List.of(candidate.cards())));
    }

    private void toFreeCell(Card card) {
        range(0, freeCells.length)
                .filter(i -> isNull(freeCells[i]))
                .mapToObj(i -> Pair.of(i, card))
                .findFirst()
                .map(it -> freeCells[it.getLeft()] = it.getRight())
                .orElseThrow();
    }

    protected int countfreeCells() {
        return (int) stream(freeCells).filter(isNull).count();
    }

    protected int maxCardsToMove(int to) {
        return countfreeCells() + countEmptyColumns() + (column(to).isEmpty() ? 0 : 1);
    }

    /*****************************************************************************************************************
     * HSD's heuristic: for each foundation pile, locate within the columns the next card that should be placed there,
     * and count the cards found on top of it. The sum of this count for each foundation is what the heuristic
     * returns. This number is multiplied by 2 if there are no available FreeCells or there are empty foundation piles.
     ****************************************************************************************************************/
    @Override
    public int score() {
        if (isNotScored()) {
            score(calcFoundationScore() * 2 - calcBlockerScore() + calcColumnScore());
        }
        return super.score();
    }

    private int calcFoundationScore() {
        return Stream.of(foundations).mapToInt(CardHelper::rank).sum();
    }

    protected int calcBlockerScore() {
        return range(0, foundations.length)
                .mapToObj(this::nextFoundationCard)
                .filter(isNotNull)
                .mapToInt(this::calcBlockers)
                .sum();
    }

    private Card nextFoundationCard(int i) {
        return Optional.of(rank(foundations[i]) + 1)
                .filter(rank -> rank <= 13)
                .map(rank -> card(VALUES.charAt(rank - 1) + suit(i).toLowerCase()))
                .orElse(null);
    }

    protected int calcBlockers(Card card) {
        if (Arrays.asList(freeCells).contains(card)) {
            return 0;
        }
        return columns.stream()
                .filter(BoardHelper.isNotEmpty)
                .filter(it -> it.contains(card))
                .map(it -> it.size() - it.indexOf(card) - 1)
                .map(i -> (noAvailablefreeCells() && noEmptyColumns() && emptyFoundations()) ? i * 2 : i)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Failed to find next card: " + card));
    }

    protected int calcColumnScore() {
        return columns().stream()
                .mapToInt(this::calcColumnScore)
                .sum();
    }

    private int calcColumnScore(Column column) {
        return Optional.of(column)
                .filter(BoardHelper.isNotEmpty)
                .filter(this::isOrderedColumn)
                .map(it -> it.get(0).isKing() ? it.size() * 2 : it.size())
                .orElse(0);
    }

    protected boolean isOrderedColumn(Column column) {
        return range(0, column.size() - 1)
                .allMatch(i -> column.get(i).isHigherWithDifferentColor(column.get(i + 1)));
    }

    @Override
    public List<String> verify() {
        return verifyBoard(columns());
    }

    protected boolean emptyFoundations() {
        return Stream.of(foundations).anyMatch(Objects::isNull);
    }

    protected boolean noAvailablefreeCells() {
        return Stream.of(freeCells).allMatch(Objects::nonNull);
    }

    protected boolean noEmptyColumns() {
        return columns().stream().allMatch(BoardHelper.isNotEmpty);
    }
}
