package org.aaii.englishlanpya.ui

import android.Manifest
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.util.Locale

private val navy=Color(0xFF0B2854)
private val blue=Color(0xFF075CE5)
private val bg=Color(0xFFF5F8FE)
private val muted=Color(0xFF6A7D9C)
private val green=Color(0xFF0C9A78)
private val lavender=Color(0xFFF0EDFF)
private val rounded=RoundedCornerShape(18.dp)
private val teachers=listOf("Emma","James","Ronnie","Rebecca","Alex","Benjamin","Adam","Jade","Gill","Jon","Valen")
private val topics=listOf("Grammar","Vocabulary","Speaking","Listening","Pronunciation","Reading","Writing","IELTS","TOEFL","Business English","Culture & Tips","Expressions","Comprehension","Slang")
private val talkCollections=listOf("Daily Conversations","Small Talk","Job Interview","Travel English","Phone Call English","Speaking Confidence","Family & Friends","Workplace Talk","Meeting New People")
private val cefr=listOf("A1 Beginner","A2 Elementary","B1 Intermediate","B2 Upper Intermediate","C1 Advanced","C2 Mastery")
private val sampleLessons=listOf("Hello and Welcome","My Name Is...","Where Are You From?","Nice to Meet You","Talking About Family","Daily Routines","My School","At the Market")
private val steps=listOf("Introduction","Vocabulary","Grammar","Listening","Speaking","Reading","Exercise","Quiz")

