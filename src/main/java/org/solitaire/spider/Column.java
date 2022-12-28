package org.solitaire.spider;

import org.solitaire.model.Card;

import java.util.List;

public record Column(int openAt, List<Card> cards) {
}
