package com.ajedrez.domain.game;

import com.ajedrez.domain.model.PieceColor;

/** Resultado de una partida con su notación PGN. */
public enum GameResult {
    WHITE_WINS("1-0", "Ganan las Blancas"),
    BLACK_WINS("0-1", "Ganan las Negras"),
    DRAW("1/2-1/2", "Tablas"),
    ONGOING("*", "En juego");

    private final String pgn;
    private final String displayName;

    GameResult(String pgn, String displayName) {
        this.pgn = pgn;
        this.displayName = displayName;
    }

    public static GameResult winFor(PieceColor winner) {
        return winner == PieceColor.WHITE ? WHITE_WINS : BLACK_WINS;
    }

    public String pgn() {
        return pgn;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isWinFor(PieceColor color) {
        return this == winFor(color);
    }

    public boolean isLossFor(PieceColor color) {
        return this == winFor(color.opposite());
    }
}
