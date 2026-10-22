package org.aaii.englishlanpya.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.auth.api.identity.AuthorizationResult
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.min

private val rootFolders=listOf(
    DriveRoot("Teachers","1WHW0fzoLoHPuBDwjQGBh4ry9--vll5b-"),
    DriveRoot("Topics","1fvpEb0CDEhNDaYgXzmJguT5QUUlsKvrD"),
    DriveRoot("Courses","1ftEI7mJqmcJcAUwuNvTQCCBU45nM4X2x"),
    DriveRoot("Let's Talk","1SyFmiUX6cmxS1Jr_Hbm7GlEIaORiEYAo")
)
private data class DriveRoot(val name:String,val id:String)
private data class DriveFile(val id:String,val name:String,val mime:String,val size:String,val canDownload:Boolean){
  val folder get() = mime=="application/vnd.google-apps.folder"
  val video get()=mime.startsWith("video/")
  val audio get()=mime.startsWith("audio/")
}
private data class Listing(val files:List<DriveFile>,val next:String?)
private const val SCOPE="https://www.googleapis.com/auth/drive.readonly"
private fun encode(s:String)=URLEncoder.encode(s,"UTF-8")
private val idPattern=Regex("[A-Za-z0-9_-]{12,180}")
private fun listDriveFiles(token:String,folder:String,pageToken:String?):Listing{
  require(idPattern.matches(folder)){"Invalid Google Drive folder ID"}
  val q="'$folder' in parents and trashed = false"
  val args="q="+encode(q)+"&fields="+encode("nextPageToken,files(id,name,mimeType,size,capabilities/canDownload,webViewLink)")+
      "&pageSize=100&orderBy="+encode("folder,name_natural")+"&supportsAllDrives=true&includeItemsFromAllDrives=true"+
      (if(pageToken==null)"" else "&pageToken="+encode(pageToken))
  val conn=URL("https://www.googleapis.com/drive/v3/files?$args").openConnection() as HttpURLConnection
  conn.setRequestProperty("Authorization","Bearer $token")
  conn.setRequestProperty("Accept","application/json")
  conn.connectTimeout=12000;conn.readTimeout=20000
  try{
    val response=conn.responseCode
    if(response!=200){
      if(response==401)throw IllegalStateException("Google authorization expired; reconnect Google Drive")
      if(response==403)throw IllegalStateException("Google Drive permission denied or API not enabled")
      throw IllegalStateException("Drive API HTTP $response")
    }
    val data=conn.inputStream.bufferedReader().use{it.readText()}
    val obj=JSONObject(data)
    val arr=obj.optJSONArray("files")
    val files=(0 until (arr?.length()?:0)).map { i->
      val a=arr!!.getJSONObject(i)
      val caps=a.optJSONObject("capabilities")
      DriveFile(a.getString("id"),a.optString("name","(untitled)"),a.optString("mimeType",""),a.optString("size",""),caps?.optBoolean("canDownload",true)?:true)
    }
    return Listing(files,obj.optString("nextPageToken","").ifBlank{null})
  } finally{conn.disconnect()}
}
@Composable
fun DirectDriveBrowser(initialSource:String="Teachers",onBack:()->Unit){
  val activity=LocalContext.current as Activity
  val authClient=remember(activity){Identity.getAuthorizationClient(activity)}
  val scope=rememberCoroutineScope()
  var accessToken by remember{mutableStateOf<String?>(null)}
  var source by remember{mutableStateOf(rootFolders.firstOrNull{it.name==initialSource}?:rootFolders[0])}
  var folderId by remember{mutableStateOf(source.id)}
  var folderName by remember{mutableStateOf(source.name)}
  var history by remember{mutableStateOf(listOf<Pair<String,String>>())}
  var files by remember{mutableStateOf<List<DriveFile>>(emptyList())}
  var loading by remember{mutableStateOf(false)}
  var error by remember{mutableStateOf<String?>(null)}
  var nextPage by remember{mutableStateOf<String?>(null)}
  var selectedVideo by remember{mutableStateOf<DriveFile?>(null)}
  var search by remember{mutableStateOf("")}
  fun refresh(token:String,folder:String,page:String?=null){
    loading=true;error=null
    scope.launch{
      try{
        val data=withContext(Dispatchers.IO){listDriveFiles(token,folder,page)}
        files=if(page==null)data.files else (files+data.files).distinctBy{it.id}
        nextPage=data.next
      }catch(e:Exception){error=e.message?:"Drive request failed"}
      finally{loading=false}
    }
  }
  fun authorized(result:AuthorizationResult){
    val token=result.accessToken
    if(token.isNullOrBlank()){error="Google authorization did not return an access token";return}
    accessToken=token
    refresh(token,folderId)
  }
  val consentLauncher=rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){r->
    if(r.resultCode==Activity.RESULT_OK && r.data!=null){
      try{authorized(authClient.getAuthorizationResultFromIntent(r.data!!))}
      catch(e:Exception){error=e.message?:"Google consent failed"}
    }else error="Google Drive authorization cancelled"
  }
  fun connect(){
    error=null
    val request=AuthorizationRequest.builder().setRequestedScopes(listOf(Scope(SCOPE))).build()
    authClient.authorize(request).addOnSuccessListener {res->
      if(res.hasResolution()){
        val pi=res.pendingIntent
        if(pi!=null)consentLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
        else error="Authorization resolution unavailable"
      }else authorized(res)
    }.addOnFailureListener{err->error="Google OAuth: ${err.message?:err.javaClass.simpleName}"}
  }
  fun changeRoot(root:DriveRoot){
    selectedVideo=null;source=root;folderId=root.id;folderName=root.name;history=emptyList();files=emptyList();nextPage=null
    accessToken?.let{refresh(it,root.id)}
  }
  fun navigate(file:DriveFile){
    if(file.folder){
      history=history+(folderId to folderName)
      folderId=file.id;folderName=file.name;selectedVideo=null;nextPage=null;files=emptyList();search=""
      accessToken?.let{refresh(it,file.id)}
    }else if(file.video||file.audio){
      selectedVideo=file
    }else {
      // Preview non-video files only through Google Drive's controlled UI.
      activity.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://drive.google.com/file/d/${file.id}/view")))
    }
  }
  if(selectedVideo!=null && accessToken!=null){
    Column(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
      TextButton(onClick={selectedVideo=null}){Text("← Back to $folderName")}
      Text(selectedVideo!!.name,style=MaterialTheme.typography.titleMedium)
      AuthorizedDrivePlayer(selectedVideo!!,accessToken!!)
      Text("Streaming Google Drive media via your Google OAuth authorization. Playback requires Drive permission, downloadable media and an approved content license.",style=MaterialTheme.typography.bodySmall)
      OutlinedButton(onClick={activity.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://drive.google.com/file/d/${selectedVideo!!.id}/view")))}){Text("Open in Google Drive")}
    }
    return
  }
  Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(6.dp)){
    Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){
      TextButton(onClick={
        if(history.isNotEmpty()){
          val prev=history.last();history=history.dropLast(1);folderId=prev.first;folderName=prev.second
          accessToken?.let{refresh(it,folderId)}
        }else onBack()
      }){Text("←")}
      Text("Google Drive · $folderName",modifier=Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
      TextButton(onClick={accessToken?.let{refresh(it,folderId)}?:connect()}){Text("↻")}
    }
    Row(Modifier.fillMaxWidth().padding(horizontal=8.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){
      rootFolders.forEach{root->
        FilterChip(selected=root.id==source.id,onClick={changeRoot(root)},label={Text(root.name,style=MaterialTheme.typography.labelSmall)})
      }
    }
    if(accessToken==null){
      Card(Modifier.fillMaxWidth().padding(12.dp)){
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
          Text("Connect your Google Account",style=MaterialTheme.typography.titleLarge)
          Text("Read-only Google Drive access is required to list the four English LanPya source folders. Google consent and Android OAuth setup are required.",style=MaterialTheme.typography.bodyMedium)
          Button(onClick={connect()},modifier=Modifier.fillMaxWidth()){Text("Connect Google Drive")}
          Text("No passwords, service-account keys or bearer tokens are bundled in the APK.",style=MaterialTheme.typography.bodySmall)
          TextButton(onClick={activity.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://drive.google.com/drive/folders/${source.id}")))}){Text("Open this folder in Google Drive instead →")}
        }
      }
    }else{
      OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth().padding(horizontal=12.dp),singleLine=true,label={Text("Search current folder")})
      if(loading)LinearProgressIndicator(Modifier.fillMaxWidth())
      if(error!=null)Text(error!!,modifier=Modifier.padding(12.dp),color=MaterialTheme.colorScheme.error)
      val filtered=files.filter{it.name.contains(search,ignoreCase=true)}
      LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        items(filtered,key={it.id}){file->
          ElevatedCard(modifier=Modifier.fillMaxWidth().clickable{navigate(file)}){
            Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
              Text(if(file.folder)"📁" else if(file.video)"🎬" else if(file.audio)"🔊" else "📄")
              Spacer(Modifier.width(12.dp))
              Column(Modifier.weight(1f)){
                Text(file.name,maxLines=2,style=MaterialTheme.typography.bodyMedium)
                Text(if(file.folder)"Folder" else file.mime,style=MaterialTheme.typography.bodySmall)
              }
              Text("›")
            }
          }
        }
        if(nextPage!=null)item{Button(onClick={accessToken?.let{refresh(it,folderId,nextPage)}},enabled=!loading,modifier=Modifier.fillMaxWidth()){Text("Load more files")}}
        if(files.isEmpty()&&!loading)item{Text("No files available or folder access not granted.")}
      }
    }
    if(error!=null&&accessToken==null)Text(error!!,modifier=Modifier.padding(10.dp),color=MaterialTheme.colorScheme.error)
  }
}
@Composable
private fun AuthorizedDrivePlayer(file:DriveFile,token:String){
  val ctx=LocalContext.current
  val player=remember(file.id,token){
    val source=DefaultHttpDataSource.Factory().setDefaultRequestProperties(mapOf("Authorization" to "Bearer $token")).setAllowCrossProtocolRedirects(false)
    ExoPlayer.Builder(ctx).setMediaSourceFactory(DefaultMediaSourceFactory(source)).build().apply{
      setMediaItem(MediaItem.fromUri("https://www.googleapis.com/drive/v3/files/${file.id}?alt=media"))
      prepare()
    }
  }
  DisposableEffect(player){onDispose{player.release()}}
  AndroidView(factory={PlayerView(it).apply{this.player=player;useController=true}},modifier=Modifier.fillMaxWidth().height(260.dp))
}
