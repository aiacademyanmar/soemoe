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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NAVY=Color(0xFF09133B)
private val INK=Color(0xFF132053)
private val PANEL=Color(0xFF1D2E70)
private val LIGHT=Color(0xFFFCFCFF)
private val MINT=Color(0xFF5AF1E3)
private val PINK=Color(0xFFFF76D5)
private val GOLD=Color(0xFFFFD965)
private val PURPLE=Color(0xFFA990FF)
private val SUB=Color(0xFFC6D3F9)
private val pageNames=listOf(
 "Splash & Welcome","Onboarding","Login & Guardian","Home Dashboard",
 "Learning Levels","Modules Explorer","Lesson Detail","Interactive Lesson",
 "AI Teacher Chat","eLibrary & Videos","Recorded Videos","Soe Moe Live Class",
 "AI Creator Studio","My Creations","Offline Downloads","Creative Tools",
 "Interactive Exercise","Quiz & Assessment","Learning Progress","Rewards & Badges",
 "Creator Community","Notifications","Parent Dashboard","My Profile",
 "Settings & Controls","Membership Plans","My Certificates","Help & Support","AI ON CAMPUS","Languages"
)
private val emoji=listOf("🚀","✨","🔐","🏠","🌈","🧠","📖","🎬","🤖","📚","🎞","🎥","🎨","🖼","⬇","🧰","✍","🧩","📈","🏆","💗","🔔","👨‍👩‍👧","🙂","⚙","👑","🎓","🛟","🏫","🌍")
private val modules=listOf(
 "Creator Mindset & AI Literacy","Digital Drawing & Visual Design","Storytelling & Digital Comics",
 "Video Basics & Creative Editing","Creative Coding & Mini Games","Digital Safety & Creator Showcase",
 "Professional Visual & Brand Design","AI Video Production & Storyboarding","Game & App Prototype Studio",
 "Creator Studio Team Production","Specialization Discovery Lab","Professional Portfolio & Demo Day",
 "Advanced Specialization Studio","Creator Rights, Safety & Professional Ethics","Team & Client Brief Simulation",
 "Production Sprint & Quality Assurance","Capstone Project & Mentor Review","Specialist Portfolio, Demo Day & Viva"
)
private val levels=listOf("Foundation Creator","Professional Creator","Specialized Professional")
private val accent=listOf(MINT,PURPLE,PINK,GOLD,Color(0xFF89D7FC))

class MainActivity:ComponentActivity() {
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   MaterialTheme(colorScheme=darkColorScheme(primary=MINT,secondary=PURPLE,background=NAVY,surface=PANEL,onSurface=LIGHT)) {
    AcademyApp()
   }
  }
 }
}

