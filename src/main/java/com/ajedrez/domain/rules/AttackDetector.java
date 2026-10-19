package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Board;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.PieceType;
import com.ajedrez.domain.model.Square;

/**
 * Responde una sola pregunta: ¿la casilla X está atacada por el bando Y?
 * Se busca "hacia atrás" desde la casilla objetivo, lo que es mucho más barato que generar
 * todos los movimientos del rival.
 */
public final class AttackDetector {

    public boolean isSquareAttacked(Board board, Square target, PieceColor attacker) {
        return isAttackedByPawn(board, target, attacker)
                || isAttackedByStepper(board, target, attacker, Directions.KNIGHT, PieceType.KNIGHT)
                || isAttackedByStepper(board, target, attacker, Directions.ALL, PieceType.KING)
                || isAttackedBySlider(board, target, attacker, Directions.ORTHOGONAL, PieceType.ROOK)
                || isAttackedBySlider(board, target, attacker, Directions.DIAGONAL, PieceType.BISHOP);
    }

    public boolean isKingInCheck(Board board, PieceColor kingColor) {
        return board.findKing(kingColor)
                .map(king -> isSquareAttacked(board, king, kingColor.opposite()))
                .orElse(false);
    }

    private boolean isAttackedByPawn(Board board, Square target, PieceColor attacker) {
        int rankFrom = -attacker.pawnDirection();
        Piece pawn = Piece.of(attacker, PieceType.PAWN);
        for (int df : new int[]{-1, 1}) {
            if (target.canOffset(df, rankFrom) && board.getOrNull(target.offset(df, rankFrom)) == pawn) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttackedByStepper(Board board, Square target, PieceColor attacker,
                                        int[][] offsets, PieceType type) {
        Piece stepper = Piece.of(attacker, type);
        for (int[] offset : offsets) {
            if (target.canOffset(offset[0], offset[1])
                    && board.getOrNull(target.offset(offset[0], offset[1])) == stepper) {
                return true;
            }
        }
        return false;
    }

    /** Busca torres/alfiles (según las direcciones) y damas en línea recta desde la casilla. */
    private boolean isAttackedBySlider(Board board, Square target, PieceColor attacker,
                                       int[][] directions, PieceType sliderType) {
        Piece slider = Piece.of(attacker, sliderType);
        Piece queen = Piece.of(attacker, PieceType.QUEEN);
        for (int[] dir : directions) {
            Square current = target;
            while (current.canOffset(dir[0], dir[1])) {
                current = current.offset(dir[0], dir[1]);
                Piece piece = board.getOrNull(current);
                if (piece == null) {
                    continue;
                }
                if (piece == slider || piece == queen) {
                    return true;
                }
                break;
            }
        }
        return false;
    }
}
