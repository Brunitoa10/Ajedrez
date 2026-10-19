package com.ajedrez.domain.learning;

import com.ajedrez.domain.model.Move;

import java.util.Optional;

/** Respuesta del ejercicio a un intento del alumno. */
public record ExerciseFeedback(Kind kind, Move opponentReply, String message) {

    public enum Kind {
        /** Jugada correcta, el ejercicio sigue (el rival ya respondió). */
        CORRECT,
        /** Jugada correcta y ejercicio terminado. */
        SOLVED,
        /** Jugada legal pero no es la solución. */
        WRONG,
        /** Jugada ilegal en la posición. */
        ILLEGAL
    }

    public Optional<Move> reply() {
        return Optional.ofNullable(opponentReply);
    }

    public boolean isPositive() {
        return kind == Kind.CORRECT || kind == Kind.SOLVED;
    }

    public static ExerciseFeedback correct(Move reply) {
        return new ExerciseFeedback(Kind.CORRECT, reply, "¡Bien! Seguí así.");
    }

    public static ExerciseFeedback solved() {
        return new ExerciseFeedback(Kind.SOLVED, null, "¡Excelente! Ejercicio resuelto.");
    }

    public static ExerciseFeedback wrong() {
        return new ExerciseFeedback(Kind.WRONG, null, "No es la mejor jugada. Probá de nuevo.");
    }

    public static ExerciseFeedback illegal() {
        return new ExerciseFeedback(Kind.ILLEGAL, null, "Esa jugada no es legal.");
    }
}
