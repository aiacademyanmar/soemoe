<?php
declare(strict_types=1);

$privateRoot = getenv('BP_TG_PRIVATE_ROOT') ?: __DIR__;
$configFile = rtrim($privateRoot, DIRECTORY_SEPARATOR) . DIRECTORY_SEPARATOR . 'config.php';
if (!is_file($configFile)) {
    http_response_code(503);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['ok' => false, 'error' => 'private_config_missing']);
    exit;
}
$config = require $configFile;
if (!is_array($config)) throw new RuntimeException('Invalid private configuration.');
$GLOBALS['bp_tg_config'] = $config;
date_default_timezone_set((string)($config['app']['timezone'] ?? 'UTC'));

function bp_config(): array { return $GLOBALS['bp_tg_config']; }

function bp_db(): PDO {
    static $pdo = null;
    if ($pdo instanceof PDO) return $pdo;
    $db = bp_config()['database'] ?? [];
    $dsn = (string)($db['dsn'] ?? '');
    if ($dsn === '') throw new RuntimeException('Database DSN is not configured.');
    $pdo = new PDO($dsn, (string)($db['username'] ?? ''), (string)($db['password'] ?? ''), [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
    return $pdo;
}

function bp_json(array $payload, int $status = 200): never {
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-store');
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}
function bp_json_body(): array {
    $data = json_decode(file_get_contents('php://input') ?: '', true);
    return is_array($data) ? $data : [];
}
function bp_require_method(string $method): void {
    if (strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET') !== strtoupper($method)) {
        header('Allow: ' . strtoupper($method));
        bp_json(['ok' => false, 'error' => 'method_not_allowed'], 405);
    }
}
function bp_bearer(): string {
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    return preg_match('/^Bearer\s+([A-Za-z0-9._~-]+)$/i', $header, $m) ? $m[1] : '';
}
function bp_apply_cors(): void {
    $origin = $_SERVER['HTTP_ORIGIN'] ?? '';
    $allowed = bp_config()['app']['allowed_origins'] ?? [];
    if ($origin !== '' && in_array($origin, $allowed, true)) {
        header('Access-Control-Allow-Origin: ' . $origin);
        header('Vary: Origin');
        header('Access-Control-Allow-Headers: Authorization, Content-Type');
        header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
    } elseif ($origin !== '') {
        bp_json(['ok' => false, 'error' => 'origin_not_allowed'], 403);
    }
    if (strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'OPTIONS') {
        http_response_code(204); exit;
    }
}

require_once __DIR__ . '/src/TelegramMiniAppAuth.php';
require_once __DIR__ . '/src/SessionStore.php';
require_once __DIR__ . '/src/BuddhaCatalog.php';
require_once __DIR__ . '/src/TelegramBotApi.php';
require_once __DIR__ . '/src/WebhookUpdateStore.php';
