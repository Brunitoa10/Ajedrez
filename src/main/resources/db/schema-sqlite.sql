CREATE TABLE IF NOT EXISTS games (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    played_at    TEXT        NOT NULL,
    mode         TEXT        NOT NULL,
    difficulty   TEXT,
    human_color  TEXT,
    white_name   TEXT        NOT NULL,
    black_name   TEXT        NOT NULL,
    result       TEXT        NOT NULL,
    termination  TEXT        NOT NULL,
    start_fen    TEXT        NOT NULL,
    moves_uci    TEXT        NOT NULL,
    pgn          TEXT        NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_games_played_at ON games (played_at);

CREATE TABLE IF NOT EXISTS lesson_progress (
    lesson_id    TEXT PRIMARY KEY,
    completed_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS puzzle_progress (
    puzzle_id       TEXT PRIMARY KEY,
    attempts        INTEGER NOT NULL DEFAULT 0,
    solved          BOOLEAN NOT NULL DEFAULT 0,
    last_attempt_at TEXT    NOT NULL
);