@Composable
fun LanPyaNativeApp(){
    val context=LocalContext.current
    val tts=remember(context){ TextToSpeech(context){ } }
    DisposableEffect(tts){ onDispose { tts.stop();tts.shutdown() } }
    var screen by remember { mutableStateOf("splash") }
    var sourceName by remember { mutableStateOf("Emma") }
    var topicName by remember { mutableStateOf("Grammar") }
    var courseName by remember { mutableStateOf("A1 Beginner") }
    var lessonName by remember { mutableStateOf(sampleLessons[0]) }
    var step by remember { mutableIntStateOf(0) }
    var completed by remember { mutableStateOf(setOf<String>()) }
    var choice by remember { mutableIntStateOf(-1) }
    var message by remember { mutableStateOf("") }
    fun openLesson(name:String){lessonName=name;step=0;choice=-1;screen="lesson"}
    fun say(s:String){tts.language=Locale.US; tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"englishlanpya")}
    MaterialTheme(colorScheme=lightColorScheme(primary=blue,secondary=green,background=bg,surface=Color.White)){
      if(screen=="splash"){
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0048C7),Color(0xFF167BEE),Color(0xFF091F63)))),contentAlignment=Alignment.Center){
          Column(Modifier.fillMaxWidth().padding(30.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){
            Text("📘",fontSize=82.sp)
            Text("English LanPya",color=Color.White,fontSize=34.sp,fontWeight=FontWeight.ExtraBold)
            Text("အင်္ဂလိပ်လမ်းပြ",color=Color.White,fontSize=23.sp)
            Text("Learn English Step by Step",color=Color.White.copy(alpha=.95f),fontSize=16.sp)
            Text("Video Lessons  •  Practice  •  Speak  •  Grow",color=Color.White.copy(alpha=.84f),fontSize=13.sp,textAlign=TextAlign.Center)
            Spacer(Modifier.height(38.dp))
            Button(onClick={screen="welcome"},colors=ButtonDefaults.buttonColors(containerColor=Color.White,contentColor=blue),modifier=Modifier.fillMaxWidth().height(54.dp)){Text("Get Started  →",fontWeight=FontWeight.Bold)}
            Text("Native Android Preview • by AAII",color=Color.White.copy(alpha=.75f),fontSize=12.sp)
          }
        }
      }else if(screen=="welcome"){
        Panel("Welcome to English LanPya","Learn • Practice • Speak • Get Certified",null){
          item{Hero("Your English Learning Journey","မိမိအဆင့်အလိုက် English စကားပြော၊ နားထောင်၊ ရေး၊ ဖတ် လေ့လာပါ။")}
          item{Feature("🎬","Video Lessons","Teacher, Topic, Course နှင့် Let's Talk collections")}
          item{Feature("📘","Interactive Learning","8-step vocabulary, grammar, listening and quizzes")}
          item{Feature("🎤","Speaking Practice","Record your voice locally; cloud review not active")}
          item{Primary("Try Offline Demo"){screen="home"}}
          item{Secondary("Activation Login"){screen="login"}}
        }
      }else if(screen=="login"){
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        Panel("Activation Login","Cloud student sign-in — pending live API verification",{screen="welcome"}){
          item{Hero("Welcome Back","အသုံးပြုသူ Account ကို Cloud Server နှင့် အတည်ပြုရန် လိုအပ်နေပါသေးသည်။")}
          item{OutlinedTextField(value=name,onValueChange={name=it},label={Text("Student Name")},modifier=Modifier.fillMaxWidth(),singleLine=true)}
          item{OutlinedTextField(value=code,onValueChange={code=it},label={Text("Activation Code")},modifier=Modifier.fillMaxWidth(),singleLine=true)}
          item{Info("Cloud Login is not enabled in this demo APK. Do not enter production credentials.")}
          item{Primary("Continue in Offline Demo"){screen="home"}}
        }
      }else{
        Scaffold(containerColor=bg,bottomBar={
          NavigationBar(containerColor=Color.White,tonalElevation=4.dp){
            listOf(Triple("home","⌂","Home"),Triple("courses","▤","Learn"),Triple("talk","◉","Practice"),Triple("exam","▣","Exam"),Triple("profile","♙","Profile")).forEach { (id,ico,label)->
              NavigationBarItem(selected=screen==id,onClick={screen=id},icon={Text(ico,fontSize=21.sp)},label={Text(label,maxLines=1)},alwaysShowLabel=true)
            }
          }
        }){
          Box(Modifier.padding(it)){
            when(screen){
              "home"->Panel("English LanPya","Hello, Student! • Ready to learn?"){ 
                item{Hero("Learn English Step by Step","အင်္ဂလိပ်စာကို နေ့စဉ် လေ့လာကြပါစို့။")}
                item{ProgressBlock(completed.size,sampleLessons.size)}
                item{Title("Explore Learning")}
                item{Feature("☁","Server Sync Library","Approved Teachers, Topics, Courses, Let\u0027s Talk"){screen="drive"}}
                item{QuickGrid(listOf(Triple("🎓","Courses","courses"),Triple("👩‍🏫","Teachers","teachers"),Triple("📚","Topics","topics"),Triple("💬","Let's Talk","talk"))){screen=it}}
                item{Title("Continue Learning")}
                item{Feature("▶","A1 • Introducing Yourself","8-step beginner lesson") {openLesson(sampleLessons.first())}}
                item{Title("Recent Lessons")}
                items(sampleLessons.take(3)){ name->RowItem("📘",name,"A1 Beginner • Original sample"){openLesson(name)}}
                item{Info("Offline sample lessons only. Real Google Drive content requires publishing approval and secure streaming.")}
              }
              "drive"->ServerLearningLibrary(onBack={screen="home"})
              "teachers"->Panel("Teachers","Google Drive • Teacher Sources",{screen="home"}){
                item{SearchNote("Browse teachers • Source metadata not downloaded")}
                items(teachers){ teacher->RowItem("👩‍🏫","Teacher $teacher","English video learning • Drive catalogue"){sourceName=teacher;screen="teacher-detail"}}
              }
              "teacher-detail"->Panel("Teacher $sourceName","Teacher profile / lessons",{screen="teachers"}){
                item{Hero("Learn with $sourceName","မူရင်း Google Drive က Teacher Videos တွေကို Cloud Review ပြီးမှ ဖွင့်ပါမယ်။")}
                item{Title("Offline Sample Lessons")}
                items(sampleLessons.take(5)){name->RowItem("🎞",name,"Demo lesson • Not a teacher video"){openLesson(name)}}
              }
              "topics"->Panel("English Topics","Grammar • Vocabulary • Speaking • more",{screen="home"}){
                item{SearchNote("14 category examples • Actual counts require Drive Sync")}
                items(topics){ topic->RowItem("📚",topic,"Explore topic lessons"){topicName=topic;screen="topic-detail"}}
              }
              "topic-detail"->Panel(topicName,"Topic lessons",{screen="topics"}){
                item{Hero("Explore $topicName","အကြောင်းအရာအလိုက် သင်ခန်းစာများ")}
                items(sampleLessons.take(6)){name->RowItem("▶",name,"Original A1 demo"){openLesson(name)}}
              }
              "courses"->Panel("Courses","CEFR A1–C2 Learning Path",{screen="home"}){
                item{Hero("Choose Your Level","500 lessons = curriculum target, not published content.")}
                items(cefr){level->RowItem("🎓",level,"Planned curriculum • sample available at A1"){courseName=level;screen="course-detail"}}
              }
              "course-detail"->Panel(courseName,"Course overview",{screen="courses"}){
                item{ProgressBlock(completed.size,sampleLessons.size)}
                item{Hero("Unit 1 — Introducing Yourself","Learn English using eight interactive steps.")}
                item{Title("Course Units")}
                items(listOf("Unit 1 • Introductions","Unit 2 • Family & Friends","Unit 3 • Daily Activities","Unit 4 • Places & Directions")){name->RowItem("▤",name,"Open unit / offline sample"){screen="unit"}}
              }
              "unit"->Panel("Unit 1 — Introductions","Course lesson list",{screen="course-detail"}){
                items(sampleLessons){name->RowItem(if(completed.contains(name))"✓" else "▶",name,if(completed.contains(name))"Completed demo" else "8 interactive lesson steps"){openLesson(name)}}
              }
              "talk"->Panel("Let's Talk","Conversation collections",{screen="home"}){
                item{Hero("Practice Real Conversations","အသုံးဝင်တဲ့ English Conversation တွေကို လေ့ကျင့်ပါ။")}
                items(talkCollections){name->RowItem("💬",name,"Conversation practice • sample"){topicName=name;screen="talk-detail"}}
              }
              "talk-detail"->Panel(topicName,"Conversation detail",{screen="talk"}){
                item{Hero("Meeting a New Friend","Hello! My name is Anna. Nice to meet you.")}
                item{Primary("▶ Listen to Example"){say("Hello. My name is Anna. Nice to meet you.")}}
                item{Feature("📘","Conversation Lesson","Vocabulary, Listening and Speaking"){openLesson(sampleLessons[0])}}
              }
              "lesson"->Panel(lessonName,"A1 Beginner • Unit 1",{screen="unit"}){
                item{StepProgress(step)}
                when(step){
                  0->{item{Hero("Introducing Yourself","သင်ခန်းစာ ရည်ရွယ်ချက်နှင့် နမူနာများ။")};item{Feature("🎯","Learning Objectives","Introduce yourself. Use greetings. Ask simple questions.")};item{Phrase("Hello! My name is Anna.",{say("Hello! My name is Anna.")})}}
                  1->{item{Title("New Words • စကားလုံးများ")};items(listOf("hello" to "မင်္ဂလာပါ","name" to "အမည်","friend" to "မိတ်ဆွေ","family" to "မိသားစု","student" to "ကျောင်းသား/သူ","teacher" to "ဆရာ/ဆရာမ")){(word,meaning)->Phrase("$word — $meaning",{say(word)})}}
                  2->{item{Hero("Grammar","My name is... / I am...")};item{Feature("📗","To Be","I am • You are • He is • She is • We are • They are")};item{Phrase("My name is Anna. I am a student.",{say("My name is Anna. I am a student.")})}}
                  3->{item{Hero("Listening Practice","နားထောင်ပြီး ထပ်ပြောကြည့်ပါ။")};item{Primary("▶ Play English Conversation"){say("Hello! My name is Anna. Nice to meet you. Where are you from? I am from Myanmar.")}};item{Info("This example uses the phone's text-to-speech engine; it is not a licensed Google Drive video.")}}
                  4->{item{Hero("Speaking Practice","Read aloud: My name is Anna. I am from Myanmar.")};item{RecordingPractice()};item{Primary("▶ Listen to Model"){say("My name is Anna. I am from Myanmar.")}};item{Info("Recording stays locally in cache for preview. No AI score or cloud upload is performed.")}}
                  5->{item{Hero("Reading • My Family","Hello! My name is John. I am a student. My father is a teacher. My mother is a doctor. I have one sister. We live in Yangon.")};item{Phrase("My family lives in Yangon.",{say("My family lives in Yangon.")})}}
                  6->{item{Hero("Exercise","Choose the correct sentence.")};item{ChoiceList(choice,{choice=it},listOf("He am a student.","He is a student.","He are a student."),1)}}
                  else->{item{Hero("Quiz & Completion","A1 Beginner • Sample assessment")};item{ChoiceList(choice,{choice=it},listOf("I is a student.","I am a student.","I are a student."),1)};if(choice==1)item{Primary("✓ Complete Demo Lesson"){completed=completed+lessonName;message="Demo lesson completed";screen="progress"}}}
                }
                item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                  OutlinedButton(onClick={if(step==0)screen="unit" else step--},modifier=Modifier.weight(1f)){Text("← Previous")}
                  Button(onClick={if(step<7){step++;choice=-1}else{message="Answer the sample quiz to finish"}},modifier=Modifier.weight(1f),enabled=step<7){Text(if(step==7)"Complete" else "Next →")}
                }}
              }
              "progress"->Panel("My Progress","Local demo learning progress",{screen="home"}){
                item{ProgressBlock(completed.size,sampleLessons.size)}
                items(sampleLessons){name->RowItem(if(completed.contains(name))"✅" else "○",name,if(completed.contains(name))"Completed" else "Not completed"){openLesson(name)}}
                item{Info("This demo progress is stored in memory and resets when the app restarts. Production progress requires authenticated Cloud sync.")}
              }
              "exam"->Panel("Exams & Certificates","Release QA — not official",{screen="home"}){
                item{Hero("English Assessment","Practice quizzes work offline. Official JLPT-style graded English exams are not connected.")}
                item{Feature("✍","Practice Quiz","Try the eight-step A1 sample"){openLesson(sampleLessons[0]);step=7}}
                item{Feature("🏅","My Certificates","PDF and QR certificates require server verification"){screen="certificates"}}
              }
              "certificates"->Panel("My Certificates","Verification pending",{screen="exam"}){
                item{Hero("Certificate of Completion","Official certificates are issued only by the Cloud backend after verified exam results and identity checks.")}
                item{Info("No certificate is issued or forged in the offline demo.")}
              }
              "profile"->Panel("My Profile","Student settings",{screen="home"}){
                item{Hero("Demo Student","Offline UI preview • by AAII")}
                item{Feature("📈","My Progress","Completed example lessons: ${completed.size}"){screen="progress"}}
                item{Feature("🎓","Certificates","Verification service pending"){screen="certificates"}}
                item{Feature("🔐","Activation Login","Cloud authorization not enabled"){screen="login"}}
                item{Feature("⚙","Settings","API and privacy information"){screen="settings"}}
              }
              "settings"->Panel("App Settings","English LanPya Native v2.7.2",{screen="profile"}){
                item{Feature("☁","Google Drive Sources","Teachers • Topics • Courses • Let's Talk")}
                item{Feature("🛡","Privacy","Demo recordings stay on the phone; remove after leaving the screen.")}
                item{Feature("ℹ","Build type","Native UI Demo APK, not Production LMS")}
              }
              else->Panel("English LanPya","Page under construction",{screen="home"}){item{Info("Select Home to continue.")}}
            }
          }
        }
      }
    }
}

