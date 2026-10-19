package com.ajedrez.ui.play;

import com.ajedrez.ai.Difficulty;
import com.ajedrez.ai.EngineFactory;
import com.ajedrez.ai.PieceSquareEvaluator;
import com.ajedrez.application.GameRecord;
import com.ajedrez.application.GameService;
import com.ajedrez.application.MatchSettings;
import com.ajedrez.application.port.GameRepository;
import com.ajedrez.domain.game.GameResult;
import com.ajedrez.domain.game.Termination;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.model.Position;
import com.ajedrez.domain.rules.ChessRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El presentador se prueba sin Swing gracias a MVP: una vista falsa registra lo que se le pide
 * mostrar y una cola simula el hilo de la interfaz.
 */
public class PlayPresenterTest {

    private final BlockingQueue<Runnable> uiQueue = new LinkedBlockingQueue<>();
    private final FakeView view = new FakeView();
    private final InMemoryGames repository = new InMemoryGames();
    private PlayPresenter presenter;

    @BeforeEach
    public void setUp() {
        ChessRules rules = ChessRules.standard();
        GameService service = new GameService(rules, new EngineFactory(rules, new PieceSquareEvaluator()), repository);
        presenter = new PlayPresenter(service, uiQueue::add, view);
    }

    private void runNextUiTask() throws InterruptedException {
        Runnable task = uiQueue.poll(15, TimeUnit.SECONDS);
        assertTrue(task != null, "La IA no respondió a tiempo");
        task.run();
    }

    @Test
    public void computerRepliesAfterHumanMove() throws InterruptedException {
        presenter.start(MatchSettings.vsComputer(PieceColor.WHITE, Difficulty.BEGINNER, "Ana"));
        assertTrue(view.interactive);

        presenter.onHumanMove(Move.fromUci("e2e4"));
        assertFalse(view.interactive, "No se puede mover mientras la IA piensa");

        runNextUiTask();
        assertEquals(PieceColor.WHITE, view.position.sideToMove());
        assertEquals(2, view.moves.size());
        assertTrue(view.interactive);
    }

    @Test
    public void computerMovesFirstWhenHumanPlaysBlack() throws InterruptedException {
        presenter.start(MatchSettings.vsComputer(PieceColor.BLACK, Difficulty.BEGINNER, "Ana"));
        runNextUiTask();
        assertEquals(1, view.moves.size());
        assertEquals(PieceColor.BLACK, view.bottomColor);
    }

    @Test
    public void undoAgainstComputerReturnsToHumanTurn() throws InterruptedException {
        presenter.start(MatchSettings.vsComputer(PieceColor.WHITE, Difficulty.BEGINNER, "Ana"));
        presenter.onHumanMove(Move.fromUci("e2e4"));
        runNextUiTask();

        presenter.undo();
        assertEquals(0, view.moves.size());
        assertEquals(Position.initial(), view.position);
        assertTrue(view.interactive);
    }

    @Test
    public void resigningSavesTheGame() {
        presenter.start(MatchSettings.vsComputer(PieceColor.WHITE, Difficulty.HARD, "Ana"));
        presenter.resign();
        assertEquals(1, repository.saved.size());
        assertEquals(GameResult.BLACK_WINS, repository.saved.get(0).result());
        assertEquals(Termination.RESIGNATION, repository.saved.get(0).termination());
        assertTrue(view.gameOverMessage.contains("abandono"));
    }

    @Test
    public void twoPlayersCheckmateIsSaved() {
        presenter.start(MatchSettings.twoPlayers());
        for (String uci : List.of("f2f3", "e7e5", "g2g4", "d8h4")) {
            presenter.onHumanMove(Move.fromUci(uci));
        }
        assertEquals(1, repository.saved.size());
        assertEquals(GameResult.BLACK_WINS, repository.saved.get(0).result());
        assertFalse(view.interactive);
        assertTrue(uiQueue.isEmpty(), "En partidas de dos jugadores no interviene la IA");
    }

    // ------------------------------------------------------------------ dobles de prueba

    private static final class FakeView implements PlayView {
        Position position;
        List<String> moves = List.of();
        boolean interactive;
        PieceColor bottomColor;
        String gameOverMessage = "";

        @Override
        public void setOrientation(PieceColor color) {
            bottomColor = color;
        }

        @Override
        public void showPlayers(String whiteName, String blackName) {
        }

        @Override
        public void showPosition(Position newPosition, Move lastMove, boolean inCheck) {
            position = newPosition;
        }

        @Override
        public void showMoves(List<String> sanMoves) {
            moves = sanMoves;
        }

        @Override
        public void showStatus(String status) {
        }

        @Override
        public void setThinking(boolean thinking) {
        }

        @Override
        public void setInteractive(boolean value) {
            interactive = value;
        }

        @Override
        public void setControlsState(boolean canUndo, boolean canResign, boolean canOfferDraw) {
        }

        @Override
        public void showHint(Move move) {
        }

        @Override
        public void showGameOver(String message) {
            gameOverMessage = message;
        }

        @Override
        public void showError(String message) {
            throw new AssertionError(message);
        }
    }

    private static final class InMemoryGames implements GameRepository {
        final List<GameRecord> saved = new ArrayList<>();

        @Override
        public GameRecord save(GameRecord record) {
            GameRecord withId = record.withId(saved.size() + 1);
            saved.add(withId);
            return withId;
        }

        @Override
        public List<GameRecord> findAll() {
            return List.copyOf(saved);
        }

        @Override
        public Optional<GameRecord> findById(long id) {
            return saved.stream().filter(g -> g.id() == id).findFirst();
        }

        @Override
        public void delete(long id) {
            saved.removeIf(g -> g.id() == id);
        }
    }
}
