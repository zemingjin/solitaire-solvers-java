package org.solitaire.tripeaks;

import org.solitaire.model.Card;
import org.solitaire.model.GameSolver;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.solitaire.model.CardHelper.buildCard;

public class TriPeaksHelper {
    public static final int LAST_BOARD = 28;
    public static final int INI_COVERED = 18;
    public static final int LAST_DECK = 51;

    public static GameSolver build(String[] cards) {
        assert nonNull(cards) && cards.length == 52: "Invalid number of cards: " + cards.length;
        return TriPeaks.builder()
                .cards(toCards(cards))
                .wastePile(toWastePile(cards))
                .build();
    }

    protected static List<Card> toWastePile(String[] cards) {
        assert 0 < cards.length && cards.length <= 52;

        return Stream.of(buildCard(51, cards[cards.length - 1])).toList();
    }

    protected static Card[] toCards(String[] cards) {
        requireNonNull(cards);
        return IntStream.range(0, min(cards.length, LAST_DECK))
                .mapToObj(i -> buildCard(i, cards[i]))
                .toArray(Card[]::new);
    }

    public static boolean isFromDeck(Card card) {
        return 28 <= card.at();
    }
}
