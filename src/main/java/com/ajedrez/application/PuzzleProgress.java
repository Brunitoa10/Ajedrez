package com.ajedrez.application;

/** Intentos y estado de un problema. */
public record PuzzleProgress(String puzzleId, int attempts, boolean solved) {

    public static PuzzleProgress none(String puzzleId) {
        return new PuzzleProgress(puzzleId, 0, false);
    }
}
