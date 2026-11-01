<?php
declare(strict_types=1);
if (PHP_SAPI !== 'cli') { http_response_code(403); exit('CLI only'); }
require dirname(__DIR__) . '/bootstrap.php';
$message = trim(implode(' ', array_slice($argv, 1)));
$tg = bp_config()['telegram'] ?? [];
$channelId = (string)($tg['channel_id'] ?? '');
if ($message === '' || $channelId === '') {
    fwrite(STDERR, "Usage: php post_channel.php \"Message text\" (configure channel_id first)\n");
    exit(2);
}
try {
    $bot = new TelegramBotApi((string)($tg['bot_token'] ?? ''));
    $bot->sendMessage($channelId, htmlspecialchars($message, ENT_QUOTES, 'UTF-8'));
    fwrite(STDOUT, "Channel post sent.\n");
} catch (Throwable $e) {
    fwrite(STDERR, "Channel post failed: " . $e->getMessage() . "\n");
    exit(1);
}
