package asia.aaii.kidcreator

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val sceneArt = listOf(
 R.drawable.lesson11_new01, R.drawable.lesson11_scene02_intro, R.drawable.lesson11_scene03_ai,
 R.drawable.lesson11_scene04_human, R.drawable.lesson11_scene05_learning, R.drawable.lesson11_scene06_daily,
 R.drawable.lesson11_scene07_prompt, R.drawable.lesson11_scene08_safety, R.drawable.lesson11_scene09_quiz)
private val mmTitles = listOf("AI ကမ္ဘာလေးထဲကို ကြိုဆိုပါတယ်!","AI နဲ့ ဘာတွေ လုပ်လို့ရမလဲ?","AI ဆိုတာ ဘာလဲ?","လူသားနဲ့ AI ကို နှိုင်းယှဉ်ကြည့်မယ်","AI ရဲ့ သင်ယူမှုခရီး","AI ကို ဘယ်နေရာတွေမှာ တွေ့နိုင်သလဲ?","AI ကို မေးခွန်းဘယ်လိုမေးမလဲ?","AI ကို ဘေးကင်းစွာ အသုံးပြုမယ်","သင်ခန်းစာ အနှစ်ချုပ်")
private val enTitles = listOf("Welcome to the World of AI!","What Can AI Help Us Do?","What Is AI?","People and AI","How AI Learns","AI in Daily Life","How to Write a Prompt","Using AI Safely","Ready for the Quiz!")
private val mmText = listOf(
 "မင်္ဂလာပါ ကလေးတို့ရေ! ဒီနေ့မှာ AI လို့ခေါ်တဲ့ ဉာဏ်ရည်တုနည်းပညာကို မြန်မာကလေးတွေနဲ့ Spark စက်ရုပ်လေးတို့အတူ လေ့လာကြမယ်။",
 "AI က ပုံဆွဲဖို့ အကြံဉာဏ်ပေးတယ်၊ ပုံပြင်ဖန်တီးဖို့ ကူညီတယ်၊ သင်ခန်းစာတွေကို ရှင်းပြနိုင်တယ်။ ဒါပေမယ့် အမြဲမှန်ကန်တာမဟုတ်လို့ ကိုယ်တိုင်ပြန်စစ်ကြမယ်။",
 "AI ဆိုတာ Artificial Intelligence ရဲ့ အတိုကောက် ဖြစ်ပါတယ်။ မြန်မာလို ဉာဏ်ရည်တုပါ။ လူတွေ ဖန်တီးထားတဲ့ ကွန်ပျူတာနည်းပညာဖြစ်ပြီး နမူနာအချက်အလက်တွေကနေ ပုံစံတွေကို သင်ယူနိုင်ပါတယ်။",
 "လူသားတွေမှာ ကိုယ်ပိုင်အတွေ့အကြုံနဲ့ တာဝန်ယူမှုရှိတယ်။ AI က အချက်အလက်တွေကို စီမံပြီး အဖြေထုတ်ပေးတဲ့ ကိရိယာပါ။ AI ဟာ လူသားမဟုတ်ပါဘူး။",
 "AI ဟာ နမူနာ Data တွေကို ကြည့်တယ်၊ Pattern တွေကို သင်ယူတယ်၊ ပြီးတော့ Output ခန့်မှန်းတယ်။ အဖြေမှားနိုင်တာကို သတိရပါ။",
 "အသံကိုစာပြောင်းတဲ့ App၊ မြေပုံလမ်းကြောင်းအကြံပြုခြင်းနဲ့ ပုံဖန်တီးတဲ့ Tool တွေမှာ AI ပါဝင်နိုင်ပါတယ်။ ရိုးရိုး Alarm တိုင်းမှာ AI မပါပါဘူး။",
 "Prompt ဆိုတာ AI ကို လုပ်စေချင်တဲ့အလုပ်အတွက် ညွှန်ကြားချက်ဖြစ်ပါတယ်။ ဘာလုပ်မယ်၊ ဘာအကြောင်း၊ ဘယ်သူ့အတွက်၊ ဘယ်လောက်ရှည်မယ် ဆိုတာ ပြောပေးပါ။",
 "Password၊ ဖုန်းနံပါတ်နဲ့ အိမ်လိပ်စာတွေကို AI နဲ့ မမျှဝေပါနဲ့။ AI အဖြေတွေကို ပြန်စစ်ပါ။ မသေချာရင် ဆရာ ဒါမှမဟုတ် မိဘကို မေးပါ။",
 "၁။ AI = ဉာဏ်ရည်တု။ ၂။ လူတွေ ဖန်တီးထားတဲ့ နည်းပညာ။ ၃။ နမူနာတွေကနေ သင်ယူတယ်။ ၄။ ဖန်တီးရေးမှာ ကူညီနိုင်တယ်။ ၅။ ကိုယ်ရေးအချက်အလက်တွေကို ကာကွယ်ရမယ်။")
