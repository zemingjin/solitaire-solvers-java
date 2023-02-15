package org.solitaire.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.model.Origin.FOUNDATION;
import static org.solitaire.model.Origin.FREECELL;
import static org.solitaire.util.CardHelper.buildCard;

class CandidateTest {
    @Test
    void test_isKing() {
        assertTrue(buildCandidate(0, COLUMN, buildCard(0, "Ks")).isKing());
        assertFalse(buildCandidate(0, COLUMN, buildCard(0, "Ad")).isKing());
    }

    @Test
    void test_isFrom() {
        var candidate = buildCandidate(0, COLUMN, COLUMN, buildCard(0, "Ks"));

        assertTrue(candidate.isFromColumn());
        assertTrue(candidate.isToColumn());
        assertFalse(candidate.isFromFreeCell());
        assertFalse(candidate.isToFreeCell());

        candidate = buildCandidate(0, FREECELL, FREECELL, buildCard(0, "Ks"));
        assertTrue(candidate.isFromFreeCell());
        assertTrue(candidate.isToFreeCell());
        assertFalse(candidate.isFromColumn());
        assertFalse(candidate.isToFoundation());

        candidate = buildCandidate(0, FREECELL, FOUNDATION, buildCard(0, "Ks"));
        assertTrue(candidate.isToFoundation());
        assertFalse(candidate.isToColumn());
    }

}