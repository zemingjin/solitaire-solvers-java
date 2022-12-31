package org.solitaire.model;

import java.util.Stack;

public class Deck extends Stack<Card> {
    public Deck() {
        super();
    }

    public Deck(Deck that) {
        addAll(that);
    }
}
