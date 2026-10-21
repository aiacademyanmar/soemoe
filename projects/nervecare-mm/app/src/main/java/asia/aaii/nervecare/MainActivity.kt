package asia.aaii.nervecare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val modules=listOf("အာရုံကြောစနစ်အခြေခံ","Neuron နှင့် Synapse","ဦးနှောက်ဖွဲ့စည်းပုံ","Brainstem နှင့် Cerebellum","ကျောရိုးအာရုံကြောမကြီး","Peripheral Nerves","Cranial Nerves ၁၂ စုံ","Motor Control","အာရုံခံမှုနှင့်နာကျင်မှု","Autonomic Nervous System","Stroke","Epilepsy နှင့် Seizure","Migraine","Peripheral Neuropathy","Disc Herniation","Parkinson Disease","Dementia နှင့် Memory","Multiple Sclerosis","Meningitis နှင့် Injury","MRI, CT, EMG နှင့် Rehab")
private val blue=Color(0xFF062740)
private val turquoise=Color(0xFF65D5D0)
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme{Academy()}}}}
@Composable fun Academy(){var selected by remember { mutableIntStateOf(-1) };var search by remember{mutableStateOf("")};val ctx=LocalContext.current
 Column(Modifier.fillMaxSize().background(blue).padding(16.dp)){
 Text("NerveCare MM",fontSize=29.sp,fontWeight=FontWeight.Bold,color=Color.White)
 Text("အာရုံကြောပညာလေ့လာရေး • V2.8 Build Preview",color=turquoise,fontSize=13.sp)
 Spacer(Modifier.height(15.dp))
 if(selected<0){Text("သင်ခန်းစာအုပ်စု ၂၀ • ခေါင်းစဉ် ၁၆၀ အတွက် စာမျက်နှာ",color=Color.White);OutlinedTextField(search,{search=it},label={Text("အကြောင်းအရာ ရှာရန်")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(6.dp))
 LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp)){itemsIndexed(modules.filter{it.contains(search,true)}){i,m->Card(onClick={selected=modules.indexOf(m)},colors=CardDefaults.cardColors(containerColor=Color(0xFF14435C))){Column(Modifier.fillMaxWidth().padding(15.dp)){Text(m,color=Color.White,fontWeight=FontWeight.SemiBold);Text("မြန်မာဘာသာ သင်ခန်းစာ ၈ ခု • Text, Image, Video, 3D, AR, AI",fontSize=11.sp,color=turquoise)}}}}}
 }else{val m=modules[selected];Text("‹ ပြန်သွားရန်",color=turquoise,modifier=Modifier.clickable{selected=-1}.padding(6.dp));Text(m,color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Text("အခြေခံသင်ကြားရေး • ဆေးပညာရှင်ပြန်လည်စိစစ်ရမည့် Preview",color=turquoise)
 LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp)){items(8){n->Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF14435C))){Column(Modifier.padding(15.dp)){Text("Lesson ${n+1} — $m",color=Color.White,fontSize=17.sp,fontWeight=FontWeight.Bold);Text("အာရုံကြောဖွဲ့စည်းပုံ၊ လုပ်ငန်းတာဝန်၊ ရောဂါလက္ခဏာနှင့် စစ်ဆေးမှုတို့ကို ခေါင်းစဉ်အလိုက်လေ့လာနိုင်ရန်။ အပြည့်အစုံ Content မတင်ရသေးပါ။",color=Color.White,fontSize=12.sp);Text("TEXT • IMAGE • VIDEO • 3D • AR • AI  — Media Pending",color=turquoise,fontSize=11.sp);if(selected==6||selected==4||selected==11)TextButton(onClick={val q=when(selected){6->"cranial+nerves";4->"spinal+cord";else->"seizure+first+aid"};ctx.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/results?search_query="+q)))}){Text("YouTube ပညာပေးဗီဒီယိုရှာဖွေရန်",color=turquoise)}}}}
 }
 }
 }
}
