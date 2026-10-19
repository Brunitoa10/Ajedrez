package com.ajedrez.application;

import com.ajedrez.ai.Difficulty;
import com.ajedrez.domain.game.GameResult;
import com.ajedrez.domain.game.Termination;
import com.ajedrez.domain.model.PieceColor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Partida terminada tal como se persiste. {@code id} es nulo hasta guardarse.
 * {@code difficulty} y {@code humanColor} son nulos en partidas de dos jugadores.
 */
public record GameRecord(Long id, LocalDateTime playedAt, GameMode mode, Difficulty difficulty,
                         PieceColor humanColor, String whiteName, String blackName,
                         GameResult result, Termination termination, String startFen,
                         List<String> uciMoves, String pgn) {

    public GameRecord {
        uciMoves = List.copyOf(uciMoves);
    }

    public GameRecord withId(long newId) {
        return new GameRecord(newId, playedAt, mode, difficulty, humanColor, whiteName, blackName,
                result, termination, startFen, uciMoves, pgn);
    }

    public Optional<Difficulty> difficultyOpt() {
        return Optional.ofNullable(difficulty);
    }

    public int plyCount() {
        return uciMoves.size();
    }

    public int fullMoveCount() {
        return (uciMoves.size() + 1) / 2;
    }
}
