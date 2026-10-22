package asia.aaii.kidcreator

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONArray

/** Phase 06: course advertisements only; external enrollment is guardian-mediated. */
@Composable
internal fun CoursePromotionsPanel(kind: String) {
 val ctx=LocalContext.current
 val api=remember { Phase03CloudSync(ctx.applicationContext) }
 val scope=rememberCoroutineScope()
 var rows by remember(kind){mutableStateOf(JSONArray())}
 var status by remember(kind){mutableStateOf("Published course announcements will appear after sync.")}
 var busy by remember{mutableStateOf(false)}
 var selection by remember{mutableStateOf(-1)}
 var guardianConfirmed by remember{mutableStateOf(false)}
 Column(verticalArrangement=Arrangement.spacedBy(9.dp)) {
  Text(if(kind=="campus")"🏫 AI ON CAMPUS" else "🎥 SOE MOE LIVE CLASS — Courses",style=MaterialTheme.typography.titleLarge)
  Text(if(kind=="campus")"AI campus courses and registration announcements." else "Online course advertisements and enrollment details. Live class joining is separate.")
  Button(enabled=!busy,onClick={
   scope.launch {
    busy=true
    try { rows=api.coursePromotions(kind);status=rows.length().toString()+" published courses";selection=-1 }
    catch(e:Exception){status="Sign in / connect server: "+(e.message?:"Unavailable")}
    finally{busy=false}
   }
  }){Text("Refresh Course Advertisements")}
  if(busy) CircularProgressIndicator()
  Text(status)
  for(i in 0 until rows.length()){
   val x=rows.optJSONObject(i)?:continue
   Card(Modifier.fillMaxWidth()){
    Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
     Text((if(kind=="campus")"🏫 " else "🎓 ")+x.optString("title"),style=MaterialTheme.typography.titleMedium)
     Text(x.optString("subtitle"))
     Text("📅 "+x.optString("starts_at","Schedule TBA")+"   📍 "+x.optString("venue","To be announced"))
     Text("👨‍🏫 "+x.optString("teacher_name","Soe Moe")+"  • "+x.optString("price_label","Contact for details"))
     Text(x.optString("description"))
     OutlinedButton(onClick={selection=if(selection==i)-1 else i;guardianConfirmed=false}){Text("Registration Details")}
     if(selection==i){
      Text("Guardian review is required before opening external enrollment websites.")
      Row { Checkbox(checked=guardianConfirmed,onCheckedChange={guardianConfirmed=it})
       Text("I am a parent/guardian and approve opening the external registration page.") }
      val url=x.optString("registration_url")
      Button(enabled=guardianConfirmed&&url.startsWith("https://"),onClick={
       val uri=Uri.parse(url)
       if(uri.scheme=="https" && uri.host!=null) ctx.startActivity(Intent(Intent.ACTION_VIEW,uri))
      }){Text("Open Registration Website")}
     }
    }
   }
  }
 }
}
