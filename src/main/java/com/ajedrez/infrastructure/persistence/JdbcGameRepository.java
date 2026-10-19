package com.ajedrez.infrastructure.persistence;

import com.ajedrez.ai.Difficulty;
import com.ajedrez.application.GameMode;
import com.ajedrez.application.GameRecord;
import com.ajedrez.application.port.GameRepository;
import com.ajedrez.domain.game.GameResult;
import com.ajedrez.domain.game.Termination;
import com.ajedrez.domain.model.PieceColor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Repositorio de partidas con SQL estándar compatible con SQLite y PostgreSQL. */
public final class JdbcGameRepository implements GameRepository {

    private static final String COLUMNS = "id, played_at, mode, difficulty, human_color, white_name, black_name, "
            + "result, termination, start_fen, moves_uci, pgn";

    private final JdbcTemplate jdbc;

    public JdbcGameRepository(ConnectionFactory connections) {
        this.jdbc = new JdbcTemplate(connections);
    }

    @Override
    public GameRecord save(GameRecord record) {
        String sql = "INSERT INTO games (played_at, mode, difficulty, human_color, white_name, black_name, "
                + "result, termination, start_fen, moves_uci, pgn) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        long id = jdbc.insert(sql, st -> {
            st.setString(1, record.playedAt().toString());
            st.setString(2, record.mode().name());
            st.setString(3, record.difficulty() == null ? null : record.difficulty().name());
            st.setString(4, record.humanColor() == null ? null : record.humanColor().name());
            st.setString(5, record.whiteName());
            st.setString(6, record.blackName());
            st.setString(7, record.result().name());
            st.setString(8, record.termination().name());
            st.setString(9, record.startFen());
            st.setString(10, String.join(" ", record.uciMoves()));
            st.setString(11, record.pgn());
        });
        return record.withId(id);
    }

    @Override
    public List<GameRecord> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM games ORDER BY played_at DESC, id DESC",
                st -> { }, JdbcGameRepository::map);
    }

    @Override
    public Optional<GameRecord> findById(long id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM games WHERE id = ?",
                st -> st.setLong(1, id), JdbcGameRepository::map).stream().findFirst();
    }

    @Override
    public void delete(long id) {
        jdbc.update("DELETE FROM games WHERE id = ?", st -> st.setLong(1, id));
    }

    private static GameRecord map(ResultSet row) throws SQLException {
        String difficulty = row.getString("difficulty");
        String humanColor = row.getString("human_color");
        String moves = row.getString("moves_uci");
        return new GameRecord(
                row.getLong("id"),
                LocalDateTime.parse(row.getString("played_at")),
                GameMode.valueOf(row.getString("mode")),
                difficulty == null ? null : Difficulty.valueOf(difficulty),
                humanColor == null ? null : PieceColor.valueOf(humanColor),
                row.getString("white_name"),
                row.getString("black_name"),
                GameResult.valueOf(row.getString("result")),
                Termination.valueOf(row.getString("termination")),
                row.getString("start_fen"),
                moves == null || moves.isBlank() ? List.of() : Arrays.asList(moves.trim().split("\\s+")),
                row.getString("pgn"));
    }
}
