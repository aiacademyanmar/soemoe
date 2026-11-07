package asia.aaii.soemoe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val Navy = Color(0xFF111B57)
private val Blue = Color(0xFF2C5DE5)
private val Violet = Color(0xFF6741D9)
private val Ink = Color(0xFF172044)
private val Muted = Color(0xFF66718E)
private val CanvasColor = Color(0xFFF6F8FF)
private val PurpleTint = Color(0xFFEDEAFF)
private val Border = Color(0xFFE7EAF4)

// ORIGINAL demonstration lessons. Not an export of the website or its full paid catalog.
data class Lesson(val id: String, val title: String, val minutes: Int, val content: String)
data class Course(val id: String, val title: String, val tag: String, val summary: String, val emoji: String, val lessons: List<Lesson>)
data class Practice(val id: String, val title: String, val description: String, val emoji: String)

object OfflineCatalog {
    val courses = listOf(
        Course("ai", "AI Classes", "Artificial Intelligence", "Learn practical AI concepts, prompting and responsible tools.", "✦", listOf(
            Lesson("ai1", "What is Artificial Intelligence?", 7, "Artificial intelligence (AI) describes computer systems that perform tasks associated with learning, reasoning, prediction or generating content.\n\nAI systems learn patterns from data. They do not automatically know whether an output is correct. Always verify important facts with reliable sources.\n\nTry it: Identify three ways that an AI assistant could support a classroom."),
            Lesson("ai2", "Writing Clear Prompts", 9, "A useful prompt describes the goal, audience, available context, constraints and preferred output.\n\nExample: ‘Create a beginner-friendly five-minute lesson about recycling. Use simple English, three examples, and a short quiz.’\n\nPractice: Write the same prompt for two different student age groups."),
            Lesson("ai3", "Responsible AI Workflow", 8, "Use AI as a collaborator rather than an unquestioned authority. Check data privacy, permissions, copyright, bias and accuracy.\n\nPractical checklist: 1. Define the goal. 2. Protect sensitive information. 3. Verify claims. 4. Review the output. 5. Credit sources when needed.")
        )),
        Course("science", "Science Documentary", "Science", "Explore observation, evidence and scientific storytelling.", "◎", listOf(
            Lesson("sci1", "Observing the World", 7, "Science begins with observable questions. An observation describes something you can detect or measure. An explanation is a possible interpretation of those observations.\n\nActivity: Write two observations and one question about the weather."),
            Lesson("sci2", "Evidence and Experiments", 8, "A scientific investigation makes a testable prediction, collects evidence, and compares the results with that prediction.\n\nVariables should be controlled where possible. Repeating an experiment improves confidence in the results."),
            Lesson("sci3", "Telling Science Stories", 10, "A strong science documentary has a clear question, verified facts, relevant visuals and an honest conclusion.\n\nCreate a 30-second script: start with a question, explain two supporting facts, then end with what remains unknown.")
        )),
        Course("neuro", "Neuroscience", "Brain & Mind", "Study neurons, attention and learning in everyday life.", "◉", listOf(
            Lesson("neu1", "The Nervous System", 8, "The nervous system receives signals, coordinates responses and helps the body adapt. The brain, spinal cord and peripheral nerves work together.\n\nNeurons communicate through electrical signals and chemical messengers."),
            Lesson("neu2", "Attention and Memory", 9, "Attention helps us select information for further processing. Memory includes encoding, storage and retrieval.\n\nUseful study techniques include spaced practice, retrieval practice, and adequate rest."),
            Lesson("neu3", "Learning and Neuroplasticity", 8, "Neuroplasticity means that the brain can change its connections and function through experience. Learning usually benefits from repeated, meaningful practice.\n\nTask: Plan a short practice schedule with review intervals.")
        )),
        Course("psych", "Buddhist Psychology", "Mind & Wellbeing", "An introductory educational discussion of awareness and compassion.", "❀", listOf(
            Lesson("psy1", "Awareness of Experience", 8, "Mindfulness can be discussed as paying attention to present experience with care. This introduction is educational and is not a substitute for professional health support.\n\nTry a brief reflection: Notice a sound, a physical sensation, and a thought without needing to judge any of them."),
            Lesson("psy2", "Compassion in Daily Life", 7, "Compassion includes noticing suffering and wishing to respond helpfully. Practical examples include patient listening, respectful speech and offering appropriate support.\n\nReflect on a difficult conversation and identify a compassionate response."),
            Lesson("psy3", "Reflection and Habits", 9, "Reflection can help us notice repeated choices and their consequences. Healthy habits often develop through small, consistent actions.\n\nWrite one daily practice that supports attention, kindness and balance.")
        ))
    )
    val practices = listOf(
        Practice("p1", "Prompt Studio", "Design a clear teaching prompt with goal, level and output format.", "✦"),
        Practice("p2", "Science Storyboard", "Plan a three-scene evidence-based science explainer.", "◎"),
        Practice("p3", "Study Planner", "Create a spaced-repetition plan for one topic.", "▤"),
        Practice("p4", "Reflection Journal", "Record a learning insight and one compassionate action.", "❀")
    )
    val allLessons get() = courses.flatMap { it.lessons }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.rgb(17, 27, 87)
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.WHITE
        val prefs = getSharedPreferences("soemoe_native_v2", MODE_PRIVATE)
        setContent {
            val done = remember { mutableStateMapOf<String, Boolean>().apply {
                OfflineCatalog.allLessons.forEach { this[it.id] = prefs.getBoolean("done_${it.id}", false) }
            } }
            val practiceDone = remember { mutableStateMapOf<String, Boolean>().apply {
                OfflineCatalog.practices.forEach { this[it.id] = prefs.getBoolean("practice_${it.id}", false) }
            } }
            var screen by rememberSaveable { mutableStateOf(if (prefs.getBoolean("onboarded", false)) "home" else "welcome") }
            var selectedCourseId by rememberSaveable { mutableStateOf("ai") }
            var selectedLessonId by rememberSaveable { mutableStateOf("ai1") }
            val navigate: (String) -> Unit = { screen = it }
            val back = {
                screen = when (screen) {
                    "lesson" -> "courseDetail"
                    "courseDetail" -> "courses"
                    "bookReader" -> "books"
                    else -> "home"
                }
            }
            BackHandler(enabled = screen != "welcome" && screen != "home") { back() }
            MaterialTheme(colorScheme = lightColorScheme(primary = Violet, secondary = Blue, surface = Color.White, background = CanvasColor)) {
                if (screen == "welcome") {
                    WelcomeScreen {
                        prefs.edit().putBoolean("onboarded", true).apply()
                        screen = "home"
                    }
                } else {
                    Scaffold(
                        containerColor = CanvasColor,
                        bottomBar = {
                            if (screen !in listOf("lesson", "bookReader")) {
                                NavigationBar(containerColor = Color.White, tonalElevation = 6.dp) {
                                    val currentTab = when(screen) { "courseDetail" -> "courses"; "books", "certificates" -> "home"; else -> screen }
                                    listOf(
                                        Triple("home", "Home", Icons.Default.Home),
                                        Triple("courses", "Courses", Icons.Default.School),
                                        Triple("projects", "Projects", Icons.Default.Code),
                                        Triple("profile", "Profile", Icons.Default.Person)
                                    ).forEach { (id, title, icon) ->
                                        NavigationBarItem(
                                            selected = currentTab == id,
                                            onClick = { screen = id },
                                            icon = { Icon(icon, contentDescription = title) },
                                            label = { Text(title, fontSize = 10.sp) },
                                            alwaysShowLabel = true
                                        )
                                    }
                                }
                            }
                        }
                    ) { inner ->
                        Box(Modifier.fillMaxSize().padding(inner)) {
                            when (screen) {
                                "home" -> HomeScreen(done, { selectedCourseId = it; screen = "courseDetail" }, navigate)
                                "courses" -> CourseListScreen(done) { selectedCourseId = it; screen = "courseDetail" }
                                "courseDetail" -> {
                                    val course = OfflineCatalog.courses.firstOrNull { it.id == selectedCourseId } ?: OfflineCatalog.courses.first()
                                    CourseDetailScreen(course, done, back) { selectedLessonId = it; screen = "lesson" }
                                }
                                "lesson" -> {
                                    val lesson = OfflineCatalog.allLessons.firstOrNull { it.id == selectedLessonId } ?: OfflineCatalog.allLessons.first()
                                    LessonScreen(lesson, done[lesson.id] == true, back) {
                                        val newValue = done[lesson.id] != true
                                        done[lesson.id] = newValue
                                        prefs.edit().putBoolean("done_${lesson.id}", newValue).apply()
                                    }
                                }
                                "books" -> NotesScreen { selectedLessonId = it; screen = "bookReader" }
                                "bookReader" -> {
                                    val lesson = OfflineCatalog.allLessons.firstOrNull { it.id == selectedLessonId } ?: OfflineCatalog.allLessons.first()
                                    NoteReaderScreen(lesson, back)
                                }
                                "projects" -> ProjectsScreen(practiceDone) { id ->
                                    val newValue = practiceDone[id] != true
                                    practiceDone[id] = newValue
                                    prefs.edit().putBoolean("practice_$id", newValue).apply()
                                }
                                "certificates" -> CertificatesScreen(done, back)
                                "profile" -> ProfileScreen(
                                    done.values.count { it },
                                    { screen = "certificates" },
                                    { screen = "books" }
                                ) {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://soemoe.aaii.asia/")))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun WelcomeScreen(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Navy, Blue, Violet)))) {
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
            Spacer(Modifier.height(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.teacher_logo), "Soe Moe teacher app logo", Modifier.size(190.dp).clip(RoundedCornerShape(42.dp)))
                Spacer(Modifier.height(30.dp))
                Text("Soe Moe", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.Black)
                Text("AI Trainer", fontSize = 30.sp, color = Color(0xFFD0E4FF), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Learn • Teach • Grow", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Navy), modifier = Modifier.fillMaxWidth().height(54.dp)) {
                    Text("Get Started", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.ArrowForward, "")
                }
                Spacer(Modifier.height(12.dp))
                Text("Native Android • Offline starter lessons", color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable private fun ScreenTitle(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) { Icon(Icons.Default.ArrowBack, "Back", tint = Ink) }
            Spacer(Modifier.width(6.dp))
        }
        Column {
            Text(title, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable private fun SectionHeading(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
        if (onAction != null && action != null) Text(action, color = Violet, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.clickable { onAction() })
    }
}

@Composable private fun CardSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier, shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(15.dp), content = content)
    }
}

@Composable private fun HomeScreen(done: Map<String, Boolean>, openCourse: (String) -> Unit, navigate: (String) -> Unit) {
    val completed = done.values.count { it }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.teacher_logo), "Teacher logo", Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Soe Moe AI Trainer", fontWeight = FontWeight.Bold, color = Ink, fontSize = 17.sp)
                    Text("Native Learning Academy", color = Muted, fontSize = 11.sp)
                }
                IconButton(onClick = { navigate("profile") }) { Icon(Icons.Default.Person, "Profile", tint = Violet) }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Blue)) {
                Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Blue, Violet))).height(174.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(start = 17.dp)) {
                        Text("Welcome to", color = Color.White.copy(alpha = .88f), fontSize = 13.sp)
                        Text("Soe Moe\nAI Trainer", fontSize = 25.sp, lineHeight = 29.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("Learn • Teach • Grow", color = Color.White.copy(alpha = .9f), fontSize = 11.sp)
                    }
                    Image(painterResource(R.drawable.teacher_portrait), "Teacher photo", Modifier.width(125.dp).fillMaxHeight().clip(RoundedCornerShape(topStart = 25.dp, bottomStart = 25.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${OfflineCatalog.allLessons.size}", "Lessons", "▷", Modifier.weight(1f))
                StatTile("${OfflineCatalog.courses.size}", "Courses", "▤", Modifier.weight(1f))
                StatTile("$completed", "Completed", "✓", Modifier.weight(1f))
            }
        }
        item {
            Column {
                SectionHeading("Featured Courses", "See All") { navigate("courses") }
                FeatureCourseCard(OfflineCatalog.courses.first(), done) { openCourse(it) }
            }
        }
        item {
            Column {
                SectionHeading("Explore")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryTile("AI Classes", "Learn AI & tools", "✦", Color(0xFFE5F3FF), Modifier.weight(1f)) { navigate("courses") }
                    CategoryTile("Books & Notes", "Offline reading", "▤", Color(0xFFF0E8FF), Modifier.weight(1f)) { navigate("books") }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryTile("Certificates", "Achievement status", "♕", Color(0xFFFFF1E3), Modifier.weight(1f)) { navigate("certificates") }
                    CategoryTile("Projects", "Practice skills", "⌘", Color(0xFFDBF7F3), Modifier.weight(1f)) { navigate("projects") }
                }
            }
        }
        item { StarterNotice() }
    }
}

