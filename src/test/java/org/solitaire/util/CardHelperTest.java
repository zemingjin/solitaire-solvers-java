package org.solitaire.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solitaire.model.GameSolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.solitaire.util.CardHelper.CLUB;
import static org.solitaire.util.CardHelper.DIAMOND;
import static org.solitaire.util.CardHelper.HEART;
import static org.solitaire.util.CardHelper.SPADE;
import static org.solitaire.util.CardHelper.buildCard;
import static org.solitaire.util.CardHelper.card;
import static org.solitaire.util.CardHelper.checkDuplicates;
import static org.solitaire.util.CardHelper.getSuit;
import static org.solitaire.util.CardHelper.higherCardOfSameSuit;
import static org.solitaire.util.CardHelper.nextCard;
import static org.solitaire.util.CardHelper.rank;
import static org.solitaire.util.CardHelper.rankDifference;
import static org.solitaire.util.CardHelper.string;
import static org.solitaire.util.CardHelper.stringOfRaws;
import static org.solitaire.util.CardHelper.suit;
import static org.solitaire.util.CardHelper.toArray;

@ExtendWith(MockitoExtension.class)
public class CardHelperTest {
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int FOUR = 4;
    public static final int FIVE = 5;
    public static final int SIX = 6;

    @Mock GameSolver gameSolver;
    @Mock RuntimeException exception;

    @BeforeEach
    void setup() {
        CardHelper.useSuit(false);
        gameSolver = spy(gameSolver);
        exception = spy(exception);
    }

    @Test
    void test_rank() {
        assertEquals(0, rank(null));
        assertEquals(1, rank(card("Ad")));
        assertEquals(13, rank(card("Kd")));
    }

    @Test
    void test_suit() {
        assertEquals("C", suit(0));
        assertEquals("D", suit(1));
        assertEquals("H", suit(2));
        assertEquals("S", suit(3));
        CardHelper.useSuit(true);
        assertEquals(CLUB, suit(0));
        assertEquals(DIAMOND, suit(1));
        assertEquals(HEART, suit(2));
        assertEquals(SPADE, suit(3));
    }

    @Test
    void test_getSuit() {
        assertEquals(DIAMOND, getSuit("d"));
        assertEquals(SPADE, getSuit("s"));
        assertEquals(HEART, getSuit("h"));
        assertEquals(CLUB, getSuit("c"));
    }

    @Test
    void test_toString() {
        assertEquals("Ah", CardHelper.stringOfRaws(buildCard(1, "Ah")));
        assertEquals("[Ah, 9h]",
                CardHelper.stringOfRaws(toArray(buildCard(1, "Ah"), buildCard(2, "9h"))));
    }

    @Test
    void test_diffOfValues() {
        var a = card("Ts");
        var b = card("9d");

        assertEquals(1, rankDifference(a, b));
        assertEquals(10, rankDifference(a, null));
    }

    @Test
    void test_checkDuplicates() {
        var cards = new String[]{"9d", "9d"};

        assertThrows(RuntimeException.class, () -> checkDuplicates(cards));
    }

    @Test
    void test_stringOfRaws() {
        assertEquals("Ad", stringOfRaws(toArray(card("Ad"))));
        assertEquals("[Ad, 2d]", stringOfRaws(toArray(card("Ad"), card("2d"))));

        assertEquals("[]", stringOfRaws(toArray()));
    }

    @Test
    void test_nextCard() {
        assertEquals("2d", nextCard(card("Ad")).raw());
    }

    @Test
    void test_higherCardOfSameSuit() {
        assertEquals("2h", higherCardOfSameSuit(card("Ah")).raw());
        assertEquals("Kh", higherCardOfSameSuit(card("Kh")).raw());
    }

    @Test
    void test_string() {
        assertEquals("Ad", string(List.of(card("Ad"))));
        assertEquals("Ad, Ks", string(List.of(toArray(card("Ad"), card("Ks")))));
        assertEquals("[Ad, Ks], Qh",
                string(List.of(toArray(card("Ad"), card("Ks")), card("Qh"))));
    }
}