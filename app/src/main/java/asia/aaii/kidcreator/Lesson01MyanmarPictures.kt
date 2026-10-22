package asia.aaii.kidcreator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MMBlue=Color(0xFF06478E)
private val MMWhite=Color(0xFFF9FDFF)
private val MMTurquoise=Color(0xFF4ADEF3)
private val MMGold=Color(0xFFFFCE6D)
private val MMSkin=Color(0xFFFFCBA7)
private val MMHair=Color(0xFF211B31)
private val MMEyes=Color(0xFF2F1935)

private fun DrawScope.dot(c:Color,x:Float,y:Float,r:Float){drawCircle(color=c,radius=r,center=Offset(x,y))}
private fun DrawScope.rect(c:Color,x:Float,y:Float,w:Float,h:Float,rad:Float=15f){
    drawRoundRect(color=c,topLeft=Offset(x,y),size=Size(w,h),cornerRadius=CornerRadius(rad,rad))
}
private fun DrawScope.tri(c:Color,x:Float,y:Float,w:Float,h:Float){
    val p=Path().apply{moveTo(x,y);lineTo(x+w*.5f,y-h);lineTo(x+w,y);close()}
    drawPath(p,c)
}
private fun DrawScope.spark(x:Float,y:Float,r:Float){
    drawLine(MMGold,Offset(x-r,y),Offset(x+r,y),strokeWidth=4f)
    drawLine(MMGold,Offset(x,y-r),Offset(x,y+r),strokeWidth=4f)
    dot(MMWhite,x,y,4f)
}
private fun DrawScope.pagoda(x:Float,y:Float){
    rect(Color(0xFFD68C34),x-57,y+18,114f,25f,4f)
    rect(MMGold,x-48,y-11,96f,32f,4f)
    tri(Color(0xFFF5BB4D),x-45,y-11,90f,115f)
    drawLine(Color(0xFFFDF0BA),Offset(x,y-130),Offset(x,y-160),strokeWidth=4f)
    dot(MMWhite,x,y-161,5f)
}
private fun DrawScope.student(x:Float,y:Float,girl:Boolean){
    // Familiar Myanmar school clothes: white shirt, green longyi / htamein.
    rect(Color(0xFFFAFAFB),x-48,y+55,96f,85f,16f)
    rect(Color(0xFF268F70),x-51,y+120,102f,59f,10f)
    rect(Color(0xFFFAFAFB),x-61,y+66,22f,58f,11f)
    rect(Color(0xFFFAFAFB),x+39,y+66,22f,58f,11f)
    if(girl){
        dot(MMHair,x-65,y+10,24f)
        dot(MMHair,x+67,y+10,24f)
        dot(Color(0xFFFFA6D4),x-70,y-12,10f)
        dot(Color(0xFFFFA6D4),x+72,y-12,10f)
    }
    dot(MMSkin,x,y,72f)
    dot(MMSkin,x-68,y+12,13f)
    dot(MMSkin,x+68,y+12,13f)
    for(i in 0..5)dot(MMHair,x-55+i*22f,y-53+(i%2)*5,21f)
    dot(MMHair,x-65,y-24,18f)
    dot(MMHair,x+65,y-24,18f)
    dot(MMWhite,x-25,y+5,11f);dot(MMWhite,x+25,y+5,11f)
    dot(MMEyes,x-25,y+5,7f);dot(MMEyes,x+25,y+5,7f)
    dot(MMWhite,x-28,y+2,2f);dot(MMWhite,x+22,y+2,2f)
    // Thanaka cheeks
    dot(Color(0xFFF8E1A7),x-52,y+30,12f)
    dot(Color(0xFFF8E1A7),x+52,y+30,12f)
    drawArc(color=Color(0xFFC65D6D),startAngle=12f,sweepAngle=155f,useCenter=false,
        topLeft=Offset(x-20,y+19),size=Size(40f,29f),style=Stroke(3f))
}
private fun DrawScope.bot(x:Float,y:Float){
    rect(Color(0xFFEBF5FF),x-80,y+54,160f,100f,34f)
    rect(Color(0xFF65CDFA),x-50,y+113,100f,14f,7f)
    rect(Color(0xFFF6FBFF),x-113,y-70,226f,166f,45f)
    rect(Color(0xFF142C57),x-92,y-51,184f,128f,36f)
    dot(MMTurquoise,x-42,y+3,13f)
    dot(MMTurquoise,x+42,y+3,13f)
    drawArc(MMTurquoise,15f,155f,false,Offset(x-27,y+14),Size(54f,39f),style=Stroke(7f))
    rect(Color(0xFF4DB5E9),x-135,y-22,22f,49f,10f)
    rect(Color(0xFF4DB5E9),x+113,y-22,22f,49f,10f)
    drawLine(MMWhite,Offset(x,y-73),Offset(x,y-94),strokeWidth=7f)
    dot(MMTurquoise,x,y-100,11f)
}
private fun DrawScope.openBook(x:Float,y:Float){
    rect(Color(0xFF8266D7),x-74,y-48,148f,94f,9f)
    rect(MMWhite,x-66,y-43,61f,84f,7f)
    rect(MMWhite,x+5,y-43,61f,84f,7f)
    drawLine(Color(0xFFB8AFE3),Offset(x,y-40),Offset(x,y+41),3f)
    for(i in 0..2){
        drawLine(Color(0xFF8FCDE5),Offset(x-52,y-24+i*19),Offset(x-17,y-24+i*19),3f)
        drawLine(Color(0xFF8FCDE5),Offset(x+18,y-24+i*19),Offset(x+53,y-24+i*19),3f)
    }
}
private fun DrawScope.shield(x:Float,y:Float){
    val p=Path().apply{
        moveTo(x,y-73);lineTo(x+66,y-43);lineTo(x+51,y+34)
        quadraticTo(x,y+98,x-51,y+34);lineTo(x-66,y-43);close()
    }
    drawPath(p,Color(0xFF36BED6))
    drawLine(MMWhite,Offset(x-29,y+6),Offset(x-3,y+33),10f)
    drawLine(MMWhite,Offset(x-3,y+33),Offset(x+39,y-23),10f)
}
/**
 * All-native scalable picture. Myanmar school children, longyi / htamein,
 * thanaka, school blackboard and Yangon-style pagoda background.
 * Different lesson sections get a different chalkboard illustration.
 * Text is never baked into the artwork; selectable native Myanmar text remains readable.
 */
