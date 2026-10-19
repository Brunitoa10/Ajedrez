package com.ajedrez.domain.model;

/**
 * Derechos de enroque restantes (valor inmutable).
 */
public record CastlingRights(boolean whiteKingSide, boolean whiteQueenSide,
                             boolean blackKingSide, boolean blackQueenSide) {

    public static final CastlingRights ALL = new CastlingRights(true, true, true, true);
    public static final CastlingRights NONE = new CastlingRights(false, false, false, false);

    public boolean canCastle(PieceColor color, boolean kingSide) {
        if (color == PieceColor.WHITE) {
            return kingSide ? whiteKingSide : whiteQueenSide;
        }
        return kingSide ? blackKingSide : blackQueenSide;
    }

    /** Quita ambos enroques de un color (por ejemplo, cuando mueve el rey). */
    public CastlingRights revokeAll(PieceColor color) {
        return color == PieceColor.WHITE
                ? new CastlingRights(false, false, blackKingSide, blackQueenSide)
                : new CastlingRights(whiteKingSide, whiteQueenSide, false, false);
    }

    /** Quita el enroque asociado a una esquina si una torre sale de ella o es capturada allí. */
    public CastlingRights revokeForCorner(Square square) {
        String name = square.toString();
        return switch (name) {
            case "h1" -> new CastlingRights(false, whiteQueenSide, blackKingSide, blackQueenSide);
            case "a1" -> new CastlingRights(whiteKingSide, false, blackKingSide, blackQueenSide);
            case "h8" -> new CastlingRights(whiteKingSide, whiteQueenSide, false, blackQueenSide);
            case "a8" -> new CastlingRights(whiteKingSide, whiteQueenSide, blackKingSide, false);
            default -> this;
        };
    }

    public String toFen() {
        StringBuilder sb = new StringBuilder();
        if (whiteKingSide) sb.append('K');
        if (whiteQueenSide) sb.append('Q');
        if (blackKingSide) sb.append('k');
        if (blackQueenSide) sb.append('q');
        return sb.isEmpty() ? "-" : sb.toString();
    }

    public static CastlingRights fromFen(String fen) {
        if ("-".equals(fen)) {
            return NONE;
        }
        return new CastlingRights(fen.indexOf('K') >= 0, fen.indexOf('Q') >= 0,
                fen.indexOf('k') >= 0, fen.indexOf('q') >= 0);
    }
}
