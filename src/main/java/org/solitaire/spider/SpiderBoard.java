package org.solitaire.spider;

import lombok.extern.slf4j.Slf4j;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;
import org.solitaire.util.BoardHelper;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Integer.compare;
import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.candidate;
import static org.solitaire.model.Candidate.columnToColumn;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.SolveExecutor.isPrint;
import static org.solitaire.util.BoardHelper.isNotNull;
import static org.solitaire.util.BoardHelper.isSingleSuit;
import static org.solitaire.util.BoardHelper.verifyBoard;
import static org.solitaire.util.CardHelper.suitCode;

@Slf4j
public class SpiderBoard extends GameBoard {
    private transient int candLimit = 2;
    protected Deck deck;
    private int runs = 0;
    private boolean singleSuit;

    public SpiderBoard(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(columns, path, totalScore);
        this.deck = deck;
        isInSequence(Card::isHigherRank);
    }

    public SpiderBoard(SpiderBoard that) {
        this(new Columns(that.columns()), new Path<>(that.path()), that.totalScore(), that.deck());
        runs(that.runs());
        isInSequence(that.isInSequence());
        singleSuit(that.singleSuit);
    }

    /**************************************************************************************************************
     * Find/Match/Sort Candidates
     *************************************************************************************************************/
    @Override
    public List<Candidate> findCandidates() {
        return Optional.of(findColumnToColumnCandidates())
                .map(this::optimizedCandidates)
                .filter(listIsNotEmpty)
                .orElseGet(this::drawDeck);
    }

    @Override
    public Candidate toColumnCandidate(int from, int to, Card card) {
        if (singleSuit()) {
            return super.toColumnCandidate(from, to, card);
        }
        return Stream.of(card)
                .peek(it -> isInSequence(Card::isHigherOfSameSuit))
                .map(it -> super.toColumnCandidate(from, to, it))
                .filter(isNotNull)
                .findFirst()
                .orElse(null);
    }
    @Override
    public Candidate toColumnCandidate(Card[] cards, int from, int to, Card card) {
        if (singleSuit()) {
            return super.toColumnCandidate(cards, from, to, card);
        }
        return Stream.of(card)
                .peek(it -> isInSequence(Card::isHigherRank))
                .map(it -> super.toColumnCandidate(cards, from, to, it))
                .filter(isNotNull)
                .findFirst()
                .orElse(null);
    }

    protected List<Candidate> optimizedCandidates(Stream<Candidate> candidates) {
        candLimit((isNotEmpty(deck) && emptyColumns() > 0) ? max(candLimit(), emptyColumns()) : 1);
        return candidates
                .collect(groupingBy(Candidate::originNotation))
                .values().stream()
                .flatMap(this::reduceCandidates)
                .toList();
    }

    protected Stream<Candidate> reduceCandidates(List<Candidate> list) {
        return list.size() > candLimit()
                ? list.stream().sorted((a, b) -> compare(targetLen(b), targetLen(a))).limit(candLimit())
                : list.stream();
    }

    @Override
    protected int targetLen(Candidate candidate) {
        var column = column(candidate.to());

        if (column.isNotEmpty() && column.peek().isHigherOfSameSuit(candidate.peek())) {
            return super.targetLen(candidate);
        }
        return candidate.cards().length;
    }

    @Override
    protected Candidate candidateToEmptyColumn(Card[] cards, int from, int to) {
        return columnToColumn(cards, from, to);
    }

    protected List<Candidate> drawDeck() {
        if (isNotEmpty(deck())) {
            if (noEmptyColumns()) {
                return Optional.of(deck().subList(0, columns.size()))
                        .map(it -> candidate(it, DECKPILE, 0, DECKPILE, 0))
                        .map(List::of)
                        .orElseThrow();
            }
            throw new RuntimeException("Can't deal a new row with empty columns or deck!");
        }
        return emptyList();
    }

    /**************************************************************************************************************
     * Update Board
     *************************************************************************************************************/
    @Override
    public SpiderBoard updateBoard(Candidate candidate) {
        return removeFromSource(candidate)
                .appendToTarget(candidate)
                .checkForRun(candidate.to());
    }

