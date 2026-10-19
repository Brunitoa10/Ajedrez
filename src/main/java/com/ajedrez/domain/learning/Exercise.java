package com.ajedrez.domain.learning;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.Position;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Ejercicio interactivo: posición inicial + secuencia de jugadas en UCI.
 * Los pasos pares los juega el alumno (se aceptan alternativas separadas por '|'),
 * los impares son la respuesta automática del rival.
 * <pre>
 *   new Exercise("fen...", List.of("h1h6", "d6d7", "h6a6"))
 *   new Exercise(Position.STARTING_FEN, List.of("e2e4|d2d4"))
 * </pre>
 */
public record Exercise(String fen, List<String> steps) {

    public Exercise {
        Objects.requireNonNull(fen, "fen");
        steps = List.copyOf(steps);
        if (steps.isEmpty() || steps.size() % 2 == 0) {
            throw new IllegalArgumentException("Un ejercicio debe terminar con una jugada del alumno");
        }
    }

    public Position startPosition() {
        return Position.fromFen(fen);
    }

    public int stepCount() {
        return steps.size();
    }

    public int userMoveCount() {
        return (steps.size() + 1) / 2;
    }

    public boolean isUserStep(int index) {
        return index % 2 == 0;
    }

    /** Jugadas aceptadas en un paso del alumno. La primera es la "principal" (la que se usa como pista). */
    public List<Move> acceptedMoves(int index) {
        return Arrays.stream(steps.get(index).split("\\|")).map(Move::fromUci).toList();
    }

    public Move opponentReply(int index) {
        return Move.fromUci(steps.get(index));
    }
}
