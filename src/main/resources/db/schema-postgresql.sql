CREATE TABLE IF NOT EXISTS games (
    id           BIGSERIAL PRIMARY KEY,
    played_at    VARCHAR(32)  NOT NULL,
    mode         VARCHAR(30)  NOT NULL,
    difficulty   VARCHAR(30),
    human_color  VARCHAR(10),
    white_name   VARCHAR(80)  NOT NULL,
    black_name   VARCHAR(80)  NOT NULL,
    result       VARCHAR(10)  NOT NULL,
    termination  VARCHAR(40)  NOT NULL,
    start_fen    VARCHAR(100) NOT NULL,
    moves_uci    TEXT         NOT NULL,
    pgn          TEXT         NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_games_played_at ON games (played_at);

CREATE TABLE IF NOT EXISTS lesson_progress (
    lesson_id    VARCHAR(60) PRIMARY KEY,
    completed_at VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS puzzle_progress (
    puzzle_id       VARCHAR(60) PRIMARY KEY,
    attempts        INTEGER     NOT NULL DEFAULT 0,
    solved          BOOLEAN     NOT NULL DEFAULT FALSE,
    last_attempt_at VARCHAR(32) NOT NULL
);
