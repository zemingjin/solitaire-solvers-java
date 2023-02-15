package org.solitaire.tripeaks;

import org.solitaire.model.Card;

import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.checkDuplicates;

public class TriPeaksHelper {
    public static final int LAST_BOARD = 28;
    public static final int INI_COVERED = 18;
    public static final int LAST_DECK = 51;

    public static TriPeaks build(String[] cards) {
        assert nonNull(cards) && cards.length == 52 : "Invalid number of cards: " + cards.length;

        checkDuplicates(cards);
        return new TriPeaks(toCards(cards), toWastePile(cards));
    }

    protected static Stack<Card> toWastePile(String[] cards) {
        assert 0 < cards.length && cards.length <= 52;

        return Stream.of(buildCard(51, cards[cards.length - 1])).collect(Collectors.toCollection(Stack::new));
    }

    protected static Card[] toCards(String[] cards) {
        requireNonNull(cards);
        return range(0, min(cards.length, LAST_DECK))
                .mapToObj(i -> buildCard(i, cards[i]))
                .toArray(Card[]::new);
    }

    protected static boolean isFromDeck(Card card) {
        assert nonNull(card);

        return 28 <= card.at();
    }
}
