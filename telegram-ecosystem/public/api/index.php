<?php
declare(strict_types=1);

$privateRoot = getenv('BP_TG_PRIVATE_ROOT') ?: dirname(__DIR__, 2) . '/private';
require rtrim($privateRoot, DIRECTORY_SEPARATOR) . '/bootstrap.php';
bp_apply_cors();

$action = (string)($_GET['action'] ?? 'health');
try {
    if ($action === 'health') {
        bp_json(['ok' => true, 'service' => 'buddha-psychology-telegram', 'version' => '1.0.0']);
    }
    if ($action === 'public_catalog') {
        bp_json(BuddhaCatalog::publicPayload());
    }
    if ($action === 'telegram_login') {
        bp_require_method('POST');
        $body = bp_json_body();
        $tg = bp_config()['telegram'] ?? [];
        $identity = TelegramMiniAppAuth::verify(
            (string)($body['init_data'] ?? ''),
            (string)($tg['bot_token'] ?? ''),
            (int)($tg['init_data_max_age_seconds'] ?? 86400)
        );
        $store = new SessionStore();
        $store->upsertTelegramUser($identity);
        $localId = $store->userId((string)$identity['id']);
        $token = $store->issueSession($localId, (int)(bp_config()['security']['session_days'] ?? 14));
        bp_json([
            'ok' => true, 'session_token' => $token,
            'user' => ['telegram_user_id' => $identity['id'], 'first_name' => $identity['first_name'], 'username' => $identity['username']],
            'account_link' => ['status' => 'not_linked', 'available' => false],
            'membership' => ['status' => 'pending_existing_server_sync', 'premium' => false],
        ]);
    }
    if ($action === 'me') {
        bp_require_method('GET');
        $user = (new SessionStore())->resolveSession(bp_bearer());
        if (!$user) bp_json(['ok' => false, 'error' => 'unauthorized'], 401);
        bp_json(['ok' => true, 'user' => $user, 'account_link' => ['status' => 'not_linked', 'available' => false],
            'membership' => ['status' => 'pending_existing_server_sync', 'premium' => false]]);
    }
    if ($action === 'account_link') {
        bp_require_method('POST');
        bp_json(['ok' => false, 'error' => 'existing_server_adapter_not_verified',
            'message' => 'Account linking will be enabled after the current Admin Server V5.3 API contract is verified.'], 409);
    }
    if ($action === 'membership') {
        bp_require_method('GET');
        $user = (new SessionStore())->resolveSession(bp_bearer());
        if (!$user) bp_json(['ok' => false, 'error' => 'unauthorized'], 401);
        bp_json(['ok' => true, 'source_of_truth' => 'existing_buddha_psychology_admin_server',
            'status' => 'adapter_pending', 'premium' => false, 'checkout_enabled' => false]);
    }
    bp_json(['ok' => false, 'error' => 'not_found'], 404);
} catch (InvalidArgumentException $e) {
    bp_json(['ok' => false, 'error' => 'invalid_request'], 400);
} catch (Throwable $e) {
    error_log('[Buddha Psychology Telegram API] ' . $e->getMessage());
    bp_json(['ok' => false, 'error' => 'service_unavailable'], 503);
}
