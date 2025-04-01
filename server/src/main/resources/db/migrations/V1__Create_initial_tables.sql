-- Initial schema for Word Battle game

-- Players table
CREATE TABLE IF NOT EXISTS players (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_active TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    stats TEXT DEFAULT '{}'
);

-- Game Sessions table
CREATE TABLE IF NOT EXISTS game_sessions (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ended_at TIMESTAMP WITH TIME ZONE,
    players TEXT DEFAULT '[]',
    winner_id UUID,
    game_mode VARCHAR(50) DEFAULT 'CLASSIC',
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_winner FOREIGN KEY (winner_id) REFERENCES players(id)
);

-- Game Rounds table
CREATE TABLE IF NOT EXISTS game_rounds (
    id UUID PRIMARY KEY,
    game_session_id UUID NOT NULL,
    round_number INTEGER NOT NULL,
    letters VARCHAR(20) NOT NULL,
    submissions TEXT DEFAULT '[]',
    round_stats TEXT DEFAULT '{}',
    CONSTRAINT fk_game_session FOREIGN KEY (game_session_id) REFERENCES game_sessions(id) ON DELETE CASCADE
);

-- Words dictionary table
CREATE TABLE IF NOT EXISTS words (
    id SERIAL PRIMARY KEY,
    word VARCHAR(100) NOT NULL UNIQUE,
    is_valid BOOLEAN DEFAULT TRUE,
    length INTEGER NOT NULL
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_player_username ON players(username);
CREATE INDEX IF NOT EXISTS idx_game_sessions_active ON game_sessions(is_active);
CREATE INDEX IF NOT EXISTS idx_game_rounds_session ON game_rounds(game_session_id);
CREATE INDEX IF NOT EXISTS idx_words_length ON words(length);