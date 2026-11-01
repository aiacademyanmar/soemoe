<?php
declare(strict_types=1);

final class TelegramMiniAppAuth {
    public static function verify(string $initData, string $botToken, int $maxAgeSeconds = 86400): array {
        if ($initData === '' || $botToken === '') throw new InvalidArgumentException('Missing Telegram initData or bot token.');
        parse_str($initData, $data);
        $receivedHash = strtolower((string)($data['hash'] ?? ''));
        unset($data['hash']);
        if (!preg_match('/^[a-f0-9]{64}$/', $receivedHash)) throw new RuntimeException('Malformed Telegram initData hash.');

        ksort($data, SORT_STRING);
        $parts = [];
        foreach ($data as $key => $value) if (is_scalar($value)) $parts[] = $key . '=' . (string)$value;
        $checkString = implode("\n", $parts);

        // Telegram Mini Apps official HMAC validation.
        $secretKey = hash_hmac('sha256', $botToken, 'WebAppData', true);
        $expectedHash = hash_hmac('sha256', $checkString, $secretKey);
        if (!hash_equals($expectedHash, $receivedHash)) throw new RuntimeException('Invalid Telegram initData signature.');

        $authDate = filter_var($data['auth_date'] ?? null, FILTER_VALIDATE_INT);
        if (!$authDate || $authDate > time() + 60 || (time() - $authDate) > $maxAgeSeconds) {
            throw new RuntimeException('Telegram initData expired or timestamp invalid.');
        }
        $user = json_decode((string)($data['user'] ?? ''), true);
        if (!is_array($user) || !isset($user['id']) || !is_numeric($user['id'])) {
            throw new RuntimeException('Telegram user identity missing.');
        }
        return [
            'id' => (string)$user['id'],
            'username' => isset($user['username']) ? (string)$user['username'] : null,
            'first_name' => isset($user['first_name']) ? (string)$user['first_name'] : '',
            'last_name' => isset($user['last_name']) ? (string)$user['last_name'] : null,
            'language_code' => isset($user['language_code']) ? (string)$user['language_code'] : null,
            'photo_url' => isset($user['photo_url']) ? (string)$user['photo_url'] : null,
            'is_premium' => !empty($user['is_premium']) ? 1 : 0,
        ];
    }
}
