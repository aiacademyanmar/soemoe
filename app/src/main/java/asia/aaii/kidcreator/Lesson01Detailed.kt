package asia.aaii.kidcreator

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AQUA = Color(0xFF5AF1E3)
private val WHITE = Color(0xFFFAFCFF)
private val SUBTLE = Color(0xFFD5E3FC)
private val GOLD = Color(0xFFFFDC80)
private val SKY = Color(0xFF829BFF)
private val ROSE = Color(0xFFFF8FC2)

internal object Lesson01Store {
    private const val PREF = "aaii_lesson_1_1"
    private fun prefs(ctx:Context)=ctx.getSharedPreferences(PREF,Context.MODE_PRIVATE)
    fun stage(ctx:Context)=prefs(ctx).getInt("stage",0).coerceIn(0,6)
    fun saveStage(ctx:Context,n:Int){prefs(ctx).edit().putInt("stage",n.coerceIn(0,6)).apply()}
    fun isCompleted(ctx:Context)=prefs(ctx).getBoolean("completed",false)
    fun score(ctx:Context)=prefs(ctx).getInt("quiz_score",0)
    fun complete(ctx:Context,score:Int){
        require(score in 4..5)
        prefs(ctx).edit().putBoolean("completed",true).putInt("quiz_score",score).putInt("stage",6).apply()
    }
}
private data class Question(
    val mm:String,val en:String,val optionsMm:List<String>,val optionsEn:List<String>,
    val correct:Int,val explainMm:String,val explainEn:String)
