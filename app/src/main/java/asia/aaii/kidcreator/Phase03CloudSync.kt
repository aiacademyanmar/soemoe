package asia.aaii.kidcreator

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** V2.4 Phase 03: user-scoped HTTPS bundle sync and encrypted offline progress queue. */
internal class Phase03CloudSync(private val ctx: Context) {
    private val prefs = ctx.getSharedPreferences("aaii_phase03", Context.MODE_PRIVATE)
    private val alias = "aaii_kid_creator_v24_aes"
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        return gen.generateKey()
    }
    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val bytes = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
    private fun decrypt(ciphertext: String): String {
        val bytes = Base64.decode(ciphertext, Base64.NO_WRAP)
        require(bytes.size > 28) { "Corrupted encrypted storage" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return cipher.doFinal(bytes.copyOfRange(12, bytes.size)).toString(Charsets.UTF_8)
    }
    private fun get(name: String): String? = prefs.getString(name, null)?.let { decrypt(it) }
    private fun put(name: String, value: String?) {
        val edit = prefs.edit()
        if (value == null) edit.remove(name) else edit.putString(name, encrypt(value))
        check(edit.commit()) { "Could not securely persist local data" }
    }
    private fun endpoint(base: String): String {
        val url = URL(base.trim().trimEnd('/'))
        require(url.protocol == "https" && url.host.isNotBlank() && url.userInfo == null && url.query == null && url.ref == null) {
            "Enter a valid HTTPS server base URL (without query or fragment)"
        }
        return url.toExternalForm().trimEnd('/') + "/api.php?route="
    }
    private suspend fun request(route: String, payload: JSONObject? = null, token: String? = get("token"), base: String = get("base") ?: "", query: Map<String,String> = emptyMap()): JSONObject = withContext(Dispatchers.IO) {
        val full = endpoint(base) + URLEncoder.encode(route, "UTF-8") + query.entries.joinToString("") { "&" + URLEncoder.encode(it.key, "UTF-8") + "=" + URLEncoder.encode(it.value, "UTF-8") }
        val connection = URL(full).openConnection() as HttpURLConnection
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 12000
            connection.readTimeout = 25000
            connection.requestMethod = if (payload == null) "GET" else "POST"
            connection.setRequestProperty("Accept", "application/json")
            token?.let { connection.setRequestProperty("Authorization", "Bearer " + it) }
            if (payload != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val input = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = input?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "{}"
            if (status !in 200..299) {
                val message = runCatching { JSONObject(body).optString("error") }.getOrNull()?.ifBlank { "HTTP $status" } ?: "HTTP $status"
                error("$message ($status)")
            }
            JSONObject(body)
        } finally { connection.disconnect() }
    }
    suspend fun login(base: String, username: String, password: String): String = withContext(Dispatchers.IO) {
        require(username.isNotBlank() && password.isNotBlank()) { "Username and password required" }
        val endpointBase = base.trim().trimEnd('/')
        endpoint(endpointBase)
        val response = request("auth/token", JSONObject().put("username",username).put("password", password), token = null, base = endpointBase)
        val user = response.getJSONObject("user")
        require(user.optString("role") == "student") { "Only guardian-approved student accounts can sync learning progress" }
        val identity = endpointBase + "|" + user.getString("id")
        if (identity != get("identity")) {
            put("bundle", null)
            put("pending", null)
        }
        put("identity", identity)
        put("base", endpointBase)
        put("username", username)
        put("token", response.getString("token"))
        "Signed in as " + username
    }
    fun hasCloudSession(): Boolean = get("token") != null
    suspend fun approvedMedia(): JSONArray = request("media/catalog").getJSONArray("items")
    suspend fun upcomingClasses(): JSONArray = request("classes").getJSONArray("classes")
    suspend fun joinClass(classId:Int): String =
        request("class/join", query=mapOf("id" to classId.toString())).getString("join_url")
    suspend fun aiAvailability(): Boolean = request("ai/status").optBoolean("enabled",false)
    suspend fun aiAsk(question:String, consent:Boolean):String {
        require(consent) { "Guardian consent must be confirmed" }
        val data = request("ai/ask",JSONObject().put("question",question).put("guardian_consent",true))
        return data.getString("reply")
    }
    suspend fun healthSummary(): String {
        val result = request("health", token = null)
        val installed = result.optBoolean("installed", false)
        val connected = result.optString("database") == "connected"
        return if(installed && connected) "Server online · MySQL connected · HTTPS check completed"
            else "Server responded but installation is incomplete"
    }
    fun sessionName(): String? = get("username")
    fun savedBase(): String = get("base") ?: ""
    fun bundle(): JSONObject? = get("bundle")?.let { JSONObject(it) }
    fun pendingCount(): Int = JSONObject(get("pending") ?: "{}").length()
    fun logout() { listOf("token","identity","base","username","bundle","pending").forEach { put(it,null) } }
    suspend fun sync(): String = withContext(Dispatchers.IO) {
        require(get("token") != null) { "Sign in before syncing" }
        val status = request("sync/status")
        val currentVersion = status.getInt("version")
        val current = bundle()
        var update = "Already up to date"
        if (current == null || current.optInt("version", -1) != currentVersion) {
            val fresh = request("sync/bundle")
            require(fresh.optString("scope") == "published-only") { "Unexpected server bundle scope" }
            val mods = fresh.getJSONArray("modules")
            require(mods.length() <= 18 && fresh.getJSONObject("counts").optInt("lessons") <= 72) { "Invalid curriculum size" }
            put("bundle", fresh.toString())
            update = "Downloaded published curriculum v" + fresh.getInt("version")
        }
        val count = flushPending()
        update + "; uploaded " + count + " queued progress item(s)"
    }
    suspend fun markComplete(lessonId: Int): String = withContext(Dispatchers.IO) {
        val cached = bundle() ?: error("Download published lessons before marking progress")
        val mods = cached.getJSONArray("modules")
        var exists = false
        for (i in 0 until mods.length()) {
            val ls = mods.getJSONObject(i).getJSONArray("lessons")
            for (j in 0 until ls.length()) if (ls.getJSONObject(j).getInt("id") == lessonId) exists = true
        }
        require(exists) { "Lesson is not in the cached published catalog" }
        val pending = JSONObject(get("pending") ?: "{}")
        pending.put(lessonId.toString(), JSONObject().put("lesson_id",lessonId).put("completed",true).put("score",0))
        put("pending",pending.toString())
        try { "Saved. " + flushPending() + " update(s) uploaded" }
        catch (e: Exception) { "Saved offline. Upload pending: " + pendingCount() }
    }
    suspend fun flushPending(): Int = withContext(Dispatchers.IO) {
        val pending = JSONObject(get("pending") ?: "{}")
        if (pending.length() == 0) return@withContext 0
        val arr = JSONArray()
        for (key in pending.keys()) arr.put(pending.getJSONObject(key))
        val result = request("progress/batch", JSONObject().put("changes", arr))
        require(result.optBoolean("ok") && result.optInt("accepted") == arr.length()) { "Server did not confirm full progress batch" }
        put("pending", null)
        arr.length()
    }
}

