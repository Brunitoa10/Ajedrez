package com.ajedrez.domain.model;

/**
 * Tipo de pieza con su valor material de referencia (en centipeones).
 */
public enum PieceType {
    PAWN('p', "", 100, "Peón"),
    KNIGHT('n', "N", 320, "Caballo"),
    BISHOP('b', "B", 330, "Alfil"),
    ROOK('r', "R", 500, "Torre"),
    QUEEN('q', "Q", 900, "Dama"),
    KING('k', "K", 0, "Rey");

    /** Piezas a las que puede coronar un peón, en orden de preferencia. */
    public static final PieceType[] PROMOTION_CHOICES = {QUEEN, ROOK, BISHOP, KNIGHT};

    private final char fenChar;
    private final String sanLetter;
    private final int value;
    private final String displayName;

    private PieceType(char fenChar, String sanLetter, int value, String displayName) {
        this.fenChar = fenChar;
        this.sanLetter = sanLetter;
        this.value = value;
        this.displayName = displayName;
    }

    public char fenChar() {
        return fenChar;
    }

    public String sanLetter() {
        return sanLetter;
    }

    public int value() {
        return value;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isSlider() {
        return this == BISHOP || this == ROOK || this == QUEEN;
    }

    public static PieceType fromChar(char c) {
        char lower = Character.toLowerCase(c);
        for (PieceType type : values()) {
            if (type.fenChar == lower) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de pieza desconocido: " + c);
    }
}
