package com.ajedrez.infrastructure.persistence;

import com.ajedrez.ai.Difficulty;
import com.ajedrez.application.GameService;
import com.ajedrez.application.GameRecord;
import com.ajedrez.application.MatchSettings;
import com.ajedrez.application.StatisticsService;
import com.ajedrez.ai.EngineFactory;
import com.ajedrez.ai.PieceSquareEvaluator;
import com.ajedrez.domain.game.Game;
import com.ajedrez.domain.game.GameResult;
import com.ajedrez.domain.learning.BuiltInLearningCatalog;
import com.ajedrez.domain.model.Move;
import com.ajedrez.domain.model.PieceColor;
import com.ajedrez.domain.rules.ChessRules;
import com.ajedrez.infrastructure.config.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcRepositoryTest {

    @TempDir
    Path tempDir;

    private JdbcGameRepository games;
    private JdbcProgressRepository progress;

    @BeforeEach
    void setUp() {
        ConnectionFactory connections = ConnectionFactories.create(DatabaseConfig.sqlite(tempDir.resolve("data/test.db")));
        new SchemaInitializer(connections).initialize();
        new SchemaInitializer(connections).initialize(); // idempotente
        games = new JdbcGameRepository(connections);
        progress = new JdbcProgressRepository(connections);
    }

    @Test
    void savesAndReadsFinishedGameWithStatistics() {
        ChessRules rules = ChessRules.standard();
        try (GameService service = new GameService(rules, new EngineFactory(rules, new PieceSquareEvaluator()), games)) {
            Game game = service.newGame();
            for (String uci : List.of("f2f3", "e7e5", "g2g4", "d8h4")) {
                game.play(Move.fromUci(uci));
            }
            MatchSettings settings = MatchSettings.vsComputer(PieceColor.BLACK, Difficulty.HARD, "Bruno");
            GameRecord saved = service.saveFinishedGame(game, settings);

            GameRecord loaded = games.findById(saved.id()).orElseThrow();
            assertEquals(GameResult.BLACK_WINS, loaded.result());
            assertEquals(List.of("f2f3", "e7e5", "g2g4", "d8h4"), loaded.uciMoves());
            assertTrue(loaded.pgn().contains("2. g4 Qh4# 0-1"), loaded.pgn());

            var summary = new StatisticsService(games, progress, new BuiltInLearningCatalog()).summary();
            StatisticsService.DifficultyStats hard = summary.perDifficulty().stream()
                    .filter(s -> s.difficulty() == Difficulty.HARD).findFirst().orElseThrow();
            assertEquals(1, hard.wins());

            games.delete(saved.id());
            assertTrue(games.findAll().isEmpty());
        }
    }

    @Test
    void upsertsProgress() {
        progress.markLessonCompleted("pawn");
        progress.markLessonCompleted("pawn");
        progress.recordPuzzleAttempt("p1", false);
        progress.recordPuzzleAttempt("p1", true);
        progress.recordPuzzleAttempt("p1", false);

        assertEquals(1, progress.completedLessons().size());
        var p1 = progress.puzzleProgress().get("p1");
        assertEquals(3, p1.attempts());
        assertTrue(p1.solved());
    }
}
