package org.solitaire.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathTest {
    private final Path<String> path = new Path<>();

    @Test
    public void test_peek() {
        path.add("abc");
        assertEquals("abc", path.peek());
    }

}