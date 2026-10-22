package org.aaii.englishlanpya.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.aaii.englishlanpya.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

private data class Source(val key:String,val title:String)
private data class Folder(val id:String,val title:String)
private data class Asset(val id:Long,val title:String,val mime:String)
private data class ServerListing(val folders:List<Folder>,val media:List<Asset>)
private fun safeId(s:String):Boolean=Regex("[A-Za-z0-9_-]{12,190}").matches(s)
private fun request(api:String,method:String="GET",token:String?=null,body:JSONObject?=null):JSONObject{
  require(api.startsWith(BuildConfig.API_BASE_URL)){"Unknown API host"}
  val conn=URL(api).openConnection() as HttpURLConnection
  conn.requestMethod=method;conn.connectTimeout=12000;conn.readTimeout=25000
  conn.setRequestProperty("Accept","application/json")
  if(token!=null)conn.setRequestProperty("Authorization","Bearer $token")
  if(body!=null){conn.doOutput=true;conn.setRequestProperty("Content-Type","application/json; charset=utf-8")
    conn.outputStream.use{it.write(body.toString().toByteArray(Charsets.UTF_8))}
  }
  try{
    val status=conn.responseCode
    val raw= (if(status in 200..299)conn.inputStream else conn.errorStream)?.bufferedReader()?.use{it.readText()} ?:""
    if(status !in 200..299){
      val reason=runCatching{JSONObject(raw).optString("error")}.getOrNull()?.take(150)
      throw IllegalStateException("Server HTTP $status: ${reason?: "Request failed"}")
    }
    return JSONObject(raw)
  }finally{conn.disconnect()}
}
private fun stableDeviceId(context:Context):String{
  val pref=context.getSharedPreferences("elp_local_install",Context.MODE_PRIVATE)
  val existing=pref.getString("device_id",null)
  if(existing!=null)return existing
  val fresh="elp-android-"+UUID.randomUUID().toString()
  pref.edit().putString("device_id",fresh).apply()
  return fresh
}
@Composable
fun ServerLearningLibrary(initialSource:String="teacher",onBack:()->Unit){
  val ctx=LocalContext.current
  val scope=rememberCoroutineScope()
  var studentName by remember { mutableStateOf("") }
  var code by remember { mutableStateOf("") }
  var authToken by remember { mutableStateOf<String?>(null) }
  var notice by remember { mutableStateOf("Connect to the English LanPya server to browse approved lessons.") }
  var busy by remember { mutableStateOf(false) }
  var selectedSource by remember { mutableStateOf(initialSource) }
  var sources by remember { mutableStateOf<List<Source>>(emptyList()) }
  var folder by remember { mutableStateOf<String?>(null) }
  var stack by remember { mutableStateOf<List<Folder>>(emptyList()) }
  var files by remember { mutableStateOf(ServerListing(emptyList(),emptyList())) }
  var selected by remember { mutableStateOf<Asset?>(null) }
  fun fetchSources(token:String){
    busy=true
    scope.launch{
      try{
        val result=withContext(Dispatchers.IO){request(BuildConfig.API_BASE_URL+"library/sources",token=token)}
        val arr=result.optJSONArray("sources")
        val list=(0 until (arr?.length()?:0)).map{i->val o=arr!!.getJSONObject(i);Source(o.getString("source_key"),o.optString("display_name",o.getString("source_key")))}
        sources=list
        if(list.none{it.key==selectedSource} && list.isNotEmpty())selectedSource=list.first().key
        if(list.isNotEmpty())notice="Select a source to load approved folders."
        else notice="No approved server sources. Admin must complete Drive Sync + Source/Media Rights Review."
      }catch(e:Exception){notice=e.message?:"Unable to load sources"}
      finally{busy=false}
    }
  }
  fun fetchFolder(token:String,source:String,parent:String?){
    busy=true
    scope.launch{
      try{
        val p=if(parent==null)"" else "&folder_id="+URLEncoder.encode(parent,"UTF-8")
        val obj=withContext(Dispatchers.IO){request(BuildConfig.API_BASE_URL+"library/browse?source="+URLEncoder.encode(source,"UTF-8")+p,token=token)}
        val fs=obj.optJSONArray("folders");val ms=obj.optJSONArray("media")
        files=ServerListing(
          (0 until (fs?.length()?:0)).map{i->val o=fs!!.getJSONObject(i);Folder(o.getString("folder_id"),o.getString("folder_name"))},
          (0 until (ms?.length()?:0)).map{i->val o=ms!!.getJSONObject(i);Asset(o.getLong("id"),o.optString("display_name"),o.optString("mime_type"))}
        )
        folder=obj.optString("folder_id")
        notice="Server-synchronized content • Approved assets only"
      }catch(e:Exception){notice=e.message?:"Unable to browse folder"}
      finally{busy=false}
    }
  }
  Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(9.dp)){
    Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){
      TextButton(onClick={
        if(selected!=null)selected=null
        else if(stack.isNotEmpty()){
          val previous=stack.last();stack=stack.dropLast(1);authToken?.let{fetchFolder(it,selectedSource,previous.id)}
        }else onBack()
      }){Text("‹ Back")}
      Text("English LanPya · Server Library",style=MaterialTheme.typography.titleMedium)
    }
    if(authToken==null){
      Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Text("Student Activation Login",style=MaterialTheme.typography.headlineSmall)
        OutlinedTextField(studentName,{studentName=it},label={Text("Student name")},singleLine=true,modifier=Modifier.fillMaxWidth())
        OutlinedTextField(code,{code=it},label={Text("Activation code")},singleLine=true,modifier=Modifier.fillMaxWidth())
        Button(enabled=!busy,onClick={
          busy=true
          scope.launch{
            try{
              val payload=JSONObject().put("student_name",studentName.trim()).put("activation_code",code.trim()).put("device_id",stableDeviceId(ctx))
              val result=withContext(Dispatchers.IO){request(BuildConfig.API_BASE_URL+"auth/activate","POST",body=payload)}
              authToken=result.getString("token");code=""
              notice="Activation successful. Loading approved Google Drive library."
              fetchSources(authToken!!)
            }catch(e:Exception){notice=e.message?:"Student login failed"}
            finally{busy=false}
          }
        },modifier=Modifier.fillMaxWidth()){Text("Connect to Server")}
        Text("Requires installed Cloud V2.8, valid activation code and HTTPS. The student token stays only in app memory.",style=MaterialTheme.typography.bodySmall)
        Text(notice,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodySmall)
      }
      return
    }
    if(selected!=null){
      val item=selected!!
      Text(item.title,style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(horizontal=16.dp))
      if(item.mime.startsWith("video/")||item.mime.startsWith("audio/"))
        StreamedNativeVideo(id=item.id,token=authToken!!)
      else Text("Media unsupported",modifier=Modifier.padding(16.dp))
      Text("Streaming via protected English LanPya Server API; not WebView.",modifier=Modifier.padding(16.dp),style=MaterialTheme.typography.bodySmall)
      return
    }
    Row(Modifier.fillMaxWidth().padding(horizontal=8.dp),horizontalArrangement=Arrangement.spacedBy(3.dp)){
      sources.forEach{src->
        FilterChip(selected=selectedSource==src.key,onClick={
          selectedSource=src.key;stack=emptyList();folder=null;files=ServerListing(emptyList(),emptyList())
          authToken?.let{fetchFolder(it,src.key,null)}
        },label={Text(src.title,style=MaterialTheme.typography.labelSmall)})
      }
    }
    if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
    Text(notice,modifier=Modifier.padding(horizontal=12.dp),style=MaterialTheme.typography.bodySmall)
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
      items(files.folders){f->
        ElevatedCard(Modifier.fillMaxWidth().clickable{
          if(safeId(f.id)){
            stack=stack+(Folder(folder?:"",f.title))
            authToken?.let{fetchFolder(it,selectedSource,f.id)}
          }
        }){
          Row(Modifier.padding(16.dp)){Text("📁");Spacer(Modifier.width(12.dp));Text(f.title,modifier=Modifier.weight(1f));Text("›")}
        }
      }
      items(files.media){m->
        ElevatedCard(Modifier.fillMaxWidth().clickable{selected=m}){
          Row(Modifier.padding(16.dp)){Text(if(m.mime.startsWith("video/"))"▶" else "♫");Spacer(Modifier.width(12.dp));Text(m.title,modifier=Modifier.weight(1f),maxLines=2);Text("Play")}
        }
      }
      if(files.folders.isEmpty()&&files.media.isEmpty()&&!busy)item{Text("No authorized content in this folder. Admin may need to approve the media.")}
    }
  }
}
@Composable
private fun StreamedNativeVideo(id:Long,token:String){
  val ctx=LocalContext.current
  val player=remember(id,token){
    val ds=DefaultHttpDataSource.Factory()
      .setDefaultRequestProperties(mapOf("Authorization" to "Bearer $token"))
      .setAllowCrossProtocolRedirects(false)
    ExoPlayer.Builder(ctx).setMediaSourceFactory(DefaultMediaSourceFactory(ds)).build().apply{
      setMediaItem(MediaItem.fromUri(BuildConfig.API_BASE_URL+"library/media/$id/stream"))
      prepare()
      playWhenReady=false
    }
  }
  DisposableEffect(player){onDispose{player.release()}}
  AndroidView(factory={PlayerView(it).apply{this.player=player;useController=true}},modifier=Modifier.fillMaxWidth().height(285.dp))
}