@Composable private fun StatTile(value: String, title: String, symbol: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(symbol, color = Violet, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(title, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable private fun CategoryTile(title: String, subtitle: String, glyph: String, background: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.height(128.dp).clip(RoundedCornerShape(18.dp)).background(background).clickable { onClick() }.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Text(glyph, fontSize = 26.sp, color = Violet, fontWeight = FontWeight.Black)
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable private fun FeatureCourseCard(course: Course, done: Map<String, Boolean>, onClick: (String) -> Unit) {
    CardSurface(Modifier.fillMaxWidth().clickable { onClick(course.id) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(79.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Navy, Violet))), contentAlignment = Alignment.Center) {
                Text(course.emoji, color = Color.White, fontSize = 43.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(course.title, fontSize = 16.sp, color = Ink, fontWeight = FontWeight.Bold)
                Text(course.summary, color = Muted, fontSize = 11.sp, maxLines = 2)
                Spacer(Modifier.height(7.dp))
                LinearProgressIndicator(progress = { courseProgress(course, done) }, color = Violet, trackColor = PurpleTint, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape))
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.PlayArrow, "Open course", tint = Violet)
        }
    }
}

private fun courseProgress(course: Course, done: Map<String, Boolean>): Float = course.lessons.count { done[it.id] == true }.toFloat() / course.lessons.size

@Composable private fun CourseListScreen(done: Map<String, Boolean>, openCourse: (String) -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    val filtered = OfflineCatalog.courses.filter { c ->
        (c.title.contains(search, true) || c.tag.contains(search, true) || c.summary.contains(search, true)) &&
        (filter == "All" || (filter == "In Progress" && courseProgress(c, done) > 0f && courseProgress(c, done) < 1f) || (filter == "Completed" && courseProgress(c, done) == 1f))
    }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Courses", "Learn at your own pace")
        OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Search courses") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true,
            shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("All", "In Progress", "Completed").forEach { title ->
                FilterChip(selected = filter == title, onClick = { filter = title }, label = { Text(title, fontSize = 12.sp) })
            }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered, key = { it.id }) { course -> FeatureCourseCard(course, done, openCourse) }
            if (filtered.isEmpty()) item { Text("No matching courses.", color = Muted, modifier = Modifier.padding(12.dp)) }
            item { StarterNotice() }
        }
    }
}

