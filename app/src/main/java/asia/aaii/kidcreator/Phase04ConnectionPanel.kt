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

@Composable
internal fun Phase04ConnectionPanel(mode: String) {
 val context=LocalContext.current
 val api=remember{Phase03CloudSync(context.applicationContext)}
 val scope=rememberCoroutineScope()
 var busy by remember{mutableStateOf(false)}
 var status by remember{mutableStateOf("Sign in with a guardian-approved student account first.")}
 var data by remember{mutableStateOf(JSONArray())}
 var question by remember{mutableStateOf("")}
 var answer by remember{mutableStateOf("")}
 var guardianConsent by remember{mutableStateOf(false)}
 fun execute(block:suspend ()->Unit) {
  if(busy)return
  scope.launch {
   busy=true
   try{block()}catch(e:Exception){status=e.message?:"Service not connected"}
   finally{busy=false}
  }
 }
 Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
  Text("Phase 04 — Connected Services",style=MaterialTheme.typography.titleMedium)
  if(!api.hasCloudSession()) Text("Open Login / Settings to connect your HTTPS Academy server.")
  else if(mode=="ai") {
   Text("AI Teacher is optional and requires explicit guardian consent.")
   OutlinedTextField(value=question,onValueChange={question=it.take(450)},modifier=Modifier.fillMaxWidth(),label={Text("Ask a creative learning question")})
   Row { Checkbox(checked=guardianConsent,onCheckedChange={guardianConsent=it});Text("Guardian authorized AI question sharing") }
   Button(enabled=!busy&&guardianConsent&&question.length>=3,onClick={execute{answer=api.aiAsk(question,guardianConsent);status="AI reply received"}}){Text("Ask AI Teacher")}
   if(answer.isNotBlank())Text(answer)
  }else{
   Button(enabled=!busy,onClick={execute{
    data=if(mode=="classes")api.upcomingClasses() else api.approvedMedia()
    status="Retrieved "+data.length()+" approved records"
   }}){Text("Refresh "+if(mode=="classes")"Live Classes" else "Media")}
   for(i in 0 until data.length()){
    val item=data.optJSONObject(i)?:continue
    if(mode!="videos"||item.optString("kind")=="video"){
     Card{Column(Modifier.fillMaxWidth().padding(12.dp)){
      Text(item.optString("title","Resource"))
      Text(if(mode=="classes")item.optString("starts_at") else item.optString("source_kind"))
      Button(enabled=!busy,onClick={execute{
       val url=if(mode=="classes")api.joinClass(item.getInt("id")) else item.optString("source_ref")
       val parsed=Uri.parse(url)
       require(parsed.scheme=="https"){"Only HTTPS resources are supported"}
       context.startActivity(Intent(Intent.ACTION_VIEW,parsed))
       status="Opening approved resource"
      }}){Text(if(mode=="classes")"Join Class" else "Open Resource")}
     }}
    }
   }
  }
  Text(status,color=MaterialTheme.colorScheme.secondary)
  if(busy)CircularProgressIndicator()
 }
}