private val questions=listOf(
    Question("AI ဆိုတာ ဘာရဲ့ အတိုကောက်လဲ။","What does AI stand for?",
        listOf("Artistic Internet","Artificial Intelligence (ဉာဏ်ရည်တု)","Automatic Ice"),
        listOf("Artistic Internet","Artificial Intelligence","Automatic Ice"),1,
        "AI ဆိုတာ Artificial Intelligence — လူတွေ တည်ဆောက်ထားတဲ့ ဉာဏ်ရည်တုနည်းပညာပါ။",
        "AI stands for Artificial Intelligence, technology made by people."),
    Question("ဘယ်ဟာမှာ AI ပါဝင်နိုင်ခြေ များသလဲ။","Which example is likely to use AI?",
        listOf("အသံကို စာအဖြစ် ပြောင်းပေးတဲ့ App","ရိုးရိုး Alarm နာရီ","လက်နှိပ် မီးခလုတ်"),
        listOf("Speech-to-text app","Basic alarm clock","Manual light switch"),0,
        "Speech-to-text က အသံထဲက Pattern တွေကို မှတ်မိပြီး စာအဖြစ် ပြောင်းပါတယ်။",
        "Speech-to-text recognizes patterns in speech to produce words."),
    Question("AI အဖြေကို ဘယ်လို အသုံးပြုသင့်သလဲ။","How should you handle an AI answer?",
        listOf("အမြဲတမ်းမှန်တယ်လို့ ယုံမယ်","အချက်အလက် ပြန်စစ်မယ်","မစဉ်းစားဘဲ မျှဝေမယ်"),
        listOf("Believe it is always correct","Check it carefully","Share it without thinking"),1,
        "AI က မှားနိုင်ပါတယ်။ စာအုပ်၊ ဆရာနဲ့ ယုံကြည်ရတဲ့ အရင်းအမြစ်တွေကို ပြန်စစ်ပါ။",
        "AI can be wrong. Verify important facts with trusted sources."),
    Question("AI နဲ့ ဘာကို မမျှဝေသင့်သလဲ။","What must you not share with AI?",
        listOf("ကြောင်ပုံ အိုင်ဒီယာ","အရောင်ရွေးချယ်မှု","Password နဲ့ အိမ်လိပ်စာ"),
        listOf("A cat drawing idea","A color palette","Your password and home address"),2,
        "Password၊ ဖုန်းနံပါတ်၊ အိမ်လိပ်စာလို ကိုယ်ရေးအချက်အလက်တွေကို မမျှဝေပါနဲ့။",
        "Never share private information, passwords or home addresses."),
    Question("ဘယ် Prompt က ရှင်းလင်းသလဲ။","Which prompt is more specific?",
        listOf("တစ်ခုခုရေး","ကောင်းကောင်းလုပ်","အသက် ၁၀ နှစ်ကလေးအတွက် သစ်ပင်ကာကွယ်ရေး ပုံပြင် ၄ ကြောင်းရေးပေးပါ"),
        listOf("Write something","Do it well","Write a four-line story about protecting trees for a 10-year-old"),2,
        "ကောင်းတဲ့ Prompt မှာ အလုပ်၊ ခေါင်းစဉ်၊ ဖတ်မယ့်သူနဲ့ ပုံစံကို ရှင်းပြပါတယ်။",
        "A good prompt specifies the task, topic, audience and format.")
)
@Composable
private fun LessonCard(emoji:String,title:String,body:String,tint:Color=AQUA) {
    Surface(color=Color(0xFF1B2D68),shape=RoundedCornerShape(19.dp),
        modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)
            .border(1.dp,tint.copy(alpha=.45f),RoundedCornerShape(19.dp))){
        Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
            Text(emoji+"  "+title,color=WHITE,fontWeight=FontWeight.Bold,fontSize=18.sp,lineHeight=25.sp)
            Text(body,color=SUBTLE,fontSize=14.sp,lineHeight=23.sp)
        }
    }
}
@Composable
private fun LessonAction(text:String,enabled:Boolean=true,onClick:()->Unit){
    Button(onClick=onClick,enabled=enabled,modifier=Modifier.fillMaxWidth().padding(vertical=5.dp).heightIn(min=50.dp),
        shape=RoundedCornerShape(15.dp),
        colors=ButtonDefaults.buttonColors(containerColor=AQUA,contentColor=Color(0xFF152554))){
        Text(text,fontWeight=FontWeight.Bold)
    }
}
@Composable
internal fun Lesson01Detailed(language:String,onPassed:(Int)->Unit) {
    val ctx=androidx.compose.ui.platform.LocalContext.current
    val my=language=="my"
    fun t(mm:String,en:String)=if(my)mm else en
    var step by remember{mutableIntStateOf(Lesson01Store.stage(ctx))}
    var theme by remember{mutableIntStateOf(0)}
    var format by remember{mutableIntStateOf(0)}
    var exampleChoice by remember{mutableIntStateOf(-1)}
    var answers by remember{mutableStateOf(List(5){-1})}
    var graded by remember{mutableStateOf(false)}
    var completed by remember{mutableStateOf(Lesson01Store.isCompleted(ctx))}
    var savedScore by remember{mutableIntStateOf(Lesson01Store.score(ctx))}
    val headings=if(my) listOf("မိတ်ဆက်","AI ဆိုတာဘာလဲ","AI ဘယ်လိုအလုပ်လုပ်လဲ","နေ့စဉ်ဘဝ AI","Prompt ရေးကြမယ်","ဘေးကင်းရေး","Quiz")
      else listOf("Welcome","What is AI?","How does AI work?","Everyday AI","Write a Prompt","Safety","Quiz")
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(7.dp)){
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF0F90CC),Color(0xFF7135BD),Color(0xFF173F9F))),
            RoundedCornerShape(23.dp)).border(1.dp,AQUA.copy(alpha=.65f),RoundedCornerShape(23.dp))){
            Column(Modifier.padding(19.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                Text("🤖🌈✨",fontSize=39.sp)
                Text(t("သင်ခန်းစာ ၁.၁ — AI ဆိုတာဘာလဲ။","Lesson 1.1 — What Is Artificial Intelligence?"),
                    color=WHITE,fontSize=23.sp,lineHeight=30.sp,fontWeight=FontWeight.ExtraBold)
                Text(t("Level 1 · Module 1 · အသက် ၈–၁၅ နှစ် · ၃၅–၄၅ မိနစ်",
                       "Level 1 · Module 1 · Ages 8–15 · 35–45 minutes"),color=WHITE,fontSize=12.sp)
                Text(t("ဖတ်ပါ → စဉ်းစားပါ → လေ့ကျင့်ပါ → Quiz ဖြေပါ",
                       "Read → Think → Practice → Take a quiz"),color=WHITE,fontSize=12.sp)
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
            for(i in 0..6){
                Box(Modifier.weight(1f).height(8.dp).background(if(i<=step)AQUA else Color(0xFF314278),RoundedCornerShape(8.dp)))
            }
        }
        Text(t("အပိုင်း ","Step ")+(step+1)+"/7 • "+headings[step],color=GOLD,fontWeight=FontWeight.Bold)
        when(step){
            0 -> {
                LessonCard("🎯",t("ဒီနေ့ သင်ယူရမယ့်အရာ","Learning goals"),
                    t("၁။ AI ရဲ့ အဓိပ္ပာယ်ကို ကိုယ့်စကားနဲ့ ရှင်းပြနိုင်မယ်။\n၂။ AI နဲ့ ရိုးရိုး Automation ကို ခွဲခြားနိုင်မယ်။\n၃။ ရှင်းလင်းတဲ့ Prompt တစ်ခု ရေးနိုင်မယ်။\n၄။ Privacy နဲ့ AI Safety စည်းကမ်းတွေ သိလာမယ်။",
                      "1. Explain AI in your own words.\n2. Compare AI and simple automation.\n3. Create a clear prompt.\n4. Use AI safely and protect privacy."),GOLD)
                LessonCard("👦🤝🤖",t("AI ဟာ ကူညီဖော်ပါ","AI is a helper"),
                    t("စက်ရုပ်လိုပဲ AI က အရာတွေ ဖန်တီးနိုင်ဖို့ ကူညီတတ်ပါတယ်။ ဒါပေမယ့် AI က လူမဟုတ်ပါ။ လူကပဲ နောက်ဆုံးဆုံးဖြတ်ချက်ကို ချရပါတယ်။",
                      "AI can help us learn and create. It is not a person and cannot replace human responsibility."))
                LessonCard("💭",t("မစခင် စဉ်းစားကြည့်","Think first"),
                    t("“ဖုန်းမှာ အသံနဲ့ စာရေးလို့ရတယ်။ အဲဒါ ဘယ်လိုဖြစ်နိုင်သလဲ” ဆိုတာ စဉ်းစားကြည့်ပါ။",
                      "How can a phone turn spoken words into written text? Think about this question."),SKY)
            }
            1 -> {
                LessonCard("🧠",t("AI = Artificial Intelligence","AI = Artificial Intelligence"),
                    t("Artificial Intelligence ကို မြန်မာလို “ဉာဏ်ရည်တု” လို့ခေါ်ပါတယ်။ လူတွေ ဖန်တီးထားတဲ့ Computer System တစ်မျိုးက စာသား၊ အသံ၊ ပုံတွေထဲက Pattern (ပုံစံ) တွေကို လေ့လာပြီး စာဖြေခြင်း၊ ခန့်မှန်းခြင်း၊ ပုံနဲ့ ပုံပြင် ဖန်တီးခြင်းတွေ လုပ်ပေးနိုင်ပါတယ်။",
                      "AI means Artificial Intelligence. People build computer systems that learn patterns from examples such as pictures, text and sound. Some AI systems can answer questions, recognize speech and help create new content."))
                LessonCard("⚖️",t("လူနဲ့ AI ရဲ့ ကွာခြားချက်","People and AI are different"),
                    t("လူမှာ ကိုယ်ပိုင် အတွေ့အကြုံ၊ ခံစားချက်၊ တာဝန်ယူမှုတွေ ရှိတယ်။ AI က လူလို အတွေ့အကြုံနဲ့ တာဝန်ယူမှု မရှိဘူး။ AI ထုတ်တဲ့အဖြေ မှန်မမှန်ကို လူက စစ်ဆေးရမယ်။",
                      "People have human experiences and responsibilities. AI does not have human judgment or responsibility. Always check its results."),GOLD)
                LessonCard("⭐",t("မှတ်သားရန်","Remember this"),
                    t("AI = လူက ဖန်တီးထားတဲ့ Smart Helper ဖြစ်ပြီး အမြဲတမ်း မမှန်နိုင်ပါ။",
                      "AI is a smart helper made by people. It can still make mistakes."),ROSE)
            }
            2 -> {
                LessonCard("📚",t("Step 1 — နမူနာအချက်အလက်","Step 1 — Examples"),
                    t("AI ကို လေ့ကျင့်ချိန်မှာ စာသား၊ အသံ၊ ပုံ နမူနာတွေ သုံးနိုင်တယ်။ နမူနာက ကောင်းဖို့ အရေးကြီးတယ်။",
                      "Training can use examples such as text, sound and pictures. Good examples matter."))
                LessonCard("🧩",t("Step 2 — ပုံစံများ လေ့လာ","Step 2 — Find patterns"),
                    t("ကြောင်ပုံတွေမှာ နားပုံစံ၊ မျက်လုံးပုံစံနဲ့ ဆက်စပ်ချက်တွေကို AI က Pattern အဖြစ် လေ့လာနိုင်တယ်။ ဒါဟာ လူလို နားလည်တာနဲ့ မတူပါ။",
                      "An AI model can learn statistical patterns from cat pictures. Pattern recognition is not the same as human understanding."),SKY)
                LessonCard("🎯",t("Step 3 — ခန့်မှန်းအဖြေထုတ်","Step 3 — Make a prediction"),
                    t("ပုံအသစ်တစ်ပုံကို ကြည့်ပြီး “ကြောင် ဖြစ်နိုင်တယ်” လို့ ခန့်မှန်းနိုင်တယ်။ နမူနာတွေ မလုံလောက်ရင် သို့မဟုတ် ထူးဆန်းနေရင် မှားနိုင်တယ်။",
                      "For a new image, AI may predict that it shows a cat. It can make mistakes."),GOLD)
            }
            3 -> {
                LessonCard("🎙️",t("အသံမှ စာ — Speech-to-Text","Speech-to-text"),
                    t("အသံကို နားထောင်ပြီး စာအဖြစ် ပြောင်းပေးတဲ့ App အချို့မှာ AI ပါဝင်တတ်ပါတယ်။",
                      "Some apps use AI to turn spoken words into text."))
                LessonCard("🗺️",t("Smart Maps","Smart maps"),
                    t("အချို့ Map App တွေက ယာဉ်ကြောပိတ်ဆို့မှုကို ခန့်မှန်းပြီး လမ်းကြောင်း အကြံပြုတတ်ပါတယ်။",
                      "Some map apps predict traffic and suggest routes."),SKY)
                LessonCard("🎨",t("AI Art နဲ့ ပုံပြင်","AI art and stories"),
                    t("AI က ဇာတ်ကောင် အိုင်ဒီယာ၊ ပုံဆွဲအကြံဉာဏ်နဲ့ ပုံပြင် မူကြမ်း ကူညီဖန်တီးပေးနိုင်တယ်။ ကိုယ်တိုင် ပြန်ပြင်ပြီး တိုးတက်အောင် လုပ်ပါ။",
                      "AI can suggest characters, pictures and story drafts. Add your own original ideas."),GOLD)
                LessonCard("⏰",t("ရိုးရိုး Alarm က AI လား။","Is a basic alarm clock AI?"),
                    t("သတ်မှတ်ထားတဲ့ အချိန်ရောက်ရင် မြည်တဲ့ နာရီက Rule တစ်ခုကို လိုက်နာတာပါ။ အဲဒီလို Automation တိုင်းမှာ AI မလိုအပ်ပါ။",
                      "A simple alarm clock follows a fixed rule. Not all automation uses AI."),ROSE)
                Text(t("စမ်းကြည့် — AI အသုံးပြုနိုင်ခြေ အများဆုံး ဘယ်ဟာလဲ။","Quick check — which is more likely to use AI?"),color=WHITE,fontWeight=FontWeight.Bold)
                listOf(t("အသံကို စာအဖြစ် ပြောင်းပေးတဲ့ App","Speech-to-text app"),
                       t("လက်နှိပ်မီးခလုတ်","Manual light switch")).forEachIndexed{i,name->
                    OutlinedButton(onClick={exampleChoice=i},modifier=Modifier.fillMaxWidth()){
                        Text((if(exampleChoice==i)"✓ " else "")+name,color=WHITE)
                    }
                }
                if(exampleChoice!=-1)LessonCard(if(exampleChoice==0)"✅" else "💡",
                    t(if(exampleChoice==0)"မှန်ပါတယ်" else "ပြန်စဉ်းစားပါ",
                      if(exampleChoice==0)"Correct" else "Try again"),
                    t("Speech-to-text က စကားသံထဲက Pattern တွေကို သင်ယူထားတဲ့ နည်းပညာဖြစ်နိုင်ပါတယ်။",
                      "Speech-to-text often uses learned speech patterns."),GOLD)
            }
            4 -> {
                LessonCard("✍️",t("Prompt ဆိုတာဘာလဲ","What is a prompt?"),
                    t("Prompt ဆိုတာ AI ကို လုပ်စေချင်တဲ့ အလုပ်ညွှန်ကြားချက်ပါ။ ကောင်းတဲ့ Prompt မှာ အလုပ်အမျိုးအစား၊ ခေါင်းစဉ်၊ ဘယ်သူ့အတွက်လဲ၊ ဘယ်လောက်ရှည်မလဲ ဆိုတာ ထည့်ပေးပါ။",
                      "A prompt is an instruction for AI. A clear prompt specifies the task, topic, audience and output length."),SKY)
                Text(t("၁။ အကြောင်းအရာ ရွေးပါ","1. Choose a topic"),color=WHITE,fontWeight=FontWeight.Bold)
                listOf(t("သစ်ပင်ကာကွယ်ရေး","Protecting trees"),t("အာကာသခရီး","Space adventure"),t("စက်ရုပ်လေး","Friendly robot")).forEachIndexed{i,v->
                    OutlinedButton(onClick={theme=i},modifier=Modifier.fillMaxWidth()){Text((if(theme==i)"✓ " else "")+v,color=WHITE)}
                }
                Text(t("၂။ ဖန်တီးမယ့် ပုံစံရွေးပါ","2. Choose a format"),color=WHITE,fontWeight=FontWeight.Bold)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    listOf(t("ပုံပြင်တို","Short story"),t("ကာတွန်း","Comic")).forEachIndexed{i,v->
                        OutlinedButton(onClick={format=i}){Text((if(format==i)"✓ " else "")+v,color=WHITE)}
                    }
                }
                val topicMm=listOf("သစ်ပင်ကာကွယ်ရေး","အာကာသခရီး","ချစ်စရာစက်ရုပ်")
                val topicEn=listOf("protecting trees","a space adventure","a friendly robot")
                val built=if(my)"အသက် ၁၀ နှစ်ကလေးအတွက် "+topicMm[theme]+
                    (if(format==0)" ပုံပြင်တို ၄ ကြောင်း" else " ကာတွန်း ၄ ကွက်")+" ရေးပေးပါ။ ပျော်စရာကောင်းပြီး ဘေးကင်းပါစေ။"
                  else "Write a safe, fun "+(if(format==0)"four-line story" else "four-panel comic script")+
                    " about "+topicEn[theme]+" for a 10-year-old."
                LessonCard("✨",t("သင့်ရဲ့ နမူနာ Prompt","Your practice prompt"),built)
                Text(t("ဒီ Prompt က Screen ထဲမှာပဲ ဖန်တီးတာဖြစ်ပြီး AI Server ကို မပို့ပါ။",
                       "This prompt is created locally; nothing is sent to an AI server."),color=GOLD,fontSize=12.sp)
            }
            5 -> {
                LessonCard("🔐",t("Privacy ကာကွယ်ပါ","Protect privacy"),
                    t("မိမိရဲ့ Password၊ ဖုန်းနံပါတ်၊ အိမ်လိပ်စာ၊ ကျောင်းလိပ်စာ၊ အခြားသူရဲ့ ကိုယ်ရေးအချက်အလက်တွေကို AI ထဲ မရေးပါနဲ့။",
                      "Never give AI your password, phone number, home or school address or other personal information."),ROSE)
                LessonCard("🔍",t("AI အဖြေကို ပြန်စစ်ပါ","Check the answer"),
                    t("AI က မှားတဲ့အချက်အလက်ကို မှန်သလို ပြောနိုင်တယ်။ ဆရာ၊ စာအုပ်နဲ့ ယုံကြည်ရတဲ့ အရင်းအမြစ်ကို ပြန်စစ်ပါ။",
                      "AI can produce incorrect facts. Verify with teachers and trustworthy sources."),GOLD)
                LessonCard("🎨",t("မူပိုင်ခွင့်လေးစားပါ","Respect creators"),
                    t("သူများရဲ့ပုံ၊ စာကို ကိုယ့်အလုပ်လို့ မပြောပါနဲ့။ AI အကူအညီသုံးခဲ့ရင် စာသင်ခန်းစည်းကမ်းနဲ့အညီ ဖော်ပြပါ။",
                      "Respect copyright and credit sources and AI help according to your class rules."))
                LessonCard("👨‍👩‍👧",t("လူကြီးဆီ အကူအညီတောင်းပါ","Ask a trusted adult"),
                    t("မသင့်လျော်တဲ့အကြောင်းအရာ တွေ့ရင် ရပ်ပြီး မိဘ၊ အုပ်ထိန်းသူ သို့မဟုတ် ဆရာကို ပြောပါ။",
                      "If something seems unsafe or upsetting, stop and talk to a parent, guardian or teacher."),SKY)
                LessonCard("⭐",t("ရွှေစည်းမျဉ်း","Golden rule"),
                    "THINK → ASK SAFELY → CHECK → CREATE",GOLD)
            }
            6 -> {
                Text(t("📝 Quiz ၅ ပုဒ် • အောင်မှတ် ၄/၅ (80%)","📝 Five questions • Pass with 4/5 (80%)"),
                    color=GOLD,fontWeight=FontWeight.Bold)
                questions.forEachIndexed{qIndex,q->
                    Surface(color=Color(0xFF20316B),shape=RoundedCornerShape(17.dp),
                        modifier=Modifier.fillMaxWidth().padding(vertical=7.dp)){
                        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                            Text("Q"+(qIndex+1)+". "+t(q.mm,q.en),color=WHITE,fontWeight=FontWeight.Bold)
                            val choices=if(my)q.optionsMm else q.optionsEn
                            choices.forEachIndexed{idx,opt->
                                val chosen=answers[qIndex]==idx
                                Surface(color=if(chosen)Color(0xFF3656A4) else Color(0xFF172751),
                                    shape=RoundedCornerShape(12.dp),
                                    modifier=Modifier.fillMaxWidth().clickable(enabled=!graded){
                                        answers=answers.toMutableList().apply{this[qIndex]=idx}
                                    }){
                                    Text((if(chosen)"● " else "○ ")+opt,modifier=Modifier.padding(11.dp),
                                        color=if(chosen)AQUA else SUBTLE,fontSize=13.sp,lineHeight=20.sp)
                                }
                            }
                            if(graded){
                                Text(if(answers[qIndex]==q.correct)t("✅ မှန်ပါတယ်","✅ Correct")
                                     else t("❌ အဖြေမှန်: ","❌ Correct: ")+choices[q.correct],
                                    color=if(answers[qIndex]==q.correct)AQUA else GOLD,
                                    fontWeight=FontWeight.Bold)
                                Text(t(q.explainMm,q.explainEn),color=SUBTLE,fontSize=12.sp,lineHeight=18.sp)
                            }
                        }
                    }
                }
                if(!graded){
                    LessonAction(t("Quiz စစ်ဆေးမယ်","Check quiz"),enabled=answers.all{it>=0}){graded=true}
                }else{
                    val result=questions.indices.count{answers[it]==questions[it].correct}
                    LessonCard(if(result>=4)"🏆" else "💪",
                        t("သင်ရမှတ် ","Your score ")+result+"/5",
                        if(result>=4)t("အောင်မြင်ပါတယ်။ သင်ခန်းစာပြီးဆုံးမှုကို Offline သိမ်းနိုင်ပါတယ်။",
                            "Well done! You can save the lesson as completed on this device.")
                        else t("ရှင်းလင်းချက်တွေဖတ်ပြီး Quiz ပြန်ဖြေပါ။",
                            "Read the explanations and try again."),if(result>=4)AQUA else GOLD)
                    if(result>=4){
                        LessonAction(t("🏆 Lesson 1.1 ပြီးဆုံးမှု သိမ်းမယ်","🏆 Save Lesson 1.1 Completion")){
                            if(!completed) onPassed(result)
                            Lesson01Store.complete(ctx,result)
                            completed=true
                            savedScore=result
                        }
                    }else{
                        LessonAction(t("🔁 ပြန်ဖြေမယ်","🔁 Retry quiz")){
                            graded=false;answers=List(5){-1}
                        }
                    }
                }
                if(completed) LessonCard("🌟",t("သင်ခန်းစာ အောင်မြင်ပြီး","Lesson completed"),
                    t("သိမ်းထားတဲ့ ရမှတ်: ","Saved score: ")+savedScore+
                        "/5. "+t("ဖုန်းထဲမှာပဲ သိမ်းထားပြီး Cloud Sync မလုပ်ရသေးပါ။",
                                  "Stored locally, not yet synced to the cloud."),AQUA)
            }
        }
        if(step>0){
            OutlinedButton(onClick={step--;Lesson01Store.saveStage(ctx,step)},
                modifier=Modifier.fillMaxWidth()){
                Text(t("← အရင်အပိုင်း","← Previous step"),color=SUBTLE)
            }
        }
        if(step<6) LessonAction(t("နောက်အပိုင်း ဆက်လေ့လာမယ် →","Next lesson section →")){
            step++;Lesson01Store.saveStage(ctx,step)
        }
        Text(t("📘 Offline Lesson • AI API မလိုပါ","📘 Offline lesson • no AI API required"),
            color=SUBTLE,fontSize=11.sp,textAlign=TextAlign.Center,
            modifier=Modifier.fillMaxWidth().padding(vertical=15.dp))
    }
}