@Composable private fun CourseDetailScreen(course: Course, done: Map<String, Boolean>, onBack: () -> Unit, openLesson: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle(course.title, course.tag, onBack)
        LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            item {
                CardSurface {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(course.emoji, fontSize = 44.sp, color = Violet)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(course.summary, color = Ink, fontSize = 13.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("${course.lessons.count { done[it.id] == true }} of ${course.lessons.size} lessons completed", color = Muted, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { courseProgress(course, done) }, color = Violet, trackColor = PurpleTint, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape))
                }
            }
            item { SectionHeading("Lessons") }
            items(course.lessons) { lesson ->
                CardSurface(Modifier.fillMaxWidth().clickable { openLesson(lesson.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if(done[lesson.id] == true) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                            null, tint = if(done[lesson.id] == true) Color(0xFF008A76) else Violet)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(lesson.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${lesson.minutes} min • Offline", color = Muted, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = Muted)
                    }
                }
            }
        }
    }
}

@Composable private fun LessonScreen(lesson: Lesson, complete: Boolean, onBack: () -> Unit, toggle: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Lesson Reader", "Original learning material • Offline", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(15.dp))
            Text(lesson.title, color = Ink, fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(18.dp))
            Text(lesson.content, color = Ink, fontSize = 16.sp, lineHeight = 27.sp)
            Spacer(Modifier.height(20.dp))
        }
        Button(onClick = toggle, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp).height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = if(complete) Color(0xFF008A76) else Violet)) {
            Icon(Icons.Default.CheckCircle, null)
            Spacer(Modifier.width(8.dp))
            Text(if (complete) "Completed — Tap to Undo" else "Mark Lesson Complete", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun NotesScreen(openNote: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Books & Study Notes", "Native offline reading")
        LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { StarterNotice("These are original in-app study notes. Published books from the website have not yet been imported.") }
            items(OfflineCatalog.courses) { c ->
                CardSurface {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.emoji, fontSize = 26.sp, color = Violet)
                        Spacer(Modifier.width(10.dp))
                        Text(c.title, fontWeight = FontWeight.Bold, color = Ink)
                    }
                    Spacer(Modifier.height(7.dp))
                    c.lessons.forEach { lesson ->
                        Row(Modifier.fillMaxWidth().clickable { openNote(lesson.id) }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, null, tint = Violet, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(lesson.title, color = Ink, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp), tint = Muted)
                        }
                        HorizontalDivider(color = Border)
                    }
                }
            }
        }
    }
}