private val enText=listOf(
 "Welcome! Explore AI with Myanmar children and your friendly robot Spark.",
 "AI can suggest ideas for stories, artwork and learning, but its answers can be wrong.",
 "AI means Artificial Intelligence. People create tools that learn statistical patterns from examples.",
 "Humans have real experiences and responsibility. AI is a tool, not a human.",
 "Examples (data) → patterns (learning) → predictions (output). Always check the result.",
 "Speech-to-text, smart maps and drawing apps may use AI. Not every device uses AI.",
 "A clear prompt states the task, subject, audience and length.",
 "Keep passwords and addresses private. Verify answers and ask a trusted adult.",
 "Remember: AI is a tool created by people; it learns from examples and requires safe, thoughtful use.")
private val qMm=listOf("AI ဆိုတာ ဘာရဲ့ အတိုကောက်လဲ?","နေ့စဉ်ဘဝမှာ AI အသုံးပြုထားနိုင်တဲ့ နမူနာက ဘာလဲ?","AI က အဖြေတစ်ခု ပေးလာရင် ဘာလုပ်သင့်သလဲ?","AI ကို မမျှဝေသင့်တဲ့ အချက်အလက်က ဘာလဲ?")
private val qEn=listOf("What does AI stand for?","Which everyday app may use AI?","What should you do with an AI answer?","What should you never share with AI?")
private val choicesMm=listOf(listOf("Artificial Intelligence","Automatic Internet","Amazing Idea"),listOf("အသံကို စာအဖြစ်ပြောင်းပေးတဲ့ App","ရိုးရိုးစားသောက်ဇွန်း","စက္ကူစာအုပ်"),listOf("အမြဲမှန်တယ်လို့ ယုံမယ်","မှန်ကန်မှုကို ပြန်စစ်မယ်","မစစ်ဘဲ မျှဝေမယ်"),listOf("ကြိုက်နှစ်သက်တဲ့အရောင်","စိတ်ကူးယဉ်ပုံပြင်","Password နဲ့ အိမ်လိပ်စာ"))
private val choicesEn=listOf(listOf("Artificial Intelligence","Automatic Internet","Amazing Idea"),listOf("Speech-to-text app","A spoon","A paper book"),listOf("Always trust it","Verify the answer","Share without checking"),listOf("Favorite color","Story idea","Password and home address"))
private val answerKey=listOf(0,0,1,2)
@Composable
internal fun Lesson11Scenes(language:String,onPassed:(Int)->Unit) {
 val ctx=LocalContext.current
 val prefs=remember{ctx.getSharedPreferences("aaii_lesson11_final_scenes",0)}
 val my=language=="my"
 var page by remember {mutableIntStateOf(prefs.getInt("scene",0).coerceIn(0,13))}
 var answers by remember {mutableStateOf(List(4){-1})}
 var selectedInfo by remember(page) {mutableIntStateOf(-1)}
 var topic by remember {mutableIntStateOf(0)}
 var work by remember {mutableIntStateOf(0)}
 var length by remember {mutableIntStateOf(0)}
 var saved by remember {mutableStateOf(Lesson01Store.isCompleted(ctx))}
 val alreadyCompleted=remember{Lesson01Store.isCompleted(ctx)}
 val tipsMm=listOf("ဒီပုံထဲက ဘာတွေတွေ့ရသလဲ?","အဓိက အကြောင်းအရာကို ပြန်ရှင်းပြကြည့်ပါ။","Spark ဆရာက အကူအညီပေးနိုင်ပေမယ့် ကိုယ်တိုင်စဉ်းစားပါ။")
 val tipsEn=listOf("What do you see in the picture?","Explain the key idea in your own words.","Spark can help, but think for yourself.")
 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(11.dp)){
  Text("LESSON 1.1 • "+(if(my)"ဉာဏ်ရည်တု အခြေခံ" else "AI Basics"),color=Color(0xFF60EDD9),fontWeight=FontWeight.Bold)
  LinearProgressIndicator(progress={(page+1)/14f},modifier=Modifier.fillMaxWidth().height(8.dp),color=Color(0xFF60EDD9),trackColor=Color(0xFF243C79))
  Text("${page+1} / 14",color=Color.White)
  if(page<=8){
   Image(painter=painterResource(sceneArt[page]),contentDescription="Myanmar-style AI lesson scene ${page+1}",contentScale=ContentScale.Fit,modifier=Modifier.fillMaxWidth().heightIn(min=145.dp,max=240.dp))
   Text(if(my)mmTitles[page] else enTitles[page],color=Color.White,fontWeight=FontWeight.ExtraBold,fontSize=21.sp)
   Text(if(my)mmText[page] else enText[page],color=Color(0xFFE1ECFF),fontSize=15.sp,lineHeight=25.sp)
   if(page<=3){
    (if(my)tipsMm else tipsEn).forEachIndexed{i,tip->
     OutlinedButton(onClick={selectedInfo=if(selectedInfo==i)-1 else i},modifier=Modifier.fillMaxWidth()){Text((if(selectedInfo==i)"✓ " else "💡 ")+tip,color=Color.White)}
    }
    if(selectedInfo>=0) Text(if(my)"ပုံနဲ့ သင်ခန်းစာကို ချိတ်ဆက်ပြီး ဆရာ၊ မိဘတွေနဲ့ ဆွေးနွေးပါ။" else "Discuss the lesson and illustration with a teacher or guardian.",color=Color(0xFFFFD97C))
   }
   if(page==4){
    listOf("📥 Data","🧠 Learning","✨ Output").forEachIndexed{i,t->
     OutlinedButton(onClick={selectedInfo=i},modifier=Modifier.fillMaxWidth()){Text(t,color=Color.White)}
    }
    if(selectedInfo>=0)Text((if(my)listOf("နမူနာအချက်အလက်","Pattern သင်ယူခြင်း","အဖြေခန့်မှန်းခြင်း")else listOf("Training examples","Learning patterns","Generating an output"))[selectedInfo],color=Color(0xFFFFD97C))
   }
   if(page==5){
    listOf("🎤 Speech to text","🗺️ Smart Maps","🎨 AI Art").forEachIndexed{i,t->OutlinedButton(onClick={selectedInfo=i},modifier=Modifier.fillMaxWidth()){Text(t,color=Color.White)}}
    if(selectedInfo>=0)Text(if(my)"အကူအညီဖြစ်နိုင်ပေမယ့် AI အဖြေကို ပြန်စစ်ရပါတယ်။" else "AI can be useful, but verify the results.",color=Color(0xFFFFD97C))
   }
   if(page==6){
    val topics=if(my)listOf("သစ်ပင်","စက်ရုပ်","အာကာသ")else listOf("trees","robots","space")
    val types=if(my)listOf("ပုံပြင်","ကဗျာ","ရှင်းလင်းချက်")else listOf("story","poem","explanation")
    listOf("🌱 Topic","📝 Type","📏 Length").forEachIndexed{i,title->
     val values=when(i){0->topics;1->types;else->listOf("4","6","8")}
     Text(title,color=Color(0xFFFFD97C),fontWeight=FontWeight.Bold)
     Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){
      values.forEachIndexed{j,label->
       val selected=when(i){0->topic==j;1->work==j;else->length==j}
       OutlinedButton(onClick={when(i){0->topic=j;1->work=j;else->length=j}},contentPadding=PaddingValues(4.dp),modifier=Modifier.weight(1f)){
        Text((if(selected)"✓ "else "")+label,color=Color.White,fontSize=11.sp)
       }
      }
     }
    }
    Text(if(my)"အသက် ၁၀ နှစ်ကလေးအတွက် ${topics[topic]} အကြောင်း ${types[work]} မြန်မာလို ${listOf(4,6,8)[length]} ကြောင်းရေးပေးပါ။" else "Write a ${listOf(4,6,8)[length]}-line ${types[work]} about ${topics[topic]} for a 10-year-old.",color=Color(0xFFFFDD7D))
   }
   if(page==7)listOf("🔐 "+(if(my)"ကိုယ်ရေးအချက်အလက် မပို့ပါနဲ့" else "Keep personal information private"),"🔍 "+(if(my)"အဖြေမှန်မမှန် ပြန်စစ်ပါ" else "Verify answers"),"👨‍👩‍👧 "+(if(my)"ယုံကြည်ရတဲ့ လူကြီးကို မေးပါ" else "Ask a trusted adult")).forEach{Text(it,color=Color.White)}
  }else if(page<=12){
   val qi=page-9
   Text("QUIZ ${qi+1}/4",color=Color(0xFFFFDD75),fontWeight=FontWeight.Bold)
   Text(if(my)qMm[qi] else qEn[qi],color=Color.White,fontWeight=FontWeight.Bold,fontSize=19.sp)
   (if(my)choicesMm[qi] else choicesEn[qi]).forEachIndexed{i,a->
    Surface(color=if(answers[qi]==i)Color(0xFF3467A4)else Color(0xFF1F336A),shape=RoundedCornerShape(13.dp),modifier=Modifier.fillMaxWidth().clickable {answers=answers.toMutableList().apply{this[qi]=i}}){
     Text((if(answers[qi]==i)"●  " else "○  ")+a,modifier=Modifier.padding(15.dp),color=Color.White)
    }
   }
  }else{
   val score=answerKey.indices.count{answers[it]==answerKey[it]}
   Text(if(my)"သင်ခန်းစာ ပြီးဆုံးပါပြီ!" else "Lesson Complete!",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.ExtraBold)
   Text("$score / 4",color=Color(0xFFFFD979),fontSize=35.sp,fontWeight=FontWeight.ExtraBold)
   Text(if(score>=3)(if(my)"🏆 ဂုဏ်ယူပါတယ်! Quiz ကို အောင်မြင်ပါပြီ။"else"🏆 Congratulations! Quiz passed.")else(if(my)"🌱 ထပ်ကြိုးစားကြည့်ရအောင်။" else "🌱 Review and try again."),color=Color.White)
   answerKey.indices.forEach{i->Text((if(answers[i]==answerKey[i])"✅ "else"❌ ")+(if(my)choicesMm[i][answerKey[i]]else choicesEn[i][answerKey[i]]),color=Color(0xFFE1ECFF))}
   Button(onClick={
    if(score>=3){
     prefs.edit().putBoolean("completed",true).putInt("score",score).apply()
     if(!saved){onPassed(score);saved=true}
    }else {answers=List(4){-1};page=9;prefs.edit().putInt("scene",page).apply()}
   },modifier=Modifier.fillMaxWidth()) {Text(if(score>=3)(if(my)"🏆 ပြီးဆုံးမှု သိမ်းမည်"else"Save Completion")else(if(my)"🔁 Quiz ပြန်ဖြေမည်"else"Retry Quiz"))}
   if(saved||alreadyCompleted)Text(if(my)"✅ ပြီးဆုံးမှုကို သိမ်းဆည်းပြီးပါပြီ။"else"✅ Completion saved.",color=Color(0xFF60EDD9))
  }
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
   OutlinedButton(enabled=page>0,onClick={page--;prefs.edit().putInt("scene",page).apply()},modifier=Modifier.weight(1f)){Text(if(my)"← နောက်သို့"else"← Back",color=Color.White)}
   Button(enabled=page<13 && (page<9||answers[page-9]>=0),onClick={page++;prefs.edit().putInt("scene",page).apply()},modifier=Modifier.weight(1f)){Text(if(my)"ရှေ့ဆက် →"else"Next →")}
  }
 }
}
