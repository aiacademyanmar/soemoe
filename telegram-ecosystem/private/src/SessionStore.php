<?php
declare(strict_types=1);

final class SessionStore {
    public function upsertTelegramUser(array $user): void {
        $sql = 'INSERT INTO bp_tg_users
            (telegram_user_id, username, first_name, last_name, language_code, photo_url, telegram_premium, created_at, updated_at)
            VALUES (:telegram_user_id, :username, :first_name, :last_name, :language_code, :photo_url, :telegram_premium, UTC_TIMESTAMP(), UTC_TIMESTAMP())
            ON DUPLICATE KEY UPDATE username = VALUES(username), first_name = VALUES(first_name),
            last_name = VALUES(last_name), language_code = VALUES(language_code), photo_url = VALUES(photo_url),
            telegram_premium = VALUES(telegram_premium), updated_at = UTC_TIMESTAMP()';
        $stmt = bp_db()->prepare($sql);
        $stmt->execute([
            ':telegram_user_id' => $user['id'], ':username' => $user['username'],
            ':first_name' => $user['first_name'], ':last_name' => $user['last_name'],
            ':language_code' => $user['language_code'], ':photo_url' => $user['photo_url'],
            ':telegram_premium' => $user['is_premium'],
        ]);
    }

    public function userId(string $telegramUserId): int {
        $stmt = bp_db()->prepare('SELECT id FROM bp_tg_users WHERE telegram_user_id = :id LIMIT 1');
        $stmt->execute([':id' => $telegramUserId]);
        $row = $stmt->fetch();
        if (!$row) throw new RuntimeException('Telegram identity was not stored.');
        return (int)$row['id'];
    }

    public function issueSession(int $localUserId, int $days = 14): string {
        $token = rtrim(strtr(base64_encode(random_bytes(32)), '+/', '-_'), '=');
        $days = max(1, min(30, $days));
        $sql = 'INSERT INTO bp_tg_sessions (token_hash, telegram_user_fk, created_at, expires_at)
                VALUES (:token_hash, :telegram_user_fk, UTC_TIMESTAMP(), DATE_ADD(UTC_TIMESTAMP(), INTERVAL ' . $days . ' DAY))';
        $stmt = bp_db()->prepare($sql);
        $stmt->execute([':token_hash' => hash('sha256', $token), ':telegram_user_fk' => $localUserId]);
        return $token;
    }

    public function resolveSession(string $token): ?array {
        if ($token === '') return null;
        $stmt = bp_db()->prepare('SELECT u.id, u.telegram_user_id, u.username, u.first_name, u.last_name,
                u.language_code, u.photo_url, s.expires_at
            FROM bp_tg_sessions s INNER JOIN bp_tg_users u ON u.id = s.telegram_user_fk
            WHERE s.token_hash = :token_hash AND s.revoked_at IS NULL AND s.expires_at > UTC_TIMESTAMP()
            LIMIT 1');
        $stmt->execute([':token_hash' => hash('sha256', $token)]);
        $row = $stmt->fetch();
        return $row ?: null;
    }
}
