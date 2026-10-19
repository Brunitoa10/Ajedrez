package com.ajedrez.application;

import com.ajedrez.ai.Difficulty;
import com.ajedrez.application.port.GameRepository;
import com.ajedrez.application.port.ProgressRepository;
import com.ajedrez.domain.learning.LearningCatalog;

import java.util.Arrays;
import java.util.List;

/** Estadísticas del jugador contra la IA y progreso de aprendizaje. */
public final class StatisticsService {

    private final GameRepository games;
    private final ProgressRepository progress;
    private final LearningCatalog catalog;

    public StatisticsService(GameRepository games, ProgressRepository progress, LearningCatalog catalog) {
        this.games = games;
        this.progress = progress;
        this.catalog = catalog;
    }

    public Summary summary() {
        List<GameRecord> all = games.findAll();
        List<DifficultyStats> perLevel = Arrays.stream(Difficulty.values())
                .map(level -> statsFor(level, all))
                .toList();
        long twoPlayer = all.stream().filter(g -> g.mode() == GameMode.LOCAL_TWO_PLAYERS).count();
        long puzzlesSolved = progress.puzzleProgress().values().stream().filter(PuzzleProgress::solved).count();
        return new Summary(perLevel, (int) twoPlayer,
                progress.completedLessons().size(), catalog.lessons().size(),
                (int) puzzlesSolved, catalog.puzzles().size());
    }

    private DifficultyStats statsFor(Difficulty level, List<GameRecord> all) {
        int wins = 0;
        int draws = 0;
        int losses = 0;
        for (GameRecord game : all) {
            if (game.mode() != GameMode.VS_COMPUTER || game.difficulty() != level) {
                continue;
            }
            if (game.result().isWinFor(game.humanColor())) {
                wins++;
            } else if (game.result().isLossFor(game.humanColor())) {
                losses++;
            } else {
                draws++;
            }
        }
        return new DifficultyStats(level, wins, draws, losses);
    }

    public record DifficultyStats(Difficulty difficulty, int wins, int draws, int losses) {
        public int played() {
            return wins + draws + losses;
        }

        /** Puntuación estilo torneo: victoria 1, tablas 0.5. */
        public double scorePercent() {
            return played() == 0 ? 0 : 100.0 * (wins + draws * 0.5) / played();
        }
    }

    public record Summary(List<DifficultyStats> perDifficulty, int twoPlayerGames,
                          int lessonsCompleted, int lessonsTotal, int puzzlesSolved, int puzzlesTotal) {
        public int gamesVsComputer() {
            return perDifficulty.stream().mapToInt(DifficultyStats::played).sum();
        }
    }
}
