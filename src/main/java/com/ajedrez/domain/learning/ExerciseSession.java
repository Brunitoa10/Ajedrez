package com.ajedrez.domain.learning;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;
import com.ajedrez.domain.rules.ChessRules;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Estado de un alumno resolviendo un {@link Exercise}. Valida cada intento, juega la respuesta
 * del rival y lleva la cuenta de errores. Cualquier jaque mate se acepta como solución
 * (un mate siempre es la mejor jugada, aunque no sea la que figura en el ejercicio).
 */
public final class ExerciseSession {

    private final ChessRules rules;
    private final Exercise exercise;
    private Position position;
    private int stepIndex;
    private int mistakes;
    private boolean solved;

    public ExerciseSession(ChessRules rules, Exercise exercise) {
        this.rules = Objects.requireNonNull(rules, "rules");
        this.exercise = Objects.requireNonNull(exercise, "exercise");
        reset();
    }

    public void reset() {
        position = exercise.startPosition();
        stepIndex = 0;
        mistakes = 0;
        solved = false;
    }

    public Position position() {
        return position;
    }

    public boolean isSolved() {
        return solved;
    }

    public int mistakes() {
        return mistakes;
    }

    public Exercise exercise() {
        return exercise;
    }

    public List<Move> legalMovesFrom(Square from) {
        return solved ? List.of() : rules.legalMovesFrom(position, from);
    }

    public boolean isInCheck() {
        return rules.isInCheck(position);
    }

    /** Bando que juega el alumno (el que mueve en la posición inicial). */
    public PieceColor studentColor() {
        return exercise.startPosition().sideToMove();
    }

    /** Jugada principal esperada en el paso actual, para mostrar como pista. */
    public Optional<Move> hint() {
        return solved ? Optional.empty() : Optional.of(exercise.acceptedMoves(stepIndex).get(0));
    }

    public ExerciseFeedback attempt(Move move) {
        if (solved) {
            return ExerciseFeedback.solved();
        }
        if (!rules.isLegal(position, move)) {
            return ExerciseFeedback.illegal();
        }
        Position after = position.apply(move);
        if (rules.isCheckmate(after)) {
            position = after;
            solved = true;
            return ExerciseFeedback.solved();
        }
        if (!isAccepted(move)) {
            mistakes++;
            return ExerciseFeedback.wrong();
        }
        position = after;
        stepIndex++;
        if (stepIndex >= exercise.stepCount()) {
            solved = true;
            return ExerciseFeedback.solved();
        }
        Move reply = exercise.opponentReply(stepIndex);
        position = position.apply(reply);
        stepIndex++;
        return ExerciseFeedback.correct(reply);
    }

    private boolean isAccepted(Move move) {
        return exercise.acceptedMoves(stepIndex).contains(move);
    }
}
