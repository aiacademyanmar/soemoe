-- Buddha Psychology Telegram Ecosystem V1.0
-- Additive extension only. Apply to a staging database after backup.
-- Does not alter or reference existing Buddha Psychology tables.

CREATE TABLE IF NOT EXISTS bp_tg_users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    telegram_user_id BIGINT UNSIGNED NOT NULL,
    username VARCHAR(64) NULL,
    first_name VARCHAR(128) NOT NULL DEFAULT '',
    last_name VARCHAR(128) NULL,
    language_code VARCHAR(16) NULL,
    photo_url VARCHAR(512) NULL,
    telegram_premium TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_bp_tg_users_telegram_id (telegram_user_id),
    KEY idx_bp_tg_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bp_tg_sessions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    token_hash CHAR(64) NOT NULL,
    telegram_user_fk BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_bp_tg_sessions_token_hash (token_hash),
    KEY idx_bp_tg_sessions_user (telegram_user_fk),
    KEY idx_bp_tg_sessions_expiry (expires_at),
    CONSTRAINT fk_bp_tg_sessions_user FOREIGN KEY (telegram_user_fk)
        REFERENCES bp_tg_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bp_tg_updates (
    update_id BIGINT NOT NULL,
    received_at DATETIME NOT NULL,
    PRIMARY KEY (update_id),
    KEY idx_bp_tg_updates_received (received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bp_tg_account_links (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    telegram_user_fk BIGINT UNSIGNED NOT NULL,
    existing_user_ref VARCHAR(191) NULL,
    link_token_hash CHAR(64) NULL,
    status ENUM('pending','linked','revoked') NOT NULL DEFAULT 'pending',
    expires_at DATETIME NULL,
    linked_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_bp_tg_account_links_telegram (telegram_user_fk),
    UNIQUE KEY uq_bp_tg_account_links_token (link_token_hash),
    CONSTRAINT fk_bp_tg_account_links_user FOREIGN KEY (telegram_user_fk)
        REFERENCES bp_tg_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
