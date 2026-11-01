<?php
declare(strict_types=1);
require dirname(__DIR__) . '/private/src/TelegramMiniAppAuth.php';

function makeInitData(string $botToken, int $authDate, array $user): string {
    $data = ['auth_date' => (string)$authDate, 'query_id' => 'AAE-test-query',
        'user' => json_encode($user, JSON_UNESCAPED_SLASHES)];
    ksort($data, SORT_STRING);
    $parts = [];
    foreach ($data as $key => $value) $parts[] = $key . '=' . $value;
    $secret = hash_hmac('sha256', $botToken, 'WebAppData', true);
    $data['hash'] = hash_hmac('sha256', implode("\n", $parts), $secret);
    return http_build_query($data);
}
$token = '123456:test-token-for-unit-tests-only';
$user = ['id' => 123456789, 'first_name' => 'Test', 'username' => 'test_user'];
$valid = makeInitData($token, time(), $user);
$identity = TelegramMiniAppAuth::verify($valid, $token, 86400);
if ($identity['id'] !== '123456789') throw new RuntimeException('Valid initData test failed.');

$invalid = str_replace('test_user', 'attacker', $valid);
try {
    TelegramMiniAppAuth::verify($invalid, $token, 86400);
    throw new RuntimeException('Tampered initData was accepted.');
} catch (RuntimeException $e) {
    if ($e->getMessage() === 'Tampered initData was accepted.') throw $e;
}
$expired = makeInitData($token, time() - 90000, $user);
try {
    TelegramMiniAppAuth::verify($expired, $token, 86400);
    throw new RuntimeException('Expired initData was accepted.');
} catch (RuntimeException $e) {
    if ($e->getMessage() === 'Expired initData was accepted.') throw $e;
}
echo "Telegram Mini App HMAC tests passed.\n";
