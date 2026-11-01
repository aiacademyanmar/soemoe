# Buddha Psychology Telegram Ecosystem V1.0

ဤ project သည် မူရင်း Buddha Psychology Website နှင့် Android App ကို မပြောင်းလဲဘဲ Telegram Channel, Bot နှင့် Mini App ကို သီးခြား integration layer အဖြစ် ထည့်သွင်းရန်ဖြစ်သည်။

## Target URLs
- Main website: https://aaii.asia/dama/psychology/
- Mini App (planned): https://aaii.asia/dama/psychology/telegram/miniapp/
- Bot webhook (planned): https://aaii.asia/dama/psychology/telegram/bot/webhook.php
- Public API (planned): https://aaii.asia/dama/psychology/telegram/api/index.php
- App key: buddha_psychology

## V1.0 scope
- Telegram Mini App UI: Home, Book Library, Audio/Video, AI Learning, Membership, Account.
- Telegram Mini App initData HMAC validation on the server.
- Telegram identity registration and short-lived server sessions.
- Telegram Bot commands: /start, /app, /help, /plans, /terms, /support, /paysupport.
- Web App launch button and optional channel publishing utility.
- Additive Telegram-only database tables prefixed bp_tg_.
- Public book catalogue: 9 chapters; Chapter 1 free, Chapters 2–9 premium.
- Existing Admin Server remains the source of truth for licenses, orders and entitlements.

## Important integration boundaries
1. Do not overwrite the current Website, Android APK, Admin Server, database tables or logo assets.
2. The live V5.3 Admin API route map and production schema have not yet been verified from source. Do not guess endpoint names.
3. Telegram identity is stored separately until an authenticated account-link adapter is mapped to the actual V5.3 server. V1.0 must not grant Premium access based only on a Telegram username or user ID.
4. Chapter 1 is public/free. Chapters 2–9 require the existing server's entitlement decision.
5. The AI section is an educational preview only until an approved AI service endpoint is configured. It is not therapy, diagnosis or crisis care.
6. Do not accept passwords, license codes, payment screenshots or payment credentials in Telegram chat.
7. Do not enable digital-goods checkout using MMK or third-party payment links inside Telegram. Telegram requires digital goods and services sold in bots/Mini Apps to use Telegram Stars (XTR). Stars pricing, successful-payment handling, refunds and /paysupport must be completed before checkout is enabled.

## Source layout
- public/miniapp/ — browser/Telegram Mini App UI
- public/api/ — public API entry point
- public/bot/ — Telegram webhook entry point
- private/ — PHP application code and private configuration
- database/schema.sql — additive Telegram extension tables
- tests/ — HMAC verification tests

## Deployment (staging first)
1. Back up the existing website files and current production database.
2. Upload the contents of public/ to the intended Telegram subdirectory under the existing psychology website.
3. Place the private/ directory outside the public web root. Set BP_TG_PRIVATE_ROOT to its absolute path.
4. Copy private/config.example.php to private/config.php. Fill in the bot token, webhook secret, database connection and Mini App URL. Never commit config.php.
5. Import database/schema.sql only into a staging database first. The SQL creates only bp_tg_* tables and does not alter existing Buddha Psychology tables.
6. Run PHP syntax checks and tests/auth_test.php.
7. Create the Telegram bot with @BotFather. Configure the Main Mini App or menu button to the Mini App URL.
8. Set the webhook URL to /telegram/bot/webhook.php and pass the configured secret_token.
9. Add the bot as an administrator of the intended Channel with permission to post messages. Set the channel ID in private/config.php.
10. Verify login, session expiry, bot commands, channel post and failure states in staging before production rollout.

## Telegram Bot setup
Use @BotFather:
- /newbot
- /setdescription
- /setabouttext
- /setuserpic
- /setmenubutton — text: Open App; URL: the Mini App URL
- /setcommands — start, app, help, plans, terms, support, paysupport

Webhook setup (run on a trusted terminal; do not paste the bot token into chat or GitHub source):

    curl -X POST "https://api.telegram.org/bot< BOT_TOKEN >/setWebhook" \
      -d "url=< WEBHOOK_URL >" \
      -d "secret_token=< WEBHOOK_SECRET >"

## Existing Admin Server adapter — pending source audit
The previously referenced API base was https://aaii.asia/book/psychology/api/. This path and its current V5.3 route contract must be verified against the deployed source before use. The adapter is deliberately not hard-coded to unverified routes.

Required mapping:
- Telegram identity → existing Buddha Psychology user/account
- Existing license / lifetime entitlement read
- Existing package/order status read
- Premium chapter access decision
- Optional account-link token issue/consume

Do not connect to the live database or run migrations until the actual V5.3 source and sanitized schema are reviewed.

## QA status
This branch contains the initial V1.0 source scaffold. It is not yet deployed, does not yet connect to the live Admin Server, and does not yet have a registered Bot/Channel. A successful local syntax test is not proof of production connectivity.
