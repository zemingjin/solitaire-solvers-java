package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

/**
 * G. Heineman’s Staged Deepening (HSD)
 *  1. Performing a depth-first search with a depth-bound of six.
 *  2. Apply the heuristic to evaluate all the board states exactly six moves away from the initial board state.
 *  3. Take the board state with the best score and do another depth-first search with a depth-bound of six from
 *      that state.
 *  4. Repeat steps 2-3 and throw away the rest until a solution or some limit is reached.
 */
@SuppressWarnings("rawtypes")
public class FreeCell extends SolveExecutor<FreeCellBoard> {
    public FreeCell(Columns columns) {
        super(new FreeCellBoard(columns, new Path<>(), new Card[4], new Card[4]), FreeCellBoard::new);
        solveBoard(this::solve);
    }

    protected void solve(FreeCellBoard board) {
        Optional.of(hsdSearch(board))
                .map(this::scoreBoards)
                .map(this::getBestBoard)
                .ifPresent(this::addBoard);
    }

    protected FreeCellBoard getBestBoard(List<FreeCellBoard> boards) {
        return boards.stream().reduce(null, (a, b) -> isNull(a) || b.score() <= a.score() ? b : a);
    }

    private List<FreeCellBoard> scoreBoards(List<FreeCellBoard> boards) {
        boards.forEach(FreeCellBoard::score);
        return boards;
    }

    private static final int DEP_LIMIT = 6;

    private List<FreeCellBoard> hsdSearch(FreeCellBoard board) {
        var boards = List.of(board);

        for (int i = 1; i <= DEP_LIMIT; i++) {
           var next = search(boards);

           if (isNotEmpty(next)) {
               boards = next;
           } else {
               break;
           }
        }
        return boards;
    }

    /**
     * @param boards the given boards
     * @return the boards of next depth.
     */
    private List<FreeCellBoard> search(Collection<FreeCellBoard> boards) {
        return boards.stream().flatMap(this::search).toList();
    }

    private Stream<FreeCellBoard> search(FreeCellBoard board) {
        return Optional.of(board)
                .map(FreeCellBoard::findCandidates)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .stream()
                .flatMap(Collection::stream);
    }

    protected List<FreeCellBoard> applyCandidates(List<Candidate> candidates, FreeCellBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull)
                .map(FreeCellBoard::checkFoundationCandidates)
                .toList();
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }
}