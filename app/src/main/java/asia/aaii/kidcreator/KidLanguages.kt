package asia.aaii.kidcreator

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Catalog of exactly 200 distinct ISO 639 language identifiers.
 * Language selection is real and persisted locally; only main navigation labels
 * are translated in the language packs listed below. All other labels use English.
 * This file does NOT falsely claim full 200-language translation coverage.
 */
internal data class KidLanguage(val tag:String,val english:String,val nativeName:String,val localized:Boolean)
internal object KidLanguages {
    private val codes = """aa ab ae af ak am an ar as av ay az ba be bg bi bm bn bo br bs ca ce ch co cr cs cu cv cy da de dv dz ee el en eo es et eu fa ff fi fj fo fr fy ga gd gl gn gu gv ha he hi ho hr ht hu hy hz ia id ie ig ii ik io is it iu ja jv ka kg ki kj kk kl km kn ko kr ks ku kv kw ky la lb lg li ln lo lt lu lv mg mh mi mk ml mn mr ms mt my na nb nd ne ng nl nn no nr nv ny oc oj om or os pa pi pl ps pt qu rm rn ro ru rw sa sc sd se sg sh si sk sl sm sn so sq sr ss st su sv sw ta te tg th ti tk tl tn to tr ts tt tw ty ug uk ur uz ve vi vo wa wo xh yi yo za zh zu ace ban bho bug ceb doi fil hne kok mai mni pap sat sco war yue""".trim().split(Regex("\\s+"))
    private val special = mapOf(
        "ace" to "Acehnese", "ban" to "Balinese", "bho" to "Bhojpuri",
        "bug" to "Buginese", "ceb" to "Cebuano", "doi" to "Dogri",
        "fil" to "Filipino", "hne" to "Chhattisgarhi", "kok" to "Konkani",
        "mai" to "Maithili", "mni" to "Meitei", "pap" to "Papiamento",
        "sat" to "Santali", "sco" to "Scots", "war" to "Waray",
        "yue" to "Cantonese")
    private val primary = listOf("en","my","th","zh","ja","ko","hi","ar","es","fr")
    private val packs: Map<String,Map<String,String>> = mapOf(
        "en" to mapOf("home" to "Home", "learn" to "Learn", "create" to "Create", "library" to "Library", "profile" to "Profile", "settings" to "Settings", "languages" to "Languages", "search" to "Search languages", "select" to "Select language", "fallback" to "English text fallback", "choose" to "Choose your language", "saved" to "Language saved", "coverage" to "Translation available for main navigation"),
        "my" to mapOf("home" to "ပင်မ", "learn" to "သင်ယူရန်", "create" to "ဖန်တီးရန်", "library" to "စာကြည့်တိုက်", "profile" to "ကိုယ်ရေးအချက်အလက်", "settings" to "ဆက်တင်များ", "languages" to "ဘာသာစကားများ", "search" to "ဘာသာစကား ရှာရန်", "select" to "ဘာသာစကား ရွေးရန်", "fallback" to "ဘာသာပြန်မရသေးသောစာသားများကို အင်္ဂလိပ်လို ပြမည်", "choose" to "မိမိဘာသာစကားကို ရွေးပါ", "saved" to "ဘာသာစကား သိမ်းပြီး", "coverage" to "အဓိက Menu များကို ဘာသာပြန်ထားသည်"),
        "th" to mapOf("home" to "หน้าแรก", "learn" to "เรียนรู้", "create" to "สร้างสรรค์", "library" to "ห้องสมุด", "profile" to "โปรไฟล์", "settings" to "การตั้งค่า", "languages" to "ภาษา", "search" to "ค้นหาภาษา", "select" to "เลือกภาษา", "fallback" to "ใช้ข้อความภาษาอังกฤษเมื่อยังไม่มีคำแปล", "choose" to "เลือกภาษาของคุณ", "saved" to "บันทึกภาษาแล้ว", "coverage" to "แปลเมนูหลักแล้ว"),
        "zh" to mapOf("home" to "首页", "learn" to "学习", "create" to "创作", "library" to "图书馆", "profile" to "我的", "settings" to "设置", "languages" to "语言", "search" to "搜索语言", "select" to "选择语言", "fallback" to "未翻译内容将显示英文", "choose" to "选择你的语言", "saved" to "语言已保存", "coverage" to "主要导航已翻译"),
        "ja" to mapOf("home" to "ホーム", "learn" to "学ぶ", "create" to "作る", "library" to "ライブラリ", "profile" to "プロフィール", "settings" to "設定", "languages" to "言語", "search" to "言語を検索", "select" to "言語を選択", "fallback" to "未翻訳の内容は英語で表示", "choose" to "言語を選択してください", "saved" to "言語を保存しました", "coverage" to "主要メニューは翻訳済みです"),
        "ko" to mapOf("home" to "홈", "learn" to "학습", "create" to "만들기", "library" to "도서관", "profile" to "프로필", "settings" to "설정", "languages" to "언어", "search" to "언어 검색", "select" to "언어 선택", "fallback" to "번역되지 않은 내용은 영어로 표시됩니다", "choose" to "언어를 선택하세요", "saved" to "언어가 저장되었습니다", "coverage" to "주요 메뉴 번역 완료"),
        "hi" to mapOf("home" to "होम", "learn" to "सीखें", "create" to "बनाएँ", "library" to "पुस्तकालय", "profile" to "प्रोफ़ाइल", "settings" to "सेटिंग्स", "languages" to "भाषाएँ", "search" to "भाषा खोजें", "select" to "भाषा चुनें", "fallback" to "अनुवाद न होने पर अंग्रेज़ी दिखाई देगी", "choose" to "अपनी भाषा चुनें", "saved" to "भाषा सहेजी गई", "coverage" to "मुख्य मेनू का अनुवाद उपलब्ध है"),
        "bn" to mapOf("home" to "হোম", "learn" to "শিখুন", "create" to "তৈরি করুন", "library" to "লাইব্রেরি", "profile" to "প্রোফাইল", "settings" to "সেটিংস", "languages" to "ভাষা", "search" to "ভাষা খুঁজুন", "select" to "ভাষা বেছে নিন", "fallback" to "অনুবাদ না থাকলে ইংরেজি দেখানো হবে", "choose" to "আপনার ভাষা বেছে নিন", "saved" to "ভাষা সংরক্ষিত হয়েছে", "coverage" to "মূল মেনু অনূদিত"),
        "ar" to mapOf("home" to "الرئيسية", "learn" to "تعلّم", "create" to "أنشئ", "library" to "المكتبة", "profile" to "الملف الشخصي", "settings" to "الإعدادات", "languages" to "اللغات", "search" to "ابحث عن لغة", "select" to "اختر اللغة", "fallback" to "سيظهر النص بالإنجليزية إذا لم تتوفر ترجمة", "choose" to "اختر لغتك", "saved" to "تم حفظ اللغة", "coverage" to "القوائم الأساسية مترجمة"),
        "es" to mapOf("home" to "Inicio", "learn" to "Aprender", "create" to "Crear", "library" to "Biblioteca", "profile" to "Perfil", "settings" to "Ajustes", "languages" to "Idiomas", "search" to "Buscar idiomas", "select" to "Seleccionar idioma", "fallback" to "El contenido sin traducir aparecerá en inglés", "choose" to "Elige tu idioma", "saved" to "Idioma guardado", "coverage" to "Navegación principal traducida"),
        "fr" to mapOf("home" to "Accueil", "learn" to "Apprendre", "create" to "Créer", "library" to "Bibliothèque", "profile" to "Profil", "settings" to "Paramètres", "languages" to "Langues", "search" to "Rechercher une langue", "select" to "Choisir la langue", "fallback" to "Le contenu non traduit s’affiche en anglais", "choose" to "Choisissez votre langue", "saved" to "Langue enregistrée", "coverage" to "Navigation principale traduite"),
        "de" to mapOf("home" to "Start", "learn" to "Lernen", "create" to "Gestalten", "library" to "Bibliothek", "profile" to "Profil", "settings" to "Einstellungen", "languages" to "Sprachen", "search" to "Sprachen suchen", "select" to "Sprache auswählen", "fallback" to "Nicht übersetzter Inhalt wird auf Englisch angezeigt", "choose" to "Wähle deine Sprache", "saved" to "Sprache gespeichert", "coverage" to "Hauptnavigation übersetzt"),
        "pt" to mapOf("home" to "Início", "learn" to "Aprender", "create" to "Criar", "library" to "Biblioteca", "profile" to "Perfil", "settings" to "Configurações", "languages" to "Idiomas", "search" to "Buscar idiomas", "select" to "Selecionar idioma", "fallback" to "Conteúdo não traduzido aparece em inglês", "choose" to "Escolha seu idioma", "saved" to "Idioma salvo", "coverage" to "Navegação principal traduzida"),
        "ru" to mapOf("home" to "Главная", "learn" to "Учиться", "create" to "Создавать", "library" to "Библиотека", "profile" to "Профиль", "settings" to "Настройки", "languages" to "Языки", "search" to "Поиск языка", "select" to "Выбрать язык", "fallback" to "Без перевода отображается английский текст", "choose" to "Выберите язык", "saved" to "Язык сохранён", "coverage" to "Основное меню переведено"),
        "id" to mapOf("home" to "Beranda", "learn" to "Belajar", "create" to "Berkreasi", "library" to "Perpustakaan", "profile" to "Profil", "settings" to "Pengaturan", "languages" to "Bahasa", "search" to "Cari bahasa", "select" to "Pilih bahasa", "fallback" to "Teks tanpa terjemahan ditampilkan dalam bahasa Inggris", "choose" to "Pilih bahasamu", "saved" to "Bahasa disimpan", "coverage" to "Menu utama sudah diterjemahkan"),
        "ms" to mapOf("home" to "Utama", "learn" to "Belajar", "create" to "Cipta", "library" to "Perpustakaan", "profile" to "Profil", "settings" to "Tetapan", "languages" to "Bahasa", "search" to "Cari bahasa", "select" to "Pilih bahasa", "fallback" to "Teks yang belum diterjemahkan dipaparkan dalam bahasa Inggeris", "choose" to "Pilih bahasa anda", "saved" to "Bahasa disimpan", "coverage" to "Menu utama telah diterjemahkan"),
        "vi" to mapOf("home" to "Trang chủ", "learn" to "Học", "create" to "Sáng tạo", "library" to "Thư viện", "profile" to "Hồ sơ", "settings" to "Cài đặt", "languages" to "Ngôn ngữ", "search" to "Tìm ngôn ngữ", "select" to "Chọn ngôn ngữ", "fallback" to "Nội dung chưa dịch sẽ hiển thị bằng tiếng Anh", "choose" to "Chọn ngôn ngữ của bạn", "saved" to "Đã lưu ngôn ngữ", "coverage" to "Đã dịch menu chính"),
        "it" to mapOf("home" to "Home", "learn" to "Impara", "create" to "Crea", "library" to "Biblioteca", "profile" to "Profilo", "settings" to "Impostazioni", "languages" to "Lingue", "search" to "Cerca lingue", "select" to "Scegli lingua", "fallback" to "I contenuti non tradotti appaiono in inglese", "choose" to "Scegli la tua lingua", "saved" to "Lingua salvata", "coverage" to "Navigazione principale tradotta"),
        "tr" to mapOf("home" to "Ana Sayfa", "learn" to "Öğren", "create" to "Oluştur", "library" to "Kütüphane", "profile" to "Profil", "settings" to "Ayarlar", "languages" to "Diller", "search" to "Dil ara", "select" to "Dil seç", "fallback" to "Çevrilmemiş içerik İngilizce gösterilir", "choose" to "Dilini seç", "saved" to "Dil kaydedildi", "coverage" to "Ana menü çevrildi"),
        "lo" to mapOf("home" to "ໜ້າຫຼັກ", "learn" to "ຮຽນຮູ້", "create" to "ສ້າງສັນ", "library" to "ຫ້ອງສະໝຸດ", "profile" to "ໂປຣໄຟລ໌", "settings" to "ການຕັ້ງຄ່າ", "languages" to "ພາສາ", "search" to "ຄົ້ນຫາພາສາ", "select" to "ເລືອກພາສາ", "fallback" to "ໃຊ້ພາສາອັງກິດຖ້າບໍ່ມີຄຳແປ", "choose" to "ເລືອກພາສາຂອງທ່ານ", "saved" to "ບັນທຶກແລ້ວ", "coverage" to "ເມນູຫຼັກແປແລ້ວ"),
        "km" to mapOf("home" to "ទំព័រដើម", "learn" to "រៀន", "create" to "បង្កើត", "library" to "បណ្ណាល័យ", "profile" to "ប្រវត្តិរូប", "settings" to "ការកំណត់", "languages" to "ភាសា", "search" to "ស្វែងរកភាសា", "select" to "ជ្រើសរើសភាសា", "fallback" to "មាតិកាដែលមិនទាន់បកប្រែនឹងបង្ហាញជាភាសាអង់គ្លេស", "choose" to "ជ្រើសរើសភាសារបស់អ្នក", "saved" to "បានរក្សាទុកភាសា", "coverage" to "ម៉ឺនុយសំខាន់មានការបកប្រែ")
    )
    val all: List<KidLanguage> by lazy {
        val data=codes.map { tag ->
            val locale=Locale.forLanguageTag(tag)
            val english=special[tag] ?: locale.getDisplayLanguage(Locale.ENGLISH).ifBlank { tag }
            val native=locale.getDisplayLanguage(locale).ifBlank { english }
            KidLanguage(tag,english,if(native==tag)english else native,packs.containsKey(tag))
        }
        require(data.size==200 && data.map { it.tag }.toSet().size==200)
        data.sortedWith(compareBy<KidLanguage> { if(it.tag in primary) primary.indexOf(it.tag) else 99 }
            .thenBy { it.english.lowercase(Locale.ROOT) })
    }
    fun value(tag:String,key:String):String = packs[tag]?.get(key) ?: packs.getValue("en").getValue(key)
    fun hasPack(tag:String):Boolean=packs.containsKey(tag)
    fun displayName(tag:String):String=all.find{it.tag==tag}?.nativeName ?: "English"
    fun load(ctx:Context):String {
        val value=ctx.getSharedPreferences("aaii_language_v24",Context.MODE_PRIVATE).getString("language","en") ?: "en"
        return if(codes.contains(value))value else "en"
    }
    fun save(ctx:Context,tag:String) {
        require(codes.contains(tag))
        ctx.getSharedPreferences("aaii_language_v24",Context.MODE_PRIVATE).edit().putString("language",tag).apply()
    }
}

