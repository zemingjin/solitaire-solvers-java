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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
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
    protected Deck deck;
    private int runs = 0;
    private boolean singleSuit;

    public SpiderBoard(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(columns, path, totalScore);
        this.deck = deck;
        isInSequence(Card::isHigherOfSameSuit);
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
        return Optional.of(findBoardCandidates())
                .filter(BoardHelper.isNotEmpty)
                .orElseGet(this::drawDeck);
    }

    private List<Candidate> findBoardCandidates() {
        return Optional.of(findCandidatesBySuit())
                .map(it -> singleSuit() ? it : concat(it.stream(), findCandidatesByRanks(it)).toList())
                .filter(BoardHelper.isNotEmpty)
                .orElseGet(Collections::emptyList);
    }

    protected List<Candidate> findCandidatesBySuit() {
        return Optional.of(findColumnToColumnCandidates().toList())
                .map(this::optimizeCandidatesBySuit)
                .orElseGet(Collections::emptyList);
    }

    protected List<Candidate> optimizeCandidatesBySuit(List<Candidate> candidates) {
        return candidates.stream()
                .map(candidate -> checkForLongerTargets(candidate, candidates))
                .filter(isNotNull)
                .toList();
    }

    private Candidate checkForLongerTargets(Candidate candidate, List<Candidate> candidates) {
        return candidates.stream()
                .filter(candidate::isSameOrigin)
                .filter(it -> hasLongerTargetSequence(it, candidate))
                .findFirst()
                .orElse(null);
    }

    private boolean hasLongerTargetSequence(Candidate a, Candidate b) {
        return a.cards().length + getOrderedCards(column(a.to())).length >=
                b.cards().length + getOrderedCards(column(b.to())).length;
    }

    @Override
    protected Candidate candidateToEmptyColumn(Card[] cards, int from, int to) {
        return columnToColumn(cards, from, to);
    }

    protected Stream<Candidate> findCandidatesByRanks(List<Candidate> candidates) {
        return range(0, columns().size())
                .filter(isNotEmpty)
                .mapToObj(this::findCandidatesByRanks)
                .flatMap(flattenStream)
                .filter(it -> notFromSameOrigin(it, candidates))
                .filter(this::isMovable);
    }

    private boolean notFromSameOrigin(Candidate candidate, List<Candidate> candidates) {
        return candidates.stream()
                .noneMatch(it -> it.isSameOrigin(candidate) && it.cards().length >= candidate.cards().length);
    }

    private Stream<Candidate> findCandidatesByRanks(int to) {
        return range(0, columns().size())
                .filter(isNotEmpty)
                .mapToObj(from -> findCandidatesByRanks(to, from))
                .filter(isNotNull);
    }

    private Candidate findCandidatesByRanks(int to, int from) {
        var target = peek(to);
        var cards = getOrderedCards(column(from));

        if (isNull(target) || target.isHigherRank(cards[0])) {
            return columnToColumn(cards, from, to);
        }
        return null;
    }

    protected boolean isMovable(Candidate candidate) {
        return Optional.of(candidate)
                .filter(this::isNotFromSequenceOfSameSuit)
                .filter(this::isNotRepeatingCandidate)
                .filter(this::isMovableToEmptyColumn)
                .filter(this::isFromPartialColumn)
                .isPresent();
    }

    protected boolean isFromPartialColumn(Candidate candidate) {
        return column(candidate.from()).size() > candidate.cards().length;
    }

    protected boolean isNotFromSequenceOfSameSuit(Candidate candidate) {
        var column = column(candidate.from());
        var cards = candidate.cards();
        return column.size() > cards.length
                && !column.get(column.size() - cards.length - 1).isHigherOfSameSuit(candidate.peek());
    }

    protected List<Candidate> drawDeck() {
        if (ableToDealCards()) {
            var cards = deck().subList(0, columns.size());

            return List.of(candidate(cards, DECKPILE, 0, COLUMN, 0));
        }
        if (emptyColumns() && !deck().isEmpty()) {
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
            case COLUMN -> {
                if (candidate.isFromDeck()) {
                    range(0, columns().size()).forEach(i -> column(i).add(candidate.cards()[i]));
                } else {
                    addToTargetColumn(candidate);
                    totalScore(totalScore() - 1);
                }
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
        var score = getOrderedCards(column).length;

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
        return !emptyColumns();
    }

    private boolean emptyColumns() {
        return columns().stream().anyMatch(Column::isEmpty);
    }

    private void cloneDeck() {
        deck = new Deck(deck);
    }

    private boolean ableToDealCards() {
        return noEmptyColumns() && isNotEmpty(deck);
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

}
