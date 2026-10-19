package com.ajedrez.domain.model;

/**
 * Las 12 piezas posibles (tipo + color). Al ser un enum son inmutables y se comparan por identidad.
 */
public enum Piece {
    WHITE_PAWN(PieceColor.WHITE, PieceType.PAWN),
    WHITE_KNIGHT(PieceColor.WHITE, PieceType.KNIGHT),
    WHITE_BISHOP(PieceColor.WHITE, PieceType.BISHOP),
    WHITE_ROOK(PieceColor.WHITE, PieceType.ROOK),
    WHITE_QUEEN(PieceColor.WHITE, PieceType.QUEEN),
    WHITE_KING(PieceColor.WHITE, PieceType.KING),
    BLACK_PAWN(PieceColor.BLACK, PieceType.PAWN),
    BLACK_KNIGHT(PieceColor.BLACK, PieceType.KNIGHT),
    BLACK_BISHOP(PieceColor.BLACK, PieceType.BISHOP),
    BLACK_ROOK(PieceColor.BLACK, PieceType.ROOK),
    BLACK_QUEEN(PieceColor.BLACK, PieceType.QUEEN),
    BLACK_KING(PieceColor.BLACK, PieceType.KING);

    private final PieceColor color;
    private final PieceType type;

    Piece(PieceColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public PieceColor color() {
        return color;
    }

    public PieceType type() {
        return type;
    }

    public boolean is(PieceColor color, PieceType type) {
        return this.color == color && this.type == type;
    }

    /** Letra FEN: mayúscula para blancas, minúscula para negras. */
    public char fenChar() {
        return color == PieceColor.WHITE ? Character.toUpperCase(type.fenChar()) : type.fenChar();
    }

    public static Piece of(PieceColor color, PieceType type) {
        return values()[(color == PieceColor.WHITE ? 0 : 6) + type.ordinal()];
    }

    public static Piece fromFenChar(char c) {
        PieceColor color = Character.isUpperCase(c) ? PieceColor.WHITE : PieceColor.BLACK;
        return of(color, PieceType.fromChar(c));
    }
}
