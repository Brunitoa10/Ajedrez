package com.ajedrez.ai;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Piece;
import com.ajedrez.domain.model.Position;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ordena jugadas para que alfa-beta pode más: primero la mejor de la iteración anterior,
 * luego coronaciones y capturas por MVV-LVA (víctima más valiosa, atacante menos valioso).
 */
final class MoveOrderer {

    private static final int PRINCIPAL_MOVE_SCORE = 1_000_000;
    private static final int PROMOTION_SCORE = 50_000;
    private static final int CAPTURE_BASE_SCORE = 10_000;

    List<Move> order(Position position, List<Move> moves, Move principal) {
        List<Move> sorted = new ArrayList<>(moves);
        sorted.sort(Comparator.comparingInt((Move m) -> score(position, m, principal)).reversed());
        return sorted;
    }

    private int score(Position position, Move move, Move principal) {
        if (move.equals(principal)) {
            return PRINCIPAL_MOVE_SCORE;
        }
        int score = 0;
        if (move.isPromotion()) {
            score += PROMOTION_SCORE + move.promotionPiece().value();
        }
        if (position.isCapture(move)) {
            int victim = position.capturedPiece(move).map(p -> p.type().value()).orElse(0);
            Piece attacker = position.board().getOrNull(move.from());
            score += CAPTURE_BASE_SCORE + 10 * victim - attacker.type().value() / 10;
        }
        return score;
    }
}
