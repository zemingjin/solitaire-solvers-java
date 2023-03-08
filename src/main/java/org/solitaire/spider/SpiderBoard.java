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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.DECKPILE;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.suitCode;

@Slf4j
public class SpiderBoard extends GameBoard<String> {
    protected final Deck deck;
    private transient final IntPredicate isNotEmpty = i -> !columns().get(i).isEmpty();
    private transient final IntPredicate isLongEnoughForRun = i -> 13 <= columns().get(i).size();
    private transient final IntPredicate isThereARun = i -> isThereARun(columns().get(i));
    private transient final BiPredicate<Card, Card> higherOfSameColor = Card::isHigherOfSameColor;
    private transient final BiPredicate<Card, Card> higherOfDifferentColor = Card::isHigherWithDifferentColor;

    public SpiderBoard(Columns columns, Path<String> path, int totalScore, Deck deck) {
        super(columns, path, totalScore);
        this.deck = deck;
    }

    public SpiderBoard(SpiderBoard that) {
        this(new Columns(that.columns()), new Path<>(that.path()), that.totalScore(), new Deck(that.deck()));
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
        return Optional.of(findCandidates(this::findCandidatesOfSameSuit))
                .filter(ObjectUtils::isNotEmpty)
                .orElseGet(() -> findCandidates(this::findCandidatesOfDifferentColors));
    }

    private List<Candidate> findCandidates(Function<Integer, List<Candidate>> findCandidate) {
        return range(0, columns().size())
                .filter(isNotEmpty)
                .boxed()
                .map(findCandidate)
                .flatMap(List::stream)
                .toList();
    }

    private List<Candidate> findCandidatesOfSameSuit(Integer i) {
        return Optional.of(columns().get(i))
                .filter(ObjectUtils::isNotEmpty)
                .map(this::getOrderedCardsAtColumn)
                .map(it -> findCandidates(i, it, higherOfSameColor))
                .orElseGet(Collections::emptyList);
    }

    private List<Candidate> findCandidates(int i, List<Card> cards, BiPredicate<Card, Card> tester) {
        return range(0, columns().size())
                .mapToObj(j -> getTargetCandidate(cards, i, j, tester))
                .filter(Objects::nonNull)
                .toList();
    }

    private Candidate getTargetCandidate(List<Card> cards, int i, int j, BiPredicate<Card, Card> tester) {
        var card = cards.get(0);

        return Optional.of(columns().get(j))
                .filter(it -> it.isEmpty() || tester.test(it.peek(), card))
                .map(it -> new Candidate(cards, COLUMN, i, COLUMN, j))
                .filter(this::isMovable)
                .orElse(null);
    }

    private List<Candidate> findCandidatesOfDifferentColors(Integer i) {
        return Optional.of(columns().get(i))
                .filter(ObjectUtils::isNotEmpty)
                .map(this::getOrderedCardsAtColumn)
                .filter(it -> it.size() == 1)
                .map(it -> findCandidates(i, it, higherOfDifferentColor))
                .orElseGet(Collections::emptyList);
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
        var prev = candidate.notation();
        return path.stream().noneMatch(it -> it.equals(prev));
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

    /**************************************************************************************************************
     * Update State
     * ***********************************************************************************************************/
    @Override
    public SpiderBoard updateBoard(Candidate candidate) {
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
        rangeClosed(candidate.to(), candidate.to())
                .filter(isNotEmpty.and(isLongEnoughForRun).and(isThereARun))
                .forEach(this::removeTheRun);
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
                    .forEach(i -> columns().get(i).add(cards.get(i)));
            path().add(new Candidate(new ArrayList<>(cards), DECKPILE, 0, COLUMN, 0).notation());
            cards.clear();
            return true;
        }
        return false;
    }

    /*****************************************************************************************************************
     * Scoring board
     ****************************************************************************************************************/
    @Override
    public int score() {
        if (super.score() == 0) {
            // The smaller, the better.
            var boardCards = columns().stream().mapToInt(Column::size).sum();
            var blockerCount = countBlockers();
            // The larger, the better.
            var sequences = calcSequences();
            super.score(sequences - boardCards - blockerCount);
        }
        return super.score();
    }

    private int countBlockers() {
        return range(0, columns().size())
                .map(this::countBlockers)
                .sum();
    }

    protected int countBlockers(int col) {
        var column = columns().get(col);

        if (column.isEmpty()) {
            return 0;
        }
        var cards = getOrderedCardsAtColumn(column);
        var card = cards.get(0);

        if (card.isNotKing()) {
            var least = new AtomicInteger(MAX_VALUE);
            var next = card(VALUES.charAt(card.rank()) + card.suit());

            range(0, columns().size())
                    .filter(i -> i != col)
                    .mapToObj(columns()::get)
                    .filter(ObjectUtils::isNotEmpty)
                    .filter(it -> it.contains(next))
                    .mapToInt(it -> it.size() - it.lastIndexOf(next) - 1)
                    .filter(i -> i < least.get())
                    .forEach(least::set);
            if (least.get() == MAX_VALUE) {
                least.set(deck.indexOf(next) / columns().size());
            }
            return least.get();
        }
        return 0;
    }

    // The bigger, the better
    protected int calcSequences() {
        return columns.stream()
                .map(this::getOrderedCardsAtColumn)
                .mapToInt(List::size)
                .filter(i -> i > 1)
                .map(it -> it * it)
                .sum();
    }

}
