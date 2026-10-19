package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Board;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.PieceType;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

import java.util.List;

/**
 * Rey: pasos de una casilla (delegados a {@link StepMoveStrategy}, composición sobre herencia) + enroques.
 */
final class KingMoveStrategy implements PieceMoveStrategy {

    private static final int KING_START_FILE = 4;

    private final PieceMoveStrategy steps = new StepMoveStrategy(Directions.ALL);
    private final AttackDetector attackDetector;

    KingMoveStrategy(AttackDetector attackDetector) {
        this.attackDetector = attackDetector;
    }

    @Override
    public void addPseudoLegalMoves(Position position, Square from, List<Move> moves) {
        steps.addPseudoLegalMoves(position, from, moves);
        addCastling(position, from, true, moves);
        addCastling(position, from, false, moves);
    }

    private void addCastling(Position position, Square from, boolean kingSide, List<Move> moves) {
        PieceColor own = position.sideToMove();
        int rank = own.backRank();
        if (!position.castlingRights().canCastle(own, kingSide)
                || from.rank() != rank || from.file() != KING_START_FILE) {
            return;
        }
        Board board = position.board();
        Square rookSquare = Square.of(kingSide ? 7 : 0, rank);
        if (board.getOrNull(rookSquare) != Piece.of(own, PieceType.ROOK)) {
            return;
        }
        if (!pathIsEmpty(board, rank, kingSide)) {
            return;
        }
        PieceColor enemy = own.opposite();
        int step = kingSide ? 1 : -1;
        Square transit = Square.of(KING_START_FILE + step, rank);
        Square destination = Square.of(KING_START_FILE + 2 * step, rank);
        if (attackDetector.isSquareAttacked(board, from, enemy)
                || attackDetector.isSquareAttacked(board, transit, enemy)
                || attackDetector.isSquareAttacked(board, destination, enemy)) {
            return;
        }
        moves.add(Move.of(from, destination));
    }

    private boolean pathIsEmpty(Board board, int rank, boolean kingSide) {
        int firstFile = kingSide ? 5 : 1;
        int lastFile = kingSide ? 6 : 3;
        for (int file = firstFile; file <= lastFile; file++) {
            if (!board.isEmpty(Square.of(file, rank))) {
                return false;
            }
        }
        return true;
    }
}
