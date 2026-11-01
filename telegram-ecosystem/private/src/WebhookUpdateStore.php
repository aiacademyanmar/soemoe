<?php
declare(strict_types=1);

final class WebhookUpdateStore {
    public function claim(int $updateId): bool {
        $stmt = bp_db()->prepare('INSERT IGNORE INTO bp_tg_updates (update_id, received_at) VALUES (:id, UTC_TIMESTAMP())');
        $stmt->execute([':id' => $updateId]);
        return $stmt->rowCount() === 1;
    }
}
