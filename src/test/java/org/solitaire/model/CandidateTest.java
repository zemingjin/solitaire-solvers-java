package org.solitaire.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.model.Candidate.buildCandidate;
import static org.solitaire.model.Origin.COLUMN;
import static org.solitaire.util.CardHelper.buildCard;

class CandidateTest {
    @Test
    void test_isKing() {
        assertTrue(buildCandidate(0, COLUMN, buildCard(0, "Ks")).isKing());
        assertFalse(buildCandidate(0, COLUMN, buildCard(0, "Ad")).isKing());
    }

}