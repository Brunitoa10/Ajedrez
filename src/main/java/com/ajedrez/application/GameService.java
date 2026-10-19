package com.ajedrez.application;

import com.ajedrez.ai.ChessEngine;
import com.ajedrez.ai.Difficulty;
import com.ajedrez.ai.EngineFactory;
import com.ajedrez.application.port.GameRepository;
import com.ajedrez.domain.game.Game;
import com.ajedrez.domain.game.GameOutcome;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.notation.PgnWriter;
import com.ajedrez.domain.rules.ChessRules;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Caso de uso "jugar una partida" (Facade de la capa de aplicación): crea partidas,
 * pide jugadas a la IA en segundo plano, genera PGN y guarda las partidas terminadas.
 * No conoce JavaFX: los callbacks se ejecutan en el hilo de la IA y la UI decide cómo volver al hilo gráfico.
 */
public final class GameService implements AutoCloseable {

    private static final Difficulty HINT_LEVEL = Difficulty.HARD;
    private static final DateTimeFormatter PGN_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final ChessRules rules;
    private final EngineFactory engineFactory;
    private final GameRepository repository;
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ia-ajedrez");
        thread.setDaemon(true);
        return thread;
    });

    public GameService(ChessRules rules, EngineFactory engineFactory, GameRepository repository) {
        this.rules = rules;
        this.engineFactory = engineFactory;
        this.repository = repository;
    }

    public ChessRules rules() {
        return rules;
    }

    public Game newGame() {
        return Game.standard(rules);
    }

    /** Pide a la IA una jugada para la posición. La posición es inmutable, así que es seguro pasarla a otro hilo. */
    public Cancellable requestComputerMove(Position position, Difficulty difficulty,
                                           Consumer<Optional<Move>> onResult, Consumer<Throwable> onError) {
        return runEngine(engineFactory.create(difficulty), position, onResult, onError);
    }

    /** Sugerencia de jugada para el humano (usa un nivel fuerte). */
    public Cancellable requestHint(Position position, Consumer<Optional<Move>> onResult, Consumer<Throwable> onError) {
        return runEngine(engineFactory.create(HINT_LEVEL), position, onResult, onError);
    }

    private Cancellable runEngine(ChessEngine engine, Position position,
                                  Consumer<Optional<Move>> onResult, Consumer<Throwable> onError) {
        Future<?> future = aiExecutor.submit(() -> {
            try {
                Optional<Move> move = engine.chooseMove(position);
                if (!Thread.currentThread().isInterrupted()) {
                    onResult.accept(move);
                }
            } catch (RuntimeException e) {
                onError.accept(e);
            }
        });
        return () -> future.cancel(true);
    }

    /** Guarda una partida terminada y devuelve el registro con su id. */
    public GameRecord saveFinishedGame(Game game, MatchSettings settings) {
        GameOutcome outcome = game.outcome()
                .orElseThrow(() -> new IllegalStateException("Solo se guardan partidas terminadas"));
        boolean vsComputer = settings.mode() == GameMode.VS_COMPUTER;
        GameRecord record = new GameRecord(null, LocalDateTime.now(), settings.mode(),
                vsComputer ? settings.difficulty() : null,
                vsComputer ? settings.humanColor() : null,
                settings.whiteName(), settings.blackName(),
                outcome.result(), outcome.termination(),
                game.startPosition().toFen(), game.uciMoves(), toPgn(game, settings));
        return repository.save(record);
    }

    public String toPgn(Game game, MatchSettings settings) {
        String result = game.outcome().map(o -> o.result().pgn()).orElse("*");
        Position start = game.startPosition();
        PgnWriter writer = new PgnWriter()
                .header("Event", settings.mode().displayName())
                .header("Site", "Ajedrez (aplicación de escritorio)")
                .header("Date", LocalDateTime.now().format(PGN_DATE))
                .header("White", settings.whiteName())
                .header("Black", settings.blackName())
                .header("Result", result);
        if (!Position.STARTING_FEN.equals(start.toFen())) {
            writer.header("SetUp", "1").header("FEN", start.toFen());
        }
        game.outcome().ifPresent(o -> writer.header("Termination", o.termination().description()));
        return writer.moves(game.sanMoves())
                .startingAt(start.fullmoveNumber(), start.sideToMove() == PieceColor.BLACK)
                .result(result)
                .write();
    }

    @Override
    public void close() {
        aiExecutor.shutdownNow();
    }
}
