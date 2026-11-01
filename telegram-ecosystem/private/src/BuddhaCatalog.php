<?php
declare(strict_types=1);

final class BuddhaCatalog {
    public static function publicPayload(): array {
        $titles = [
            'Buddha Psychology and Its Future',
            'Neuroplasticity and Vipassana',
            'The Roots of Mental Distress',
            'The Four Noble Truths and Psychological Interpretation',
            'The Five Aggregates and the Mind',
            'Non-Self and Mental Suffering',
            'Dependent Origination and Emotions',
            'Mindfulness and Therapeutic Perspectives',
            'Practical Benefits of Buddhist Psychology',
        ];
        $chapters = [];
        foreach ($titles as $index => $title) {
            $number = $index + 1;
            $chapters[] = ['number' => $number, 'title' => $title, 'access' => $number === 1 ? 'free' : 'premium'];
        }
        return [
            'ok' => true,
            'product' => [
                'key' => 'buddha_psychology',
                'name' => 'ဗုဒ္ဓ၏ စိတ်ပညာ',
                'subtitle' => 'စိတ်ကိုနားလည်၊ ဘဝကိုညှိနှိုင်း',
                'author' => 'Soe Moe',
                'book_pages' => 77,
                'chapter_count' => 9,
                'website_url' => 'https://aaii.asia/dama/psychology/',
            ],
            'topics' => ['Mindfulness','Vipassana','Neuroplasticity','Four Noble Truths','Five Aggregates','Non-Self','Dependent Origination','Emotional Awareness'],
            'chapters' => $chapters,
            'packages' => [
                ['key' => 'free', 'name' => 'Free Explorer', 'checkout_enabled' => false],
                ['key' => 'lifetime_app', 'name' => 'Lifetime App', 'checkout_enabled' => false],
                ['key' => 'lifetime_app_books', 'name' => 'Lifetime App + Books', 'checkout_enabled' => false],
            ],
            'notices' => [
                'premium_source' => 'existing_admin_server',
                'telegram_checkout' => 'disabled_until_telegram_stars_pricing_and_payment_flow_are_approved',
                'ai_service' => 'educational_preview_only_until_live_provider_is_configured',
            ],
        ];
    }
}
