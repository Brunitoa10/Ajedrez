package com.ajedrez.domain.game;

import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.model.Square;
import com.ajedrez.domain.notation.SanFormatter;
import com.ajedrez.domain.rules.ChessRules;
import com.ajedrez.domain.rules.PositionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Agregado principal: una partida en curso. Valida jugadas, mantiene el historial,
 * detecta el final y notifica a los observadores.
 * <p>
 * Deshacer funciona guardando la posición inmutable de cada turno (patrón Memento):
 * volver atrás es simplemente descartar el último snapshot.
 */
public final class Game {

    private static final int REPETITIONS_FOR_DRAW = 3;

    private final ChessRules rules;
    private final SanFormatter sanFormatter;
    private final Position startPosition;
    private final List<MoveRecord> history = new ArrayList<>();
    private final List<GameListener> listeners = new CopyOnWriteArrayList<>();
    private GameOutcome outcome;

    public Game(ChessRules rules, Position startPosition) {
        this.rules = Objects.requireNonNull(rules, "rules");
        this.sanFormatter = new SanFormatter(rules);
        this.startPosition = Objects.requireNonNull(startPosition, "startPosition");
    }

    public static Game standard(ChessRules rules) {
        return new Game(rules, Position.initial());
    }

    public Position startPosition() {
        return startPosition;
    }

    public Position currentPosition() {
        return history.isEmpty() ? startPosition : history.get(history.size() - 1).after();
    }

    public PieceColor sideToMove() {
        return currentPosition().sideToMove();
    }

    public List<MoveRecord> history() {
        return Collections.unmodifiableList(history);
    }

    public List<String> sanMoves() {
        return history.stream().map(MoveRecord::san).toList();
    }

    public List<String> uciMoves() {
        return history.stream().map(r -> r.move().toUci()).toList();
    }

    public Optional<MoveRecord> lastMove() {
        return history.isEmpty() ? Optional.empty() : Optional.of(history.get(history.size() - 1));
    }

    public Optional<GameOutcome> outcome() {
        return Optional.ofNullable(outcome);
    }

    public boolean isOver() {
        return outcome != null;
    }

    public boolean isInCheck() {
        return rules.isInCheck(currentPosition());
    }

    public List<Move> legalMoves() {
        return isOver() ? List.of() : rules.legalMoves(currentPosition());
    }

    public List<Move> legalMovesFrom(Square from) {
        return isOver() ? List.of() : rules.legalMovesFrom(currentPosition(), from);
    }

    public MoveRecord play(Move move) {
        if (isOver()) {
            throw new IllegalMoveException("La partida ya terminó");
        }
        Position before = currentPosition();
        if (!rules.isLegal(before, move)) {
            throw new IllegalMoveException("Jugada ilegal: " + move.toUci());
        }
        String san = sanFormatter.format(before, move);
        MoveRecord record = new MoveRecord(move, san, before, before.apply(move));
        history.add(record);
        listeners.forEach(l -> l.onMovePlayed(this, record));
        detectEnd();
        return record;
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    /** Deshace la última jugada (medio movimiento). Reabre la partida si había terminado. */
    public void undo() {
        if (!canUndo()) {
            return;
        }
        MoveRecord undone = history.remove(history.size() - 1);
        outcome = null;
        listeners.forEach(l -> l.onMoveUndone(this, undone));
    }

    public void resign(PieceColor loser) {
        finish(new GameOutcome(GameResult.winFor(loser.opposite()), Termination.RESIGNATION));
    }

    public void agreeDraw() {
        finish(new GameOutcome(GameResult.DRAW, Termination.AGREEMENT));
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    private void detectEnd() {
        Position current = currentPosition();
        PositionStatus status = rules.status(current);
        if (status == PositionStatus.CHECKMATE) {
            finish(new GameOutcome(GameResult.winFor(current.sideToMove().opposite()), Termination.CHECKMATE));
        } else if (status.isTerminal()) {
            finish(new GameOutcome(GameResult.DRAW, Termination.fromStatus(status)));
        } else if (repetitionCount(current) >= REPETITIONS_FOR_DRAW) {
            finish(new GameOutcome(GameResult.DRAW, Termination.THREEFOLD_REPETITION));
        }
    }

    private long repetitionCount(Position position) {
        String key = position.repetitionKey();
        long count = startPosition.repetitionKey().equals(key) ? 1 : 0;
        return count + history.stream().filter(r -> r.after().repetitionKey().equals(key)).count();
    }

    private void finish(GameOutcome gameOutcome) {
        if (isOver()) {
            return;
        }
        this.outcome = gameOutcome;
        listeners.forEach(l -> l.onGameOver(this, gameOutcome));
    }
}
