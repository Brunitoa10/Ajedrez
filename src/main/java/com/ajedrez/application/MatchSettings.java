package com.ajedrez.application;

import com.ajedrez.ai.Difficulty;
import com.ajedrez.domain.model.PieceColor;

import java.util.Objects;

/**
 * Configuración elegida al iniciar una partida.
 *
 * @param humanColor color del jugador humano (solo relevante contra la computadora)
 */
public record MatchSettings(GameMode mode, PieceColor humanColor, Difficulty difficulty, String playerName) {

    private static final String COMPUTER_NAME_PREFIX = "Computadora";

    public MatchSettings {
        Objects.requireNonNull(mode, "mode");
        humanColor = humanColor == null ? PieceColor.WHITE : humanColor;
        difficulty = difficulty == null ? Difficulty.MEDIUM : difficulty;
        playerName = playerName == null || playerName.isBlank() ? "Jugador" : playerName.trim();
    }

    public static MatchSettings vsComputer(PieceColor humanColor, Difficulty difficulty, String playerName) {
        return new MatchSettings(GameMode.VS_COMPUTER, humanColor, difficulty, playerName);
    }

    public static MatchSettings twoPlayers() {
        return new MatchSettings(GameMode.LOCAL_TWO_PLAYERS, PieceColor.WHITE, null, null);
    }

    public boolean isComputerTurn(PieceColor sideToMove) {
        return mode == GameMode.VS_COMPUTER && sideToMove != humanColor;
    }

    public String whiteName() {
        return nameFor(PieceColor.WHITE);
    }

    public String blackName() {
        return nameFor(PieceColor.BLACK);
    }

    private String nameFor(PieceColor color) {
        if (mode == GameMode.LOCAL_TWO_PLAYERS) {
            return color.displayName();
        }
        return color == humanColor ? playerName : COMPUTER_NAME_PREFIX + " (" + difficulty.displayName() + ")";
    }
}
