package com.ajedrez.domain.rules;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.PieceType;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;

import java.util.ArrayList;
import java.util.List;

/**
 * Fachada (Facade) de las reglas del ajedrez: es el único punto que el resto de la aplicación
 * necesita conocer para preguntar por jugadas legales, jaques y finales de partida.
 * Es inmutable y sin estado, por lo que es seguro compartirla entre hilos (IA en segundo plano).
 */
public final class ChessRules {

    private static final int FIFTY_MOVES_IN_PLIES = 100;

    private final MoveGenerator moveGenerator;
    private final AttackDetector attackDetector;

    public ChessRules(MoveGenerator moveGenerator, AttackDetector attackDetector) {
        this.moveGenerator = moveGenerator;
        this.attackDetector = attackDetector;
    }

    /** Reglas estándar de la FIDE con la implementación por defecto. */
    public static ChessRules standard() {
        AttackDetector detector = new AttackDetector();
        return new ChessRules(new LegalMoveGenerator(new MoveStrategyRegistry(detector), detector), detector);
    }

    public List<Move> legalMoves(Position position) {
        return moveGenerator.legalMoves(position);
    }

    public List<Move> legalMovesFrom(Position position, Square from) {
        return moveGenerator.legalMovesFrom(position, from);
    }

    public boolean isLegal(Position position, Move move) {
        return legalMovesFrom(position, move.from()).contains(move);
    }

    public boolean isInCheck(Position position) {
        return attackDetector.isKingInCheck(position.board(), position.sideToMove());
    }

    public boolean isSquareAttacked(Position position, Square square) {
        return attackDetector.isSquareAttacked(position.board(), square, position.sideToMove().opposite());
    }

    public boolean isCheckmate(Position position) {
        return isInCheck(position) && legalMoves(position).isEmpty();
    }

    public boolean isStalemate(Position position) {
        return !isInCheck(position) && legalMoves(position).isEmpty();
    }

    public PositionStatus status(Position position) {
        if (legalMoves(position).isEmpty()) {
            return isInCheck(position) ? PositionStatus.CHECKMATE : PositionStatus.STALEMATE;
        }
        if (hasInsufficientMaterial(position)) {
            return PositionStatus.INSUFFICIENT_MATERIAL;
        }
        if (position.halfmoveClock() >= FIFTY_MOVES_IN_PLIES) {
            return PositionStatus.FIFTY_MOVE_RULE;
        }
        return PositionStatus.IN_PROGRESS;
    }

    /**
     * Ningún bando puede dar mate: R vs R, R+menor vs R, o solo alfiles del mismo color de casilla.
     */
    public boolean hasInsufficientMaterial(Position position) {
        List<Square> minorSquares = new ArrayList<>();
        List<Piece> minors = new ArrayList<>();
        for (Square square : Square.ALL) {
            Piece piece = position.board().getOrNull(square);
            if (piece == null || piece.type() == PieceType.KING) {
                continue;
            }
            PieceType type = piece.type();
            if (type == PieceType.PAWN || type == PieceType.ROOK || type == PieceType.QUEEN) {
                return false;
            }
            minors.add(piece);
            minorSquares.add(square);
        }
        if (minors.size() <= 1) {
            return true;
        }
        boolean onlyBishops = minors.stream().allMatch(p -> p.type() == PieceType.BISHOP);
        if (!onlyBishops) {
            return false;
        }
        boolean firstIsLight = minorSquares.get(0).isLight();
        return minorSquares.stream().allMatch(sq -> sq.isLight() == firstIsLight);
    }
}
