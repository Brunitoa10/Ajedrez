package com.ajedrez.infrastructure.persistence;

import com.ajedrez.application.PuzzleProgress;
import com.ajedrez.application.port.ProgressRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Progreso de aprendizaje. Usa {@code INSERT ... ON CONFLICT ... DO UPDATE} ("upsert"),
 * sintaxis soportada tanto por SQLite (3.24+) como por PostgreSQL (9.5+).
 */
public final class JdbcProgressRepository implements ProgressRepository {

    private final JdbcTemplate jdbc;

    public JdbcProgressRepository(ConnectionFactory connections) {
        this.jdbc = new JdbcTemplate(connections);
    }

    @Override
    public void markLessonCompleted(String lessonId) {
        jdbc.update("INSERT INTO lesson_progress (lesson_id, completed_at) VALUES (?, ?) "
                        + "ON CONFLICT (lesson_id) DO NOTHING",
                st -> {
                    st.setString(1, lessonId);
                    st.setString(2, LocalDateTime.now().toString());
                });
    }

    @Override
    public Set<String> completedLessons() {
        return new HashSet<>(jdbc.query("SELECT lesson_id FROM lesson_progress", st -> { },
                row -> row.getString("lesson_id")));
    }

    @Override
    public void recordPuzzleAttempt(String puzzleId, boolean solved) {
        jdbc.update("INSERT INTO puzzle_progress (puzzle_id, attempts, solved, last_attempt_at) VALUES (?, 1, ?, ?) "
                        + "ON CONFLICT (puzzle_id) DO UPDATE SET "
                        + "attempts = puzzle_progress.attempts + 1, "
                        + "solved = (puzzle_progress.solved OR excluded.solved), "
                        + "last_attempt_at = excluded.last_attempt_at",
                st -> {
                    st.setString(1, puzzleId);
                    st.setBoolean(2, solved);
                    st.setString(3, LocalDateTime.now().toString());
                });
    }

    @Override
    public Map<String, PuzzleProgress> puzzleProgress() {
        return jdbc.query("SELECT puzzle_id, attempts, solved FROM puzzle_progress", st -> { },
                        row -> new PuzzleProgress(row.getString("puzzle_id"), row.getInt("attempts"),
                                row.getBoolean("solved")))
                .stream()
                .collect(Collectors.toMap(PuzzleProgress::puzzleId, Function.identity()));
    }
}
