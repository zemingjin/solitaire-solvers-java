package org.solitaire.freecell;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.solitaire.model.Candidate;
import org.solitaire.model.Card;
import org.solitaire.model.Columns;
import org.solitaire.model.Path;
import org.solitaire.model.SolveExecutor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.IntStream.range;

/**
 * G. Heineman’s Staged Deepening (HSD)
 * 1. Performing a depth-first search with a depth-bound of six.
 * 2. Apply the heuristic to evaluate all the board states exactly six moves away from the initial board state.
 * 3. Take the board state with the best score and do another depth-first search with a depth-bound of six from
 * that state.
 * 4. Repeat steps 2-3 and throw away the rest until a solution or some limit is reached.
 */
@SuppressWarnings("rawtypes")
public class FreeCell extends SolveExecutor<FreeCellBoard> {
    private static final Function<List<FreeCellBoard>, List<FreeCellBoard>> reduceBoards =
            boards -> range(boards.size() / 2, boards.size()).mapToObj(boards::get).toList();

    public FreeCell(Columns columns) {
        super(new FreeCellBoard(columns, new Path<>(), new Card[4], new Card[4]), FreeCellBoard::new);
        solveBoard(this::solve);
    }

    @Override
    public List<List> solve() {
        var verify = board().verifyBoard();

        if (verify.isEmpty()) {
            return super.solve();
        }
        throw new RuntimeException(verify.toString());
    }

    protected void solve(FreeCellBoard board) {
        Optional.of(board)
                .map(FreeCellBoard::findCandidates)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .filter(ObjectUtils::isNotEmpty)
                .map(reduceBoards)
                .ifPresent(this::addBoards);
    }

    protected List<FreeCellBoard> applyCandidates(List<Candidate> candidates, FreeCellBoard board) {
        return candidates.stream()
                .map(it -> clone(board).updateBoard(it))
                .filter(Objects::nonNull)
                .sorted(comparingInt(FreeCellBoard::score))
                .collect(Collectors.toList());
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }

}