CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `nickname` VARCHAR(100) DEFAULT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `preference_tags` VARCHAR(500) DEFAULT NULL,
    `age` INT DEFAULT NULL,
    `gender` VARCHAR(10) DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `song` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `song_name` VARCHAR(200) NOT NULL,
    `singer` VARCHAR(100) NOT NULL,
    `album` VARCHAR(200) DEFAULT NULL,
    `genre` VARCHAR(50) DEFAULT NULL,
    `scene_tags` VARCHAR(500) DEFAULT NULL,
    `external_id` VARCHAR(100) DEFAULT NULL,
    `audio_url` VARCHAR(500) DEFAULT NULL,
    `cover_url` VARCHAR(500) DEFAULT NULL,
    `popularity` INT DEFAULT 0,
    `duration` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_song_name ON song(song_name);
CREATE INDEX IF NOT EXISTS idx_singer ON song(singer);
CREATE INDEX IF NOT EXISTS idx_genre ON song(genre);

CREATE TABLE IF NOT EXISTS `user_playlist` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `playlist_name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `song_ids` TEXT DEFAULT NULL,
    `play_count` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_id ON user_playlist(user_id);

CREATE TABLE IF NOT EXISTS `chat_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(100) NOT NULL,
    `user_id` BIGINT DEFAULT NULL,
    `user_input` TEXT NOT NULL,
    `ai_response` TEXT NOT NULL,
    `intent_type` VARCHAR(50) DEFAULT NULL,
    `preference_tags` VARCHAR(500) DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_session_id ON chat_log(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_user_id ON chat_log(user_id);