@Composable
private fun AcademyCard(title:String, detail:String="", symbol:String="✨", color:Color=MINT, onClick:(()->Unit)?=null) {
 val surface=Modifier.fillMaxWidth().padding(bottom=10.dp)
  .background(Brush.horizontalGradient(listOf(PANEL,Color(0xFF283F90))),RoundedCornerShape(20.dp))
  .border(1.dp,color.copy(alpha=.50f),RoundedCornerShape(20.dp))
  .then(if(onClick==null)Modifier else Modifier.clickable{onClick()})
 Column(surface.padding(15.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Text(symbol,fontSize=30.sp)
   Column(Modifier.weight(1f)) {
    Text(title,color=LIGHT,fontSize=16.sp,fontWeight=FontWeight.Bold)
    if(detail.isNotBlank())Text(detail,color=SUB,fontSize=12.sp,lineHeight=16.sp)
   }
   if(onClick!=null)Text("❯",fontSize=20.sp,color=color)
  }
 }
}
@Composable
private fun BigAction(title:String,onClick:()->Unit, gold:Boolean=false) {
 Button(onClick=onClick,modifier=Modifier.fillMaxWidth().padding(vertical=4.dp).heightIn(min=46.dp),
  colors=ButtonDefaults.buttonColors(containerColor=if(gold)GOLD else MINT,contentColor=INK),
  shape=RoundedCornerShape(14.dp)){
  Text(title,fontWeight=FontWeight.ExtraBold,fontSize=14.sp)
 }
}
@Composable
private fun SceneHero(title:String,subtitle:String,symbol:String="🚀") {
 Box(Modifier.fillMaxWidth().padding(bottom=13.dp)
  .background(Brush.linearGradient(listOf(Color(0xFF1E71F0),Color(0xFF8539D4),Color(0xFF1A49B1))),
   RoundedCornerShape(24.dp))
  .border(1.dp,Color(0xFF8AE5FF),RoundedCornerShape(24.dp)).padding(18.dp)){
  Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
   Text(symbol,fontSize=45.sp)
   Text(title,color=LIGHT,fontWeight=FontWeight.ExtraBold,fontSize=24.sp,lineHeight=27.sp)
   Text(subtitle,color=LIGHT,fontSize=13.sp,lineHeight=18.sp)
  }
 }
}
@Composable
private fun Heading(title:String,detail:String="") {
 Column(Modifier.padding(vertical=8.dp)){
  Text(title,color=LIGHT,fontWeight=FontWeight.ExtraBold,fontSize=21.sp)
  if(detail.isNotBlank())Text(detail,color=SUB,fontSize=12.sp)
 }
}
@Composable
private fun InfoWarning() {
 Surface(Modifier.fillMaxWidth().padding(bottom=12.dp),color=Color(0xFF513756),shape=RoundedCornerShape(13.dp)) {
  Text("🔒 PROTOTYPE ONLY — No live AI, Zoom, Google Drive, membership payment or public community service is connected.",
   modifier=Modifier.padding(12.dp),color=GOLD,fontSize=11.sp,lineHeight=16.sp)
 }
}
@Composable
private fun AcademyApp() {
 val ctx=LocalContext.current
 var uiLanguage by remember { mutableStateOf(KidLanguages.load(ctx)) }
 var page by remember{mutableIntStateOf(4)}
 var previous by remember{mutableIntStateOf(4)}
 var index by remember{mutableStateOf(false)}
 var level by remember{mutableIntStateOf(1)}
 var selectedModule by remember{mutableIntStateOf(0)}
 var lessonNumber by remember{mutableIntStateOf(1)}
 var step by remember{mutableIntStateOf(0)}
 var qAnswer by remember{mutableIntStateOf(-1)}
 var completed by remember{mutableIntStateOf(0)}
 var projectInput by remember{mutableStateOf("")}
 var exerciseInput by remember{mutableStateOf("")}
 var studentText by remember{mutableStateOf("")}
 var aiAnswer by remember{mutableStateOf("Hello, creator! Ask about a lesson or ask for a fun creative activity. This is a scripted teaching demo.")}
 var projects by remember{mutableStateOf(listOf<String>())}
 var reduceMotion by remember{mutableStateOf(false)}
 var safeMode by remember{mutableStateOf(true)}
 fun open(n:Int) { previous=page;page=n;index=false }
 fun back() { if(index) index=false else if(page!=4)page=4 }
 BackHandler(index||page!=4) { back() }
 CompositionLocalProvider(LocalLayoutDirection provides if(uiLanguage in setOf("ar","he","fa","ur","ps","sd","dv","yi","ug","ks")) LayoutDirection.Rtl else LayoutDirection.Ltr) {
 Scaffold(containerColor=NAVY,bottomBar={
  NavigationBar(containerColor=Color(0xFF142054),tonalElevation=0.dp) {
   listOf(4,5,13,10,24).forEachIndexed { i,n->
    NavigationBarItem(
     selected=page==n || (i==1&&page in listOf(6,7,8,17,18)),
     onClick={open(n)},
     icon={Text(listOf("🏠","📖","🎨","📚","👤")[i],fontSize=20.sp)},
     label={Text(listOf("home","learn","create","library","profile").map{KidLanguages.value(uiLanguage,it)}[i],fontSize=10.sp,maxLines=1)},
     alwaysShowLabel=true
    )
   }
  }
 }){inset->
  Column(Modifier.fillMaxSize().padding(inset)){
   Row(Modifier.fillMaxWidth().background(Color(0xFF13225C)).padding(horizontal=14.dp,vertical=9.dp),
    verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
    Column {
     Text("✦ AAII",fontSize=23.sp,fontWeight=FontWeight.Black,color=MINT)
     Text("KID CREATOR ACADEMY • V2.4 • 200 LANGUAGES",fontSize=9.sp,color=LIGHT,letterSpacing=1.sp)
    }
    TextButton(onClick={index=!index}){
     Text(if(index)"Close ✕" else "30 Pages ☰",color=GOLD,fontSize=13.sp,fontWeight=FontWeight.Bold)
    }
   }
   LazyColumn(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NAVY,Color(0xFF0E1D5C),NAVY)))
    .padding(horizontal=14.dp),contentPadding=PaddingValues(top=14.dp,bottom=26.dp)){
    if(index){
     item{SceneHero("28 Screen Explorer","Open any page inside the Android Native Prototype.","🌈")}
     items(pageNames.indices.toList()){i->
      AcademyCard(String.format("%02d",i+1)+"  "+pageNames[i],"Open native screen",emoji[i],accent[i%accent.size]){open(i+1)}
     }
    }else {
     item {
      Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.padding(bottom=12.dp)){
       Text(emoji[page-1],fontSize=26.sp)
       Spacer(Modifier.width(9.dp))
       Column {
        Text(when(page){4->KidLanguages.value(uiLanguage,"home");5->KidLanguages.value(uiLanguage,"learn");10->KidLanguages.value(uiLanguage,"library");13->KidLanguages.value(uiLanguage,"create");24->KidLanguages.value(uiLanguage,"profile");25->KidLanguages.value(uiLanguage,"settings");30->KidLanguages.value(uiLanguage,"languages");else->pageNames[page-1]},fontWeight=FontWeight.ExtraBold,fontSize=21.sp,color=LIGHT)
        Text("NK-"+String.format("%02d",page)+" • Native Compose UI prototype",color=SUB,fontSize=11.sp)
       }
      }
     }
     when(page){
      1 -> {
       item{SceneHero("Welcome to AAII!","Create · Learn · Play · Grow. Welcome to a world of imagination.","🚀")}
       item{BigAction("Start My Creative Adventure →",{open(2)},true)}
      }
      2 -> {
       item{SceneHero("Discover • Create • Learn","Choose something wonderful and practice a little each day.","🌈")}
       items(listOf(4,13,9)){n->AcademyCard(pageNames[n-1],"Explore a playful learning feature",emoji[n-1]){open(n)}}
       item{BigAction("Let's Get Started →",{open(3)},true)}
      }
      3 -> {
       item{Phase03SyncPanel("account")}
       item{SceneHero("Welcome Back!","This build runs in guest demo mode. No account is required.","🔐")}
       item{BigAction("Continue as Guest →",{open(4)},true)}
       item{AcademyCard("Parent / Guardian Access","Guardian consent needed for connected services","👨‍👩‍👧"){open(23)}}
       item{InfoWarning()}
      }
      4 -> {
       item{SceneHero("Hi, Young Creator!","Learn, imagine and make something great today!","👋")}
       item{AcademyCard("My Learning Progress", completed.toString()+" lessons completed in this APK session","🏆"){open(19)}}
       items(listOf(5,13,9,11,10,12,29,30,20,23)){n->AcademyCard(pageNames[n-1],"Tap to open this part of your creator world",emoji[n-1],accent[n%accent.size]){open(n)}}
      }
      5 -> {
       item{Phase03SyncPanel("learn")}
       item{SceneHero("Choose Your Learning Level","3 levels • 18 modules • 72 lessons","🌱")}
       items(levels.indices.toList()){i->
        AcademyCard("Level "+(i+1)+" · "+levels[i],"6 modules • 24 lessons",listOf("🌱","🚀","👑")[i],accent[i]){level=i+1;open(6)}
       }
      }
      6 -> {
       item{Heading("Level "+level+" Modules","6 modules • 4 lessons in each")}
       items((0..5).toList()){i->
        val m=(level-1)*6+i
        AcademyCard("Module "+(m+1)+" · "+modules[m],"Browse lesson preview and activities",listOf("🤖","🎨","📖","🎥","🎮","🛡")[i],accent[i%accent.size]){selectedModule=m;lessonNumber=1;open(7)}
       }
      }
      7 -> {
       item{SceneHero(modules[selectedModule],"Module "+(selectedModule+1)+" • 4 lessons · Learn at your own pace","📖")}
       items((1..4).toList()){n->
        AcademyCard("Lesson "+(selectedModule+1)+"."+n,when(n){1->"Discover and explore";2->"Watch and learn";3->"Practice your skills";else->"Create and reflect"},"✨"){
         lessonNumber=n;step=0;open(8)
        }
       }
       item{BigAction("Watch Recorded Lesson (Demo)",{open(11)})}
      }
      8 -> {
       if(selectedModule==0 && lessonNumber==1){
        item { Lesson01Detailed(uiLanguage){ score -> completed += 1 } }
       }else{
       item{SceneHero("Learn Step by Step","Module "+(selectedModule+1)+" • Lesson "+lessonNumber+" • Step "+(step+1)+"/4","🎬")}
       item{LinearProgressIndicator(progress={ (step+1)/4f },modifier=Modifier.fillMaxWidth().padding(bottom=16.dp),color=MINT,trackColor=PANEL)}
       item{AcademyCard(listOf("Discover Your Idea","Imagine a Solution","Create a First Draft","Improve Your Project")[step],
          listOf("Find something interesting you want to explore.","Think of at least two ideas for your creation.","Use drawing, writing or a simple prototype.","Show your work and make one thoughtful improvement.")[step],"💡")}
       item{BigAction(if(step<3)"Next Learning Step →" else "Try Exercise →",{if(step<3)step++ else open(17)},true)}
       item{AcademyCard("Ask AI Teacher","Get a friendly scripted explanation","🤖"){open(9)}}
      }
       }
      9 -> {

      item { Phase04ConnectionPanel(if(page==9)"ai" else if(page==12)"classes" else if(page==11)"videos" else "library") }

       item{SceneHero("Meet Spark, your AI Friend","Friendly creative lesson guidance.","🤖")}
       item{InfoWarning()}
       item{AcademyCard("Teacher Spark",aiAnswer,"💬")}
       item {
        OutlinedTextField(value=studentText,onValueChange={studentText=it},label={Text("Ask a learning question")},modifier=Modifier.fillMaxWidth(),singleLine=false)
       }
       item{BigAction("Ask the Teacher",{aiAnswer=when {
        studentText.contains("quiz",true)->"Great idea! Open Quiz & Assessment and practice your knowledge."
        studentText.contains("draw",true)||studentText.contains("art",true)->"Draw a simple shape, add details, then choose colors that express your idea."
        studentText.contains("code",true)->"Start with small steps: pick a character, choose a movement and test it."
        else->"Let's learn step by step! First, find one creative idea. Make a simple draft and improve it."
       };studentText=""})}
       item{AcademyCard("Try a Quiz","Practice your learning","🧩"){open(18)}}
      }
      10 -> {

      item { Phase04ConnectionPanel(if(page==9)"ai" else if(page==12)"classes" else if(page==11)"videos" else "library") }

       item{SceneHero("eLibrary & Videos Hub","Read · Watch · Learn · Create","📚")}
       items(listOf(11,15,7,9)){n->AcademyCard(pageNames[n-1],"Explore learning resources",emoji[n-1]){open(n)}}
       item{InfoWarning()}
      }
      11 -> {

      item { Phase04ConnectionPanel(if(page==9)"ai" else if(page==12)"classes" else if(page==11)"videos" else "library") }

       item{SceneHero("Recorded Video Lessons","Native video player integration is planned.","🎞")}
       item{InfoWarning()}
       item{BigAction("Open Offline Lesson Instead",{open(8)})}
      }
      12 -> {
       item { CoursePromotionsPanel("online") }

      item { Phase04ConnectionPanel(if(page==9)"ai" else if(page==12)"classes" else if(page==11)"videos" else "library") }

       item{SceneHero("Soe Moe Live Class","Human teacher • Interactive classroom • Zoom integration pending","👨‍🏫")}
       item{InfoWarning()}
       item{AcademyCard("Join Class","No real meeting has been configured","🎥"){open(28)}}
       item{AcademyCard("Class Reminder","Teacher-managed schedule will be shown here","🔔"){open(22)}}
      }
      13 -> {
       item{SceneHero("Creator Studio","Your imagination is your superpower!","🎨")}
       items(listOf(16,14,17,9)){n->AcademyCard(pageNames[n-1],"Explore fun creation activities",emoji[n-1]){open(n)}}
      }
      14 -> {
       item{Heading("My Creations","Local session-only sample projects")}
       item{OutlinedTextField(value=projectInput,onValueChange={projectInput=it},label={Text("New project title")},modifier=Modifier.fillMaxWidth())}
       item{BigAction("Save Project Idea",{if(projectInput.isNotBlank()){projects=projects+projectInput;projectInput=""}})}
       items(projects){p->AcademyCard(p,"Saved in this open app session","🎨")}
       if(projects.isEmpty())item{AcademyCard("No projects saved yet","Try naming your first creative idea","🌈")}
      }
      15 -> {
       item{SceneHero("Offline Learning","Practice anywhere using built-in app screens.","⬇")}
       item{AcademyCard("App UI Available Offline","This 28-screen layout is packaged in the APK.","✅")}
       item{AcademyCard("Full 72 Lesson Data","Import the complete private curriculum source to enable detailed content.","📚")}
      }
      16 -> {
       item{SceneHero("Creative Learning Tools","Design art, stories and game ideas.","🎮")}
       item{AcademyCard("Drawing Studio","Advanced native drawing canvas planned. WEB prototype has an interactive canvas.","🖌"){open(13)}}
       item{AcademyCard("Storyboard Builder","Save a project idea in My Creations.","📖"){open(14)}}
       item{AcademyCard("Practice with Teacher","Ask the scripted AI guide.","🤖"){open(9)}}
      }
      17 -> {
       item{Heading("Interactive Exercise","Reflection and practice")}
       item{AcademyCard("Your Creative Challenge","Describe what you would make and why.","✍")}
       item{OutlinedTextField(value=exerciseInput,onValueChange={exerciseInput=it},label={Text("Your idea / exercise response")},modifier=Modifier.fillMaxWidth(),minLines=3)}
       item{BigAction("Save Exercise (Session)",{if(exerciseInput.isNotBlank())open(18)})}
      }
      18 -> {
       item{SceneHero("Quiz Time!","What does a creator do?","🧩")}
       items(listOf("Copies someone else's work","Observes, imagines and makes new things","Never practices or improves")){ans->
        AcademyCard(ans,if(qAnswer==listOf("Copies someone else's work","Observes, imagines and makes new things","Never practices or improves").indexOf(ans))"Selected" else "Tap to choose","🔹"){qAnswer=listOf("Copies someone else's work","Observes, imagines and makes new things","Never practices or improves").indexOf(ans)}
       }
       item {
        BigAction("Submit Answer",{if(qAnswer==1){completed++;open(20)}else open(17)},true)
       }
      }
      19 -> {
       item{Phase03SyncPanel("status")}
       item{SceneHero("Your Learning Progress","Keep learning and creating every day!","📈")}
       item{AcademyCard("Lessons Completed",completed.toString()+" in this APK session","✅")}
       item{AcademyCard("Creator Points",(completed*50).toString()+" practice XP","🌟")}
       item{AcademyCard("My Portfolio",projects.size.toString()+" saved ideas","🎨"){open(14)}}
      }
      20 -> {
       item{SceneHero("Rewards & Badges","Celebrate every little creative achievement!","🏆")}
       items(listOf("First Lesson Badge","Creative Explorer","Future Storyteller","AI Explorer")){b->AcademyCard(b,"Rewards shown as UI previews","🌟")}
       item{BigAction("See My Certificates",{open(27)})}
      }
      21 -> {
       item{SceneHero("Creator Community","A safe, supervised place for shared ideas.","💗")}
       item{InfoWarning()}
       item{AcademyCard("Private Projects Only","Public posts are not enabled in this build.","🔒"){open(14)}}
      }
      22 -> {
       item{Heading("Notifications","Sample learning reminders")}
       items(listOf("Lesson Completed!","New Creative Badge","Teacher Class Reminder","Time for a Creative Challenge")){x->
        AcademyCard(x,"Preview notification (not a real push message)","🔔")
       }
      }
      23 -> {
       item{SceneHero("Parent Dashboard","Support creativity and learning safely.","👨‍👩‍👧")}
       item{AcademyCard("Learning Summary",completed.toString()+" completed lesson practice attempts","📈"){open(19)}}
       item{AcademyCard("Privacy & Safety","Guardian access and parental controls are planned","🛡"){open(25)}}
       item{InfoWarning()}
      }
      24 -> {
       item{SceneHero("My Creator Profile","Young Creator • Learning by doing","🙂")}
       item{AcademyCard("My Learning",(completed*50).toString()+" XP","📚"){open(19)}}
       item{AcademyCard("My Creations",projects.size.toString()+" saved project ideas","🎨"){open(14)}}
       item{AcademyCard("Certificates","Sample certificate preview","🎓"){open(27)}}
       item{AcademyCard(KidLanguages.value(uiLanguage,"settings"),"Controls and accessibility","⚙"){open(25)}}
       item{AcademyCard("🌍 "+KidLanguages.value(uiLanguage,"languages"),"Select from 200 languages","🌐"){open(30)}}
      }
      25 -> {
       item { Phase05ConnectionCheck() }
       item{Phase03SyncPanel("account")}
       item{Heading("Settings & Parental Control","Comfortable and safe by design")}
       item{AcademyCard("🌍 "+KidLanguages.value(uiLanguage,"languages"),KidLanguages.displayName(uiLanguage)+" · 200 languages available","🌐"){open(30)}}
       item {
        Row(Modifier.fillMaxWidth().padding(vertical=9.dp),verticalAlignment=Alignment.CenterVertically){
         Text("🛡  Safe Learning Mode",Modifier.weight(1f),color=LIGHT)
         Switch(checked=safeMode,onCheckedChange={safeMode=it})
        }
       }
       item {
        Row(Modifier.fillMaxWidth().padding(vertical=9.dp),verticalAlignment=Alignment.CenterVertically){
         Text("✨  Reduce Animations",Modifier.weight(1f),color=LIGHT)
         Switch(checked=reduceMotion,onCheckedChange={reduceMotion=it})
        }
       }
       item{InfoWarning()}
      }
      26 -> {
       item{SceneHero("Membership Plans","Choose a plan in the future guardian portal.","👑")}
       item{InfoWarning()}
       item{AcademyCard("Free Plan","Basic sample educational activities","🌱")}
       item{AcademyCard("Premium Plan","Future access to lessons, AI and teacher support","💎")}
       item{AcademyCard("Family Plan","Parent dashboard and multiple learner profiles","👨‍👩‍👧")}
      }
      27 -> {
       item{SceneHero("My Certificates","Your hard work deserves a celebration!","🎓")}
       item{AcademyCard("AAII Creator Certificate","SAMPLE • Certificate is not valid until issued by an administrator","🏅")}
       item{InfoWarning()}
      }
      29 -> {
       item { CoursePromotionsPanel("campus") }
      }
      30 -> {
       item { KidLanguageCenter(uiLanguage) { code -> KidLanguages.save(ctx,code);uiLanguage=code } }
      }
      28 -> {
       item{SceneHero("Help & Support","We're here to help young creators.","🛟")}
       items(listOf("How do I start learning?","How can I ask the AI Teacher?","How do I get live classes?","Where is the Google Drive library?")){s->
        AcademyCard(s,"Contact your guardian or teacher for support.","❔")
       }
       item{InfoWarning()}
      }
     }
     item {
      Spacer(Modifier.height(12.dp))
      Text("AAII V2.4 • 200 LANGUAGE SETTINGS",color=SUB,fontSize=10.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
     }
    }
   }
  }
 }
 }
}
