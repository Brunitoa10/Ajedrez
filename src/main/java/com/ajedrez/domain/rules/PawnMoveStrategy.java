package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Board;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.PieceType;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

import java.util.List;

/** Avance simple y doble, capturas diagonales, captura al paso y coronación. */
final class PawnMoveStrategy implements PieceMoveStrategy {

    @Override
    public void addPseudoLegalMoves(Position position, Square from, List<Move> moves) {
        Board board = position.board();
        PieceColor own = position.sideToMove();
        int dir = own.pawnDirection();

        if (from.canOffset(0, dir)) {
            Square oneStep = from.offset(0, dir);
            if (board.isEmpty(oneStep)) {
                addWithPromotions(from, oneStep, own, moves);
                if (from.rank() == own.pawnStartRank()) {
                    Square twoSteps = from.offset(0, 2 * dir);
                    if (board.isEmpty(twoSteps)) {
                        moves.add(Move.of(from, twoSteps));
                    }
                }
            }
        }

        for (int df : new int[]{-1, 1}) {
            if (!from.canOffset(df, dir)) {
                continue;
            }
            Square target = from.offset(df, dir);
            boolean enemyThere = board.isOccupiedBy(target, own.opposite());
            boolean enPassant = position.enPassantTarget().map(target::equals).orElse(false);
            if (enemyThere || enPassant) {
                addWithPromotions(from, target, own, moves);
            }
        }
    }

    private void addWithPromotions(Square from, Square to, PieceColor own, List<Move> moves) {
        if (to.rank() != own.promotionRank()) {
            moves.add(Move.of(from, to));
            return;
        }
        for (PieceType choice : PieceType.PROMOTION_CHOICES) {
            moves.add(Move.promotion(from, to, choice));
        }
    }
}
