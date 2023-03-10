package org.solitaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.util.CardHelperTest.ONE;
import static org.solitaire.util.CardHelperTest.TWO;

class BoardStackTest {
    private BoardStack<MockBoard> queue;

    @BeforeEach
    void setup() {
        queue = new BoardStack<>(List.of(new MockBoard(null, 2), new MockBoard(null, 1)));
    }

    @Test
    void test_construct() {
        assertEquals(TWO, queue.size());
        assert queue.peek() != null;
        assertEquals(TWO, queue.peek().score());
        assertEquals(ONE, queue.get(0).score());
    }

    @Test
    void test_isNotEmpty() {
        assertTrue(queue.isNotEmpty());
        queue.clear();
        assertFalse(queue.isNotEmpty());
    }

    record MockBoard(List<Card> path, int score) implements Board<Card, Card> {
        @Override
        public boolean isSolved() {
            return false;
        }

        @Override
        public List<String> verify() {
            return emptyList();
        }

        @Override
        public Board<Card, Card> updateBoard(Card candidate) {
            return null;
        }
    }

}