package com.ajedrez.ai;

import com.ajedrez.domain.model.Board;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.PieceType;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

/**
 * Evaluación clásica: material + tablas pieza-casilla, con el rey interpolado entre
 * medio juego y final según el material que queda (evaluación "tapered"), pareja de alfiles
 * y un término de "mop-up" para que la IA sepa rematar finales ganados (llevar el rey rival al borde).
 */
public final class PieceSquareEvaluator implements Evaluator {

    private static final int MAX_PHASE = 24;
    private static final int BISHOP_PAIR_BONUS = 30;
    private static final int MOP_UP_MATERIAL_EDGE = 300;
    private static final int MOP_UP_MAX_PHASE = 8;

    @Override
    public int evaluate(Position position) {
        Board board = position.board();
        int[] material = new int[2];
        int[] positional = new int[2];
        int[] bishops = new int[2];
        int phase = 0;
        Square[] kings = new Square[2];

        for (Square square : Square.ALL) {
            Piece piece = board.getOrNull(square);
            if (piece == null) {
                continue;
            }
            int side = piece.color().ordinal();
            PieceType type = piece.type();
            if (type == PieceType.KING) {
                kings[side] = square;
                continue;
            }
            material[side] += type.value();
            positional[side] += PieceSquareTables.value(type, piece.color(), square);
            phase += phaseWeight(type);
            if (type == PieceType.BISHOP) {
                bishops[side]++;
            }
        }
        phase = Math.min(phase, MAX_PHASE);

        int[] score = new int[2];
        for (PieceColor color : PieceColor.values()) {
            int side = color.ordinal();
            score[side] = material[side] + positional[side] + kingScore(color, kings[side], phase);
            if (bishops[side] >= 2) {
                score[side] += BISHOP_PAIR_BONUS;
            }
        }
        int whiteView = score[0] - score[1] + mopUp(material, kings, phase);
        return position.sideToMove() == PieceColor.WHITE ? whiteView : -whiteView;
    }

    private int kingScore(PieceColor color, Square king, int phase) {
        if (king == null) {
            return 0;
        }
        int middle = PieceSquareTables.kingMiddlegame(color, king);
        int end = PieceSquareTables.kingEndgame(color, king);
        return (middle * phase + end * (MAX_PHASE - phase)) / MAX_PHASE;
    }

    /** En finales con ventaja clara, premia arrinconar al rey rival y acercar el propio. */
    private int mopUp(int[] material, Square[] kings, int phase) {
        int diff = material[0] - material[1];
        if (phase > MOP_UP_MAX_PHASE || Math.abs(diff) < MOP_UP_MATERIAL_EDGE || kings[0] == null || kings[1] == null) {
            return 0;
        }
        int winner = diff > 0 ? 0 : 1;
        Square loserKing = kings[1 - winner];
        int centerDistance = Math.max(3 - loserKing.file(), loserKing.file() - 4)
                + Math.max(3 - loserKing.rank(), loserKing.rank() - 4);
        int kingsDistance = Math.abs(kings[0].file() - kings[1].file()) + Math.abs(kings[0].rank() - kings[1].rank());
        int bonus = 10 * centerDistance + 4 * (14 - kingsDistance);
        return winner == 0 ? bonus : -bonus;
    }

    private static int phaseWeight(PieceType type) {
        return switch (type) {
            case KNIGHT, BISHOP -> 1;
            case ROOK -> 2;
            case QUEEN -> 4;
            default -> 0;
        };
    }
}
