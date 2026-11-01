<?php
declare(strict_types=1);

final class TelegramBotApi {
    private string $token;
    public function __construct(string $token) {
        if ($token === '') throw new RuntimeException('Telegram bot token is not configured.');
        $this->token = $token;
    }
    public function call(string $method, array $payload): array {
        if (!function_exists('curl_init')) throw new RuntimeException('PHP cURL extension is required.');
        $ch = curl_init('https://api.telegram.org/bot' . $this->token . '/' . $method);
        curl_setopt_array($ch, [
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
            CURLOPT_HTTPHEADER => ['Content-Type: application/json'],
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_CONNECTTIMEOUT => 8,
            CURLOPT_TIMEOUT => 20,
        ]);
        $raw = curl_exec($ch);
        $status = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $error = curl_error($ch);
        curl_close($ch);
        if ($raw === false || $error !== '') throw new RuntimeException('Telegram API transport failed.');
        $result = json_decode((string)$raw, true);
        if ($status < 200 || $status >= 300 || !is_array($result) || empty($result['ok'])) {
            throw new RuntimeException('Telegram API rejected request: ' . (is_array($result) ? (string)($result['description'] ?? 'unknown_error') : 'invalid_response'));
        }
        return $result;
    }
    public function sendMessage(string|int $chatId, string $text, ?array $replyMarkup = null): array {
        $payload = ['chat_id' => $chatId, 'text' => $text, 'parse_mode' => 'HTML', 'disable_web_page_preview' => true];
        if ($replyMarkup !== null) $payload['reply_markup'] = $replyMarkup;
        return $this->call('sendMessage', $payload);
    }
}
