package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Board;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

import java.util.List;

/** Piezas que saltan una sola vez por dirección: caballo y (sin enroque) rey. */
final class StepMoveStrategy implements PieceMoveStrategy {

    private final int[][] offsets;

    public StepMoveStrategy(int[][] offsets) {
        this.offsets = offsets;
    }

    @Override
    public void addPseudoLegalMoves(Position position, Square from, List<Move> moves) {
        Board board = position.board();
        PieceColor own = position.sideToMove();
        for (int[] offset : offsets) {
            if (!from.canOffset(offset[0], offset[1])) {
                continue;
            }
            Square to = from.offset(offset[0], offset[1]);
            if (!board.isOccupiedBy(to, own)) {
                moves.add(Move.of(from, to));
            }
        }
    }
}
