package org.solitaire.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Stack;

@ToString
@Builder
@Getter
public class Candidate {
    private Stack<Card> cards;
    private Origin origin;
    private int from;
    private int target;

    public Candidate setTarget(int target) {
        this.target = target;
        return this;
    }
}
