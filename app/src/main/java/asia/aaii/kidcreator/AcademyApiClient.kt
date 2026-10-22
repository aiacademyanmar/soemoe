package asia.aaii.kidcreator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class CloudLesson(val id: Int, val number: Int, val title: String)
data class CloudModule(val id: Int, val level: Int, val title: String, val lessons: List<CloudLesson>)

class AcademyApiClient {
    private var endpoint: String = ""
    var token: String? = null
        private set

    fun configure(serverBase: String) {
        val trimmed = serverBase.trim().trimEnd('/')
        require(trimmed.startsWith("https://")) { "Production server must use HTTPS" }
        val url = URL(trimmed)
        require(url.host.isNotEmpty()) { "Invalid host" }
        endpoint = trimmed + "/api.php?route="
        token = null
    }

    private suspend fun call(route: String, payload: JSONObject? = null, query: Map<String,String> = emptyMap()): JSONObject = withContext(Dispatchers.IO) {
        require(endpoint.isNotEmpty()) { "Configure the server address first" }
        val params = query.entries.joinToString("") { "&" + URLEncoder.encode(it.key, "UTF-8") + "=" + URLEncoder.encode(it.value, "UTF-8") }
        val url = URL(endpoint + URLEncoder.encode(route, "UTF-8") + params)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 12000
            conn.readTimeout = 18000
            conn.requestMethod = if (payload == null) "GET" else "POST"
            conn.setRequestProperty("Accept", "application/json")
            token?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
            if (payload != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            }
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "{}"
            val result = JSONObject(body)
            if (conn.responseCode !in 200..299) error(result.optString("error", "HTTP " + conn.responseCode))
            result
        } finally { conn.disconnect() }
    }

    suspend fun login(username: String, password: String): String {
        val out = call("auth/token", JSONObject().put("username", username).put("password", password))
        token = out.getString("token")
        return "Signed in as " + out.getJSONObject("user").optString("username")
    }

    suspend fun catalog(): List<CloudModule> {
        val result = call("catalog").getJSONArray("modules")
        return (0 until result.length()).map { i ->
            val m = result.getJSONObject(i)
            val lessons = m.optJSONArray("lessons") ?: JSONArray()
            CloudModule(m.getInt("id"), m.getInt("level"), m.getString("title"),
                (0 until lessons.length()).map { j ->
                    val ls = lessons.getJSONObject(j)
                    CloudLesson(ls.getInt("id"), ls.getInt("lesson_no"), ls.getString("title"))
                })
        }
    }

    suspend fun lesson(id: Int): JSONObject = call("lesson", null, mapOf("id" to id.toString())).getJSONObject("lesson")

    suspend fun progress(lessonId: Int, score: Int) {
        call("progress", JSONObject().put("lesson_id", lessonId).put("score", score).put("completed", true))
    }
}