    protected SpiderBoard removeFromSource(Candidate candidate) {
        switch (candidate.origin()) {
            case COLUMN -> removeFromColumn(candidate);
            case DECKPILE -> removeFromDeck(candidate);
        }
        return this;
    }

    private void removeFromDeck(Candidate candidate) {
        cloneDeck();
        Stream.of(candidate.cards()).forEach(it -> deck.remove(it));
    }

    protected SpiderBoard appendToTarget(Candidate candidate) {
        path().add(candidate.notation());
        switch (candidate.target()) {
            case DECKPILE -> range(0, columns().size()).forEach(i -> column(i).add(candidate.cards()[i]));
            case COLUMN -> {
                    addToTargetColumn(candidate);
                    totalScore(totalScore() - 1);
            }
            case FOUNDATION -> {
                totalScore(totalScore() + 100);
                runs(runs() + 1);
                if (isPrint()) {
                    System.out.println(path().peek());
                }
            }
        }
        return this;
    }

    protected SpiderBoard checkForRun(int colAt) {
        IntStream.of(colAt)
                .filter(i -> isThereARun(column(i)))
                .mapToObj(this::getCandidateForTheRun)
                .findFirst()
                .ifPresent(it -> removeFromSource(it).appendToTarget(it));
        return this;
    }

    protected boolean isThereARun(Column column) {
        if (nonNull(column) && 13 <= column.size()) {
            for (int i = column.size() - 1, floor = column.size() - 13; i > floor; i--) {
                if (!column.get(i - 1).isHigherOfSameColor(column.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Candidate getCandidateForTheRun(int colAt) {
        return Optional.of(colAt)
                .map(this::column)
                .map(it -> it.subList(it.size() - 13, it.size()))
                .map(it -> candidate(it, COLUMN, colAt, FOUNDATION, suitCode(it.get(0))))
                .orElseThrow();
    }

    /*****************************************************************************************************************
     * Scoring board
     ****************************************************************************************************************/
    @Override
    public int score() {
        if (isNotScored()) {
            // The smaller, the better.
            var coveredCards = columns().stream().filter(BoardHelper.isNotEmpty).mapToInt(Column::openAt).sum();
            // The larger, the better.
            var sequenceScore = calcSequences();
            var runScore = runs() * 39;

            super.score(sequenceScore - coveredCards + runScore);
        }
        return super.score();
    }

    // The bigger, the better
    protected int calcSequences() {
       return range(0, columns().size())
                .filter(isNotEmpty)
                .map(this::calcSequenceScore)
                .sum();
    }

    private int calcSequenceScore(int i) {
        var column = column(i);
        var score = getOrderedCards(i).length;

        return column.size() == score && column.get(0).isKing() ? score * 2 : score;
    }

    /***********************************************************************************************************
     * Helpers
     **********************************************************************************************************/
    @Override
    public List<String> verify() {
        singleSuit(isSingleSuit(columns(), deck()));
        return verifyBoard(columns(), deck());
    }

    protected boolean noEmptyColumns() {
        return emptyColumns() == 0;
    }

    protected int emptyColumns() {
        return (int) columns().stream().filter(Column::isEmpty).count();
    }

    private void cloneDeck() {
        deck = new Deck(deck);
    }

    /***********************************************************************************************************
     * Accessors
     **********************************************************************************************************/
    public Deck deck() {
        return deck;
    }

    @Override
    public boolean isSolved() {
        return super.isSolved() && deck.isEmpty();
    }

    public int runs() {
        return runs;
    }

    public void runs(int runs) {
        this.runs = runs;
    }

    public boolean singleSuit() {
        return singleSuit;
    }

    public void singleSuit(boolean singleSuit) {
        this.singleSuit = singleSuit;
    }

    protected void candLimit(int candLimit) {
        this.candLimit = candLimit;
    }

    public int candLimit() {
        return candLimit;
    }

}
