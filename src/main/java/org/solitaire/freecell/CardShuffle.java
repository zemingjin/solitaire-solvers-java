package org.solitaire.freecell;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.suit;

/**
 * From: jimh@exmsft.com (Jim Horne)
 * <p>
 * I'm happy to share the card shuffle algorithm, but I warn you,
 * it does depend on the rand() and srand() function built into MS
 * compilers.  The good news is that I believe these work the same
 * for all our compilers.
 * <p>
 * I use cards.dll which has its own mapping of numbers (0-51) to
 * cards.  The following will give you the idea.  Play around with
 * this, and you'll be able to generate all the games.
 * <p>
 * Go ahead and post the code.  People might as well have fun with it.
 * Please keep me posted on anything interesting that comes of it.
 * Thanks.
 * <p>
 * _______________________________________________________________
 * <p>
 * #define     BLACK           0               // COLOUR(card)
 * #define     RED             1
 * <p>
 * #define     ACE             0               // VALUE(card)
 * #define     DEUCE           1
 * <p>
 * #define     CLUB            0               // SUIT(card)
 * #define     DIAMOND         1
 * #define     HEART           2
 * #define     SPADE           3
 * <p>
 * #define     SUIT(card)      ((card) % 4)
 * #define     VALUE(card)     ((card) / 4)
 * #define     COLOUR(card)    (SUIT(card) == DIAMOND || SUIT(card) == HEART)
 * <p>
 * #define     MAXPOS         21
 * #define     MAXCOL          9    // includes top row as column 0
 * CARD    card[MAXCOL][MAXPOS];    // current layout of cards, CARDs are ints
 * <p>
 * int  i, j;                // generic counters
 * int  col, pos;
 * int  wLeft = 52;          // cards left to be chosen in shuffle
 * CARD deck[52];            // deck of 52 unique cards
 * <p>
 * for (col = 0; col < MAXCOL; col++)          // clear the deck
 * for (pos = 0; pos < MAXPOS; pos++)
 * card[col][pos] = EMPTY;
 * <p>
 * shuffle cards
 * <p>
 * for(i=0;i< 52;i++)      // put unique card in each deck loc.
 * deck[i]=i;
 * <p>
 * srand(gamenumber);            // gamenumber is seed for rand()
 * for(i=0;i< 52;i++)
 * {
 * j=rand()%wLeft;
 * card[(i%8)+1][i/8]=deck[j];
 * deck[j]=deck[--wLeft];
 * }
 */
public class CardShuffle {
    protected static final int NUM_CARDS = 52;
    protected static final int MAXPOS = 21;
    protected static final int MAXCOL = 8;

    private static int msRandom(AtomicInteger seed) {
        seed.set(seed.get() * 214013 + 2531011);
        return (seed.get() >> 16) & 0x7fff;
    }

    private static int[][] cleanUp(int[][] card) {
        for (int i = 0; i < card.length; i++) {
            card[i] = Arrays.copyOf(card[i], i < 4 ? 7 : 6);
        }
        return card;
    }

    private static String[][] toStrings(int[][] cards) {
        return stream(cards)
                .map(CardShuffle::toRow)
                .toArray(String[][]::new);
    }

    private static String[] toRow(int[] cards) {
        return stream(cards)
                .mapToObj(card -> format("%s%s", value(card), suit(card % 4)))
                .toArray(String[]::new);
    }

    private static String value(int at) {
        return String.valueOf(VALUES.charAt(at / 4));
    }

    public String[][] genBoard(int gameNumber) {
        return Optional.of(getShuffledCards(gameNumber))
                .map(CardShuffle::cleanUp)
                .map(CardShuffle::toStrings)
                .orElseThrow();
    }

    private int[][] getShuffledCards(int gameNumber) {
        var deck = initDeck();
        var card = new int[MAXCOL][MAXPOS];
        var wLeft = new AtomicInteger(NUM_CARDS);
        var seed = new AtomicInteger(gameNumber);

        range(0, NUM_CARDS).forEach(i -> {
            var j = msRandom(seed) % wLeft.get();

            card[i % 8][i / 8] = deck[j];
            deck[j] = deck[wLeft.decrementAndGet()];
        });
        return card;
    }

    private int[] initDeck() {
        var deck = new int[NUM_CARDS];

        range(0, deck.length).forEach(i -> deck[i] = i);

        return deck;
    }

}
