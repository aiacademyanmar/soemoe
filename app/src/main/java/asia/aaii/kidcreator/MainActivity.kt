package asia.aaii.kidcreator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
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
import androidx.compose.ui.unit.sp

private val navy = Color(0xFF101D45)
private val panel = Color(0xFF22356D)
private val mint = Color(0xFF65E8DF)
private val lavender = Color(0xFFBBA4FF)
private val light = Color(0xFFF5F7FF)
private val muted = Color(0xFFC9D5F4)

private val modules = listOf(
 "Creator Mindset & AI Literacy", "Digital Drawing & Visual Design", "Storytelling & Digital Comics",
 "Video Basics & Creative Editing", "Creative Coding & Mini Games", "Digital Safety & Creator Showcase",
 "Professional Visual & Brand Design", "AI Video Production & Storyboarding",
 "Game & App Prototype Studio", "Creator Studio Team Production", "Specialization Discovery Lab",
 "Professional Portfolio & Demo Day", "Advanced Specialization Studio",
 "Creator Rights, Safety & Professional Ethics", "Team & Client Brief Simulation",
 "Production Sprint & Quality Assurance", "Capstone Project & Mentor Review",
 "Specialist Portfolio, Demo Day & Viva"
)
private val levels = listOf("Foundation Creator","Professional Creator","Specialized Professional")
class MainActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   MaterialTheme(colorScheme=darkColorScheme(primary=mint,secondary=lavender,background=navy,surface=panel,onSurface=light)) {
    LearningApp()
   }
  }
 }
}
@Composable
private fun LearningApp() {
 var level by remember { mutableIntStateOf(-1) }
 var module by remember { mutableIntStateOf(-1) }
 var lesson by remember { mutableIntStateOf(-1) }
 fun back() { if(lesson>=0)lesson=-1 else if(module>=0)module=-1 else level=-1 }
 BackHandler(level>=0||module>=0||lesson>=0) { back() }
 Scaffold(containerColor=navy,bottomBar={
  Surface(color=Color(0xFF192A59)) {
   Row(Modifier.fillMaxWidth().padding(16.dp),horizontalArrangement=Arrangement.SpaceAround) {
    listOf("⌂ Home","✦ Learn","▤ Library","✧ AI","◉ Profile").forEachIndexed { index,title ->
     Text(title,color=if(index<=1)mint else muted,fontSize=12.sp,modifier=Modifier.clickable {
      lesson=-1;module=-1;level=if(index==1)0 else -1
     })
    }
   }
  }
 }) { padding ->
 LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=18.dp),
  contentPadding=PaddingValues(vertical=22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
   item {
    Text("✦ AAII  |  AI FOR KID CREATOR ACADEMY",color=mint,fontWeight=FontWeight.Bold,fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    Text("Create Your Future ✨",color=light,fontWeight=FontWeight.ExtraBold,fontSize=26.sp)
    Text("NATIVE ANDROID · V2.1.1 BUILD TEST",color=Color(0xFFFFD678),fontWeight=FontWeight.Bold,fontSize=12.sp)
   }
   item {
    Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF36407A)),shape=RoundedCornerShape(20.dp)){
     Column(Modifier.fillMaxWidth().padding(17.dp)){
      Text("BUILD VALIDATION ONLY",color=Color(0xFFFFD678),fontWeight=FontWeight.Bold)
      Spacer(Modifier.height(5.dp))
      Text("This is a real Android test app, not the complete teaching release. Full lesson texts, quizzes, media and cloud services are not included yet.",color=light,fontSize=13.sp,lineHeight=18.sp)
     }
    }
   }
   when {
    lesson>=0 -> item {
     LearningCard("LESSON ${module+1}.${lesson+1}","Lesson content and assessments will be imported from the full V2.1.1 source archive.",onClick={back})
    }
    module>=0 -> {
     item {Text("MODULE ${module+1} · ${modules[module]}",color=mint,fontWeight=FontWeight.Bold,fontSize=19.sp)}
     items((0..3).toList()) { i-> LearningCard("Lesson ${module+1}.${i+1}","Tap to open this lesson",onClick={lesson=i}) }
    }
    level>=0 -> {
     item {Text("LEVEL ${level+1} · ${levels[level]}",color=mint,fontWeight=FontWeight.Bold,fontSize=20.sp)}
     items((level*6 until level*6+6).toList()) { i ->
      LearningCard("MODULE ${i+1}"," ${modules[i]} · 4 Lessons",onClick={module=i})
     }
    }
    else -> {
     item {Text("Your Learning Journey",color=light,fontSize=21.sp,fontWeight=FontWeight.Bold)}
     items((0..2).toList()) { i ->
      LearningCard("LEVEL ${i+1}", "${levels[i]}  •  6 Modules  •  24 Lessons",onClick={level=i})
     }
     item {LearningCard("AI Teacher + Live Class","Demo navigation only · backend not connected",onClick={level=0})}
    }
   }
  }
 }
}
@Composable
private fun LearningCard(title:String,subtitle:String,onClick:()->Unit) {
 Card(modifier=Modifier.fillMaxWidth().clickable(onClick=onClick),
 colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(19.dp)) {
  Column(Modifier.padding(18.dp)) {
   Text(title,color=mint,fontSize=17.sp,fontWeight=FontWeight.Bold)
   Spacer(Modifier.height(7.dp))
   Text(subtitle,color=muted,fontSize=13.sp)
   Spacer(Modifier.height(7.dp))
   Text("Explore  →",color=lavender,fontSize=12.sp)
  }
 }
}
