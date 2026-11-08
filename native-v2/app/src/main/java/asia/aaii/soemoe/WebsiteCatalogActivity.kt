package asia.aaii.soemoe

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val Night=Color(0xFF111A4A)
private val Indigo=Color(0xFF6047DC)
private val Blue=Color(0xFF3071E5)
private val Soft=Color(0xFFF7F8FD)
private val Gray=Color(0xFF627087)
private data class SiteEntry(val id:String,val title:String,val text:String,val url:String="",val source:String="")
private data class SiteGroup(val id:String,val entries:List<SiteEntry>)
private data class SiteData(val groups:List<SiteGroup>,val sections:List<SiteEntry>) {
    fun list(id:String)=groups.find{it.id==id}?.entries.orEmpty()
}
private fun JSONArray.toEntries():List<SiteEntry>=(0 until length()).map {i->
    val o=getJSONObject(i)
    SiteEntry(o.optString("id"),o.optString("title"),o.optString("text"),o.optString("url"),o.optString("source"))
}

class WebsiteCatalogActivity:ComponentActivity(){
    private fun openSource(url:String){
        if(!(url.startsWith("https://")||url.startsWith("mailto:")||url.startsWith("tel:")))return
        try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE))}
        catch(_:ActivityNotFoundException){Toast.makeText(this,"No browser available",Toast.LENGTH_LONG).show()}
        catch(_:Exception){Toast.makeText(this,"Could not open link",Toast.LENGTH_LONG).show()}
    }
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.statusBarColor=android.graphics.Color.rgb(17,26,74)
        val data=try{
            val root=JSONObject(assets.open("website_catalog.json").bufferedReader(Charsets.UTF_8).use{it.readText()})
            val g=root.getJSONArray("groups")
            SiteData((0 until g.length()).map {i->
                val o=g.getJSONObject(i)
                SiteGroup(o.getString("id"),o.getJSONArray("items").toEntries())
            },root.getJSONArray("sections").toEntries())
        }catch(e:Exception){
            Toast.makeText(this,"Offline content database unavailable",Toast.LENGTH_LONG).show()
            SiteData(emptyList(),emptyList())
        }
        setContent{MaterialTheme{SoeMoeWebsiteNative(data,::openSource)}}
    }
}

