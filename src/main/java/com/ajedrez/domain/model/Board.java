package com.ajedrez.domain.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Tablero inmutable de 64 casillas. Para modificarlo se usa {@link Builder} (patrón Builder),
 * lo que garantiza que una posición ya creada nunca cambie (útil para deshacer y para la IA).
 */
public final class Board {

    private static final Board EMPTY = new Board(new Piece[64]);

    private final Piece[] squares;

    private Board(Piece[] squares) {
        this.squares = squares;
    }

    public static Board empty() {
        return EMPTY;
    }

    public Optional<Piece> pieceAt(Square square) {
        return Optional.ofNullable(squares[square.index()]);
    }

    /** Variante sin Optional para los caminos calientes (generación de movimientos, IA). */
    public Piece getOrNull(Square square) {
        return squares[square.index()];
    }

    public boolean isEmpty(Square square) {
        return squares[square.index()] == null;
    }

    public boolean isOccupiedBy(Square square, PieceColor color) {
        Piece piece = squares[square.index()];
        return piece != null && piece.color() == color;
    }

    public Optional<Square> findKing(PieceColor color) {
        Piece king = Piece.of(color, PieceType.KING);
        for (int i = 0; i < 64; i++) {
            if (squares[i] == king) {
                return Optional.of(Square.fromIndex(i));
            }
        }
        return Optional.empty();
    }

    /** Casillas ocupadas por piezas del color indicado. */
    public List<Square> squaresOf(PieceColor color) {
        List<Square> result = new ArrayList<>(16);
        for (int i = 0; i < 64; i++) {
            Piece piece = squares[i];
            if (piece != null && piece.color() == color) {
                result.add(Square.fromIndex(i));
            }
        }
        return result;
    }

    public Builder toBuilder() {
        return new Builder(squares.clone());
    }

    public static Builder builder() {
        return new Builder(new Piece[64]);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Board board && Arrays.equals(squares, board.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(squares);
    }

    /** Builder mutable para construir tableros nuevos. */
    public static final class Builder {
        private final Piece[] squares;

        private Builder(Piece[] squares) {
            this.squares = squares;
        }

        public Builder put(Square square, Piece piece) {
            squares[square.index()] = piece;
            return this;
        }

        public Builder remove(Square square) {
            squares[square.index()] = null;
            return this;
        }

        public Piece get(Square square) {
            return squares[square.index()];
        }

        public Board build() {
            return new Board(squares.clone());
        }
    }
}
