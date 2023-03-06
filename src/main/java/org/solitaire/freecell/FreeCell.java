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
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
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
    private static final int DEP_LIMIT = 6;
    private static final Function<List<FreeCellBoard>, List<FreeCellBoard>> reduceBoards =
            boards -> range(boards.size() * 3 / 5, boards.size()).mapToObj(boards::get).toList();
    protected final Function<List<FreeCellBoard>, FreeCellBoard> getBestBoard =
            boards -> boards.stream().reduce(null, (a, b) -> isNull(a) || b.score() >= a.score() ? b : a);

    public FreeCell(Columns columns) {
        super(new FreeCellBoard(columns, new Path<>(), new Card[4], new Card[4]), FreeCellBoard::new);
        solveBoard(solveByHSD() ? this::solveByHSD : this::solveByDFS);
    }

    protected void solveByDFS(FreeCellBoard board) {
        Optional.of(board)
                .map(FreeCellBoard::findCandidates)
                .filter(ObjectUtils::isNotEmpty)
                .map(it -> applyCandidates(it, board))
                .map(it -> it.sorted(comparingInt(FreeCellBoard::score)).toList())
                .map(reduceBoards)
                .ifPresent(this::addBoards);
    }

    protected void solveByHSD(FreeCellBoard board) {
        Optional.of(hsdSearch(board))
                .filter(ObjectUtils::isNotEmpty)
                .map(getBestBoard)
                .ifPresent(super::addBoard);
    }

    private List<FreeCellBoard> hsdSearch(FreeCellBoard board) {
        var boards = List.of(board);

        for (int i = 1; i <= DEP_LIMIT; i++) {
            var next = search(boards);

            if (next.isEmpty()) {
                break;
            } else {
                boards = next;
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
                .flatMap(it -> it);
    }

    protected Stream<FreeCellBoard> applyCandidates(List<Candidate> candidates, FreeCellBoard board) {
        return candidates.stream()
                .map(it -> Optional.ofNullable(clone(board)).map(b -> b.updateBoard(it)).orElse(null))
                .filter(Objects::nonNull);
    }

    @Override
    public Pair<Integer, List> getMaxScore(List<List> results) {
        throw new RuntimeException("Not applicable");
    }
}