@Composable private fun SoeMoeWebsiteNative(data:SiteData,openUrl:(String)->Unit){
    var tab by rememberSaveable{mutableStateOf("home")}
    var detail by remember{mutableStateOf<SiteEntry?>(null)}
    var search by rememberSaveable{mutableStateOf("")}
    BackHandler(detail!=null||tab!="home"){if(detail!=null)detail=null else tab="home"}
    val tabs=listOf("home","courses","books","projects","profile")
    val labels=listOf("Home","Classes","Books","Projects","Profile")
    val icons=listOf(Icons.Default.Home,Icons.Default.School,Icons.Default.Book,Icons.Default.Assignment,Icons.Default.Person)
    Scaffold(containerColor=Soft,
        topBar={
            Surface(color=Night){
                Row(Modifier.fillMaxWidth().height(58.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){
                    if(detail!=null)IconButton(onClick={detail=null}){Icon(Icons.Default.ArrowBack,"Back",tint=Color.White)}
                    else Image(painterResource(R.drawable.teacher_logo),"Teacher logo",Modifier.size(40.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)){
                        Text("Soe Moe AI Trainer",fontWeight=FontWeight.Bold,color=Color.White,fontSize=17.sp)
                        Text("Native • Offline Website Catalog",color=Color(0xFFD3D9FF),fontSize=10.sp)
                    }
                    IconButton(onClick={openUrl("https://soemoe.aaii.asia/")}){Icon(Icons.Default.Language,"Official website",tint=Color.White)}
                }
            }
        },
        bottomBar={
            NavigationBar(containerColor=Color.White){
                tabs.forEachIndexed{i,t->
                    NavigationBarItem(selected=(tab==t&&detail==null),onClick={tab=t;detail=null;search=""},
                        icon={Icon(icons[i],labels[i],Modifier.size(21.dp))},
                        label={Text(labels[i],fontSize=10.sp)},
                        colors=NavigationBarItemDefaults.colors(selectedIconColor=Indigo,selectedTextColor=Indigo,indicatorColor=Color(0xFFEBE8FF)))
                }
            }
        }
    ){pad->
        Box(Modifier.fillMaxSize().padding(pad)){
            when{
                detail!=null->SiteDetail(detail!!,openUrl)
                tab=="home"->SiteDashboard(data,select={tab=it})
                tab=="courses"->SiteCards("20 AI Classes","Syllabi, course outputs and practical learning",data.list("courses"),search,{search=it},{detail=it})
                tab=="books"->SiteCards("Published Books","Book reviews and previews — not full manuscripts",data.list("books")+data.list("book_features"),search,{search=it},{detail=it})
                tab=="projects"->SiteCards("AI Projects & Experience","AI solutions and delivered project portfolio",data.list("ai_projects")+data.list("projects"),search,{search=it},{detail=it})
                tab=="certificates"->SiteCards("Teacher Credentials","Credentials and original verification URLs",data.list("certificates"),search,{search=it},{detail=it})
                tab=="sections"->SiteCards("Complete Site Text","Every archived website section",data.sections,search,{search=it},{detail=it})
                else->SiteProfile(data,{tab=it},{detail=it},openUrl)
            }
        }
    }
}

@Composable private fun SiteDashboard(data:SiteData,select:(String)->Unit){
    LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        item{
            Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Night)){
                Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Blue,Indigo))).padding(14.dp),verticalAlignment=Alignment.CenterVertically){
                    Column(Modifier.weight(1f)){
                        Text("SOE MOE",fontWeight=FontWeight.Black,fontSize=25.sp,color=Color.White)
                        Text("AI TRAINER",fontWeight=FontWeight.Bold,fontSize=18.sp,color=Color.White)
                        Spacer(Modifier.height(10.dp))
                        Text("Practical AI for business, professionals, teachers and creators",color=Color.White,fontSize=12.sp,lineHeight=18.sp)
                    }
                    Image(painterResource(R.drawable.teacher_portrait),"Teacher portrait",Modifier.size(112.dp),contentScale=ContentScale.Crop)
                }
            }
        }
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                SiteCounter(data.list("courses").size.toString(),"Classes",Modifier.weight(1f))
                SiteCounter(data.list("books").size.toString(),"Books",Modifier.weight(1f))
                SiteCounter(data.list("certificates").size.toString(),"Credentials",Modifier.weight(1f))
            }
        }
        item{Text("Original Website Content",color=Night,fontSize=20.sp,fontWeight=FontWeight.Bold)}
        item{SiteShortcut("20 AI Classes","Syllabus, final projects and teaching outcomes",Icons.Default.School){select("courses")}}
        item{SiteShortcut("Published Books","Descriptions and editorial book reviews",Icons.Default.Book){select("books")}}
        item{SiteShortcut("Teacher Certificates","19 external verification records",Icons.Default.Verified){select("certificates")}}
        item{SiteShortcut("AI Projects & Work","AI portfolio plus project experience",Icons.Default.Assignment){select("projects")}}
        item{SiteShortcut("All Website Sections","Profile, training, contact and archive details",Icons.Default.Home){select("sections")}}
        item{SnapshotDisclaimer()}
    }
}
@Composable private fun SiteCounter(count:String,label:String,modifier:Modifier){
    Card(modifier,colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)){
        Column(Modifier.padding(11.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Text(count,fontWeight=FontWeight.Bold,fontSize=21.sp,color=Indigo)
            Text(label,color=Gray,fontSize=10.sp)
        }
    }
}
@Composable private fun SiteShortcut(name:String,info:String,icon:ImageVector,clicked:()->Unit){
    Card(Modifier.fillMaxWidth().clickable(onClick=clicked),colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(15.dp)){
        Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){
            Icon(icon,null,tint=Indigo,modifier=Modifier.size(27.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)){
                Text(name,color=Night,fontWeight=FontWeight.Bold,fontSize=15.sp)
                Text(info,color=Gray,fontSize=11.sp,lineHeight=16.sp)
            }
            Icon(Icons.Default.ArrowForward,null,tint=Indigo)
        }
    }
}
@Composable private fun SiteCards(title:String,sub:String,records:List<SiteEntry>,search:String,onSearch:(String)->Unit,open:(SiteEntry)->Unit){
    val filtered=remember(records,search){if(search.isBlank())records else records.filter{it.title.contains(search,true)||it.text.contains(search,true)}}
    Column(Modifier.fillMaxSize()){
        Column(Modifier.padding(16.dp)){
            Text(title,color=Night,fontWeight=FontWeight.Bold,fontSize=23.sp)
            Text(sub,color=Gray,fontSize=12.sp,lineHeight=17.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(search,onSearch,modifier=Modifier.fillMaxWidth(),singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)},label={Text("Search titles and content")})
            Text(filtered.size.toString()+" entries",color=Gray,fontSize=12.sp,modifier=Modifier.padding(top=8.dp))
        }
        LazyColumn(contentPadding=PaddingValues(start=16.dp,end=16.dp,bottom=20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            items(filtered,key={it.id}){record->
                Card(Modifier.fillMaxWidth().clickable{open(record)},colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(15.dp)){
                    Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){
                            Text(record.title,color=Night,fontWeight=FontWeight.Bold,fontSize=15.sp,lineHeight=21.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(record.text.replace("\n"," • "),color=Gray,fontSize=12.sp,lineHeight=17.sp,maxLines=3,overflow=TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Default.ArrowForward,"Read",tint=Indigo)
                    }
                }
            }
        }
    }
}
@Composable private fun SiteDetail(record:SiteEntry,openUrl:(String)->Unit){
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Text(record.title,color=Night,fontWeight=FontWeight.Bold,fontSize=24.sp,lineHeight=31.sp)}
        item{Text(record.text,color=Night,fontSize=15.sp,lineHeight=24.sp)}
        if(record.url.isNotBlank())item{Button(onClick={openUrl(record.url)}){
            Icon(Icons.Default.Language,null);Spacer(Modifier.width(8.dp));Text("Open original source link")
        }}
        item{SnapshotDisclaimer()}
    }
}
@Composable private fun SiteProfile(data:SiteData,select:(String)->Unit,open:(SiteEntry)->Unit,openUrl:(String)->Unit){
    LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        item{
            Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Night)){
                Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Blue,Indigo))).padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Image(painterResource(R.drawable.teacher_portrait),"Soe Moe",Modifier.height(175.dp).fillMaxWidth(),contentScale=ContentScale.Fit)
                    Text("Soe Moe",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold)
                    Text("AI Trainer • Business • Professional Management",fontSize=12.sp,color=Color.White)
                }
            }
        }
        item{Text("Training & Profile",color=Night,fontSize=19.sp,fontWeight=FontWeight.Bold)}
        items(data.list("flagship")){entry->SiteShortcut(entry.title,"Read course details",Icons.Default.School){open(entry)}}
        item{SiteShortcut("Credentials","Official teacher certificate listings",Icons.Default.Verified){select("certificates")}}
        item{SiteShortcut("Website Archive","Profile, courses and contact text",Icons.Default.Book){select("sections")}}
        item{Button(onClick={openUrl("https://soemoe.aaii.asia/")},modifier=Modifier.fillMaxWidth()){
            Icon(Icons.Default.Language,null);Spacer(Modifier.width(8.dp));Text("Visit Live Website in Browser")
        }}
        item{SnapshotDisclaimer()}
    }
}
@Composable private fun SnapshotDisclaimer(){
    Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFE9EFFF)),shape=RoundedCornerShape(13.dp)){
        Text("From September 2026 website HTML backups (index.html and books.html). Native offline catalog contains website syllabi, book summaries, portfolio descriptions and credential references. Full book manuscripts, live account data, certificate issuance and newer website changes have NOT been imported or verified.",
            color=Gray,fontSize=11.sp,lineHeight=17.sp,modifier=Modifier.padding(12.dp))
    }
}