/** Functional native panel included in original 28-screen Compose layout. */
@Composable
internal fun Phase03SyncPanel(mode: String) {
    val context = LocalContext.current
    val client = remember { Phase03CloudSync(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var base by remember { mutableStateOf(client.savedBase()) }
    var username by remember { mutableStateOf(client.sessionName() ?: "") }
    var password by remember { mutableStateOf("") }
    var bundle by remember { mutableStateOf(client.bundle()) }
    var message by remember { mutableStateOf("Offline cache is available after your first successful sync.") }
    var busy by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<JSONObject?>(null) }
    var pending by remember { mutableIntStateOf(client.pendingCount()) }
    fun execute(block: suspend () -> String) {
        if (busy) return
        scope.launch {
            busy = true
            try { message = withContext(Dispatchers.IO) { block() } }
            catch (e: Exception) { message = "Unable to connect: " + (e.message ?: "Unknown error") }
            finally { bundle = client.bundle(); pending=client.pendingCount();busy = false }
        }
    }
    val c = Color(0xFF5AF1E3)
    if (mode == "account") {
        Text("Cloud Login & Sync",color=c,style=MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(value=base,onValueChange={base=it},modifier=Modifier.fillMaxWidth(),label={Text("HTTPS Server URL")},placeholder={Text("https://your-domain/edu/kidcreator")},singleLine=true)
        OutlinedTextField(value=username,onValueChange={username=it},modifier=Modifier.fillMaxWidth(),label={Text("Student username")},singleLine=true)
        OutlinedTextField(value=password,onValueChange={password=it},modifier=Modifier.fillMaxWidth(),label={Text("Student password")},visualTransformation=androidx.compose.ui.text.input.PasswordVisualTransformation(),singleLine=true)
        Button(enabled=!busy,onClick={execute { val result=client.login(base,username,password);password="";result }}){Text("Student Login")}
        Button(enabled=!busy,onClick={execute { client.sync() }}){Text("Sync Published Lessons + Upload Progress")}
        TextButton(onClick={client.logout();username="";base="";bundle=null;pending=0;message="Signed out; offline learner data cleared"}){Text("Sign Out & Clear Offline Data")}
    } else if (mode == "learn") {
        Text("Published Learning Catalog",color=c,style=MaterialTheme.typography.titleLarge)
        Text("Only lessons published by your academy administrator appear here.",color=Color.White)
        if (bundle == null) Text("No downloaded lessons yet. Open Settings / Cloud Login to sign in and sync.",color=Color.White)
        val modules = bundle?.optJSONArray("modules") ?: JSONArray()
        for(i in 0 until modules.length()) {
            val module = modules.getJSONObject(i)
            Text(module.optString("title"),color=Color.White,style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=12.dp))
            val lessons=module.optJSONArray("lessons") ?: JSONArray()
            for (j in 0 until lessons.length()) {
                val item=lessons.getJSONObject(j)
                OutlinedButton(onClick={chosen=item},modifier=Modifier.fillMaxWidth()) { Text(item.optString("title")) }
            }
        }
        chosen?.let { lesson ->
            HorizontalDivider()
            Text("Lesson: "+lesson.optString("title"),color=c,style=MaterialTheme.typography.titleMedium)
            val body=lesson.optJSONObject("body") ?: JSONObject()
            Text(body.toString(2).take(3300),color=Color.White)
            Button(enabled=!busy,onClick={execute { client.markComplete(lesson.getInt("id")) }}){Text("Mark Lesson Completed (Saved Offline)")}
        }
    } else {
        Text("Offline Sync Status",color=c,style=MaterialTheme.typography.titleLarge)
        Text("Account: " + (client.sessionName() ?: "Not signed in"),color=Color.White)
        Text("Saved version: " + (bundle?.optInt("version") ?: 0),color=Color.White)
        Text("Pending progress uploads: " + pending,color=Color.White)
        Button(enabled=!busy,onClick={execute { client.sync() }}){Text("Sync Now")}
    }
    Text(message,color=Color(0xFFFFD965),modifier=Modifier.padding(vertical=10.dp))
    if (busy) CircularProgressIndicator()
}
