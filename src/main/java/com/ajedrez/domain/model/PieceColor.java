package com.ajedrez.domain.model;

/**
 * Bando de una pieza o jugador.
 */
public enum PieceColor {
    WHITE("Blancas"),
    BLACK("Negras");

    private final String displayName;

    private PieceColor(String displayName) {
        this.displayName = displayName;
    }

    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /** Dirección en la que avanzan los peones de este color (+1 hacia la fila 8, -1 hacia la fila 1). */
    public int pawnDirection() {
        return this == WHITE ? 1 : -1;
    }

    /** Fila (0..7) donde arrancan los peones de este color. */
    public int pawnStartRank() {
        return this == WHITE ? 1 : 6;
    }

    /** Fila (0..7) donde corona un peón de este color. */
    public int promotionRank() {
        return this == WHITE ? 7 : 0;
    }

    /** Fila (0..7) donde arrancan las piezas mayores de este color. */
    public int backRank() {
        return this == WHITE ? 0 : 7;
    }

    public String displayName() {
        return displayName;
    }
}
