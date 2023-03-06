package org.solitaire.spider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameBoard;
import org.solitaire.model.Path;
import org.solitaire.util.CandidateCompare;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.compare;
import static java.lang.Math.max;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.util.CardHelper.suitCode;

@Slf4j
public class SpiderBoard extends GameBoard<String> {
    private transient final Predicate<Integer> isNotEmpty = i -> ObjectUtils.isNotEmpty(columns().get(i));
    private transient final Predicate<Integer> isLongEnoughForRun = i -> 13 <= columns().get(i).size();
    private transient final Predicate<Integer> isThereARun = i -> isThereARun(columns().get(i));

    protected final Deck deck;

    public SpiderBoard(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(columns, path, totalScore);
        this.deck = deck;
    }

    public SpiderBoard(SpiderBoard that) {
        this(new Columns(that.columns()), new Path<>(that.path()), that.totalScore(), new Deck(that.deck()));
        score(that.score());
    }

    public Deck deck() {
        return deck;
    }

    @Override
    public boolean isCleared() {
        return super.isCleared() && deck.isEmpty();
    }

    /**************************************************************************************************************
     * Find/Match/Sort Candidates
     *************************************************************************************************************/
    protected List<Candidate> findCandidates() {
        return Optional.of(findOpenCandidates()
                        .flatMap(this::matchCandidateToTargets))
                .map(this::handleMultiples)
                .orElseThrow();
    }

    protected List<Candidate> handleMultiples(Stream<Candidate> candidates) {
        return candidates
                .filter(this::isMovable)
                .collect(Collectors.groupingBy(Candidate::peek))
                .values().stream()
                .map(this::selectCandidate)
                .sorted(this::compareCandidates)
                .toList();
    }

    protected boolean isMovable(Candidate candidate) {
        return Optional.of(candidate)
                .filter(this::isNotRepeatingCandidate)
                .map(it -> !(it.isKing() && isAtTop(it)))
                .orElse(false);
    }

    private boolean isAtTop(Candidate candidate) {
        return columns.get(candidate.from()).indexOf(candidate.peek()) == 0;
    }

    private boolean isNotRepeatingCandidate(Candidate candidate) {
        var prev = Optional.of(candidate)
                .map(Candidate::notation)
                .map(it -> it.charAt(1) + it.substring(0, 1) + it.substring(2))
                .orElseThrow();
        return path.stream().noneMatch(it -> it.equals(prev));
    }

    /**
     * This method filter out the candidates of the same source, but different targets
     *
     * @return the selected candidate
     */
    protected Candidate selectCandidate(List<Candidate> candidates) {
        return candidates.stream()
                .reduce(null, (a, b) -> (compareCandidates(a, b) <= 0) ? a : b);
    }

    /**
     * @return -1: a; 0: a == b; 1: b
     */
    protected int compareCandidates(Candidate a, Candidate b) {
        if (isNull(a)) {
            return 1;
        } else if (isNull(b)) {
            return -1;
        }
        return new CandidateCompare(a, b).compare(
                this::compareKings,
                this::compareTargetSuits,
                this::compareDistanceToRevealCard,
                this::compareCardChains);
    }

    protected int compareKings(Candidate a, Candidate b) {
        var cardA = a.peek();
        var cardB = b.peek();

        return cardA.isKing() ? -1 : cardB.isKing() ? 1 : 0;
    }

    /**
     * @return -1: OrderedCards at column a longer; 0: lengths are the same; 1: one at column b longer
     */
    protected int compareCardChains(Candidate a, Candidate b) {
        return compare(getChainLength(b), getChainLength(a));
    }

    private int getChainLength(Candidate candidate) {
        return candidate.cards().size() + targetChainLength(candidate);
    }

    private int targetChainLength(Candidate candidate) {
        return isMatchingTargetSuit(candidate)
                ? getOrderedCardsAtColumn(columns.get(candidate.to())).size()
                : 0;
    }

    /**
     * @return -1: same suit for a/target; 0: same suit for both a/target and b/target; 1: same suit for b/target
     */
    protected int compareTargetSuits(Candidate a, Candidate b) {
        var matchA = isMatchingTargetSuit(a);
        var matchB = isMatchingTargetSuit(b);

        if (matchA) {
            return matchB ? 0 : -1;
        }
        return matchB ? 1 : 0;
    }

    protected int compareDistanceToRevealCard(Candidate a, Candidate b) {
        var distA = getDistanceToFlipCard(a);
        var distB = getDistanceToFlipCard(b);

        return compare(distB, distA);
    }