@Composable
internal fun MyanmarLessonArtwork(step:Int,isMyanmar:Boolean){
    var picked by remember(step){mutableIntStateOf(-1)}
    val labels=if(isMyanmar)
        listOf("👧 မြန်မာကလေးများ","🤖 AI ကူညီဖော်","📚 နမူနာပုံ")
        else listOf("👧 Myanmar learners","🤖 AI helper","📚 Picture example")
    val explanations=if(isMyanmar)
        listOf("မြန်မာကျောင်းသား/ကျောင်းသူများက AI အကြောင်းကို ကိုယ်တိုင်စဉ်းစားမေးမြန်းပြီး သင်ယူကြပါတယ်။",
        "AI စက်ရုပ်က စာရေး၊ ပုံဆွဲ၊ မေးခွန်းဖြေရာမှာ ကူညီနိုင်ပေမယ့် လူမဟုတ်ပါ။ အဖြေကို စစ်ဆေးရပါတယ်။",
        "နမူနာတွေကို ကြည့်ပြီး Pattern ကို သင်ယူနိုင်ပါတယ်။ AI ဟာ မှားနိုင်လို့ ကိုယ်တိုင် ပြန်စစ်ပါ။")
        else listOf("Myanmar schoolchildren learn to ask questions and think critically.",
            "The friendly AI helper can support learning, but it is not a person and may be wrong.",
            "AI can learn patterns from examples. Always verify its output.")
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(9.dp)){
        Text(if(isMyanmar)"🖼 မြန်မာကာတွန်းပုံ — ပုံအောက်ကခလုတ်ကို နှိပ်ပြီး လေ့လာပါ"
            else "🖼 Myanmar cartoon illustration — tap a label to learn",
            color=MMGold,style=MaterialTheme.typography.titleSmall)
        Canvas(Modifier.fillMaxWidth().height(252.dp)
            .background(MMBlue,RoundedCornerShape(20.dp))
            .border(1.dp,MMTurquoise,RoundedCornerShape(20.dp))
            .semantics { contentDescription=if(isMyanmar)"မြန်မာကျောင်းသားကျောင်းသူ၊ ဉာဏ်ရည်တုစက်ရုပ်၊ ကျောင်းခန်း၊ စေတီပုံ"
                                     else "Myanmar students, AI robot, classroom and pagoda" }){
            withTransform({ scale(size.width/1000f,size.height/520f,Offset.Zero) }){
                drawRoundRect(Brush.verticalGradient(
                    listOf(Color(0xFF65C8F8),Color(0xFFC4EFFF),Color(0xFFCAE8C0)),
                    startY=0f,endY=520f),size=Size(1000f,520f),cornerRadius=CornerRadius(42f))
                dot(MMWhite,160f,70f,60f)
                dot(Color(0xFFF4FDFF),239f,94f,54f)
                dot(MMWhite,780f,73f,57f)
                dot(MMWhite,852f,92f,49f)
                rect(Color(0xFF60B987),0f,411f,1000f,109f,12f)
                rect(Color(0xFF3E9062),0f,456f,1000f,64f,9f)
                pagoda(871f,341f)
                // Myanmar school blackboard
                rect(Color(0xFFB27651),370f,75f,280f,237f,14f)
                rect(Color(0xFF0A5255),384f,87f,252f,212f,9f)
                when(step){
                    1->{openBook(512f,193f);spark(579f,125f,25f)}
                    2->{dot(Color(0xFFFFF1B9),500f,190f,56f);dot(Color(0xFFEA90B9),500f,190f,25f);spark(567f,124f,21f)}
                    3->{openBook(511f,198f);dot(Color(0xFF70DBF9),584f,120f,17f)}
                    4->{rect(Color(0xFF4F89E8),447f,151f,109f,75f,15f);spark(496f,185f,29f)}
                    5->{shield(514f,198f)}
                    6->{dot(MMGold,505f,187f,53f);spark(505f,180f,56f)}
                    else->{spark(510f,193f,58f);dot(MMGold,510f,193f,19f)}
                }
                student(165f,235f,false)
                student(338f,258f,true)
                bot(761f,231f)
                spark(88f,49f,24f);spark(929f,59f,29f);spark(688f,44f,18f)
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(4.dp),modifier=Modifier.fillMaxWidth()){
            labels.forEachIndexed { i,label->
                Surface(shape=RoundedCornerShape(12.dp),
                    color=if(picked==i)Color(0xFF236BAA)else Color(0xFF1B326E),
                    modifier=Modifier.weight(1f).clickable{picked=i}){
                    Text(label,Modifier.padding(horizontal=5.dp,vertical=11.dp),
                        fontSize=10.sp,color=MMWhite,maxLines=2,fontWeight=FontWeight.Bold)
                }
            }
        }
        if(picked>=0){
            Surface(color=Color(0xFF1D3875),shape=RoundedCornerShape(14.dp)){
                Text(explanations[picked],color=MMWhite,style=MaterialTheme.typography.bodyMedium,
                    modifier=Modifier.fillMaxWidth().padding(13.dp))
            }
        }
    }
}
