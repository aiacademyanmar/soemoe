package asia.aaii.kidcreator

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Phase05: Read-only connectivity diagnostic; cannot silently send child data. */
@Composable
internal fun Phase05ConnectionCheck() {
 val ctx = LocalContext.current
 val cloud = remember { Phase03CloudSync(ctx.applicationContext) }
 val scope = rememberCoroutineScope()
 var status by remember { mutableStateOf("Not checked. This is not a production deployment certificate.") }
 var busy by remember { mutableStateOf(false) }
 val bundle = remember { cloud.bundle() }
 Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
  Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   Text("V2.4 Phase 05 • Connection QA",style=MaterialTheme.typography.titleMedium)
   Text("Saved HTTPS server: " + if(cloud.savedBase().isBlank()) "Not configured" else cloud.savedBase())
   Text("Student session: " + if(cloud.hasCloudSession()) "Signed in" else "Not signed in")
   Text("Cached content version: " + (bundle?.optInt("version") ?: 0))
   Text("Pending offline progress: " + cloud.pendingCount())
   Button(enabled=!busy && cloud.savedBase().isNotBlank(),onClick={
    scope.launch {
     busy=true
     try { status = cloud.healthSummary() }
     catch(e:Exception) { status="Health check failed: "+(e.message ?: "Server unavailable") }
     finally { busy=false }
    }
   }) { Text("Check HTTPS Server Health") }
   Text(status,color=MaterialTheme.colorScheme.secondary)
   Text("Publishing, AI and Drive credentials must be configured and verified on the admin server separately.",
       style=MaterialTheme.typography.bodySmall)
   if(busy) LinearProgressIndicator(modifier=Modifier.fillMaxWidth())
  }
 }
}
