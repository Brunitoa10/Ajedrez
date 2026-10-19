package com.ajedrez.ui.play;

import com.ajedrez.application.Cancellable;
import com.ajedrez.application.GameMode;
import com.ajedrez.application.GameService;
import com.ajedrez.application.MatchSettings;
import com.ajedrez.domain.game.Game;
import com.ajedrez.domain.game.GameListener;
import com.ajedrez.domain.game.GameOutcome;
import com.ajedrez.domain.game.IllegalMoveException;
import com.ajedrez.domain.game.MoveRecord;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Square;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presentador de la partida: coordina turnos humano/IA, deshacer, pistas, abandono y guardado.
 * Observa la partida (Observer) y actualiza la {@link PlayView}.
 * <p>
 * Las respuestas de la IA llegan en otro hilo; se reenvían al hilo de la interfaz con
 * {@code uiExecutor} y se descartan si quedaron viejas (número de "generación"),
 * por ejemplo si el usuario empezó otra partida mientras la IA pensaba.
 */
public final class PlayPresenter implements GameListener {

    private static final Logger LOG = Logger.getLogger(PlayPresenter.class.getName());

    private final GameService service;
    private final Executor uiExecutor;
    private final PlayView view;

    private Game game;
    private MatchSettings settings;
    private Cancellable pendingTask;
    private int generation;
    private boolean thinking;

    public PlayPresenter(GameService service, Executor uiExecutor, PlayView view) {
        this.service = service;
        this.uiExecutor = uiExecutor;
        this.view = view;
    }

    public void start(MatchSettings matchSettings) {
        cancelPending();
        if (game != null) {
            game.removeListener(this);
        }
        settings = matchSettings;
        game = service.newGame();
        game.addListener(this);
        view.setOrientation(settings.mode() == GameMode.VS_COMPUTER ? settings.humanColor() : PieceColor.WHITE);
        view.showPlayers(settings.whiteName(), settings.blackName());
        refresh();
        requestComputerMoveIfNeeded();
    }

    public boolean hasGame() {
        return game != null;
    }

    public List<Move> legalMovesFrom(Square square) {
        return isHumanTurn() ? game.legalMovesFrom(square) : List.of();
    }

    public void onHumanMove(Move move) {
        if (!isHumanTurn()) {
            return;
        }
        try {
            game.play(move);
        } catch (IllegalMoveException e) {
            view.showStatus(e.getMessage());
            return;
        }
        requestComputerMoveIfNeeded();
    }

    /** Contra la IA deshace hasta que vuelve a ser turno del humano (normalmente 2 medias jugadas). */
    public void undo() {
        if (!canUndo()) {
            return;
        }
        cancelPending();
        game.undo();
        if (settings.mode() == GameMode.VS_COMPUTER) {
            while (game.canUndo() && settings.isComputerTurn(game.sideToMove())) {
                game.undo();
            }
        }
        refresh();
        requestComputerMoveIfNeeded();
    }

    public void resign() {
        if (game == null || game.isOver()) {
            return;
        }
        cancelPending();
        PieceColor loser = settings.mode() == GameMode.VS_COMPUTER ? settings.humanColor() : game.sideToMove();
        game.resign(loser);
    }

    public void agreeDraw() {
        if (game != null && !game.isOver() && settings.mode() == GameMode.LOCAL_TWO_PLAYERS) {
            game.agreeDraw();
        }
    }

    public void requestHint() {
        if (!isHumanTurn()) {
            return;
        }
        int requestId = ++generation;
        setThinking(true);
        view.showStatus("Buscando la mejor jugada...");
        pendingTask = service.requestHint(game.currentPosition(),
                move -> uiExecutor.execute(() -> onHint(requestId, move)),
                error -> uiExecutor.execute(() -> onEngineError(requestId, error)));
    }

    public Optional<String> pgn() {
        return game == null ? Optional.empty() : Optional.of(service.toPgn(game, settings));
    }

    public void dispose() {
        cancelPending();
    }

    // ------------------------------------------------------------------ GameListener

    @Override
    public void onMovePlayed(Game source, MoveRecord record) {
        refresh();
    }

    @Override
    public void onMoveUndone(Game source, MoveRecord undone) {
        refresh();
    }

    @Override
    public void onGameOver(Game source, GameOutcome outcome) {
        refresh();
        try {
            service.saveFinishedGame(source, settings);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo guardar la partida", e);
            view.showError("La partida terminó pero no se pudo guardar: " + e.getMessage());
        }
        view.showGameOver(outcome.describe());
    }

    // ------------------------------------------------------------------ internos

    private void requestComputerMoveIfNeeded() {
        if (game.isOver() || !settings.isComputerTurn(game.sideToMove())) {
            return;
        }
        int requestId = ++generation;
        setThinking(true);
        refresh();
        pendingTask = service.requestComputerMove(game.currentPosition(), settings.difficulty(),
                move -> uiExecutor.execute(() -> onComputerMove(requestId, move)),
                error -> uiExecutor.execute(() -> onEngineError(requestId, error)));
    }

    private void onComputerMove(int requestId, Optional<Move> move) {
        if (requestId != generation) {
            return;
        }
        pendingTask = null;
        setThinking(false);
        move.ifPresent(game::play);
        refresh();
    }

    private void onHint(int requestId, Optional<Move> move) {
        if (requestId != generation) {
            return;
        }
        pendingTask = null;
        setThinking(false);
        refresh();
        move.ifPresent(view::showHint);
    }

    private void onEngineError(int requestId, Throwable error) {
        if (requestId != generation) {
            return;
        }
        LOG.log(Level.SEVERE, "Error del motor", error);
        pendingTask = null;
        setThinking(false);
        refresh();
        view.showError("El motor tuvo un error: " + error.getMessage());
    }

    private void cancelPending() {
        generation++;
        if (pendingTask != null) {
            pendingTask.cancel();
            pendingTask = null;
        }
        setThinking(false);
    }

    private void setThinking(boolean value) {
        thinking = value;
        view.setThinking(value);
    }

    private boolean isHumanTurn() {
        return game != null && !game.isOver() && !thinking && !settings.isComputerTurn(game.sideToMove());
    }

    private boolean canUndo() {
        if (game == null || game.isOver() || !game.canUndo()) {
            return false;
        }
        if (settings.mode() == GameMode.VS_COMPUTER) {
            // Hace falta al menos una jugada humana para deshacer.
            return game.history().stream().anyMatch(r -> r.before().sideToMove() == settings.humanColor());
        }
        return true;
    }

    private void refresh() {
        if (game == null) {
            return;
        }
        view.showPosition(game.currentPosition(), game.lastMove().map(MoveRecord::move).orElse(null),
                game.isInCheck());
        view.showMoves(game.sanMoves());
        view.showStatus(statusText());
        view.setInteractive(isHumanTurn());
        view.setControlsState(canUndo(), !game.isOver(),
                !game.isOver() && settings.mode() == GameMode.LOCAL_TWO_PLAYERS);
    }

    private String statusText() {
        if (game.isOver()) {
            return game.outcome().map(GameOutcome::describe).orElse("Partida terminada");
        }
        PieceColor side = game.sideToMove();
        String check = game.isInCheck() ? "  ·  ¡Jaque!" : "";
        if (settings.mode() == GameMode.VS_COMPUTER) {
            return (settings.isComputerTurn(side) ? "La computadora está pensando..." : "Tu turno") + check;
        }
        return "Juegan las " + side.displayName() + check;
    }
}
