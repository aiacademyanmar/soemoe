package asia.aaii.tipitaka;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AudioLibraryActivity extends Activity {
  private static final int PICK_AUDIO=42;
  private static final int PURPLE=0xff33214f;
  private static final int GOLD=0xffd9a850;
  private final String[] modes={"အားလုံး","YouTube","Website","Offline MP3"};
  private String mode="အားလုံး", query="";
  private LinearLayout content;
  private TextView progressText;
  private ProgressBar progress;
  private MediaPlayer player;
  private final ArrayList<Item> catalog=new ArrayList<>();
  private Store db;
  private String pickedName="";
  private static final String CATALOG_URL="https://raw.githubusercontent.com/aiacademyanmar/soemoe/build/myanmar-tipitaka-v1-20261030/tipitaka-app/app/src/main/assets/tipitaka_audio_catalog.json";
  private static class Item {
    String id,kind,category,title,url,source;
    Item(JSONObject o){
      id=o.optString("id");kind=o.optString("kind");category=o.optString("category");
      title=o.optString("title");url=o.optString("url");source=o.optString("source");
    }
  }
  private static class Store extends SQLiteOpenHelper {
    Store(Activity a){super(a,"tipitaka_audio_sync.db",null,1);}
    @Override public void onCreate(SQLiteDatabase d){
      d.execSQL("CREATE TABLE imports(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,filename TEXT NOT NULL UNIQUE,created INTEGER)");
      d.execSQL("CREATE TABLE sync_log(id INTEGER PRIMARY KEY AUTOINCREMENT,synced INTEGER NOT NULL,items INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase d,int old,int newer){}
  }
  @Override public void onCreate(Bundle b){
    super.onCreate(b);db=new Store(this);loadCatalog();show();
  }
  @Override protected void onDestroy(){stopAudio();db.close();super.onDestroy();}
  private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
  private GradientDrawable rounded(int color){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(14));return g;}
  private TextView text(String s,int size,boolean strong,int color){
    TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(7),dp(8),dp(7),dp(8));v.setLineSpacing(dp(3),1.1f);
    if(strong)v.setTypeface(null,1);return v;
  }
  private LinearLayout column(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);return c;}
  private LinearLayout card(){
    LinearLayout c=column();c.setPadding(dp(12),dp(10),dp(12),dp(10));c.setBackground(rounded(Color.WHITE));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(12),dp(6),dp(12),dp(4));content.addView(c,p);return c;
  }
  private void button(LinearLayout parent,String s,Runnable action){
    TextView v=text(s,14,true,Color.WHITE);v.setBackground(rounded(PURPLE));v.setMinHeight(dp(44));v.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(6),0,dp(4));parent.addView(v,p);v.setOnClickListener(w->action.run());
  }
  private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
  private void loadCatalog(){
    catalog.clear();
    File cached=new File(getFilesDir(),"tipitaka_audio_catalog_sync.json");
    try(InputStream in=cached.exists()?new FileInputStream(cached):getAssets().open("tipitaka_audio_catalog.json")){
      ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] bytes=new byte[4096];int k;
      while((k=in.read(bytes))!=-1)out.write(bytes,0,k);
      JSONObject root=new JSONObject(out.toString("UTF-8"));
      JSONArray arr=root.getJSONArray("items");
      for(int i=0;i<arr.length();i++)catalog.add(new Item(arr.getJSONObject(i)));
    }catch(Exception e){toast("Audio Catalog မဖွင့်နိုင်ပါ — "+e.getMessage());}
  }
  private void show(){
    getWindow().setStatusBarColor(PURPLE);getWindow().setNavigationBarColor(PURPLE);
    LinearLayout root=column();root.setBackgroundColor(0xfff8f6f2);setContentView(root);
    TextView head=text("☸  တရားအသံ · Offline Sync Center",21,true,Color.WHITE);head.setPadding(dp(14),dp(16),dp(14),dp(16));head.setBackgroundColor(PURPLE);root.addView(head);
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    content=column();scroll.addView(content);
    LinearLayout status=card();
    status.addView(text("Research Catalog · "+catalog.size()+" links",18,true,0xff322644));
    status.addView(text("သုတေသနပြုစုထားသော Link များကို APK ထဲ တစ်ခါတည်း ထည့်ထားသောကြောင့် Offline စာရင်းကြည့်နိုင်သည်။ Website/YouTube ဖွင့်ရန် Internet လိုသည်။",13,false,0xff6b6072));
    status.addView(text("Sync = APK ထဲပါ Catalog ကို စစ်ဆေးအတည်ပြုခြင်း + ဖုန်းတွင်း MP3 စာရင်းပြန်တည်ဆောက်ခြင်း။ Remote website မှ အသံဖိုင်များ အလိုအလျောက်ကူးယူခြင်း မဟုတ်ပါ။",12,false,0xff765b35));
    progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);progress.setProgress(0);status.addView(progress);
    progressText=text("Sync မစရသေးပါ",13,true,0xff4d3e60);status.addView(progressText);
    button(status,"⟳ Local Offline Sync",this::syncLocal);
    button(status,"☁ Research Link Catalog Update (Internet)",this::syncRemote);
    button(status,"＋ MP3 ကို ဖုန်းထဲ Offline ထည့်ရန်",this::pickAudio);
    EditText search=new EditText(this);search.setSingleLine(true);search.setHint("တရားခေါင်းစဉ် ရှာရန်");search.setText(query);status.addView(search);
    button(status,"ရှာဖွေမည်",()->{query=search.getText().toString().trim();show();});
    LinearLayout selector=card();
    for(String m:modes)button(selector,(m.equals(mode)?"✓ ":"")+m,()->{mode=m;show();});
    if(mode.equals("Offline MP3")||mode.equals("အားလုံး"))showDownloads();
    if(!mode.equals("Offline MP3"))showReferences();
    LinearLayout note=card();note.addView(text("အသုံးပြုခွင့်နှင့် Offline Playback",16,true,0xff322644));
    note.addView(text("အခွင့်ရှိသော အသံဖိုင် သို့မဟုတ် မိမိပိုင် MP3 ကိုသာ Import လုပ်ပါ။ YouTube မှ အသံကို MP3 အဖြစ် Download မလုပ်ပါ။ မူရင်းတရားဟောဆရာတော်နှင့် ထုတ်ဝေသူတို့၏ ခွင့်ပြုချက်ကို လေးစားပါ။",13,false,0xff6b6072));
  }
  private void showReferences(){
    int count=0;
    for(Item i:catalog){
      if(mode.equals("YouTube")&&!i.kind.equals("youtube"))continue;
      if(mode.equals("Website")&&!i.kind.equals("website"))continue;
      if(!query.isEmpty()&&!(i.title+" "+i.category+" "+i.source).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;
      count++;
      LinearLayout c=card();c.addView(text(i.title,16,true,0xff302341));
      c.addView(text(i.category+" · "+i.source+" · "+(i.kind.equals("youtube")?"YouTube":"Website"),12,false,0xff777078));
      c.addView(text("Source URL: "+i.url,11,false,0xff716878));
      button(c,"မူရင်း Link ဖွင့်ရန် (Online)",()->{
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(i.url)));}catch(Exception e){toast("Link ဖွင့်မရပါ");}
      });
    }
    if(count==0){LinearLayout c=card();c.addView(text("Link ရလဒ် မတွေ့ပါ။",14,false,0xff776d7a));}
  }
  private void showDownloads(){
    LinearLayout title=card();title.addView(text("ဖုန်းတွင်း Offline MP3 ဖိုင်များ",17,true,0xff302341));
    int count=0;
    try(Cursor cur=db.getReadableDatabase().rawQuery("SELECT title,filename FROM imports ORDER BY created DESC",null)){
      while(cur.moveToNext()){
        count++;String name=cur.getString(0),filename=cur.getString(1);
        if(!query.isEmpty()&&!name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;
        LinearLayout c=card();c.addView(text(name,15,true,0xff302341));c.addView(text("သိမ်းထားပြီး · Offline",12,false,0xff326f4b));
        button(c,"▶ Offline နားဆင်ရန်",()->play(filename));
        button(c,"■ ရပ်ရန်",this::stopAudio);
        button(c,"ဖိုင် ဖျက်ရန်",()->new AlertDialog.Builder(this).setMessage("ဤအသံဖိုင်ကို ဖျက်မည်လား")
           .setPositiveButton("ဖျက်မည်",(d,w)->{stopAudio();new File(getFilesDir(),"tipitaka_audio/"+filename).delete();
             db.getWritableDatabase().delete("imports","filename=?",new String[]{filename});show();})
           .setNegativeButton("မဖျက်ပါ",null).show());
      }
    }
    if(count==0)title.addView(text("Offline အသံဖိုင်မရှိသေးပါ။ မိမိအသုံးပြုခွင့်ရှိသော MP3 ကို ထည့်သွင်းပါ။",13,false,0xff736a77));
  }
  private void pickAudio(){
    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("audio/*");i.addCategory(Intent.CATEGORY_OPENABLE);
    try{startActivityForResult(i,PICK_AUDIO);}catch(Exception e){toast("MP3 ရွေးချယ်မှု မရရှိပါ။");}
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(request!=PICK_AUDIO||result!=RESULT_OK||data==null||data.getData()==null)return;
    Uri uri=data.getData();String display="Dhamma Audio";
    try(Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){
      if(c!=null&&c.moveToFirst())display=c.getString(0);
    }catch(Exception ignored){}
    final String name=display==null?"Dhamma Audio":display;
    new AlertDialog.Builder(this).setTitle("Offline MP3 ထည့်သွင်းမည်").setMessage(name+"\n\nကိုယ်ပိုင်အသုံးပြုခွင့်ရှိသော ဖိုင်သာ ထည့်ပါ။")
       .setPositiveButton("ထည့်မည်",(d,w)->copyAudio(uri,name)).setNegativeButton("မထည့်ပါ",null).show();
  }
  private void copyAudio(Uri uri,String original){
    progressText.setText("MP3 ဖိုင် ကူးနေပါသည်…");progress.setProgress(1);
    new Thread(()->{
      File folder=new File(getFilesDir(),"tipitaka_audio");if(!folder.exists())folder.mkdirs();
      String filename=UUID.randomUUID().toString()+".mp3";File target=new File(folder,filename);
      try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(target)){
        if(in==null)throw new IOException("File unreadable");
        byte[] chunk=new byte[32768];int n;long size=0;int cycles=0;
        while((n=in.read(chunk))!=-1){size+=n;if(size>300*1024*1024L)throw new IOException("MP3 exceeds 300 MB");
          out.write(chunk,0,n);cycles++;if(cycles%25==0){final int p=Math.min(95,cycles/25);runOnUiThread(()->progress.setProgress(p));}}
        if(size<128)throw new IOException("Empty or invalid audio file");
        android.content.ContentValues values=new android.content.ContentValues();
        values.put("title",original);values.put("filename",filename);values.put("created",System.currentTimeMillis());
        db.getWritableDatabase().insertOrThrow("imports",null,values);
        runOnUiThread(()->{progress.setProgress(100);progressText.setText("Offline MP3 သိမ်းဆည်းပြီး ✓");show();});
      }catch(Exception e){target.delete();runOnUiThread(()->{progressText.setText("Import Error: "+e.getMessage());progress.setProgress(0);});}
    }).start();
  }
  private void play(String filename){
    stopAudio();
    File file=new File(getFilesDir(),"tipitaka_audio/"+filename);
    if(!file.isFile()){toast("အသံဖိုင် မတွေ့ပါ");return;}
    try{
      player=new MediaPlayer();player.setDataSource(file.getAbsolutePath());
      player.setOnCompletionListener(mp->stopAudio());player.prepare();player.start();toast("Offline MP3 နားဆင်နေပါသည်");
    }catch(Exception e){stopAudio();toast("MP3 ဖွင့်မရပါ: "+e.getMessage());}
  }
  private void stopAudio(){if(player!=null){try{player.stop();}catch(Exception ignored){}player.release();player=null;}}
  private void syncRemote(){
    progress.setProgress(2);progressText.setText("Online Catalog Update စစ်နေပါသည်…");
    new Thread(()->{
      File output=new File(getFilesDir(),"tipitaka_audio_catalog_sync.json");
      File temporary=new File(getFilesDir(),"tipitaka_audio_catalog_pending.json");
      HttpURLConnection connection=null;
      try{
        connection=(HttpURLConnection)new URL(CATALOG_URL).openConnection();
        connection.setConnectTimeout(15000);connection.setReadTimeout(20000);
        connection.setInstanceFollowRedirects(false);connection.setRequestProperty("Accept","application/json");
        if(connection.getResponseCode()!=200)throw new IOException("HTTP "+connection.getResponseCode());
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(InputStream input=connection.getInputStream()){
          byte[] buffer=new byte[8192];int n;
          while((n=input.read(buffer))!=-1){
            if(bytes.size()+n>1024*1024)throw new IOException("Catalog too large");
            bytes.write(buffer,0,n);
          }
        }
        byte[] raw=bytes.toByteArray();
        JSONObject parsed=new JSONObject(new String(raw,StandardCharsets.UTF_8));
        JSONArray items=parsed.getJSONArray("items");
        if(items.length()<1||items.length()>5000)throw new IOException("Invalid item count");
        for(int i=0;i<items.length();i++){
          JSONObject obj=items.getJSONObject(i);
          String url=obj.getString("url");
          if(!url.startsWith("https://"))throw new IOException("Unsafe source URL");
          if(obj.optString("title").isEmpty())throw new IOException("Missing title");
        }
        try(FileOutputStream out=new FileOutputStream(temporary)){out.write(raw);}
        if(!temporary.renameTo(output)){
          try(FileOutputStream out=new FileOutputStream(output)){out.write(raw);}
          temporary.delete();
        }
        runOnUiThread(()->{loadCatalog();show();toast("Catalog Update အောင်မြင် · "+items.length+" links · Offline Cache ရရှိပါပြီ။");});
      }catch(Exception e){
        temporary.delete();final String msg=e.getMessage();
        runOnUiThread(()->{progress.setProgress(0);progressText.setText("Update မအောင်မြင်ပါ · Offline Catalog ဆက်သုံးနိုင်သည်");toast("Sync failed: "+msg);});
      }finally{if(connection!=null)connection.disconnect();}
    }).start();
  }
  private void syncLocal(){
    progress.setProgress(10);progressText.setText("Offline Catalog စစ်ဆေးနေပါသည်…");
    int count=0;
    try(Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM imports",null)){if(c.moveToFirst())count=c.getInt(0);}
    progress.setProgress(75);
    db.getWritableDatabase().execSQL("INSERT INTO sync_log(synced,items) VALUES(?,?)",new Object[]{System.currentTimeMillis(),catalog.size()});
    progress.setProgress(100);
    progressText.setText("Offline Sync ပြီး ✓ · Source Links "+catalog.size()+" · MP3 "+count+" ဖိုင်");
  }
}