    protected int getDistanceToFlipCard(Candidate candidate) {
        var column = columns.get(candidate.from());
        var dist = column.lastIndexOf(candidate.peek());

        return dist == 0 ? MAX_VALUE : dist - column.getOpenAt();
    }

    private boolean isMatchingTargetSuit(Candidate candidate) {
        return Optional.of(columns.get(candidate.to()))
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> candidate.peek().isSameSuit(it.peek()))
                .orElse(true);
    }

    protected Stream<Candidate> findOpenCandidates() {
        return range(0, columns.size())
                .mapToObj(this::findCandidateAtColumn)
                .filter(Objects::nonNull);
    }

    protected Candidate findCandidateAtColumn(int colAt) {
        return Optional.of(columns.get(colAt))
                .filter(ObjectUtils::isNotEmpty)
                .map(this::getOrderedCardsAtColumn)
                .map(it -> buildCandidate(colAt, COLUMN, it))
                .orElse(null);
    }

    protected List<Card> getOrderedCardsAtColumn(Column column) {
        var collector = new LinkedList<Card>();

        for (int i = column.size(), floor = max(column.getOpenAt(), 0); i-- > floor; ) {
            var card = column.get(i);

            if (collector.isEmpty() || card.isHigherOfSameColor(collector.get(0))) {
                collector.add(0, card);
            } else {
                break;
            }
        }
        return collector;
    }

    protected Stream<Candidate> matchCandidateToTargets(Candidate candidate) {
        return range(0, columns.size())
                .mapToObj(i -> findTargetColumn(i, candidate))
                .filter(Objects::nonNull);
    }

    protected Candidate findTargetColumn(int colAt, Candidate candidate) {
        var card = candidate.peek();

        return Optional.of(colAt)
                .filter(it -> it != candidate.from())
                .map(columns::get)
                .filter(it -> it.isEmpty() || it.peek().isHigherOrder(card))
                .map(it -> Candidate.buildColumnCandidate(candidate, colAt))
                .orElse(null);
    }

    /**************************************************************************************************************
     * Update State
     * ***********************************************************************************************************/
    protected SpiderBoard updateBoard(Candidate candidate) {
        return removeFromSource(candidate)
                .appendToTarget(candidate)
                .checkForRun(candidate);
    }

    protected SpiderBoard removeFromSource(Candidate candidate) {
        removeFromColumn(candidate);
        return this;
    }

    protected SpiderBoard appendToTarget(Candidate candidate) {
        path.add(candidate.notation());
        appendToTargetColumn(candidate);
        if (candidate.origin() != DECKPILE) {
            totalScore--;
        }
        return this;
    }

    protected SpiderBoard checkForRun(Candidate candidate) {
        Optional.of(candidate.to())
                .filter(isNotEmpty.and(isLongEnoughForRun).and(isThereARun))
                .ifPresent(this::removeTheRun);
        return this;
    }

    private boolean isThereARun(Column column) {
        assert nonNull(column) && 13 <= column.size();

        for (int i = column.size() - 1, floor = column.size() - 13; i > floor; i--) {
            var a = column.get(i);
            var b = column.get(i - 1);

            if (!b.isHigherOfSameColor(a)) {
                return false;
            }
        }
        return true;
    }

    private void removeTheRun(int i) {
        var column = columns().get(i);
        assert nonNull(column) && 13 <= column.size();

        var run = column.subList(column.size() - 13, column.size());
        var candidate = new Candidate(run, COLUMN, i, FOUNDATION, suitCode(run.get(0)));

        path().add(candidate.notation());
        System.out.printf("Run: %s\n", run);
        totalScore += 100;
        run.clear();
    }

    protected boolean drawDeck() {
        if (isNotEmpty(deck)) {
            assert columns().size() <= deck().size();

            var cards = deck().subList(0, columns.size());
            range(0, cards.size())
                    .mapToObj(i -> candidate(i, cards.get(i)))
                    .forEach(this::appendToTarget);
            cards.clear();
            return true;
        }
        return false;
    }

    protected Candidate candidate(int to, Card card) {
        return new Candidate(List.of(card), DECKPILE, to, COLUMN, to);
    }

    /*****************************************************************************************************************
     * Scoring board
     ****************************************************************************************************************/
    @Override
    public int score() {
        if (super.score() == 0) {
            super.score(calcSequences());
        }
        return super.score();
    }

    protected int calcSequences() {
        return columns.stream()
                .map(this::getOrderedCardsAtColumn)
                .mapToInt(List::size)
                .filter(i -> i > 1)
                .map(it -> it * it)
                .sum();
    }

}
