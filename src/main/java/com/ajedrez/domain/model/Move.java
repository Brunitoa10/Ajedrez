package com.ajedrez.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Movimiento de una casilla a otra, con pieza de coronación opcional.
 * Los detalles derivados (captura, enroque, al paso) se consultan a {@link Position}.
 */
public record Move(Square from, Square to, PieceType promotionPiece) {

    public Move {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (promotionPiece == PieceType.PAWN || promotionPiece == PieceType.KING) {
            throw new IllegalArgumentException("No se puede coronar a " + promotionPiece);
        }
    }

    public static Move of(Square from, Square to) {
        return new Move(from, to, null);
    }

    public static Move promotion(Square from, Square to, PieceType piece) {
        return new Move(from, to, Objects.requireNonNull(piece, "piece"));
    }

    public boolean isPromotion() {
        return promotionPiece != null;
    }

    public Optional<PieceType> promotion() {
        return Optional.ofNullable(promotionPiece);
    }

    /** Notación UCI: "e2e4", "e7e8q". */
    public String toUci() {
        return from.toString() + to + (isPromotion() ? String.valueOf(promotionPiece.fenChar()) : "");
    }

    public static Move fromUci(String uci) {
        Objects.requireNonNull(uci, "uci");
        String trimmed = uci.trim();
        if (trimmed.length() != 4 && trimmed.length() != 5) {
            throw new IllegalArgumentException("Movimiento UCI inválido: " + uci);
        }
        Square from = Square.parse(trimmed.substring(0, 2));
        Square to = Square.parse(trimmed.substring(2, 4));
        PieceType promotion = trimmed.length() == 5 ? PieceType.fromChar(trimmed.charAt(4)) : null;
        return new Move(from, to, promotion);
    }

    @Override
    public String toString() {
        return toUci();
    }
}
