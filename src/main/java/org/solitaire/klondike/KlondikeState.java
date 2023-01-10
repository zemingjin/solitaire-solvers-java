package org.solitaire.klondike;

import lombok.Getter;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Deck;
import org.solitaire.model.GameState;
import org.solitaire.model.Path;

import java.util.List;
import java.util.Stack;

import static org.solitaire.util.CardHelper.cloneStack;
import static org.solitaire.util.CardHelper.cloneStacks;

@Getter
class KlondikeState extends GameState<String> {
    protected final Deck deck;
    protected final Stack<Card> deckPile;
    protected final List<Stack<Card>> foundations;
    protected boolean stateChanged;

    KlondikeState(Columns columns,
                  Path<String> path,
                  int totalScore,
                  Deck deck,
                  Stack<Card> deckPile,
                  List<Stack<Card>> foundations,
                  boolean stateChanged) {
        super(columns, path, totalScore);
        this.deck = deck;
        this.deckPile = deckPile;
        this.foundations = foundations;
        this.stateChanged = stateChanged;
    }

    KlondikeState(KlondikeState that) {
        this(new Columns(that.getColumns()),
                new Path<>(that.getPath()),
                that.getTotalScore(),
                new Deck(that.getDeck()),
                cloneStack(that.getDeckPile()),
                cloneStacks(that.getFoundations()),
                that.stateChanged);
    }

    protected void setStateChanged(boolean stateChanged) {
        this.stateChanged = stateChanged;
    }
}
