package asia.aaii.kidcreator
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val art=listOf(R.drawable.lesson11_scene01_welcome,R.drawable.lesson11_scene02_intro,R.drawable.lesson11_scene03_ai,R.drawable.lesson11_scene04_human,R.drawable.lesson11_scene05_learning,R.drawable.lesson11_scene06_daily,R.drawable.lesson11_scene07_prompt,R.drawable.lesson11_scene08_safety,R.drawable.lesson11_scene09_quiz)
private val titles=listOf("ကြိုဆိုပါတယ်","AI ကို ဘာကြောင့် သင်ယူမလဲ။","AI ဆိုတာ ဘာလဲ။","လူနှင့် AI","AI ဘယ်လို သင်ယူသလဲ။","နေ့စဉ်ဘဝထဲက AI","Prompt ရေးနည်း","AI ကို ဘေးကင်းစွာ သုံးမယ်")
private val details=listOf("မြန်မာကျောင်းသားတွေ၊ AI စက်ရုပ်ဆရာ Spark နဲ့ အတူ ပျော်ပျော်ရွှင်ရွှင် လေ့လာကြမယ်။","AI က ပုံရေးတာ၊ စာရေးတာနဲ့ ပညာသင်ယူတာတွေမှာ ကူညီနိုင်ပါတယ်။ ကိုယ်တိုင်စဉ်းစားဖို့က အရေးကြီးပါတယ်။","AI ဆိုတာ ဉာဏ်ရည်တုပါ။ လူတွေ ဖန်တီးထားတဲ့ နည်းပညာက နမူနာတွေထဲက Pattern ကို သင်ယူပြီး အဖြေခန့်မှန်းပေးတယ်။","လူမှာ အတွေ့အကြုံ၊ တာဝန်ယူမှု ရှိပါတယ်။ AI က လူမဟုတ်ပါ။ AI ပြောတာကို ပြန်စစ်ဖို့လိုပါတယ်။","နမူနာ Data ကြည့် → Pattern သင်ယူ → Output ခန့်မှန်းပါတယ်။ မှားနိုင်တဲ့အတွက် စစ်ဆေးပါ။","အသံကိုစာပြောင်းတာ၊ Smart Maps နဲ့ ပုံဖန်တီးတာတွေမှာ AI ကူညီနိုင်ပါတယ်။ ရိုးရိုး Alarm တိုင်းက AI မဟုတ်ပါ။","Prompt ဆိုတာ AI ကို ညွှန်ကြားချက်ပေးတာပါ။ ဘာလုပ်စေချင်တယ်၊ ဘယ်သူ့အတွက်နဲ့ ဘယ်လောက်ရှည်မယ်ဆိုတာ ရှင်းရေးပါ။","Password၊ ဖုန်းနံပါတ်နဲ့ အိမ်လိပ်စာ မမျှဝေပါနဲ့။ AI အဖြေကို ပြန်စစ်ပြီး မသင့်လျော်တာတွေ့ရင် မိဘ/ဆရာကို ပြောပါ။")
private val q=listOf("AI ဆိုတာ ဘာရဲ့အတိုကောက်လဲ။","AI သုံးနိုင်တဲ့ နေရာက ဘယ်ဟာလဲ။","AI အဖြေကို ဘာလုပ်သင့်လဲ။","AI ကို မမျှဝေသင့်တာ ဘာလဲ။","ရှင်းလင်းတဲ့ Prompt က ဘယ်ဟာလဲ။")
private val opts=listOf(listOf("Artistic Internet","Artificial Intelligence","Automatic Ice"),listOf("အသံကို စာပြောင်းတဲ့ App","ရိုးရိုး Alarm","လက်နှိပ် မီးခလုတ်"),listOf("အမြဲမှန်တယ် ယုံကြည်","အချက်အလက် ပြန်စစ်","မစစ်ဘဲ မျှဝေ"),listOf("ပုံပြင် အိုင်ဒီယာ","ကြိုက်တဲ့အရောင်","Password နဲ့ အိမ်လိပ်စာ"),listOf("တစ်ခုခုရေး","ကောင်းအောင်လုပ်","၁၀ နှစ်ကလေးအတွက် သစ်ပင်အကြောင်း ၄ ကြောင်းရေး"))
private val key=listOf(1,0,1,2,2)
@Composable
internal fun Lesson11Scenes(language:String,onPassed:(Int)->Unit){
 val ctx=LocalContext.current
 val pref=remember{ctx.getSharedPreferences("lesson_11_scenes",0)}
 var screen by remember{mutableIntStateOf(pref.getInt("page",0).coerceIn(0,13))}
 var ans by remember{mutableStateOf(List(5){-1})}
 var promptTopic by remember{mutableIntStateOf(0)}
 var tapped by remember(screen){mutableIntStateOf(-1)}
 val my=language=="my"
 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){
  Text("LESSON 1.1 • "+(if(my)"တစ်မျက်နှာချင်း လေ့လာမယ်"else "Interactive lesson scenes"),color=Color(0xFF5AF1E3),fontWeight=FontWeight.Bold)
  LinearProgressIndicator(progress={(screen+1)/14f},modifier=Modifier.fillMaxWidth().height(8.dp),color=Color(0xFF5AF1E3),trackColor=Color(0xFF253E7B))
  Text((screen+1).toString()+" / 14",color=Color.White)
  Image(painterResource(art[screen.coerceAtMost(8)]),contentDescription="Original Myanmar 3D cartoon lesson image",modifier=Modifier.fillMaxWidth().heightIn(min=165.dp,max=260.dp),contentScale=ContentScale.FillWidth)
  if(screen<8){
   Text(if(my)titles[screen]else listOf("Welcome","Why learn AI?","What is AI?","People vs AI","How AI learns","AI in daily life","Writing a prompt","Stay safe")[screen],color=Color.White,fontSize=21.sp,fontWeight=FontWeight.Bold)
   Text(if(my)details[screen]else listOf("Learn with Myanmar kids and friendly robot Spark.","AI helps us write, draw and learn.","AI learns patterns from examples and makes predictions.","People have responsibilities. AI is a tool.","Examples → Patterns → Predictions. Check results.","Speech recognition, maps and art apps may use AI.","Tell AI the task, topic, audience and output length.","Never share passwords and addresses; verify answers.")[screen],color=Color(0xFFE2EAFF),lineHeight=25.sp)
   if(screen<6){
    listOf("🧒 "+(if(my)"မြန်မာကလေးများ"else "Learners"),"🤖 "+(if(my)"AI ဆရာ"else "Robot teacher"),"💡 "+(if(my)"သင်ယူစရာ"else "Key idea")).forEachIndexed{i,s->
     OutlinedButton(onClick={tapped=i},modifier=Modifier.fillMaxWidth()){Text(s,color=Color.White)}
    }
    if(tapped>=0)Text((if(my)listOf("ကလေးတွေက ကိုယ်တိုင် မေးခွန်းမေးပြီး သင်ယူကြတယ်။","AI စက်ရုပ်က ကူညီတတ်ပေမယ့် အဖြေကို စစ်ရပါတယ်။","ပုံကိုကြည့်ပြီး ကိုယ်တိုင် စဉ်းစားစမ်းသပ်ပါ။")else listOf("Learners ask questions.","AI can help but may be wrong.","Look at the image, think and practice."))[tapped],color=Color(0xFFFFDD78))
   }
   if(screen==6){
    val topics=if(my)listOf("သစ်ပင်","အာကာသ","စက်ရုပ်")else listOf("trees","space","robots")
    topics.forEachIndexed{i,s->OutlinedButton(onClick={promptTopic=i},modifier=Modifier.fillMaxWidth()){Text((if(i==promptTopic)"✓ "else "")+s,color=Color.White)}}
    Text(if(my)"အသက် ၁၀ နှစ်ကလေးအတွက် "+topics[promptTopic]+" အကြောင်း ပုံပြင် ၄ ကြောင်းရေးပါ။"else "Write a four-line story about "+topics[promptTopic]+" for a ten-year-old.",color=Color(0xFFFFDD78))
   }
   if(screen==7) listOf("🔐 Privacy","🔍 Fact-check","👨‍👩‍👧 Trusted adult").forEach{Text(it,color=Color.White)}
  }else if(screen<13){
   val i=screen-8
   Text("Quiz "+(i+1)+"/5",color=Color(0xFFFFD97A),fontWeight=FontWeight.Bold)
   Text(if(my)q[i]else listOf("What does AI stand for?","Which app might use AI?","What should you do with AI answers?","What must you not share?","Which is a specific prompt?")[i],color=Color.White,fontSize=19.sp)
   opts[i].forEachIndexed{j,v->
    Surface(color=if(ans[i]==j)Color(0xFF3167AD)else Color(0xFF1C316B),shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth().clickable{ans=ans.toMutableList().also{it[i]=j}}){
     Text((if(ans[i]==j)"● "else "○ ")+v,Modifier.padding(14.dp),color=Color.White)
    }
   }
  }else{
   val score=key.indices.count{key[it]==ans[it]}
   Text((if(my)"ရမှတ်: "else "Score: ")+score+"/5",fontSize=27.sp,color=Color(0xFFFFDE75),fontWeight=FontWeight.Bold)
   Text(if(score>=4)(if(my)"🏆 အောင်မြင်ပါတယ်"else "🏆 Well done!")else(if(my)"အမှားတွေကြည့်ပြီး ပြန်ဖြေပါ"else "Review and retry"),color=Color.White)
   key.indices.forEach{i->Text((if(ans[i]==key[i])"✅ "else "❌ ")+opts[i][key[i]],color=Color.White,fontSize=12.sp)}
   Button(onClick={if(score>=4){if(!Lesson01Store.isCompleted(ctx))onPassed(score);Lesson01Store.complete(ctx,score)}else{ans=List(5){-1};screen=8;pref.edit().putInt("page",8).apply()}},modifier=Modifier.fillMaxWidth()){
    Text(if(score>=4)(if(my)"🏆 ပြီးဆုံးမှု သိမ်းမယ်"else "Save completion")else(if(my)"🔁 ပြန်ဖြေမယ်"else "Retry"))
   }
  }
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
   OutlinedButton(enabled=screen>0,onClick={screen--;pref.edit().putInt("page",screen).apply()},modifier=Modifier.weight(1f)){Text(if(my)"← နောက်သို့"else "← Back",color=Color.White)}
   Button(enabled=screen<13 && (screen !in 8..12 || ans[screen-8]>=0),onClick={screen++;pref.edit().putInt("page",screen).apply()},modifier=Modifier.weight(1f)){Text(if(my)"ရှေ့ဆက် →"else "Next →")}
  }
 }
}
