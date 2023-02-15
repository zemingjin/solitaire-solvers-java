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
import static org.solitaire.util.CardHelper.cloneArray;
import static org.solitaire.util.CardHelper.rank;
import static org.solitaire.util.CardHelper.suitCode;

public class FreeCellBoard extends GameBoard<Candidate> {
    private static final Function<List<Card>, Consumer<Card>> check = collector -> card -> {
        if (collector.isEmpty() || card.isHigherWithDifferentColor(collector.get(0))) {
            collector.add(0, card);
        }
    };
    private final Card[] freeCells;
    private final Card[] foundations;

    public FreeCellBoard(Columns columns, Path<Candidate> path, Card[] freeCells, Card[] foundations) {
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
        if (isFreeCellAvailable()) {
            return range(0, columns.size())
                    .filter(i -> columns.get(i).isNotEmpty())
                    .mapToObj(i -> buildCandidate(i, COLUMN, FREECELL, columns.get(i).peek()));
        }
        return Stream.empty();
    }

    private boolean isFreeCellAvailable() {
        return Stream.of(freeCells).anyMatch(Objects::isNull);
    }

    protected boolean isFoundationable(Card card) {
        var foundationCard = foundations[suitCode(card)];

        return card.isAce() ||
                (nonNull(foundationCard) && card.isHigherOfSameSuit(foundationCard));

    }

    protected Stream<Candidate> getTargetCandidates(Candidate candidate) {
        return range(0, columns.size())
                .mapToObj(i -> Pair.of(i, candidate))
                .filter(this::isAppendableToColumn)
                .filter(this::isMovable)
                .map(it -> Candidate.buildColumnCandidate(it.getRight(), it.getLeft()));
    }

    private boolean isAppendableToColumn(Pair<Integer, Candidate> pair) {
        return Optional.of(pair.getLeft())
                .map(columns::get)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> Optional.of(it.peek())
                        .filter(card -> card.isHigherWithDifferentColor(pair.getRight().peek()))
                        .isPresent())
                .orElse(true);
    }

    protected boolean isMovable(Pair<Integer, Candidate> pair) {
        return Optional.of(pair)
                .map(Pair::getRight)
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
        }
        return candidate;
    }

    protected FreeCellBoard moveToTarget(Candidate candidate) {
        path.add(candidate);
        switch (candidate.target()) {
            case COLUMN -> moveToColumn(candidate);
            case FREECELL -> toFreeCell(candidate.peek());
            case FOUNDATION -> toFoundation(candidate.peek());
            default -> throw new RuntimeException("Invalid candidate target: " + candidate);
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

    protected int maxCardsToMove() {
        var emptyColumns = columns.stream().filter(ObjectUtils::isEmpty).count();
        var emptyFreeCells = stream(freeCells).filter(Objects::isNull).count();

        return (int) ((emptyFreeCells + 1) * (emptyColumns + 1));
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
     * Score the board
     ****************************************************************************************************************/
    @Override
    public double score() {
        if (super.score() == 0) {
            var foundationScore = Stream.of(foundations).mapToInt(CardHelper::rank).sum();
            var freeCellCount = Stream.of(freeCells).filter(Objects::isNull).count();
            var freeColumnCount = columns.stream().filter(List::isEmpty).count();
            var inSequenceScore = calcInSequenceScore();
            var blockingScore = calcBlockingScore();

            score(foundationScore * 39 +
                    freeCellCount * 26 +
                    freeColumnCount * 13 +
                    inSequenceScore * 8 +
                    blockingScore * 5);
        }
        return super.score();
    }

    protected double calcBlockingScore() {
        var blockingScore = 0.0;
        int lowestHomeRank = getLowestFoundationRank();

        for (var column : columns) {
            for (int j = column.size() - 1; j >= 0; j--) {
                var card = column.get(j);
                var cardRank = card.rank();
                // degree of concern
                double concern = (2 * cardRank - lowestHomeRank - rank(foundations[suitCode(card)])) / 2.;

                // 1 is the highest concern, larger number is lower concern
                // cards blocking it
                double blockings = 0;
                for (int k = j + 1; k < column.size(); k++) {
                    if (column.get(k).rank() >= cardRank) {
                        blockings += 1;
                    }
                }

                // give priority for cards that can be moved to home cell
                if (concern == 1 && blockings <= 2) {
                    blockingScore += (9 - Math.pow((blockings + 1), 2)) * 4;
                }

                concern = Math.pow(concern, 1.8);

                blockings -= concern - 1;

                if (blockings >= 0) {
                    // if too much blocking, let's worry about it less (giving up for now)
                    blockingScore -= Math.pow(blockings, .85) * 13 / concern;
                }
            }
        }
        return blockingScore;
    }

    protected int getLowestFoundationRank() {
        return Stream.of(foundations).mapToInt(CardHelper::rank).min().orElseThrow();
    }

    protected int calcInSequenceScore() {
        var inSequenceScore = 0;

        for (Column column : columns) {
            if (isGoodColumn(column)) {
                inSequenceScore += column.size() * 2;
            } else if (column.isNotEmpty()) {
                inSequenceScore += getCardsInSequence(column);
            }
        }
        return inSequenceScore;
    }

    protected boolean isGoodColumn(Column column) {
        return column.size() >= 4 && column.get(0).isKing() && getCardsInSequence(column) == column.size();
    }
}