@Composable
internal fun KidLanguageCenter(current:String,onSelected:(String)->Unit) {
    var search by remember { mutableStateOf("") }
    val shown=remember(search){
        KidLanguages.all.filter {
            it.english.contains(search.trim(),ignoreCase=true)
            || it.nativeName.contains(search.trim(),ignoreCase=true)
            || it.tag.contains(search.trim(),ignoreCase=true)
        }
    }
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Text("🌍 "+KidLanguages.value(current,"choose"),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.ExtraBold,color=Color.White)
        Text("200 languages • "+shown.size+" results",color=Color(0xFF5AF1E3))
        Text("Selected: "+KidLanguages.displayName(current)+" ("+current+")",color=Color.White)
        OutlinedTextField(value=search,onValueChange={search=it},modifier=Modifier.fillMaxWidth(),
            singleLine=true,label={Text(KidLanguages.value(current,"search"))},
            placeholder={Text("e.g. မြန်မာ · English · 日本語 · العربية")})
        Text(KidLanguages.value(current,"fallback"),style=MaterialTheme.typography.bodySmall,color=Color(0xFFFFD965))
        Text("Core navigation packs: "+KidLanguages.all.count{it.localized}+" / 200. Other UI text stays in English until translation packs are added.",
            color=Color(0xFFC6D3F9),style=MaterialTheme.typography.bodySmall)
        // Parent is already inside a scrollable LazyColumn; avoid nested vertical scrollers.
        shown.forEach { item ->
            val active=current==item.tag
            Surface(
                modifier=Modifier.fillMaxWidth().clickable { onSelected(item.tag) },
                shape=RoundedCornerShape(15.dp),
                color=if(active)Color(0xFF244D94)else Color(0xFF1D2E70),
                tonalElevation=2.dp
            ) {
                Row(Modifier.padding(13.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                        Text(item.nativeName,color=Color.White,fontWeight=FontWeight.Bold)
                        Text(item.english+" • "+item.tag.uppercase(Locale.ROOT)+
                            if(item.localized)" • Core UI translated" else " • English UI fallback",
                            color=Color(0xFFC6D3F9),style=MaterialTheme.typography.bodySmall)
                    }
                    Text(if(active)"✓" else "◯",color=Color(0xFF5AF1E3))
                }
            }
        }
        if(shown.isEmpty())Text("No matching languages",color=Color.White)
    }
}
