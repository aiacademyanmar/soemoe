package asia.aaii.dhammalight

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val deep = Color(0xFF0B2A2E)
private val night = Color(0xFF062327)
private val gold = Color(0xFFE0C078)
private val teal = Color(0xFF168A7E)
private val ivory = Color(0xFFFBFAF6)
private val muted = Color(0xFF617C7C)
private val tabs = listOf("Home", "Stories", "Watch", "Sponsor", "Academy")
private val glyphs = listOf("⌂", "▤", "▶", "♡", "✦")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DhammaLightApp() }
    }
}

@Composable
fun DharmaWheel(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = center
        val r = size.minDimension * 0.32f
        val w = size.minDimension * 0.055f
        drawCircle(gold, r, c, style = Stroke(w))
        for (i in 0 until 8) {
            val a = Math.PI * i / 4.0
            val dx = cos(a).toFloat()
            val dy = sin(a).toFloat()
            drawLine(gold,
                Offset(c.x + dx * r * 0.18f, c.y + dy * r * 0.18f),
                Offset(c.x + dx * r * 1.23f, c.y + dy * r * 1.23f),
                w, cap = StrokeCap.Round)
        }
        drawCircle(gold, r * 0.26f, c)
        drawCircle(deep, r * 0.12f, c)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhammaLightApp() {
    var current by rememberSaveable { mutableIntStateOf(0) }
    var more by rememberSaveable { mutableStateOf("") }
    MaterialTheme(colorScheme = lightColorScheme(primary = teal, background = ivory, surface = Color.White, onSurface = deep)) {
        Scaffold(
            containerColor = ivory,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DharmaWheel(Modifier.size(38.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Dhamma Light", fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = deep)
                                Text("ANIMATION PROJECT", fontSize = 9.sp, color = teal, letterSpacing = 1.4.sp)
                            }
                        }
                    },
                    actions = {
                        TextButton(onClick = { more = "Impact" }) { Text("Impact", color = teal) }
                        TextButton(onClick = { more = "Settings" }) { Text("⚙", color = teal, fontSize = 18.sp) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ivory)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    tabs.forEachIndexed { i, label ->
                        NavigationBarItem(
                            selected = current == i && more.isEmpty(),
                            onClick = { current = i; more = "" },
                            icon = { Text(glyphs[i], fontSize = 20.sp) },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (more) {
                    "Impact" -> Page { ImpactPage() }
                    "Settings" -> Page { SettingsPage() }
                    else -> when (current) {
                        0 -> Page { HomePage(
                            onStories = {current = 1},
                            onWatch = {current = 2},
                            onSponsor = {current = 3}
                        ) }
                        1 -> Page { StoriesPage() }
                        2 -> Page { WatchPage() }
                        3 -> Page { SponsorPage() }
                        else -> Page { AcademyPage() }
                    }
                }
            }
        }
    }
}

@Composable
private fun Page(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content) }
    }
}

@Composable
private fun Heading(top: String, big: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(top.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = teal, letterSpacing = 1.5.sp)
        Text(big, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.SemiBold, color = deep)
        Text(subtitle, color = muted, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun Tile(title: String, body: String, badge: String = "") {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(17.dp),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            if (badge.isNotEmpty()) Text(badge.uppercase(), color = teal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 18.sp, color = deep, fontWeight = FontWeight.SemiBold)
            Text(body, fontSize = 13.sp, color = muted, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun HomePage(onStories: () -> Unit, onWatch: () -> Unit, onSponsor: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = deep)) {
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(deep, Color(0xFF175B5A)))).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DharmaWheel(Modifier.size(110.dp))
            Text("Dhamma Light", fontSize = 30.sp, color = ivory, fontWeight = FontWeight.SemiBold)
            Text("Ancient wisdom. New light.", fontSize = 19.sp, color = gold)
            Text("ဓမ္မအလင်း — ဗုဒ္ဓတရားတော်နှင့် ဓမ္မရသ", fontSize = 13.sp, color = ivory, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Button(onClick = onStories, colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = deep)) {
                Text("Explore Stories")
            }
        }
    }
    Heading("Cinematic Buddhist animations", "Stories that stay with the soul", "Free bilingual Buddhist animation project for Myanmar and international audiences.")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Metric("30", "Monthly target", Modifier.weight(1f))
        Metric("02", "Languages", Modifier.weight(1f))
        Metric("FREE", "Viewing goal", Modifier.weight(1f))
    }
    Tile("Watch our channels", "Explore the official creator-supplied YouTube and TikTok accounts.", "VIDEO")
    Button(onClick = onWatch, modifier = Modifier.fillMaxWidth()) { Text("Open Watch") }
    Tile("Support the light", "Proposed sponsorship: 300,000 MMK per bilingual episode. Enquiries only; no payment processed.", "DHAMMA DANA")
    OutlinedButton(onClick = onSponsor, modifier = Modifier.fillMaxWidth()) { Text("Sponsorship Details") }
}

@Composable
private fun Metric(number: String, label: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp)) {
            Text(number, fontWeight = FontWeight.Bold, color = deep, fontSize = 20.sp)
            Text(label, fontSize = 10.sp, color = muted)
        }
    }
}

