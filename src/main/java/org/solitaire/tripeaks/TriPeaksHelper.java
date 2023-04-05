package org.solitaire.tripeaks;

import org.solitaire.model.Card;
import org.solitaire.model.Column;

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

    protected static Column toWastePile(String[] cards) {
        assert 0 < cards.length && cards.length <= 52;

        return Stream.of(buildCard(51, cards[cards.length - 1])).collect(Collectors.toCollection(Column::new));
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

    protected static int calcCoveredAt(Card card) {
        var at = card.at();

        return switch (row(at)) {
            case 4 -> 0;
            case 3 -> at + 9;
            case 2 -> at + (at - 3) / 2 + 6;
            default -> at * 2 + 3;
        };
    }

    protected static int row(int at) {
        if (at < 0 || at >= LAST_BOARD) {
            throw new RuntimeException("Invalid card position: " + at);
        } else if (at >= INI_COVERED) {
            return 4;
        } else if (at >= 9) {
            return 3;
        } else if (at >= 3) {
            return 2;
        }
        return 1;
    }

}