@Composable private fun NoteReaderScreen(lesson: Lesson, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Study Note", "Offline native reader", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text(lesson.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(20.dp))
            Text(lesson.content, color = Ink, fontSize = 17.sp, lineHeight = 28.sp)
        }
    }
}

@Composable private fun ProjectsScreen(done: Map<String, Boolean>, toggle: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Projects", "Practical, offline activities")
        LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            items(OfflineCatalog.practices) { p ->
                CardSurface {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(p.emoji, fontSize = 32.sp, color = Violet)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(p.description, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = done[p.id] == true, onCheckedChange = { toggle(p.id) })
                        Text(if (done[p.id] == true) "Completed" else "Mark as practiced", color = Ink, fontSize = 13.sp, modifier = Modifier.clickable { toggle(p.id) })
                    }
                }
            }
        }
    }
}

@Composable private fun CertificatesScreen(done: Map<String, Boolean>, onBack: () -> Unit) {
    val finished = OfflineCatalog.courses.filter { c -> c.lessons.all { done[it.id] == true } }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Certificates", "Learning progress overview", onBack)
        Column(Modifier.padding(18.dp)) {
            CardSurface {
                Icon(Icons.Default.WorkspacePremium, "", tint = Violet, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(10.dp))
                Text("${finished.size} / ${OfflineCatalog.courses.size} courses completed", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Completion is stored locally on this device. Official certificates are NOT issued by this demo app.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
            }
            Spacer(Modifier.height(15.dp))
            finished.forEach { c ->
                Text("✓ ${c.title} — learning milestone complete", color = Color(0xFF008A76), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable private fun ProfileScreen(completedCount: Int, openCertificates: () -> Unit, openBooks: () -> Unit, openWebsite: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { ScreenTitle("Teacher Profile") }
        item {
            Box(Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 18.dp).clip(RoundedCornerShape(24.dp)).background(Brush.horizontalGradient(listOf(Blue,Violet)))) {
                Image(painterResource(R.drawable.teacher_portrait), "Soe Moe teacher portrait", modifier = Modifier.align(Alignment.Center).fillMaxHeight().width(210.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("Soe Moe", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp)
                Text("AI Trainer • Educator", color = Violet, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Text("Learn, teach and grow through AI, science, neuroscience and thoughtful learning.", color = Muted, fontSize = 14.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("${OfflineCatalog.courses.size}", "Courses", "▤", Modifier.weight(1f))
                    StatTile("${OfflineCatalog.allLessons.size}", "Lessons", "▷", Modifier.weight(1f))
                    StatTile("$completedCount", "Done", "✓", Modifier.weight(1f))
                }
                Spacer(Modifier.height(18.dp))
                Button(onClick = openWebsite, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Launch, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Visit Official Website (Browser)")
                }
                Spacer(Modifier.height(16.dp))
                SectionHeading("Explore")
                CardSurface(Modifier.fillMaxWidth().clickable(onClick = openBooks)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Book, null, tint = Violet)
                        Spacer(Modifier.width(10.dp))
                        Text("Books & Study Notes", color = Ink, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, null, tint = Muted)
                    }
                }
                Spacer(Modifier.height(10.dp))
                CardSurface(Modifier.fillMaxWidth().clickable(onClick = openCertificates)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, null, tint = Violet)
                        Spacer(Modifier.width(10.dp))
                        Text("Certificates & Milestones", color = Ink, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, null, tint = Muted)
                    }
                }
                Spacer(Modifier.height(17.dp))
                StarterNotice("This is a native Android app, not a WebView. Course data is a local starter curriculum; website account syncing and official certificates are not connected.")
            }
        }
    }
}

@Composable private fun StarterNotice(message: String = "Starter curriculum • All in-app lessons are original demonstration content and can be read offline. The full website catalog has not been imported.") {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(PurpleTint).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AutoAwesome, null, tint = Violet, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, color = Ink, fontSize = 11.sp, lineHeight = 17.sp)
    }
}