@Composable
private fun StoriesPage() {
    Heading("Story library", "Discover a moment of reflection", "Sample story cards only. Real animations have not yet been imported.")
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Search sample stories") },
        modifier = Modifier.fillMaxWidth(), singleLine = true)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("All", "Compassion", "Wisdom").forEach { key ->
            FilterChip(selected = key == filter, onClick = {filter = key}, label = {Text(key, fontSize = 11.sp)})
        }
    }
    val stories = listOf(
        Triple("The Gift of Kindness", "Compassion", "A sample concept about helping others"),
        Triple("The Quiet Mind", "Wisdom", "An illustrative lesson in reflection"),
        Triple("A Light in the Forest", "Wisdom", "A sample theme about awareness")
    )
    stories.filter { (filter == "All" || it.second == filter) && (search.isEmpty() || it.first.contains(search, true)) }
        .forEach { Tile(it.first, it.third + ". Content not yet produced.", it.second) }
}

@Composable
private fun WatchPage() {
    val ctx = LocalContext.current
    Heading("Watch & listen", "Dhamma Light channels", "Open the creator-supplied accounts in their official app or mobile browser.")
    Tile("YouTube — Knowledge Zone AI", "@knowledgezoneai\nhttps://www.youtube.com/@knowledgezoneai", "YOUTUBE")
    Button(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@knowledgezoneai"))) },
        modifier = Modifier.fillMaxWidth()) { Text("Open YouTube Channel ↗") }
    Tile("TikTok — AI Soe Moe", "@aisoemoe\nhttps://www.tiktok.com/@aisoemoe", "TIKTOK")
    OutlinedButton(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@aisoemoe"))) },
        modifier = Modifier.fillMaxWidth()) { Text("Open TikTok Channel ↗") }
    Text("No videos are downloaded or copied. Links open external apps; video availability depends on the channel.", color = muted, fontSize = 12.sp)
}

@Composable
private fun SponsorPage() {
    val ctx = LocalContext.current
    var count by rememberSaveable { mutableIntStateOf(1) }
    val amount = 300000L * count
    Heading("Dhamma dana", "Sponsor an episode", "Estimate your planned contribution. This is not a checkout or payment service.")
    Card(colors = CardDefaults.cardColors(containerColor = deep), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(23.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Text("SPONSORSHIP ESTIMATOR", fontSize = 11.sp, color = gold)
            Text("MMK " + NumberFormat.getNumberInstance(Locale.US).format(amount), fontSize = 29.sp,
                fontWeight = FontWeight.Bold, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedButton(onClick = { count = (count - 1).coerceAtLeast(1) }) { Text("−", color = gold) }
                Text("$count episodes", color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                OutlinedButton(onClick = { count = (count + 1).coerceAtMost(100) }) { Text("+", color = gold) }
            }
        }
    }
    Tile("Includes", "Myanmar + English editions and optional donor acknowledgement (subject to agreement).")
    Button(onClick = {
        val message = "Dhamma Light sponsorship enquiry: $count episodes, MMK " +
                NumberFormat.getNumberInstance(Locale.US).format(amount)
        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message) }
        ctx.startActivity(Intent.createChooser(send, "Share sponsorship enquiry"))
    }, modifier = Modifier.fillMaxWidth()) { Text("Share Enquiry — No Payment") }
}

@Composable
private fun AcademyPage() {
    Heading("AI animation academy", "Bring stories to life", "Proposed creator-training program. Contact the team for enrollment and official terms.")
    Tile("Professional AI Animation Course", "Proposal tuition: 500,000 MMK per learner. Storyboards, scripts, animation production and ethical review.", "TRAINING")
    Tile("Practical curriculum", "Creative concepts · Visual storytelling · Animation workflow · Editing · Language adaptation.")
    Tile("Career pathway", "Possible portfolio and project opportunities are not guaranteed income or employment.")
    Text("Course enrollment and online payments are not connected in this demonstration.", color = muted, fontSize = 12.sp)
}

@Composable
private fun ImpactPage() {
    Heading("Project impact", "A brighter world", "Published numbers below are project goals, not verified achievements.")
    Metric("30", "Planned episodes per month", Modifier.fillMaxWidth())
    Metric("02", "Myanmar + English", Modifier.fillMaxWidth())
    Tile("Free Dhamma access", "The project proposes sharing completed Buddhist animations free via online media.")
    Tile("Sponsor transparency", "300,000 MMK per episode is a proposal amount; actual donation and expense records are not yet connected.")
}

@Composable
private fun SettingsPage() {
    Heading("Preferences", "About the application", "Dhamma Light • Native Android v1.0")
    Tile("Platform", "Kotlin + Jetpack Compose; native application, not a WebView wrapper.")
    Tile("Video links", "YouTube: @knowledgezoneai\nTikTok: @aisoemoe")
    Tile("Privacy", "This demonstration does not require sign-in and does not collect payment or donor details.")
    Tile("Language", "English interface with Myanmar project content. Full language switching is planned.")
}
