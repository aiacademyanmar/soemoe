<?php
declare(strict_types=1);

$privateRoot = getenv('BP_TG_PRIVATE_ROOT') ?: dirname(__DIR__, 2) . '/private';
require rtrim($privateRoot, DIRECTORY_SEPARATOR) . '/bootstrap.php';
$config = bp_config();
$expectedSecret = (string)($config['telegram']['webhook_secret'] ?? '');
$providedSecret = (string)($_SERVER['HTTP_X_TELEGRAM_BOT_API_SECRET_TOKEN'] ?? '');
if ($expectedSecret === '' || !hash_equals($expectedSecret, $providedSecret)) {
    http_response_code(403); exit('Forbidden');
}
$update = json_decode(file_get_contents('php://input') ?: '', true);
if (!is_array($update)) { http_response_code(400); exit('Bad Request'); }

try {
    if (isset($update['update_id']) && !(new WebhookUpdateStore())->claim((int)$update['update_id'])) {
        http_response_code(200); exit('OK');
    }
    $message = $update['message'] ?? null;
    if (!is_array($message) || !isset($message['chat']['id'])) { http_response_code(200); exit('OK'); }

    $chatId = $message['chat']['id'];
    $chatType = (string)($message['chat']['type'] ?? '');
    $text = trim((string)($message['text'] ?? ''));
    $tg = $config['telegram'] ?? [];
    $bot = new TelegramBotApi((string)($tg['bot_token'] ?? ''));
    $miniAppUrl = (string)($config['app']['mini_app_url'] ?? '');
    $websiteUrl = (string)($config['app']['website_url'] ?? '');
    $supportUrl = (string)($config['support']['url'] ?? $websiteUrl);
    $markup = ['inline_keyboard' => [
        [['text' => 'Open Buddha Psychology', 'web_app' => ['url' => $miniAppUrl]]],
        [['text' => 'Official Website', 'url' => $websiteUrl]],
    ]];
    $command = strtolower((string)(preg_split('/\s+/', $text)[0] ?? ''));
    $command = preg_replace('/@\w+$/', '', $command);

    if ($command === '/start' || $command === '/app') {
        $name = htmlspecialchars((string)($message['from']['first_name'] ?? 'မိတ်ဆွေ'), ENT_QUOTES, 'UTF-8');
        $bot->sendMessage($chatId, 'မင်္ဂလာပါ ' . $name . '။' . "\n\n" .
            '<b>Buddha Psychology</b>' . "\nစိတ်ကိုနားလည်၊ ဘဝကိုညှိနှိုင်း။\n\n" .
            'စာအုပ်၊ Audio/Video နှင့် စိတ်ပညာလေ့လာရေး Mini App ကို ဖွင့်နိုင်ပါသည်။ Premium access ကို မူရင်း Admin Server မှ စစ်ဆေးမည်ဖြစ်သည်。',
            $chatType === 'private' ? $markup : null);
    } elseif ($command === '/help') {
        $bot->sendMessage($chatId, '<b>Buddha Psychology Bot Help</b>' . "\n/start — စတင်ရန်\n/app — Mini App ဖွင့်ရန်\n/plans — Membership အချက်အလက်\n/terms — စည်းကမ်းချက်များ\n/support — အကူအညီ\n/paysupport — ငွေပေးချေမှု အကူအညီ",
            $chatType === 'private' ? $markup : null);
    } elseif ($command === '/plans') {
        $bot->sendMessage($chatId, '<b>Membership</b>' . "\nFree Explorer, Lifetime App နှင့် Lifetime App + Books အစီအစဉ်များ ရှိပါသည်။\n\n" .
            'Telegram အတွင်း digital goods/services checkout ကို Telegram Stars (XTR) ဖြင့်သာ ဖွင့်နိုင်သည်။ Stars ဈေးနှုန်းနှင့် payment/refund flow မတည်ဆောက်ရသေးသဖြင့် Bot checkout ကို ယာယီပိတ်ထားပါသည်။',
            $chatType === 'private' ? $markup : null);
    } elseif ($command === '/terms') {
        $bot->sendMessage($chatId, 'Buddha Psychology ဝန်ဆောင်မှုအချက်အလက်များကို Official Website တွင် ကြည့်ရှုပါ။' . "\n" . $websiteUrl);
    } elseif ($command === '/support' || $command === '/paysupport') {
        $bot->sendMessage($chatId, 'အကူအညီလိုအပ်ပါက Official Support Website ကို ဆက်သွယ်ပါ။' . "\n" . $supportUrl);
    } else {
        $bot->sendMessage($chatId, 'Buddha Psychology ကို အသုံးပြုရန် /start ကိုနှိပ်ပါ။', $chatType === 'private' ? $markup : null);
    }
    http_response_code(200); echo 'OK';
} catch (Throwable $e) {
    error_log('[Buddha Psychology Telegram Webhook] ' . $e->getMessage());
    http_response_code(200); echo 'OK';
}
