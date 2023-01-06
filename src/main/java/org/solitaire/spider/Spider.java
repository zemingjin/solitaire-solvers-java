package org.solitaire.spider;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Column;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameSolver;
import org.solitaire.model.GameState;
import org.solitaire.model.Path;
import org.solitaire.util.CardHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.util.SolitaireHelper.incTotal;

@SuppressWarnings("rawtypes")
@Getter
@EqualsAndHashCode(callSuper = true)
public class Spider extends GameState implements GameSolver {
    private final Deck deck;

    @Builder
    public Spider(Columns columns, Deck deck, Path path, int totalScore) {
        super(columns, path, totalScore);
        this.deck = deck;
    }

    @Override
    public List<List> solve() {
        if (isCleared()) {
            return singletonList(path);
        }
        incTotal();
        return Optional.of(findTargets())
                .filter(ObjectUtils::isNotEmpty)
                .map(this::optimizeCandidates)
                .map(this::updateColumns)
                .orElseGet(this::drawDeck);
    }

    @Override
    public boolean isCleared() {
        return super.isCleared() && deck.isEmpty();
    }

    protected List<Candidate> optimizeCandidates(List<Candidate> candidates) {
        return Optional.of(candidates)
                .orElseThrow();
    }

    protected List<List> drawDeck() {
        if (isNotEmpty(deck)) {
            return drawDeckCards()
                    .solve();
        }
        return singletonList(emptyList());
    }

    protected Spider drawDeckCards() {
        assert columns.size() <= deck.size();

        columns.forEach(column -> column.add(deck.remove(0)));
        return this;
    }

    protected List<List> updateColumns(List<Candidate> candidates) {
        return candidates.stream()
                .map(this::updateTargetColumn)
                .map(Spider::solve)
                .flatMap(List::stream)
                .toList();
    }

    protected Spider updateTargetColumn(Candidate candidate) {
        return SpiderHelper.clone(this)
                .removeFromSource(candidate)
                .appendToTarget(candidate)
                .checkForRuns();
    }

    protected Spider removeFromSource(Candidate candidate) {
        removeFromColumn(candidate);
        return this;
    }

    protected Spider appendToTarget(Candidate candidate) {
        var cards = candidate.getCards();

        path.add(CardHelper.stringOfRaws(cards.toArray(Card[]::new)));
        appendToTargetColumn(candidate);
        totalScore--;
        return this;
    }

    protected Spider checkForRuns() {
        columns.stream()
                .filter(this::isThereARun)
                .forEach(this::removeTheRun);
        return this;
    }

    private boolean isThereARun(Column column) {
        assert nonNull(column);

        if (column.size() >= 13) {
            for (int i = column.size() - 1, floor = column.size() - 13; i > floor; i--) {
                var a = column.get(i);
                var b = column.get(i - 1);

                if (!b.isHigherOfSameColor(a)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void removeTheRun(Column column) {
        assert nonNull(column) && column.size() >= 13;

        var collector = new ArrayList<Card>(13);

        for (int i = 0; i < 13; i++) {
            Optional.of(column)
                    .map(Column::pop)
                    .ifPresent(collector::add);
        }
        getPath().add(Arrays.toString(collector.stream().map(Card::raw).toArray(String[]::new)));
        System.out.printf("Run: %s\n", collector);
        totalScore += 100;
    }

    protected List<Candidate> findOpenCandidates() {
        return IntStream.range(0, columns.size())
                .filter(i -> columns.get(i).isNotEmpty())
                .mapToObj(this::findCandidateAtColumn)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    protected Candidate findCandidateAtColumn(int colAt) {
        return Optional.of(columns.get(colAt))
                .map(this::getOrderedCards)
                .map(it -> buildCandidate(colAt, COLUMN, it))
                .orElse(null);
    }

    private List<Card> getOrderedCards(Column column) {
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

    protected List<Candidate> findTargets() {
        return findOpenCandidates().stream()
                .flatMap(this::findTargetsByCandidate)
                .toList();
    }

    protected Stream<Candidate> findTargetsByCandidate(Candidate candidate) {
        return IntStream.range(0, columns.size())
                .mapToObj(i -> findTargetColumn(i, candidate))
                .filter(Objects::nonNull);
    }

    protected Candidate findTargetColumn(int colAt, Candidate candidate) {
        var card = candidate.getCards().get(0);
        return Optional.of(colAt)
                .filter(it -> it != candidate.getFrom())
                .map(columns::get)
                .filter(it -> it.isEmpty() || it.peek().isHigherOfSameColor(card))
                .map(it -> new Candidate(candidate).setTarget(colAt))
                .orElse(null);
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        return Pair.of(0, Collections.emptyList());
    }
}
