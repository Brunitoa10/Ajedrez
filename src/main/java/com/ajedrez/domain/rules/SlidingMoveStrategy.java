package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Board;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

import java.util.List;

/** Piezas que se deslizan hasta chocar: alfil, torre y dama (difieren solo en las direcciones). */
final class SlidingMoveStrategy implements PieceMoveStrategy {

    private final int[][] directions;

    SlidingMoveStrategy(int[][] directions) {
        this.directions = directions;
    }

    @Override
    public void addPseudoLegalMoves(Position position, Square from, List<Move> moves) {
        Board board = position.board();
        PieceColor own = position.sideToMove();
        for (int[] dir : directions) {
            Square current = from;
            while (current.canOffset(dir[0], dir[1])) {
                current = current.offset(dir[0], dir[1]);
                Piece occupant = board.getOrNull(current);
                if (occupant == null) {
                    moves.add(Move.of(from, current));
                    continue;
                }
                if (occupant.color() != own) {
                    moves.add(Move.of(from, current));
                }
                break;
            }
        }
    }
}
