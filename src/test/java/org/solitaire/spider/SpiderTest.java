package org.solitaire.spider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solitaire.model.Column;
import org.solitaire.util.CardHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.solitaire.spider.SpiderHelper.LAST_DECK;
import static org.solitaire.spider.SpiderHelper.build;
import static org.solitaire.spider.SpiderHelperTest.cards;
import static org.solitaire.util.CardHelper.VALUES;
import static org.solitaire.util.CardHelper.buildCard;

class SpiderTest {
    private Spider spider;

    private static Column mockRun() {
        return mockRun(VALUES.length());
    }

    private static Column mockRun(int length) {
        var column = new Column();

        for (int i = length; i-- > 0; ) {
            column.add(buildCard(0, VALUES.charAt(i) + "d"));
        }
        return column;
    }

    @BeforeEach
    public void setup() {
        CardHelper.useSuit = false;
        spider = build(cards);
    }

    @Test
    public void test_solve() {
//        var result = spider.solve();
//
//        assertNull(result);
    }

    @Test
    public void test_getMaxScore() {
//        var result = spider.getMaxScore(spider.solve());
//
//        assertNotNull(result);
//        assertEquals(0, result.getLeft());
//        assertNotNull(result.getRight());
//        assertTrue(result.getRight().isEmpty());
    }

    @Test
    public void test_updateTargetColumn() {
        var candidate = spider.findCandidateAtColumn(5).setTarget(9);

        assertEquals(5, spider.getColumns().get(candidate.getFrom()).size());
        assertEquals(5, spider.getColumns().get(candidate.getTarget()).size());
        var clone = spider.updateTargetColumn(candidate);

        assertNotNull(clone);
        assertEquals(4, clone.getColumns().get(candidate.getFrom()).size());
        assertEquals(6, clone.getColumns().get(candidate.getTarget()).size());
    }

    @Test
    public void test_drawDeckCards() {
        assertEquals(LAST_DECK, spider.getDeck().size());

        spider.drawDeckCards();

        assertEquals(LAST_DECK - spider.getColumns().size(), spider.getDeck().size());
        assertEquals("5s", spider.getColumns().get(0).peek().raw());
        assertEquals("Qh", spider.getColumns().get(9).peek().raw());
    }

    @Test
    public void test_checkForRuns() {
        spider.getColumns().set(0, mockRun());
        var column = spider.getColumns().get(0);

        assertEquals(13, column.size());
        assertEquals("0:Kd", column.get(0).toString());
        assertEquals("0:Ad", column.get(12).toString());
        assertEquals(500, spider.getTotalScore());

        var result = spider.checkForRuns();

        assertNotNull(result);
        assertTrue(column.isEmpty());
        assertEquals("[Ad, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, Td, Jd, Qd, Kd]",
                spider.getPath().get(0));
        assertEquals(600, spider.getTotalScore());
    }

    @Test
    public void test_checkForRuns_noRuns() {
        var column = spider.getColumns().get(0);
        column.addAll(mockRun(10));

        assertEquals(16, column.size());
        assertEquals(500, spider.getTotalScore());

        var result = spider.checkForRuns();

        assertNotNull(result);
        assertEquals(16, column.size());
        assertEquals(500, spider.getTotalScore());
    }

    @Test
    public void test_appendToTarget() {
        var candidate = spider.findCandidateAtColumn(5).setTarget(9);
        var column = spider.getColumns().get(candidate.getTarget());

        assertEquals(5, column.size());
        assertEquals("53:3h", column.peek().toString());
        assertTrue(spider.getPath().isEmpty());
        assertEquals(500, spider.getTotalScore());

        spider.appendToTarget(candidate);

        assertEquals(6, column.size());
        assertNotEquals("53:3h", column.peek().toString());
        assertEquals("33:2h", column.peek().toString());

        assertFalse(spider.getPath().isEmpty());
        assertEquals(1, spider.getPath().size());
        assertEquals("2h", spider.getPath().get(0));
        assertEquals(499, spider.getTotalScore());
    }

    @Test
    public void test_removeFromSource() {
        var candidate = spider.findCandidateAtColumn(5).setTarget(9);
        var column = spider.getColumns().get(candidate.getFrom());

        assertEquals(5, column.size());
        assertEquals("33:2h", column.peek().toString());

        spider.removeFromSource(candidate);

        assertEquals(4, column.size());
        assertNotEquals("33:2h", column.peek().toString());
    }

    @Test
    public void test_findTargets() {
        var targets = spider.findTargets();

        assertNotNull(targets);
        assertEquals(2, targets.size());
        assertEquals(7, targets.get(0).getTarget());
        assertEquals(9, targets.get(1).getTarget());
    }

    @Test
    public void test_findTargetsByCandidate() {
        var candidate = spider.findCandidateAtColumn(5);
        var targets = spider.findTargetsByCandidate(candidate).toList();

        assertNotNull(targets);
        assertEquals(9, targets.get(0).getTarget());
        assertEquals("Candidate(cards=[33:2h], origin=COLUMN, from=5, target=9)", targets.get(0).toString());

        candidate = spider.findCandidateAtColumn(1);
        targets = spider.findTargetsByCandidate(candidate).toList();
        assertNotNull(targets);
        assertEquals(7, targets.get(0).getTarget());
        assertEquals("Candidate(cards=[11:5h], origin=COLUMN, from=1, target=7)", targets.get(0).toString());
    }

    @Test
    public void test_findOpenCandidates() {
        var candidates = spider.findOpenCandidates();

        assertNotNull(candidates);
        assertEquals(10, candidates.size());
        assertEquals("Candidate(cards=[5:Th], origin=COLUMN, from=0, target=-1)", candidates.get(0).toString());
        assertEquals("Candidate(cards=[53:3h], origin=COLUMN, from=9, target=-1)", candidates.get(9).toString());
    }

    @Test
    public void test_findCandidateAtColumn() {
        var candidate = spider.findCandidateAtColumn(0);

        assertNotNull(candidate);
        assertEquals("Candidate(cards=[5:Th], origin=COLUMN, from=0, target=-1)", candidate.toString());

        candidate = spider.findCandidateAtColumn(spider.getColumns().size() - 1);
        assertEquals("Candidate(cards=[53:3h], origin=COLUMN, from=9, target=-1)", candidate.toString());
    }
}