@Composable private fun Panel(title:String,sub:String,back:(()->Unit)?=null,body:LazyListScope.()->Unit){
  Column(Modifier.fillMaxSize()){
    Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal=16.dp,vertical=13.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
      if(back!=null)Text("‹",color=blue,fontSize=35.sp,modifier=Modifier.clickable(onClick=back))
      Text("📘",fontSize=24.sp)
      Column(Modifier.weight(1f)){Text(title,color=navy,fontSize=21.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(sub,color=muted,fontSize=11.sp,maxLines=2)}
    }
    LazyColumn(Modifier.fillMaxSize().background(bg),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=body)
  }
}
@Composable private fun Hero(title:String,sub:String){
  Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFE7F1FF)),shape=rounded,modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
      Text(title,color=navy,fontWeight=FontWeight.ExtraBold,fontSize=23.sp)
      Text(sub,color=Color(0xFF45658C),fontSize=14.sp,lineHeight=21.sp)
    }
  }
}
@Composable private fun Title(t:String){Text(t,color=navy,fontSize=17.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp))}
@Composable private fun Info(t:String){Text(t,color=muted,fontSize=12.sp,lineHeight=18.sp,modifier=Modifier.padding(8.dp))}
@Composable private fun Feature(icon:String,title:String,subtitle:String,onClick:(()->Unit)?=null){
  Card(shape=rounded,colors=CardDefaults.cardColors(containerColor=Color.White),modifier=Modifier.fillMaxWidth().then(if(onClick!=null)Modifier.clickable(onClick=onClick) else Modifier)){
    Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
      Text(icon,fontSize=28.sp);Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.SemiBold,color=navy);Text(subtitle,color=muted,fontSize=12.sp,lineHeight=17.sp)};if(onClick!=null)Text("›",color=blue,fontSize=23.sp)
    }
  }
}
@Composable private fun RowItem(icon:String,title:String,sub:String,onClick:()->Unit){Feature(icon,title,sub,onClick)}
@Composable private fun Primary(t:String,onClick:()->Unit){Button(onClick=onClick,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(13.dp)){Text(t,fontWeight=FontWeight.Bold)}}
@Composable private fun Secondary(t:String,onClick:()->Unit){OutlinedButton(onClick=onClick,modifier=Modifier.fillMaxWidth().height(50.dp)){Text(t)}}
@Composable private fun SearchNote(t:String){Info("🔎  $t")}
@Composable private fun ProgressBlock(done:Int,total:Int){
  Card(shape=rounded,colors=CardDefaults.cardColors(containerColor=Color.White),modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
      Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){Text("My Learning Progress",fontWeight=FontWeight.Bold,color=navy);Text("${if(total==0)0 else done*100/total}%",fontWeight=FontWeight.Bold,color=blue)}
      LinearProgressIndicator(progress={if(total==0)0f else done.toFloat()/total},modifier=Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(8.dp)),color=green,trackColor=Color(0xFFE6EDFA))
      Text("$done / $total offline demo lessons",color=muted,fontSize=12.sp)
    }
  }
}
@Composable private fun QuickGrid(items:List<Triple<String,String,String>>,go:(String)->Unit){
  Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
    items.chunked(2).forEach { pair->
      Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
        pair.forEach { (icon,title,id)->
          Card(Modifier.weight(1f).clickable{go(id)},shape=rounded,colors=CardDefaults.cardColors(containerColor=if(id=="talk")lavender else Color.White)){
            Column(Modifier.padding(18.dp).height(75.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){
              Text(icon,fontSize=29.sp);Text(title,color=navy,fontWeight=FontWeight.Bold,fontSize=13.sp)
            }
          }
        }
        if(pair.size==1)Spacer(Modifier.weight(1f))
      }
    }
  }
}
@Composable private fun StepProgress(step:Int){
  Card(shape=rounded,colors=CardDefaults.cardColors(containerColor=Color(0xFFE7F1FF))){
    Column(Modifier.padding(12.dp)){
      Text("Step ${step+1} of 8 — ${steps[step]}",color=navy,fontWeight=FontWeight.Bold)
      Spacer(Modifier.height(9.dp))
      LinearProgressIndicator(progress={(step+1)/8f},modifier=Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(5.dp)))
      Spacer(Modifier.height(8.dp))
      Text(steps.joinToString("  •  "),fontSize=10.sp,color=muted,lineHeight=14.sp)
    }
  }
}
@Composable private fun Phrase(t:String,onPlay:()->Unit){
  Card(shape=rounded,colors=CardDefaults.cardColors(containerColor=Color.White)){
    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){Text(t,Modifier.weight(1f),color=navy,fontWeight=FontWeight.SemiBold);TextButton(onClick=onPlay){Text("🔊 Play")}}
  }
}
@Composable private fun ChoiceList(choice:Int,choose:(Int)->Unit,answers:List<String>,correct:Int){
  Column(verticalArrangement=Arrangement.spacedBy(9.dp)){
    answers.forEachIndexed { i,a->
      OutlinedButton(onClick={choose(i)},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.outlinedButtonColors(contentColor=navy)){
        Text("${'A'+i}. $a",modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Start)
      }
    }
    if(choice>=0){
      Text(if(choice==correct)"✓ Correct! — မှန်ပါတယ်" else "Try again — ထပ်ကြိုးစားပါ။",color=if(choice==correct)green else Color(0xFFBC6514),fontWeight=FontWeight.Bold)
    }
  }
}
@Composable private fun RecordingPractice(){
  val context=LocalContext.current
  var recording by remember { mutableStateOf(false) }
  var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
  var output by remember { mutableStateOf<File?>(null) }
  var label by remember { mutableStateOf("Tap to record • အသံဖမ်းရန် နှိပ်ပါ") }
  fun stop(){
    try{recorder?.stop();label="Voice captured locally — not uploaded"}catch(_:Exception){label="Recording ended"}
    try{recorder?.release()}catch(_:Exception){}
    recorder=null;recording=false
  }
  val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->
    if(!granted)label="Microphone permission denied"
    else try{
      val file=File.createTempFile("lanpya_speech_",".m4a",context.cacheDir)
      @Suppress("DEPRECATION")
      val r=MediaRecorder()
      r.setAudioSource(MediaRecorder.AudioSource.MIC)
      r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
      r.setOutputFile(file.absolutePath)
      r.prepare();r.start()
      output=file;recorder=r;recording=true;label="Recording…"
    }catch(e:Exception){label="Audio recording unavailable"}
  }
  DisposableEffect(Unit){onDispose{
    if(recording)stop()
    output?.delete()
  }}
  Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=rounded){
    Column(Modifier.padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){
      Text("🎤",fontSize=48.sp)
      Text(label,textAlign=TextAlign.Center,color=navy)
      Button(onClick={if(recording)stop() else permission.launch(Manifest.permission.RECORD_AUDIO)},modifier=Modifier.fillMaxWidth()){
        Text(if(recording)"■ Stop Recording" else "● Start Recording")
      }
    }
  }
}
