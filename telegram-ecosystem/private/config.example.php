<?php
declare(strict_types=1);

/* Copy to config.php outside the public web root. Never commit real secrets. */
return [
    'app' => [
        'name' => 'Buddha Psychology',
        'website_url' => 'https://aaii.asia/dama/psychology/',
        'mini_app_url' => 'https://aaii.asia/dama/psychology/telegram/miniapp/',
        'allowed_origins' => ['https://aaii.asia'],
        'timezone' => 'UTC',
    ],
    'database' => [
        'dsn' => 'mysql:host=localhost;dbname=CHANGE_ME;charset=utf8mb4',
        'username' => 'CHANGE_ME',
        'password' => 'CHANGE_ME',
    ],
    'telegram' => [
        'bot_token' => '',
        'webhook_secret' => '',
        'channel_id' => '',
        'channel_url' => '',
        'admin_user_ids' => [],
        'init_data_max_age_seconds' => 86400,
    ],
    'security' => ['session_days' => 14],
    'existing_server' => [
        // Previously referenced base URL; verify against deployed V5.3 source.
        'base_url' => 'https://aaii.asia/book/psychology/api/',
        'enabled' => false,
        'server_token' => '',
        // Leave empty until the real V5.3 route/request/response contract is audited.
        'routes' => [],
    ],
    'support' => ['url' => 'https://aaii.asia/dama/psychology/'],
];
