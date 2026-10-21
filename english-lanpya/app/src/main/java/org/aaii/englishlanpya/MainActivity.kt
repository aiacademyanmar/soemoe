package org.aaii.englishlanpya

import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private data class Lesson(val title:String, val text:String, val quiz:String, val answers:List<String>,val correct:Int)
private val lessons=listOf(
    Lesson("Greetings & Introductions","Hello! My name is Anna. Nice to meet you.\nမိတ်ဆက်စကားပြောကို လေ့လာပါ။","Which greeting is used in the morning?",listOf("Good morning","Good night","Goodbye"),0),
    Lesson("Numbers & Ages","How old are you? I am twenty years old.\nအသက်မေးမြန်းပုံ။","Twenty means:",listOf("12","20","30"),1),
    Lesson("Daily Routines","I wake up at seven. I go to school.\nနေ့စဉ်လုပ်ငန်းများ။","Choose the correct sentence:",listOf("I wakes up","I wake up","I waking"),1))
class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LanPyaRoot(cacheDir) } }
    }
}
@Composable private fun LanPyaRoot(cache:File){
    var page by remember{ mutableStateOf("home") }
    var name by remember{ mutableStateOf("") }
    var activation by remember{ mutableStateOf("") }
    var message by remember{ mutableStateOf("BUILD QA Demo • Cloud authentication unverified") }
    var auth by remember{ mutableStateOf(false) }
    var choice by remember{ mutableIntStateOf(-1) }
    var index by remember{ mutableIntStateOf(0) }
    var recording by remember{ mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audio by remember { mutableStateOf<File?>(null) }
    val scope=rememberCoroutineScope()
    fun stopRecording(){try{recorder?.stop()}catch(_:Exception){}; try{recorder?.release()}catch(_:Exception){};recorder=null;recording=false}
    DisposableEffect(Unit){onDispose{stopRecording();audio?.delete()}}
    val microphone= rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ allowed ->
       if(!allowed){message="Microphone permission denied"}
       else try{
          val f=File.createTempFile("english_speech_",".m4a",cache)
          @Suppress("DEPRECATION")
          val r=MediaRecorder()
          r.setAudioSource(MediaRecorder.AudioSource.MIC)
          r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
          r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
          r.setOutputFile(f.absolutePath)
          r.prepare();r.start();audio=f;recorder=r;recording=true;message="Recording locally; not uploaded"
       }catch(e:Exception){message="Recording error: "+e.javaClass.simpleName}
    }
    Scaffold(topBar={Surface(tonalElevation=3.dp){Column(Modifier.fillMaxWidth().padding(16.dp)){Text("English LanPya",style=MaterialTheme.typography.titleLarge);Text("V2.7.1 BUILD QA • Not production",style=MaterialTheme.typography.labelSmall)}}}){ padding ->
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
      Text(message,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)
      when(page){
        "home" -> {
          Text("Learn English • မြန်မာလိုရှင်းလင်းချက်",style=MaterialTheme.typography.titleMedium)
          Button(onClick={page="login"}){Text("Activation Login (Cloud Test)")}
          Button(onClick={page="courses"}){Text("A1 Offline Demo Lessons")}
          Button(onClick={page="speaking"}){Text("Speaking Recorder QA")}
          Button(onClick={page="exam"}){Text("Exam & Certificate QA")}
          Text("Cloud activation, speaking upload, remote playback and certificate issuing are not yet end-to-end verified.")
        }
        "login" ->{
          OutlinedTextField(name,{name=it},label={Text("Student Name")})
          OutlinedTextField(activation,{activation=it},label={Text("Activation Code")})
          Button(onClick={
            if(name.isBlank()||activation.isBlank()){message="Enter name and activation code"}
            else scope.launch{
              message="Contacting configured HTTPS Cloud API..."
              val res=withContext(Dispatchers.IO){
                try{
                  val conn=URL(BuildConfig.API_BASE_URL+"auth/activate").openConnection() as HttpURLConnection
                  conn.requestMethod="POST";conn.connectTimeout=10000;conn.readTimeout=10000
                  conn.setRequestProperty("Content-Type","application/json");conn.doOutput=true
                  val payload=JSONObject().put("name",name.trim()).put("activation_code",activation.trim()).put("device_id","build-qa-device").toString()
                  conn.outputStream.use{it.write(payload.toByteArray(Charsets.UTF_8))}
                  val code=conn.responseCode
                  conn.disconnect()
                  "HTTP $code (response intentionally not stored in public QA build)"
                }catch(e:Exception){"Network error: "+e.javaClass.simpleName}
              }
              message=res
            }
          }){Text("Test Activation Endpoint")}
          Text("QA only: no session token saved. Device binding and complete login require the full native application.")
          OutlinedButton(onClick={page="home"}){Text("Back")}
        }
        "courses" -> {
          Text("A1 Original Demo — 3 Lessons",style=MaterialTheme.typography.titleMedium)
          LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){
            items(lessons.indices.toList()){i->OutlinedButton(onClick={index=i;choice=-1;page="lesson"}){Text("A1-${i+1}: ${lessons[i].title}")}}
          }
          OutlinedButton(onClick={page="home"}){Text("Back")}
        }
        "lesson" ->{
          Text(lessons[index].title,style=MaterialTheme.typography.titleLarge)
          Text(lessons[index].text)
          HorizontalDivider()
          Text(lessons[index].quiz)
          lessons[index].answers.forEachIndexed { i,a->
            OutlinedButton(onClick={choice=i}){Text("${if(choice==i)"✓ " else ""}$a")}
          }
          if(choice>=0)Text(if(choice==lessons[index].correct)"Correct! • မှန်ပါတယ်။" else "Try again • ထပ်ကြိုးစားပါ။")
          OutlinedButton(onClick={page="courses"}){Text("Back to Lessons")}
        }
        "speaking" ->{
          Text("Speaking Assessment — Recorder QA",style=MaterialTheme.typography.titleMedium)
          Text("Say: My name is Anna. I like learning English.")
          if(!recording){
            Button(onClick={microphone.launch(Manifest.permission.RECORD_AUDIO)}){Text("Record Practice Audio")}
          }else Button(onClick={stopRecording();message="Recording saved locally (not uploaded)"}){Text("Stop Recording")}
          Text("Local file: ${if(audio?.exists()==true) "Recorded" else "None"}")
          Text("Teacher upload, grading and AI pronunciation assessment are NOT active in this build.")
          OutlinedButton(onClick={page="home"}){Text("Back")}
        }
        "exam" ->{
          Text("Exam & Certificate Release Gate",style=MaterialTheme.typography.titleMedium)
          Text("Offline quiz is demo-only and does not generate an official grade.")
          Text("Live exam, speaking review and certificate QR/PDF require deployed Cloud LMS + tested student account.")
          OutlinedButton(onClick={page="courses"}){Text("Practice A1 Quiz")}
          OutlinedButton(onClick={page="home"}){Text("Back")}
        }
      }
    }}
}