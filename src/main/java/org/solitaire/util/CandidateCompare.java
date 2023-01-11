package org.solitaire.util;

import org.solitaire.model.Candidate;

import java.util.Comparator;

public class CandidateCompare {
    private final Candidate a, b;

    public CandidateCompare(Candidate a, Candidate b) {
        this.a = a;
        this.b = b;
    }

    @SafeVarargs
    public final int compare(Comparator<Candidate>... comparators) {
        var x = 0;

        for (Comparator<Candidate> comparator : comparators) {
            if (x == 0) {
                x = comparator.compare(a, b);
            } else {
                break;
            }
        }
        return x;
    }
}
