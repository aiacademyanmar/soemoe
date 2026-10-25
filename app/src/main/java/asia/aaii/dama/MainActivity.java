package asia.aaii.dama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioFocusRequest;
import android.content.Context;
import android.os.SystemClock;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** DAMA RADIO V2 native Android: offline metadata and licensed portraits,
 * publisher-linked online audio and user-initiated private offline downloads. */
public class MainActivity extends DamaActivity {
  final int BG=Color.rgb(4,28,28), DEEP=Color.rgb(2,20,21), CARD=Color.rgb(14,52,48), GOLD=Color.rgb(247,202,108), GOLDDIM=Color.rgb(183,147,82);
  final int IVORY=Color.rgb(255,250,237), MUTED=Color.rgb(195,212,204), WHITE=Color.rgb(250,244,232), GREEN=Color.rgb(40,104,79);
  final Handler handler=new Handler(Looper.getMainLooper());
  final ExecutorService background=Executors.newSingleThreadExecutor();
  final ArrayList<Monk> monks=new ArrayList<>();
  final LinkedHashMap<String,Track> catalog=new LinkedHashMap<>();
  final ArrayList<Track> results=new ArrayList<>();
  final HashSet<String> favorites=new HashSet<>();
  final ArrayList<String> recent=new ArrayList<>();
  final String CACHE_FILE="dama_radio_catalog_v2.json";
  String screen="Home",query="",libraryMode="Favorites",priorScreen="Home";
  int monkFilter=0, categoryFilter=0, seekInterval=1000;
  String[] cat={"အားလုံး","ပဋ္ဌာန်း","အဘိဓမ္မာ","ပဋိစ္စသမုပ္ပါဒ်","ဝိပဿနာ","သမထ / ဘာဝနာ","ပရိတ် / ဝတ်ရွတ်","သုတ္တန်","သီလ / မေတ္တာ / ဒါန","အခြား"};
  LinearLayout outer,body,miniBar,listArea,categoryChips;
  int displayedTracks=0; static final int LIST_PAGE_SIZE=35;
  ScrollView radioScroll;
  TextView listSummary;
  final LinkedHashMap<String,Integer> linkAudit=new LinkedHashMap<>();
  TextView counter,information,miniLabel,radioSyncStatus;
  ProgressBar radioSyncProgress;
  volatile int radioSyncDone=0,radioSyncTotal=0;
  EditText searchBox;
  MediaPlayer player; Track selected;
  boolean ready=false,syncing=false,repeat=false,shuffle=false,buffering=false;
  String playerState="Idle",lastPlayerError="";
  TextView stateLabel,miniStateLabel;
  Runnable sleepJob;long sleepUntilMs=0;
  int playerGeneration=0;
  AudioManager audioManager;AudioFocusRequest audioFocusRequest;
  boolean focusGranted=false;
  float speed=1f;
  SeekBar playSeek;
  TextView timeLabel;
  Runnable ticker;
  SharedPreferences settings;

  static class Monk {
    int id;String name,school,source;
    Monk(JSONObject x){ id=x.optInt("id");name=x.optString("name");school=x.optString("school");source=x.optString("source_url"); }
  }
  static class Track {
    String url,title,monk,category,id,source,status;
    int monkId;
    Track(JSONObject x){ url=x.optString("url");title=x.optString("title");monk=x.optString("monk");category=x.optString("category");id=x.optString("id");source=x.optString("listing_url");status=x.optString("status");monkId=x.optInt("monk_id"); }
    JSONObject json(){JSONObject x=new JSONObject();try{x.put("url",url);x.put("title",title);x.put("monk",monk);x.put("category",category);x.put("id",id);x.put("listing_url",source);x.put("status",status);x.put("monk_id",monkId);}catch(Exception ignored){}return x;}
  }
  int dp(float d){return (int)(d*getResources().getDisplayMetrics().density+.5f);}
  LinearLayout vert(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}
  LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(0);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
  LinearLayout.LayoutParams lp(int width,int height){return new LinearLayout.LayoutParams(width<0?width:dp(width),height<0?height:dp(height));}
  View space(int height){View v=new View(this);v.setLayoutParams(lp(1,height));return v;}
  GradientDrawable shape(int color,int rad,int border){GradientDrawable x=new GradientDrawable();x.setColor(color);x.setCornerRadius(dp(rad));if(border!=0)x.setStroke(dp(1),border);return x;}
  TextView text(String s,int size,int color,boolean bold){TextView x=new TextView(this);x.setText(s);x.setTextSize(size);x.setTextColor(color);x.setGravity(Gravity.CENTER_VERTICAL);if(bold)x.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return x;}
  TextView click(String title,Runnable fn){TextView x=text(title,13,GOLD,true);x.setGravity(Gravity.CENTER);x.setPadding(dp(12),dp(11),dp(12),dp(11));x.setBackground(shape(CARD,14,GOLDDIM));x.setOnClickListener(v->fn.run());return x;}
  TextView simpleButton(String title,int color,Runnable run){TextView x=text(title,12,color,true);x.setGravity(Gravity.CENTER);x.setPadding(dp(11),dp(8),dp(11),dp(8));x.setBackground(shape(DEEP,20,GOLDDIM));x.setOnClickListener(v->run.run());return x;}
  void heading(String s){TextView x=text(s,18,GOLD,true);x.setPadding(0,dp(7),0,dp(12));body.addView(x);}
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
  void addCatalog(Track t){if(t.url==null||!t.url.startsWith("http")||t.title.isEmpty())return;String k=t.url.replaceFirst("^http://","https://").replaceFirst("^https://www\\.","https://");synchronized(catalog){catalog.putIfAbsent(k,t);}}
  JSONArray assetArray(String file){try(InputStream stream=getAssets().open(file);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=stream.read(b))!=-1)out.write(b,0,n);return new JSONArray(out.toString("UTF-8"));}catch(Exception ex){return new JSONArray();}}
  void initData(){settings=getSharedPreferences("dama_radio_preferences",MODE_PRIVATE);favorites.addAll(settings.getStringSet("favorites",new HashSet<>()));try{JSONArray x=new JSONArray(settings.getString("recent","[]"));for(int i=0;i<x.length();i++)recent.add(x.optString(i));}catch(Exception ignored){}
    JSONArray people=assetArray("monks.json");for(int i=0;i<people.length();i++){JSONObject m=people.optJSONObject(i);if(m!=null)monks.add(new Monk(m));}
    for(String f:new String[]{"seed_tracks.json"}){JSONArray rows=assetArray(f);for(int i=0;i<rows.length();i++){JSONObject j=rows.optJSONObject(i);if(j!=null)addCatalog(new Track(j));}}
    // V10: full 12,973-link catalog bundled offline, no network needed to populate UI.
    try(InputStream asset=getAssets().open("radio_catalog.dat");java.util.zip.GZIPInputStream zip=new java.util.zip.GZIPInputStream(asset);ByteArrayOutputStream buffer=new ByteArrayOutputStream()){
      byte[] block=new byte[65536];int n;
      while((n=zip.read(block))!=-1){if(buffer.size()+n>20000000)throw new IOException("Catalog too large");buffer.write(block,0,n);}
      JSONArray entries=new JSONArray(buffer.toString("UTF-8"));
      for(int i=0;i<entries.length();i++){JSONObject x=entries.optJSONObject(i);if(x!=null)addCatalog(new Track(x));}
    }catch(Exception ex){android.util.Log.e("DAMA","Offline radio catalog unavailable",ex);}
    File cache=new File(getFilesDir(),CACHE_FILE);if(cache.exists())try(FileInputStream in=new FileInputStream(cache);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);JSONArray rows=new JSONArray(out.toString("UTF-8"));for(int i=0;i<rows.length();i++){JSONObject j=rows.optJSONObject(i);if(j!=null)addCatalog(new Track(j));}}catch(Exception ignored){}
  }
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(DEEP);getWindow().setNavigationBarColor(DEEP);initData();audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);lastPlayerError=settings.getString("last_player_error","");int profileId=getIntent().getIntExtra("dama_profile_monk_id",-1);
        if(profileId>0){
            monkFilter=0;
            for(int i=0;i<monks.size();i++){if(monks.get(i).id==profileId){monkFilter=i+1;break;}}
            categoryFilter=0;query="";show("Radio");
        }else if(getIntent().getBooleanExtra("dama_test_player_ui",false)){openPlayerPreview();}
        else show("Home");}
  void show(String next){if(next.equals("Menu")&&!screen.equals("Menu"))priorScreen=screen;screen=next;outer=vert();outer.setBackgroundColor(BG);setContentView(outer);applySafeInsets(outer);
    createTop(); ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);outer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=vert();body.setPadding(dp(15),dp(13),dp(15),dp(14));scroll.addView(body);
    if(next.equals("Home"))home();else if(next.equals("Radio"))browse();else if(next.equals("Playing"))nowPlaying();else if(next.equals("Collection"))collection();else if(next.equals("Menu"))menuScreen();else if(next.equals("Library")){startActivity(new Intent(this,LibraryActivity.class));screen="Home";home();}else if(next.equals("TV")){startActivity(new Intent(this,TvActivity.class));screen="Home";home();}else if(next.equals("Worship"))other("DAILY WORSHIP","✿ နေ့စဉ်ဝတ်ပြုခြင်း", "https://www.dhammadownload.com/");
    if(selected!=null&&!next.equals("Playing"))miniPlayer();bottomBar();
  }
  /** Android 15 enforces edge-to-edge. Keep our native footer ABOVE OS gestures/buttons. */
  void applySafeInsets(LinearLayout root){
      if(android.os.Build.VERSION.SDK_INT>=35){
          root.setOnApplyWindowInsetsListener((v,insets)->{
              android.graphics.Insets safe=insets.getInsets(
                  android.view.WindowInsets.Type.systemBars() |
                  android.view.WindowInsets.Type.displayCutout());
              v.setPadding(0,safe.top,0,safe.bottom);
              return insets;
          });
          root.requestApplyInsets();
      }
      if(android.os.Build.VERSION.SDK_INT>=29){
          getWindow().setNavigationBarContrastEnforced(false);
      }
  }
  LinearLayout menuTile(String mark,String title,String description,Runnable clickAction){
      LinearLayout tile=vert();tile.setPadding(dp(14),dp(14),dp(12),dp(12));
      GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
          new int[]{0xff194c41,0xff0c332e});
      bg.setCornerRadius(dp(18));bg.setStroke(dp(1),0xff765f39);
      tile.setBackground(bg);tile.setElevation(dp(3));
      TextView icon=text(mark,30,GOLD,true);tile.addView(icon);
      tile.addView(space(8));
      TextView t=text(title,15,IVORY,true);tile.addView(t);
      TextView sub=text(description,11,MUTED,false);
      sub.setMaxLines(2);tile.addView(sub);
      tile.setOnClickListener(v->clickAction.run());
      return tile;
  }
  void menuOption(String symbol,String name,String info,Runnable task){
      LinearLayout panel=row();panel.setPadding(dp(15),dp(10),dp(14),dp(10));
      panel.setBackground(shape(CARD,14,0xff385e51));
      TextView mark=text(symbol,23,GOLD,true);mark.setGravity(Gravity.CENTER);
      panel.addView(mark,lp(38,43));
      LinearLayout words=vert();words.setPadding(dp(12),0,0,0);
      words.addView(text(name,14,IVORY,true));
      words.addView(text(info,11,MUTED,false));
      panel.addView(words,new LinearLayout.LayoutParams(0,-2,1));
      panel.addView(text("›",24,GOLD,true));
      body.addView(panel,lp(-1,64));body.addView(space(9));panel.setOnClickListener(v->task.run());
  }
  void menuScreen(){
      LinearLayout intro=vert();intro.setPadding(dp(20),dp(22),dp(20),dp(18));
      GradientDrawable plate=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
          new int[]{0xff254c40,0xff092b29,0xff102b26});
      plate.setCornerRadius(dp(22));plate.setStroke(dp(1),GOLDDIM);
      intro.setBackground(plate);
      TextView emblem=text("✿",52,GOLD,true);emblem.setGravity(Gravity.CENTER);intro.addView(emblem);
      TextView title=text("DAMA APP",24,GOLD,true);title.setGravity(Gravity.CENTER);intro.addView(title);
      TextView message=text("Listen  •  Read  •  Watch  •  Worship",12,IVORY,false);
      message.setGravity(Gravity.CENTER);intro.addView(message);
      TextView mm=text("ဓမ္မအလင်းဖြင့် နေ့စဉ်ဘဝကို ဖြည့်ဆည်းပါ",12,MUTED,false);
      mm.setGravity(Gravity.CENTER);intro.addView(mm);
      body.addView(intro,lp(-1,-2));body.addView(space(19));
      heading("အဓိက ကဏ္ဍများ");
      LinearLayout first=row(),second=row();
      LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(0,dp(156),1);a.setMargins(0,0,dp(8),0);
      LinearLayout.LayoutParams b=new LinearLayout.LayoutParams(0,dp(156),1);b.setMargins(dp(8),0,0,0);
      first.addView(menuTile("♫","DAMA RADIO","တရားတော် MP3 များ",()->show("Radio")),a);
      first.addView(menuTile("▤","DAMA LIBRARY","ဓမ္မစာအုပ်များ",()->show("Library")),b);
      second.addView(menuTile("▶","DAMA TV","တရားတော်ဗီဒီယို",()->show("TV")),
         new LinearLayout.LayoutParams(0,dp(156),1));
      second.addView(menuTile("✿","DAILY WORSHIP","နေ့စဉ် ဝတ်ပြုမှု",()->show("Worship")),
         new LinearLayout.LayoutParams(0,dp(156),1));
      body.addView(first);body.addView(space(12));body.addView(second);body.addView(space(19));
      heading("မြန်ဆန်သော လုပ်ဆောင်ချက်များ");
      menuOption("♡","Favorites / Downloads","ကြိုက်နှစ်သက်ရာနှင့် Offline Audio",()->show("Collection"));
      menuOption("⌕","တရားတော် ရှာဖွေရန်","ဆရာတော်၊ ခေါင်းစဉ်၊ ပဋ္ဌာန်း",()->show("Radio"));
      menuOption("↻","MP3 Catalog Sync","မူရင်းစာမျက်နှာများမှ Link အသစ်များ",()->sync());
      menuOption("©","ဆရာတော်ဓာတ်ပုံ မူရင်းနှင့် Credit","Wikimedia Commons ပုံအသုံးပြုခွင့်",()->new AlertDialog.Builder(this)
        .setTitle("Sayadaw Portrait Sources")
        .setMessage("Mogok Sayadaw U Vimala: Wikimedia Commons / Public Domain, 1940s.\nMahasi Sayadaw U Sobhana: Wikimedia Commons / Public Domain, 1949.\nDr. Nandamalabhivamsa: Kiwiladi / CC BY-SA 4.0, Wikimedia Commons (2009).\n\nဓာတ်ပုံ မရရှိနိုင်ပါက ဓမ္မသင်္ကေတပုံဖြင့် အစားထိုးပါသည်။")
        .setPositiveButton("OK",null)
        .setNeutralButton("Commons Photo",(d,w)->open("https://commons.wikimedia.org/wiki/File:Sayadaw_nandamalabhivamsa.jpg"))
        .show());
      menuOption("⌂","Home Dashboard","ပင်မစာမျက်နှာ ပြန်ဖွင့်ရန်",()->show("Home"));
      menuOption("!","Player Error Report","နောက်ဆုံး Audio Error နှင့် ဖြေရှင်းနည်း",()->playerErrorReport());
      TextView source=click("မူရင်း DhammaDownload Website  ↗",
         ()->open("https://www.dhammadownload.com/"));
      body.addView(source,lp(-1,50));body.addView(space(20));
      TextView version=text("DAMA RADIO  •  V6  •  Native Android",11,MUTED,false);
      version.setGravity(Gravity.CENTER);body.addView(version);
  }
  void createTop(){
    LinearLayout bar=row();bar.setPadding(dp(16),dp(12),dp(16),dp(13));
    bar.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
       new int[]{0xff061f1d,0xff17413a,0xff062521}));
    TextView menu=text(screen.equals("Menu")?"‹":"☰",25,GOLD,true);
    menu.setGravity(Gravity.CENTER);menu.setBackground(shape(0xff1a4b40,13,0xff58755a));
    bar.addView(menu,lp(44,44));menu.setOnClickListener(v->{
       if(screen.equals("Menu"))show(priorScreen.equals("Menu")?"Home":priorScreen);
       else menu();
    });
    LinearLayout name=vert();name.setGravity(Gravity.CENTER);
    TextView brand=text("DAMA  ✿  RADIO",21,GOLD,true);brand.setGravity(Gravity.CENTER);
    name.addView(brand);
    TextView subtitle=text(screen.equals("Menu")?"DISCOVER • LISTEN • LEARN":"ဓမ္မအလင်း • ရွှေရောင်အသံ",10,MUTED,false);
    subtitle.setGravity(Gravity.CENTER);name.addView(subtitle);
    bar.addView(name,new LinearLayout.LayoutParams(0,dp(51),1));
    TextView find=text(screen.equals("Menu")?"✕":"⌕",25,GOLD,true);
    find.setGravity(Gravity.CENTER);find.setBackground(shape(0xff1a4b40,13,0xff58755a));
    bar.addView(find,lp(44,44));find.setOnClickListener(v->{
       if(screen.equals("Menu"))show(priorScreen.equals("Menu")?"Home":priorScreen);
       else show("Radio");
    });outer.addView(bar);
}
  void menu(){show("Menu");}
  ImageView picture(int res,int h){ImageView img=new ImageView(this);img.setImageResource(res);img.setScaleType(ImageView.ScaleType.CENTER_CROP);img.setBackground(shape(CARD,18,0));img.setClipToOutline(true);img.setLayoutParams(lp(-1,h));return img;}
  void hero(){
  android.widget.FrameLayout frame=new android.widget.FrameLayout(this);frame.setBackground(shape(CARD,22,GOLDDIM));frame.setClipToOutline(true);
  ImageView picture=picture(R.drawable.radio_hero,218);
  frame.addView(picture,new android.widget.FrameLayout.LayoutParams(-1,dp(218)));
  LinearLayout overlay=vert();overlay.setGravity(Gravity.CENTER_VERTICAL);overlay.setPadding(dp(22),dp(14),dp(22),dp(14));
  overlay.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xee051d1b,0xc5123d36,0x20062523}));
  TextView kicker=text("✦  TODAY'S DHAMMA  ✦",10,0xffffdf9b,true);kicker.setLetterSpacing(.12f);overlay.addView(kicker);
  overlay.addView(space(10));TextView h=text("အသံတရားဖြင့်\nစိတ်အေးချမ်းစေပါ",22,IVORY,true);overlay.addView(h);
  overlay.addView(space(8));overlay.addView(text("ဆရာတော်ကြီးများ၏ တရားတော်များ",12,0xfff5dcaa,false));overlay.addView(space(17));
  TextView cta=click("▶  PLAYER ဖွင့်ရန်",()->openPlayerPreview());cta.setTextColor(DEEP);cta.setBackground(shape(GOLD,24,0));
  overlay.addView(cta,lp(178,43));frame.addView(overlay,new android.widget.FrameLayout.LayoutParams(-1,-1));
  body.addView(frame,lp(-1,218));body.addView(space(17));
}
  void summary(){LinearLayout r=row();r.setBackground(shape(CARD,14,GOLDDIM));r.setPadding(dp(12),dp(12),dp(8),dp(12));
    TextView qty=text("♫  "+catalog.size()+" MP3 links",14,GOLD,true);r.addView(qty,new LinearLayout.LayoutParams(0,-2,1));TextView n=text(monks.size()+" ဆရာတော်",12,MUTED,false);r.addView(n);body.addView(r);body.addView(space(10));}
  int[] categoryTotals(int selectedMonk,String keyword){
    int[] result=new int[cat.length];String q=keyword==null?"":keyword.toLowerCase(Locale.ROOT).trim();
    synchronized(catalog){for(Track track:catalog.values()){
      if(selectedMonk>0&&track.monkId!=selectedMonk)continue;
      if(!q.isEmpty()&&!(track.title+" "+track.monk).toLowerCase(Locale.ROOT).contains(q))continue;
      result[0]++;
      for(int i=1;i<cat.length;i++)if(cat[i].equals(group(track.title))){result[i]++;break;}
    }}return result;
  }
  int countMonk(int id){int n=0;synchronized(catalog){for(Track track:catalog.values())if(track.monkId==id)n++;}return n;}
  void catPills(){
    heading("ဓမ္မခေါင်းစဉ်အလိုက်  ·  CATEGORIES");
    HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);
    LinearLayout items=row();int[] totals=categoryTotals(0,"");
    for(int i=1;i<cat.length;i++){
      final int index=i;
      LinearLayout chip=vert();chip.setGravity(Gravity.CENTER);chip.setPadding(dp(13),dp(8),dp(13),dp(8));
      chip.setBackground(shape(0xff153d34,15,GOLDDIM));
      TextView label=text(cat[i],12,IVORY,true);label.setGravity(Gravity.CENTER);chip.addView(label);
      TextView count=text(String.format(Locale.US,"%,d MP3",totals[i]),11,GOLD,true);count.setGravity(Gravity.CENTER);chip.addView(count);
      LinearLayout.LayoutParams p=lp(-2,62);p.setMargins(0,0,dp(8),0);items.addView(chip,p);
      chip.setOnClickListener(v->{categoryFilter=index;monkFilter=0;query="";show("Radio");});
    }
    sc.addView(items);body.addView(sc);body.addView(space(17));
  }
  void monkTiles(){
    heading("ဆရာတော်ကြီးများ  ·  SAYADAWS");
    HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);
    LinearLayout tiles=row();
    for(Monk monk:monks){
      LinearLayout tile=vert();tile.setGravity(Gravity.CENTER);
      tile.setPadding(dp(9),dp(11),dp(9),dp(9));
      tile.setBackground(shape(0xff17453b,18,0xff806d44));
      ImageView portrait=monkPic(monk.id,72);tile.addView(portrait,lp(72,72));
      tile.addView(space(7));
      TextView name=text(monk.name,11,IVORY,true);name.setMaxLines(2);name.setGravity(Gravity.CENTER);tile.addView(name,lp(110,41));
      TextView count=text(String.format(Locale.US,"%,d MP3",countMonk(monk.id)),11,GOLD,true);
      count.setGravity(Gravity.CENTER);tile.addView(count);
      LinearLayout.LayoutParams p=lp(126,162);p.setMargins(0,0,dp(11),0);tiles.addView(tile,p);
      tile.setOnClickListener(v->{monkFilter=monk.id;categoryFilter=0;query="";show("Radio");});
    }
    sc.addView(tiles);body.addView(sc);body.addView(space(18));
    TextView note=text("ဆရာတော်ဓာတ်ပုံ ၃ ပုံကို Wikimedia Commons မှ ရယူရန် စီစဉ်ထားပါသည်။ အခြားသူများအတွက် ဓမ္မသင်္ကေတပုံ ပြထားသည်။",10,MUTED,false);
    body.addView(note);body.addView(space(12));
  }
  ImageView monkPic(int id,int dim){ImageView img=new ImageView(this);int drawable=id==1?R.drawable.mogok_sayadaw:id==2?R.drawable.mahasi_sayadaw:id==4?R.drawable.nandamala_sayadaw:R.drawable.monk_symbol;img.setImageResource(drawable);img.setScaleType(ImageView.ScaleType.CENTER_CROP);img.setBackground(shape(CARD,dim/2,GOLDDIM));img.setClipToOutline(true);return img;}
  void home(){hero();summary();catPills();monkTiles();
    heading("တရားတော် နားထောင်ရန်");quickTrackList(6,false);body.addView(space(12));
    LinearLayout r=row();r.addView(click("↻  Sync MP3",()->sync()),new LinearLayout.LayoutParams(0,-2,1));r.addView(spaceX(8));r.addView(click("▶  Open Player",()->openPlayerPreview()),new LinearLayout.LayoutParams(0,-2,1));body.addView(r);
    body.addView(space(15));heading("Featured Collections");addCollectionCard("ပဋ္ဌာန်းတရားတော်",R.drawable.radio_pattana,1);addCollectionCard("ဝိပဿနာတရားတော်",R.drawable.radio_meditation,4);body.addView(space(10));footnote();
  }
  View spaceX(int w){View v=new View(this);v.setLayoutParams(lp(w,1));return v;}
  void addCollectionCard(String s,int img,int category){LinearLayout r=row();ImageView pic=picture(img,75);r.addView(pic,lp(104,75));LinearLayout text=vert();text.setPadding(dp(14),dp(8),dp(8),dp(8));text.addView(text(s,16,IVORY,true));text.addView(text("တရားတော်များ ရှာဖွေကြည့်ရှုရန်  ›",12,MUTED,false));r.addView(text,new LinearLayout.LayoutParams(0,dp(75),1));r.setBackground(shape(CARD,13,0));body.addView(r,lp(-1,78));body.addView(space(8));r.setOnClickListener(v->{categoryFilter=category;show("Radio");});}
  void footnote(){TextView n=text("မူရင်း DhammaDownload MP3 လင့်ခ်များကို Index လုပ်ထားပါသည်။ Offline = စာရင်း/ပုံများ; အသံကို Stream လုပ်ရန် Internet လိုအပ်သည်။ ကိုယ်တိုင် Download လုပ်ထားသော MP3 ကိုသာ Offline နားဆင်နိုင်သည်။",11,MUTED,false);n.setPadding(dp(4),dp(10),dp(4),dp(12));body.addView(n);}
  void browse(){
    // Entire track list shares ONE outer ScrollView, unlike the old fixed-height nested ListView.
    heading("DAMA RADIO  ·  SPECIAL LIBRARY");
    LinearLayout top=row();top.setPadding(dp(12),dp(12),dp(12),dp(12));
    top.setBackground(shape(0xff16443c,17,GOLDDIM));
    ImageView banner=picture(R.drawable.radio_hero,85);top.addView(banner,lp(125,85));
    LinearLayout titles=vert();titles.setPadding(dp(12),0,0,0);
    titles.addView(text("DHAMMA AUDIO",17,GOLD,true));
    titles.addView(text("တရားတော်အားလုံး ရှာဖွေ • Scroll • Play",11,IVORY,false));
    titles.addView(text(String.format(Locale.US,"%,d indexed MP3 links",catalog.size()),12,MUTED,false));
    top.addView(titles,new LinearLayout.LayoutParams(0,-2,1));body.addView(top);body.addView(space(14));
    monkTiles(); // Premium Radio category page also shows Sayadaw portraits and per-teacher MP3 totals.


    searchBox=new EditText(this);searchBox.setSingleLine(true);searchBox.setTextSize(14);
    searchBox.setTextColor(IVORY);searchBox.setHintTextColor(MUTED);
    searchBox.setHint("⌕  တရားခေါင်းစဉ် / ဆရာတော်အမည် ရှာရန်");
    searchBox.setPadding(dp(15),0,dp(15),0);searchBox.setBackground(shape(0xff1b483e,15,GOLDDIM));
    searchBox.setText(query);body.addView(searchBox,lp(-1,52));body.addView(space(11));

    LinearLayout toolbar=row();
    Spinner monk=new Spinner(this);ArrayList<String> names=new ArrayList<>();names.add("ဆရာတော်အားလုံး");
    for(Monk m:monks)names.add(m.name);
    initSpinner(monk,names,monkFilter,i->{monkFilter=i;filter();});
    toolbar.addView(monk,new LinearLayout.LayoutParams(0,dp(50),1));toolbar.addView(spaceX(8));
    TextView reset=click("✕  RESET",()->{monkFilter=0;categoryFilter=0;query="";show("Radio");});
    toolbar.addView(reset,lp(112,49));body.addView(toolbar);body.addView(space(13));

    heading("ဓမ္မခေါင်းစဉ်များ • Category & MP3 Count");
    HorizontalScrollView horiz=new HorizontalScrollView(this);horiz.setHorizontalScrollBarEnabled(false);
    categoryChips=row();horiz.addView(categoryChips);body.addView(horiz);body.addView(space(14));
    renderCategoryChips();

    LinearLayout shortcuts=row();
    TextView sync=click("↻  MP3 SYNC",()->sync());
    shortcuts.addView(sync,new LinearLayout.LayoutParams(0,dp(46),1));shortcuts.addView(spaceX(8));
    shortcuts.addView(click("▶  PLAYER",()->openPlayerPreview()),new LinearLayout.LayoutParams(0,dp(46),1));
    body.addView(shortcuts);body.addView(space(10));
    information=text("MP3 Links Offline Index • Audio availability unverified",11,MUTED,false);
    body.addView(information);body.addView(space(6));
    radioSyncStatus=text(syncing?"Sync "+radioSyncDone+"/"+radioSyncTotal:
      settings.getString("radio_sync_summary","Offline catalog ready • Live sync not checked"),11,GOLD,false);
    body.addView(radioSyncStatus);body.addView(space(7));
    radioSyncProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
    radioSyncProgress.setMax(Math.max(1,monks.size()));radioSyncProgress.setProgress(syncing?radioSyncDone:0);
    radioSyncProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));
    radioSyncProgress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff355d4b));
    body.addView(radioSyncProgress,lp(-1,6));body.addView(space(13));

    listArea=vert();body.addView(listArea);
    searchBox.addTextChangedListener(new TextWatcher(){
      public void beforeTextChanged(CharSequence s,int a,int b,int c){}
      public void afterTextChanged(Editable e){}
      public void onTextChanged(CharSequence s,int a,int b,int c){
        query=s.toString();renderCategoryChips();filter();
      }
    });
    filter();
    body.addView(space(13));
    TextView helper=text("စာရင်းကို အပေါ်/အောက် ဆွဲကြည့်နိုင်သည်။ နောက်ထပ် MP3 များအတွက် LOAD MORE နှိပ်ပါ။ Track ကို ဖိထားလျှင် Link Check / Download ရွေးချယ်နိုင်သည်။",11,MUTED,false);
    helper.setPadding(dp(5),dp(12),dp(5),dp(16));body.addView(helper);
  }
  void renderCategoryChips(){
    if(categoryChips==null||!screen.equals("Radio"))return;
    categoryChips.removeAllViews();int[] counts=categoryTotals(monkFilter,query);
    String[] glyph={"◉","☸","✦","∞","✿","❋","♫","▣","♡","•"};
    for(int i=0;i<cat.length;i++){
      final int x=i;boolean active=i==categoryFilter;
      LinearLayout chip=vert();chip.setGravity(Gravity.CENTER);
      chip.setPadding(dp(13),dp(9),dp(13),dp(9));
      chip.setBackground(shape(active?0xffdaba78:0xff16483e,14,active?GOLD:GOLDDIM));
      TextView label=text(glyph[i]+" "+cat[i],12,active?DEEP:IVORY,true);label.setGravity(Gravity.CENTER);
      chip.addView(label);
      TextView qty=text(String.format(Locale.US,"%,d ပုဒ်",counts[i]),11,active?DEEP:GOLD,true);
      qty.setGravity(Gravity.CENTER);chip.addView(qty);
      LinearLayout.LayoutParams p=lp(-2,66);p.setMargins(0,0,dp(8),0);categoryChips.addView(chip,p);
      chip.setOnClickListener(v->{categoryFilter=x;renderCategoryChips();filter();});
    }
  }
  interface Select {void chose(int index);}
  void initSpinner(Spinner s,List<String> values,int current,Select listener){ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values){@Override public View getView(int p,View old,ViewGroup parent){TextView t=(TextView)super.getView(p,old,parent);t.setTextSize(12);t.setTextColor(GOLD);t.setPadding(dp(8),dp(8),dp(4),dp(8));return t;}};adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);s.setAdapter(adapter);s.setBackground(shape(CARD,12,GOLDDIM));s.setSelection(Math.max(0,Math.min(current,values.size()-1)));s.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){listener.chose(pos);}public void onNothingSelected(android.widget.AdapterView<?> p){}});}
  String group(String s){
    String l=(s==null?"":s).toLowerCase(Locale.ROOT);
    if(s==null)return cat[cat.length-1];
    if(s.contains("ပဋ္ဌာန်း")||s.contains("ပဌာန်း")||l.contains("pahtan")||l.contains("patthana")||l.contains("patan"))return cat[1];
    if(s.contains("အဘိဓမ္မာ")||l.contains("abhidhamma"))return cat[2];
    if(s.contains("ပဋိစ္စ")||l.contains("paticca")||l.contains("dependent origination"))return cat[3];
    if(s.contains("ဝိပဿနာ")||s.contains("ဝိပဿ")||s.contains("သတိပဋ္ဌာန်")||l.contains("vipassana"))return cat[4];
    if(s.contains("သမထ")||s.contains("ဘာဝနာ")||s.contains("တရားထိုင်")||l.contains("samatha")||l.contains("meditation"))return cat[5];
    if(s.contains("ပရိတ်")||s.contains("ဂါထာ")||s.contains("ဝတ်ရွတ်")||l.contains("paritta")||l.contains("chant"))return cat[6];
    if(s.contains("သုတ်")||s.contains("သုတ္တန်")||l.contains("sutta")||l.contains("sutra"))return cat[7];
    if(s.contains("သီလ")||s.contains("မေတ္တာ")||s.contains("ဒါန")||s.contains("ကရုဏာ")||l.contains("metta"))return cat[8];
    return cat[9];
  }
  void filter(){
    if(listArea==null||!screen.equals("Radio"))return;
    results.clear();String q=query.toLowerCase(Locale.ROOT).trim();
    synchronized(catalog){for(Track t:catalog.values()){
      if(monkFilter>0&&t.monkId!=monkFilter)continue;
      if(categoryFilter>0&&!cat[categoryFilter].equals(group(t.title)))continue;
      if(!q.isEmpty()&&!(t.title+" "+t.monk).toLowerCase(Locale.ROOT).contains(q))continue;
      results.add(t);
    }}
    listArea.removeAllViews();displayedTracks=0;
    LinearLayout summary=row();summary.setPadding(dp(12),dp(12),dp(12),dp(12));
    summary.setBackground(shape(0xff183f37,12,0xff4b634c));
    listSummary=text(String.format(Locale.US,"တရားတော် %,d ပုဒ် • %s",results.size(),cat[categoryFilter]),14,GOLD,true);
    summary.addView(listSummary,new LinearLayout.LayoutParams(0,-2,1));
    TextView check=text("↓ SCROLL",11,MUTED,true);summary.addView(check);
    listArea.addView(summary);listArea.addView(space(10));
    if(results.isEmpty()){
      listArea.addView(text("ရှာဖွေမှုနှင့်ကိုက်ညီသော MP3 မတွေ့ပါ။ Category သို့မဟုတ် ဆရာတော်ကို ပြန်ရွေးပါ။",12,MUTED,false));
      return;
    }
    appendTrackPage();
  }
  void appendTrackPage(){
    if(listArea==null||!screen.equals("Radio"))return;
    // Remove previous load-more footer before appending new items.
    if(displayedTracks>0&&displayedTracks<results.size()&&listArea.getChildCount()>0){
      View last=listArea.getChildAt(listArea.getChildCount()-1);
      if("load_more".equals(last.getTag()))listArea.removeView(last);
    }
    int to=Math.min(results.size(),displayedTracks+LIST_PAGE_SIZE);
    for(int i=displayedTracks;i<to;i++){
      Track track=results.get(i);LinearLayout card=trackRow(track);
      LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,0,0,dp(9));
      listArea.addView(card,p);
      card.setOnClickListener(v->play(track));
      card.setOnLongClickListener(v->{trackActions(track);return true;});
    }
    displayedTracks=to;
    if(displayedTracks<results.size()){
      String title=String.format(Locale.US,"↓  နောက်ထပ် %d ပုဒ်ကြည့်ရန်  •  %,d / %,d",
        Math.min(LIST_PAGE_SIZE,results.size()-displayedTracks),displayedTracks,results.size());
      TextView load=click(title,()->appendTrackPage());
      load.setTag("load_more");load.setPadding(dp(14),dp(16),dp(14),dp(16));
      listArea.addView(load,lp(-1,-2));
    }else{
      TextView end=text(String.format(Locale.US,"END OF RESULTS • %,d MP3",displayedTracks),11,MUTED,false);
      end.setGravity(Gravity.CENTER);listArea.addView(end);
    }
  }
  LinearLayout trackRow(Track t){
    LinearLayout card=row();card.setPadding(dp(12),dp(13),dp(12),dp(13));
    card.setBackground(shape(0xff143d35,16,0xff355b4c));
    ImageView photo=monkPic(t.monkId,60);card.addView(photo,lp(60,60));
    LinearLayout title=vert();title.setPadding(dp(12),0,dp(6),0);
    TextView name=text(t.title,14,IVORY,true);name.setMaxLines(2);title.addView(name);
    TextView who=text(t.monk,11,MUTED,false);who.setMaxLines(1);title.addView(who);
    String saved=settings.getString("audit_"+t.url.hashCode(),"");
    String label=downloadFile(t).exists()?"● OFFLINE SAVED":saved.startsWith("OK")?"● LINK REACHABLE":
      saved.startsWith("ERR")?"● LINK CHECK FAILED":"♫ MP3 LINK · NOT YET CHECKED";
    TextView status=text(label,10,saved.startsWith("ERR")?0xffe4bba8:GOLD,false);
    status.setMaxLines(1);title.addView(status);
    card.addView(title,new LinearLayout.LayoutParams(0,-2,1));
    TextView play=text("▶",19,DEEP,true);play.setGravity(Gravity.CENTER);
    play.setBackground(shape(GOLD,25,0));card.addView(play,lp(42,42));
    return card;
  }
  void quickTrackList(int max,boolean onlyFav){int i=0;ArrayList<Track> data; synchronized(catalog){data=new ArrayList<>(catalog.values());}for(Track t:data){if(onlyFav&&!favorites.contains(t.url))continue;if(i++>=max)break;LinearLayout r=trackRow(t);body.addView(r,lp(-1,77));body.addView(space(7));r.setOnClickListener(v->play(t));r.setOnLongClickListener(v->{trackActions(t);return true;});}if(i==0)body.addView(text("စာရင်းမရှိသေးပါ",12,MUTED,false));}
  void trackActions(Track t){
    String fav=favorites.contains(t.url)?"♥ Favorites မှဖယ်":"♡ Favorites ထည့်";
    String downloaded=downloadFile(t).exists()?"Offline နားထောင်":"↓ Offline Download";
    new AlertDialog.Builder(this).setTitle(t.title).setMessage(t.monk+"\n"+group(t.title))
      .setItems(new String[]{"▶  နားထောင်ရန်",fav,downloaded,
        "🔎 MP3 Link စစ်ဆေးရန်","↗ မူရင်း MP3 ဖွင့်ရန်","↗ ဆရာတော် Source Page"},(d,i)->{
        switch(i){
          case 0:play(t);break;
          case 1:toggleFavorite(t);break;
          case 2:download(t);break;
          case 3:checkTrackLink(t);break;
          case 4:open(t.url);break;
          case 5:open(t.source==null||t.source.isEmpty()?"https://www.dhammadownload.com/":t.source);break;
        }
      }).show();
  }
  void checkTrackLink(Track t){
    toast("MP3 Link ကို စစ်ဆေးနေသည်…");
    background.execute(()->{
      String error=null,good="";
      try{
        URL url=new URL(t.url);String host=url.getHost().toLowerCase(Locale.ROOT);
        if(!"https".equalsIgnoreCase(url.getProtocol())&&! "http".equalsIgnoreCase(url.getProtocol()))
          throw new IOException("HTTP(S) URL required");
        if(!(host.equals("dhammadownload.com")||host.endsWith(".dhammadownload.com")))
          throw new IOException("Unknown audio host: "+host);
        HttpURLConnection c=(HttpURLConnection)url.openConnection();
        c.setConnectTimeout(12000);c.setReadTimeout(15000);
        c.setRequestProperty("User-Agent","Mozilla/5.0 (Android; DAMA Link Check)");
        c.setRequestProperty("Range","bytes=0-511");
        try{
          int code=c.getResponseCode();String type=c.getContentType();
          if(code!=200&&code!=206)throw new IOException("HTTP "+code);
          if(type!=null&&(type.toLowerCase(Locale.ROOT).contains("text/html")||
             type.toLowerCase(Locale.ROOT).contains("text/plain")))
            throw new IOException("Not MP3 content: "+type);
          good="HTTP "+code+" • "+(type==null?"unknown content type":type);
        }finally{c.disconnect();}
      }catch(Exception ex){error=ex.getMessage()==null?ex.getClass().getSimpleName():ex.getMessage();}
      final String result=error==null?"OK "+good:"ERR "+error;
      settings.edit().putString("audit_"+t.url.hashCode(),result).apply();
      handler.post(()->{
        if(isFinishing()||isDestroyed())return;
        new AlertDialog.Builder(this).setTitle(result.startsWith("OK")?"MP3 Link Reachable":"MP3 Link Problem")
          .setMessage(t.title+"\n"+result+"\n\nHTTP reachability only. A playable MP3 stream is not guaranteed.")
          .setPositiveButton("▶  PLAY",(d,w)->play(t))
          .setNeutralButton("SOURCE",(d,w)->open(t.source==null||t.source.isEmpty()?t.url:t.source))
          .setNegativeButton("OK",null).show();
        if(screen.equals("Radio"))filter();
      });
    });
  }
  void toggleFavorite(Track t){if(!favorites.add(t.url))favorites.remove(t.url);settings.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();toast(favorites.contains(t.url)?"Favorites ထဲထည့်ပြီးပါပြီ":"Favorites မှဖယ်ရှားပြီးပါပြီ");if(screen.equals("Collection"))show("Collection");}
  void rememberRecent(Track t){recent.remove(t.url);recent.add(0,t.url);while(recent.size()>50)recent.remove(recent.size()-1);settings.edit().putString("recent",new JSONArray(recent).toString()).apply();}
  /**
   * V11: Open the Player screen FIRST. Loading a network URI can take several seconds
   * and must not block the main UI thread. The controls remain usable on errors.
   */
  void play(Track track){
    if(track==null)return;
    release();
    selected=track;
    ready=false;buffering=true;playerState="Loading";
    rememberRecent(track);
    final int generation=playerGeneration;
    show("Playing");
    // Show artwork, title, seekbar and controls before attempting any network work.
    handler.postDelayed(()->prepareTrackAsync(track,generation),140);
  }

  void openPlayerPreview(){
    if(selected==null){
      synchronized(catalog){
        if(!catalog.isEmpty())selected=catalog.values().iterator().next();
      }
    }
    if(selected==null){toast("MP3 စာရင်းမရှိသေးပါ");return;}
    if(player==null){ready=false;buffering=false;playerState="Paused";}
    show("Playing");
  }

  void prepareTrackAsync(Track track,int generation){
    if(generation!=playerGeneration||isFinishing()||isDestroyed())return;
    if(track.url==null||(!track.url.startsWith("https://")&&!track.url.startsWith("http://"))){
      recordPlayerError("SOURCE_INVALID","MP3 URL is not HTTP(S)");
      buffering=false;playerState="Error";updatePlayerStatus();showPlayerErrorDialog();return;
    }
    final File saved=downloadFile(track);
    final MediaPlayer instance;
    try{
      instance=new MediaPlayer();player=instance;
      instance.setAudioAttributes(new AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .setUsage(AudioAttributes.USAGE_MEDIA).build());
      instance.setOnPreparedListener(p->{
        if(generation!=playerGeneration||p!=player)return;
        ready=true;buffering=false;playerState="Ready";
        if(!requestPlayerAudioFocus()){
          playerState="Paused";
        }else{
          try{
            p.start();playerState="Playing";
            if(speed!=1f)applySpeed(false);
          }catch(Exception e){
            recordPlayerError("START",e.toString());release();
            playerState="Error";showPlayerErrorDialog();return;
          }
        }
        if(screen.equals("Playing"))show("Playing");else updatePlayerStatus();
      });
      instance.setOnInfoListener((p,what,extra)->{
        if(p!=player)return true;
        if(what==MediaPlayer.MEDIA_INFO_BUFFERING_START){
          buffering=true;playerState="Buffering";updatePlayerStatus();
        }else if(what==MediaPlayer.MEDIA_INFO_BUFFERING_END){
          buffering=false;playerState=ready&&p.isPlaying()?"Playing":"Paused";updatePlayerStatus();
        }
        return false;
      });
      instance.setOnCompletionListener(p->{
        if(p!=player)return;
        if(repeat){try{p.seekTo(0);p.start();playerState="Playing";}catch(Exception ignored){playerState="Stopped";}}
        else if(shuffle)skip(1);
        else{playerState="Completed";updatePlayerStatus();}
      });
      instance.setOnErrorListener((p,what,extra)->{
        if(generation!=playerGeneration||p!=player)return true;
        recordPlayerError("MP_"+what+"/"+extra,errorMeaning(what,extra));
        release();playerState="Error";buffering=false;
        handler.post(()->{
          if(isFinishing()||isDestroyed())return;
          updatePlayerStatus();
          if(screen.equals("Playing"))show("Playing");
          showPlayerErrorDialog();
        });
        return true;
      });
    }catch(Exception e){
      recordPlayerError("PLAYER_INIT",e.toString());release();
      playerState="Error";buffering=false;updatePlayerStatus();showPlayerErrorDialog();return;
    }
    // setDataSource can block on DNS/TLS/redirects: never run it on the UI thread.
    background.execute(()->{
      try{
        if(generation!=playerGeneration)return;
        if(saved.isFile())instance.setDataSource(saved.getAbsolutePath());
        else instance.setDataSource(MainActivity.this,Uri.parse(track.url));
        if(generation!=playerGeneration)return;
        instance.prepareAsync();
      }catch(Exception ex){
        if(generation!=playerGeneration)return;
        handler.post(()->{
          if(generation!=playerGeneration||isFinishing()||isDestroyed())return;
          recordPlayerError("NETWORK_SOURCE",ex.getClass().getSimpleName()+": "+ex.getMessage());
          release();playerState="Error";buffering=false;
          if(screen.equals("Playing"))show("Playing");
          showPlayerErrorDialog();
        });
      }
    });
  }
  void pause(){
  if(player==null||!ready){
    if(selected!=null&&player==null){play(selected);}
    else toast("အသံဖိုင် ပြင်ဆင်နေပါတယ်…");
    return;
  }
  try{
    if(player.isPlaying()){player.pause();playerState="Paused";}
    else{
      if(!requestPlayerAudioFocus()){toast("Audio Focus မရနိုင်သေးပါ");return;}
      if(playerState.equals("Completed"))player.seekTo(0);
      player.start();playerState="Playing";
    }
    updatePlayerStatus();
    if(screen.equals("Playing"))show("Playing");
  }catch(Exception ex){
    recordPlayerError("PLAY_PAUSE",ex.toString());release();playerState="Error";showPlayerErrorDialog();
  }
}
  void release(){
  ++playerGeneration;
  MediaPlayer old=player;player=null;ready=false;buffering=false;
  if(old!=null){try{old.setOnPreparedListener(null);old.setOnInfoListener(null);old.setOnErrorListener(null);old.setOnCompletionListener(null);old.reset();old.release();}catch(Exception ignored){try{old.release();}catch(Exception unused){}}}
  abandonPlayerAudioFocus();
}
  void miniPlayer(){
  miniBar=vert();miniBar.setPadding(dp(10),dp(7),dp(10),dp(6));
  GradientDrawable panel=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
     new int[]{0xff173b34,0xff0a2c29});
  panel.setCornerRadius(dp(14));panel.setStroke(dp(1),GOLDDIM);miniBar.setBackground(panel);
  LinearLayout line=row();
  ImageView cover=new ImageView(this);cover.setImageResource(R.drawable.radio_pattana);
  cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setBackground(shape(CARD,9,0));
  cover.setClipToOutline(true);line.addView(cover,lp(43,43));
  LinearLayout labelGroup=vert();labelGroup.setPadding(dp(10),0,dp(3),0);
  TextView label=text(selected.title,11,IVORY,true);label.setMaxLines(1);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);
  labelGroup.addView(label);
  miniStateLabel=text("♫  "+selected.monk+"  •  "+playerState,10,MUTED,false);miniStateLabel.setSingleLine(true);
  labelGroup.addView(miniStateLabel);line.addView(labelGroup,new LinearLayout.LayoutParams(0,-2,1));
  TextView open=text("⌃",22,GOLD,true);open.setGravity(Gravity.CENTER);line.addView(open,lp(30,43));open.setOnClickListener(v->show("Playing"));
  TextView toggle=text(ready&&player!=null&&player.isPlaying()?"Ⅱ":"▶",18,DEEP,true);
  toggle.setGravity(Gravity.CENTER);toggle.setBackground(shape(GOLD,18,0));line.addView(toggle,lp(39,39));
  toggle.setOnClickListener(v->pause());labelGroup.setOnClickListener(v->show("Playing"));
  miniBar.addView(line);
  ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
  progress.setMax(1000);
  progress.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));
  progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff33544c));
  if(ready&&player!=null)try{int length=player.getDuration();progress.setProgress(length>0?player.getCurrentPosition()*1000/length:0);}catch(Exception ignored){}
  LinearLayout.LayoutParams p=lp(-1,3);p.topMargin=dp(6);miniBar.addView(progress,p);
  LinearLayout.LayoutParams wrap=lp(-1,77);wrap.setMargins(dp(9),0,dp(9),0);outer.addView(miniBar,wrap);
}
  String playbackStatusText(){
    String msg=buffering?"Buffering • အင်တာနက်ကြောင့် ခဏစောင့်နေသည်":playerState;
    if("Loading".equals(msg))msg="Loading • တရားတော် ဖွင့်ရန်ပြင်ဆင်နေသည်";
    if("Playing".equals(msg))msg="♫  တရားတော် ဖွင့်နေပါသည်";
    if("Paused".equals(msg))msg="Ⅱ  ခေတ္တရပ်ထားသည်";
    if("Completed".equals(msg))msg="✔  တရားတော် ပြီးဆုံးပါပြီ";
    if("Error".equals(msg))msg="!  Player Error — Help ကိုနှိပ်ပါ";
    return msg;
  }
  void updatePlayerStatus(){
    if(stateLabel!=null&&screen.equals("Playing")){stateLabel.setText(playbackStatusText());stateLabel.setTextColor("Error".equals(playerState)?0xffff8e88:MUTED);}
    if(miniStateLabel!=null&&!screen.equals("Playing")&&selected!=null)miniStateLabel.setText("♫  "+selected.monk+"  •  "+playbackStatusText());
  }
  TextView roundPlayerButton(String label,int height,boolean main,Runnable action){
    TextView button=text(label,main?25:16,main?DEEP:GOLD,true);
    button.setGravity(Gravity.CENTER);button.setBackground(shape(main?GOLD:CARD,35,main?0:GOLDDIM));
    button.setOnClickListener(v->action.run());button.setLayoutParams(lp(height,height));return button;
  }
  TextView playerAction(String label,Runnable action){
    TextView item=text(label,11,GOLD,true);item.setGravity(Gravity.CENTER);
    item.setBackground(shape(CARD,14,0xff557359));item.setOnClickListener(v->action.run());return item;
  }
  void seekBy(int delta){
    if(player==null||!ready){toast("Audio Ready မဖြစ်သေးပါ");return;}
    try{int max=player.getDuration();if(max<=0){toast("ဤ MP3 မှာ Seek မရနိုင်ပါ");return;}
      int where=(int)Math.max(0,Math.min((long)max,(long)player.getCurrentPosition()+delta));
      player.seekTo(where);
      if(timeLabel!=null)timeLabel.setText(durationTime(where));
    }catch(Exception ex){recordPlayerError("SEEK_15_SEC",ex.toString());toast("Seek မအောင်မြင်ပါ");}
  }
  void applySpeed(boolean refresh){
    if(player!=null&&ready)try{
      boolean paused=!player.isPlaying();
      PlaybackParams params=player.getPlaybackParams();params.setSpeed(speed);
      player.setPlaybackParams(params);if(paused)player.pause();
    }catch(Exception ex){recordPlayerError("SPEED",ex.toString());toast("Playback Speed ပြောင်းမရပါ");}
    if(refresh&&screen.equals("Playing"))show("Playing");
  }
  void speedMenu(){
    String[] labels={"0.75×  Slow","1.0×  Normal","1.25×","1.5×","1.75×","2.0×"};
    float[] values={.75f,1f,1.25f,1.5f,1.75f,2f};
    new AlertDialog.Builder(this).setTitle("Playback Speed").setSingleChoiceItems(labels,-1,(dialog,choice)->{
      speed=values[choice];applySpeed(true);dialog.dismiss();
    }).setNegativeButton("Cancel",null).show();
  }
  void sleepMenu(){
    String[] options={"Off","10 minutes","20 minutes","30 minutes","45 minutes","60 minutes"};
    int[] minutes={0,10,20,30,45,60};
    new AlertDialog.Builder(this).setTitle("⏱ Sleep Timer").setItems(options,(d,i)->{
      if(sleepJob!=null)handler.removeCallbacks(sleepJob);
      if(minutes[i]==0){sleepUntilMs=0;sleepJob=null;toast("Sleep Timer OFF");}
      else{
        sleepUntilMs=SystemClock.elapsedRealtime()+minutes[i]*60000L;
        sleepJob=()->{
          sleepUntilMs=0;if(player!=null&&ready&&player.isPlaying()){
            try{player.pause();playerState="Paused";updatePlayerStatus();}
            catch(Exception ex){recordPlayerError("SLEEP_TIMER",ex.toString());}
          }
        };
        handler.postDelayed(sleepJob,minutes[i]*60000L);
        toast("Sleep Timer "+minutes[i]+" minutes");
      }
      if(screen.equals("Playing"))show("Playing");
    }).show();
  }
  void queueMenu(){
    ArrayList<Track> tracks=new ArrayList<>();synchronized(catalog){tracks.addAll(catalog.values());}
    if(tracks.isEmpty()){toast("Queue မရှိသေးပါ");return;}
    int current=0;for(int i=0;i<tracks.size();i++)if(selected!=null&&tracks.get(i).url.equals(selected.url)){current=i;break;}
    int start=Math.max(0,current-4),end=Math.min(tracks.size(),current+20);
    String[] labels=new String[end-start];
    for(int i=start;i<end;i++)labels[i-start]=(i==current?"▶ ":"♫ ")+tracks.get(i).title+"\n"+tracks.get(i).monk;
    final int offset=start;
    new AlertDialog.Builder(this).setTitle("PLAY QUEUE • "+tracks.size()+" tracks")
      .setItems(labels,(d,index)->play(tracks.get(offset+index))).setNegativeButton("Close",null).show();
  }
  boolean requestPlayerAudioFocus(){
    if(audioManager==null)return true;
    try{
      if(audioFocusRequest==null){
        audioFocusRequest=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
          .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
          .setOnAudioFocusChangeListener(change->{
            if(change==AudioManager.AUDIOFOCUS_LOSS||change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
              handler.post(()->{
                if(player!=null&&ready)try{if(player.isPlaying()){player.pause();playerState="Paused";updatePlayerStatus();}}
                catch(Exception ignored){}
              });
            } else if(change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
              handler.post(()->{if(player!=null)try{player.setVolume(.3f,.3f);}catch(Exception ignored){}});
            } else if(change==AudioManager.AUDIOFOCUS_GAIN){
              handler.post(()->{if(player!=null)try{player.setVolume(1f,1f);}catch(Exception ignored){}});
            }
          }).build();
      }
      focusGranted=audioManager.requestAudioFocus(audioFocusRequest)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
      return focusGranted;
    }catch(Exception ex){recordPlayerError("AUDIO_FOCUS",ex.toString());return false;}
  }
  void abandonPlayerAudioFocus(){
    if(audioManager!=null&&audioFocusRequest!=null&&focusGranted){
      try{audioManager.abandonAudioFocusRequest(audioFocusRequest);}catch(Exception ignored){}
    }focusGranted=false;
  }
  String errorMeaning(int what,int extra){
    switch(extra){
      case -1004:return "Network/Connection Error (MEDIA_ERROR_IO) • Internet သို့မဟုတ် Server စစ်ပါ။";
      case -1007:return "Corrupt/Malformed MP3 (MEDIA_ERROR_MALFORMED).";
      case -1010:return "Unsupported Audio Format (MEDIA_ERROR_UNSUPPORTED).";
      case -110:return "Server Timeout (MEDIA_ERROR_TIMED_OUT) • Retry လုပ်ပါ။";
      default: return "Audio Decoder/Media Server Error "+what+"/"+extra+".";
    }
  }
  void recordPlayerError(String code,String reason){
    String title=selected==null?"Unknown track":selected.title;
    lastPlayerError="DAMA RADIO V7\nError: "+code+"\nReason: "+reason+
      "\nTrack: "+title+"\nTime: "+new java.util.Date().toString()+
      "\nHint: Check network, permissions, or open publisher source.\n"+
      "This report records application error state, not proof of full MP3 playback.";
    if(settings!=null)settings.edit().putString("last_player_error",lastPlayerError).apply();
    android.util.Log.e("DAMA_PLAYER",lastPlayerError);
  }
  void playerErrorReport(){
    String message=lastPlayerError.isEmpty()?"No player errors recorded on this device yet.\n\nIf MP3 fails, Retry or open source website.":lastPlayerError;
    new AlertDialog.Builder(this).setTitle("DAMA PLAYER • Error Report")
      .setMessage(message)
      .setPositiveButton("Copy Report",(d,w)->{
        android.content.ClipboardManager clip=(android.content.ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if(clip!=null)clip.setPrimaryClip(android.content.ClipData.newPlainText("DAMA Player Error",message));
        toast("Report ကို Copy လုပ်ပြီးပါပြီ");
      }).setNeutralButton("Retry",(d,w)->{if(selected!=null)play(selected);else toast("MP3 ရွေးချယ်ပါ");})
      .setNegativeButton("Close",null).show();
  }
  void showPlayerErrorDialog(){
    if(isFinishing()||isDestroyed())return;
    String issue=lastPlayerError.isEmpty()?"Audio မဖွင့်နိုင်ပါ":lastPlayerError;
    new AlertDialog.Builder(this).setTitle("!  MP3 Playback Error")
      .setMessage(issue)
      .setPositiveButton("Retry",(d,w)->{if(selected!=null)play(selected);})
      .setNeutralButton("Publisher Link",(d,w)->{if(selected!=null)open(selected.url);})
      .setNegativeButton("Close",(d,w)->{}).show();
  }
  void nowPlaying(){
  android.util.Log.i("DAMA_PLAYER_UI","NOW_PLAYING_SCREEN_VISIBLE title="+(selected==null?"none":selected.title));
  if(selected==null){heading("♫  NOW PLAYING");body.addView(text("နားဆင်လိုသော တရားတော်ကို ရွေးချယ်ပါ။",15,MUTED,false));return;}
  LinearLayout top=row();
  TextView back=click("‹  စာရင်းသို့",()->show("Radio"));top.addView(back);
  TextView titleTop=text("NOW PLAYING  ✦",12,GOLD,true);titleTop.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
  top.addView(titleTop,new LinearLayout.LayoutParams(0,dp(44),1));body.addView(top);body.addView(space(12));
  android.widget.FrameLayout artFrame=new android.widget.FrameLayout(this);
  GradientDrawable artShape=shape(CARD,24,GOLD);artFrame.setBackground(artShape);artFrame.setClipToOutline(true);
  ImageView artwork=picture(R.drawable.radio_pattana,235);artFrame.addView(artwork,new android.widget.FrameLayout.LayoutParams(-1,dp(235)));
  TextView watermark=text("  ✿ DAMA RADIO  ",12,IVORY,true);
  watermark.setPadding(dp(13),dp(7),dp(13),dp(7));
  watermark.setBackground(shape(0xcc09221d,12,GOLDDIM));
  android.widget.FrameLayout.LayoutParams mark=new android.widget.FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
  mark.bottomMargin=dp(12);artFrame.addView(watermark,mark);
  body.addView(artFrame,lp(-1,235));body.addView(space(17));
  TextView sermon=text(selected.title,21,IVORY,true);sermon.setGravity(Gravity.CENTER);body.addView(sermon);
  TextView sayadaw=text(selected.monk,14,GOLD,false);sayadaw.setGravity(Gravity.CENTER);
  body.addView(sayadaw);body.addView(space(13));
  stateLabel=text(playbackStatusText(),12,playerState.equals("Error")?0xffff7d7d:MUTED,false);
  stateLabel.setGravity(Gravity.CENTER);stateLabel.setPadding(0,dp(7),0,dp(7));
  stateLabel.setBackground(shape(CARD,16,GOLDDIM));body.addView(stateLabel,lp(-1,33));body.addView(space(12));
  playSeek=new SeekBar(this);playSeek.setMax(1000);
  playSeek.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));
  playSeek.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff3f5a54));
  playSeek.setThumbTintList(android.content.res.ColorStateList.valueOf(GOLD));
  body.addView(playSeek,lp(-1,35));
  playSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
    public void onStartTrackingTouch(SeekBar s){}
    public void onStopTrackingTouch(SeekBar s){
      if(player!=null&&ready)try{long length=player.getDuration();if(length>0)player.seekTo((int)(length*s.getProgress()/1000));}
      catch(Exception ex){recordPlayerError("SEEK",ex.toString());}
    }
    public void onProgressChanged(SeekBar s,int p,boolean fromUser){}
  });
  LinearLayout clocks=row();
  TextView elapsed=text("00:00",11,MUTED,false);timeLabel=elapsed;
  clocks.addView(elapsed,new LinearLayout.LayoutParams(0,-2,1));
  TextView duration=text("--:--",11,MUTED,false);duration.setGravity(Gravity.RIGHT);
  clocks.addView(duration,new LinearLayout.LayoutParams(0,-2,1));body.addView(clocks);
  if(ticker!=null)handler.removeCallbacks(ticker);
  final SeekBar seekRef=playSeek;final TextView currentRef=elapsed,wholeRef=duration;
  ticker=new Runnable(){public void run(){
    if(!screen.equals("Playing")||seekRef!=playSeek||player==null)return;
    if(ready)try{
      int length=player.getDuration(),current=player.getCurrentPosition();
      seekRef.setProgress(length>0?(int)(current*1000L/length):0);
      currentRef.setText(durationTime(current));wholeRef.setText(durationTime(length));
    }catch(Exception ignored){}
    handler.postDelayed(this,950);
  }};
  handler.postDelayed(ticker,250);
  body.addView(space(16));
  LinearLayout transport=row();transport.setGravity(Gravity.CENTER);
  transport.addView(roundPlayerButton("❮❮",45,false,()->skip(-1)));transport.addView(spaceX(9));
  transport.addView(roundPlayerButton("−15",48,false,()->seekBy(-15000)));transport.addView(spaceX(10));
  transport.addView(roundPlayerButton(ready&&player!=null&&player.isPlaying()?"Ⅱ":"▶",65,true,()->pause()));transport.addView(spaceX(10));
  transport.addView(roundPlayerButton("+15",48,false,()->seekBy(15000)));transport.addView(spaceX(9));
  transport.addView(roundPlayerButton("❯❯",45,false,()->skip(1)));
  body.addView(transport);body.addView(space(17));
  LinearLayout modes=row();modes.setGravity(Gravity.CENTER);
  modes.addView(playerAction(shuffle?"Shuffle ON":"Shuffle",()->{shuffle=!shuffle;show("Playing");}),new LinearLayout.LayoutParams(0,dp(41),1));
  modes.addView(playerAction(repeat?"Repeat ON":"Repeat 1",()->{repeat=!repeat;show("Playing");}),new LinearLayout.LayoutParams(0,dp(41),1));
  modes.addView(playerAction(speed+"×",()->speedMenu()),new LinearLayout.LayoutParams(0,dp(41),1));
  modes.addView(playerAction("⏱ Sleep",()->sleepMenu()),new LinearLayout.LayoutParams(0,dp(41),1));
  body.addView(modes);body.addView(space(11));
  LinearLayout utilities=row();utilities.setGravity(Gravity.CENTER);
  utilities.addView(playerAction(favorites.contains(selected.url)?"♥ Saved":"♡ Save",()->toggleFavorite(selected)),new LinearLayout.LayoutParams(0,dp(40),1));
  utilities.addView(playerAction("↓ Offline",()->download(selected)),new LinearLayout.LayoutParams(0,dp(40),1));
  utilities.addView(playerAction("☰ Queue",()->queueMenu()),new LinearLayout.LayoutParams(0,dp(40),1));
  utilities.addView(playerAction("! Help",()->playerErrorReport()),new LinearLayout.LayoutParams(0,dp(40),1));
  body.addView(utilities);
  body.addView(space(15));
  if(playerState.equals("Error")){
    TextView retry=click("↻  Retry MP3 or open source website",()->showPlayerErrorDialog());
    retry.setTextColor(0xffffbbbb);body.addView(retry);body.addView(space(9));
  }
  TextView source=click("↗  မူရင်း တရားတော် Link ကိုဖွင့်ရန်",()->open(selected.url));
  body.addView(source);body.addView(space(17));
  if(sleepUntilMs>SystemClock.elapsedRealtime()){
    TextView sleep=text("⏱  Sleep timer active",11,GOLD,false);body.addView(sleep);
  }
  TextView note=text("MediaPlayer • MP3 Streaming • Offline Downloaded Audio supported. အသံမဖွင့်နိုင်ပါက Help မှ Error ကိုကြည့်နိုင်ပါသည်။",11,MUTED,false);
  body.addView(note);body.addView(space(24));
}
  String durationTime(int ms){int seconds=Math.max(0,ms/1000);return String.format(Locale.US,"%02d:%02d",seconds/60,seconds%60);}
  void skip(int delta){
  if(selected==null)return;
  ArrayList<Track> tracks=new ArrayList<>();synchronized(catalog){tracks.addAll(catalog.values());}
  if(tracks.isEmpty())return;
  int at=-1;for(int i=0;i<tracks.size();i++)if(tracks.get(i).url.equals(selected.url)){at=i;break;}
  int next;
  if(shuffle&&tracks.size()>1){next=new Random().nextInt(tracks.size()-1);if(next>=at&&at>=0)next++;}
  else next=(at+delta+tracks.size())%tracks.size();
  play(tracks.get(Math.max(0,Math.min(next,tracks.size()-1))));
}
  void collection(){heading("သိမ်းထားသော တရားတော်များ");LinearLayout tabs=row();for(String mode:new String[]{"Favorites","Downloads","Recent"}){TextView b=click(mode,()->{libraryMode=mode;show("Collection");});tabs.addView(b,new LinearLayout.LayoutParams(0,-2,1));}body.addView(tabs);body.addView(space(15));
    int found=0;for(Track t:new ArrayList<>(catalog.values())){boolean yes=libraryMode.equals("Favorites")?favorites.contains(t.url):libraryMode.equals("Downloads")?downloadFile(t).exists():recent.contains(t.url);if(!yes)continue;LinearLayout r=trackRow(t);body.addView(r,lp(-1,72));body.addView(space(8));r.setOnClickListener(v->play(t));r.setOnLongClickListener(v->{trackActions(t);return true;});found++;}if(found==0)body.addView(text("ဤကဏ္ဍတွင် မရှိသေးပါ။ တရားတော်ကို နားထောင်ပြီး Favorites/Download ပြုလုပ်နိုင်ပါတယ်။",13,MUTED,false));footnote();
  }
  File downloadFile(Track t){String key=Integer.toHexString(t.url.hashCode());File dir=new File(getExternalFilesDir(null),"DhammaMP3");if(!dir.exists())dir.mkdirs();return new File(dir,key+".mp3");}
  void download(Track t){File target=downloadFile(t);if(target.exists()){play(t);return;}
    new AlertDialog.Builder(this).setTitle("Offline MP3 ရယူရန်").setMessage("တရားတော်အသံဖိုင်ကို မူရင်း Website မှ ကိုယ်ပိုင်ဖုန်းထဲ Download လုပ်ပြီး Offline နားထောင်ရန် ဖြစ်ပါသည်။ Download ပမာဏကြီးနိုင်ပါတယ်။\n\n"+t.title).setPositiveButton("Download",(d,w)->{
    toast("Download စတင်နေပါပြီ…");background.execute(()->{File tmp=new File(target.getPath()+".part");try{URL url=new URL(t.url);HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(18000);c.setReadTimeout(45000);c.setInstanceFollowRedirects(true);if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());long length=c.getContentLengthLong();if(length>180L*1024L*1024L)throw new IOException("Audio file too large");try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(tmp)){byte[] bytes=new byte[32768];int n;long total=0;while((n=in.read(bytes))!=-1){total+=n;if(total>180L*1024L*1024L)throw new IOException("Audio limit exceeded");out.write(bytes,0,n);}if(total<10000)throw new IOException("File is unexpectedly small");}if(!tmp.renameTo(target))throw new IOException("File move failed");handler.post(()->{toast("MP3 Download ပြီးပါပြီ · Offline ဖွင့်နိုင်ပါပြီ");if(screen.equals("Collection"))show("Collection");});}catch(Exception e){tmp.delete();handler.post(()->toast("Download မအောင်မြင်: "+e.getMessage()));}});
    }).setNegativeButton("မလုပ်တော့ပါ",null).show();}
  void other(String title,String b,String url){heading(title);ImageView img=picture(R.drawable.radio_hero,220);body.addView(img,lp(-1,220));body.addView(space(14));body.addView(text(b,18,IVORY,true));body.addView(space(14));body.addView(click("မူရင်းဓမ္မစာမျက်နှာသို့ သွားရန် ↗",()->open(url)));body.addView(space(16));body.addView(text("DAMA Radio V2 အဓိက MP3 Library ကို ပြန်သွားနိုင်ပါတယ်။",12,MUTED,false));body.addView(click("♫  DAMA RADIO",()->show("Radio")));}
  void bottomBar(){FooterNav.attach(this,outer,"Radio");}
  void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception ex){toast("Website မဖွင့်နိုင်ပါ");}}
  void saveCache(){File out=new File(getFilesDir(),CACHE_FILE),tmp=new File(getFilesDir(),CACHE_FILE+".tmp");try(FileOutputStream f=new FileOutputStream(tmp)){JSONArray a=new JSONArray();synchronized(catalog){for(Track t:catalog.values())a.put(t.json());}f.write(a.toString().getBytes(StandardCharsets.UTF_8));f.getFD().sync();if(!tmp.renameTo(out))throw new IOException("rename failed");}catch(Exception e){android.util.Log.e("DAMA","Unable to cache catalog",e);}}
  String fetch(String ref)throws Exception{
    URL initial=new URL(ref);
    if(!"https".equalsIgnoreCase(initial.getProtocol()))throw new IOException("HTTPS required");
    String host=initial.getHost().toLowerCase(Locale.ROOT);
    if(!(host.equals("dhammadownload.com")||host.equals("www.dhammadownload.com")))throw new IOException("Untrusted host");
    HttpURLConnection c=(HttpURLConnection)initial.openConnection();
    c.setConnectTimeout(14000);c.setReadTimeout(22000);
    c.setRequestProperty("User-Agent","Mozilla/5.0 (Android; DAMA Radio Catalog)");
    c.setRequestProperty("Accept","text/html,application/xhtml+xml");
    c.setRequestProperty("Accept-Encoding","identity");
    try{
      int status=c.getResponseCode();if(status!=200)throw new IOException("HTTP "+status);
      URL landed=c.getURL();String landedHost=landed.getHost().toLowerCase(Locale.ROOT);
      if(!(landedHost.equals("dhammadownload.com")||landedHost.equals("www.dhammadownload.com")))throw new IOException("Redirect host not allowed");
      ByteArrayOutputStream buffer=new ByteArrayOutputStream();
      try(InputStream in=c.getInputStream()){
        byte[] data=new byte[16384];int n;
        while((n=in.read(data))!=-1){
          if(buffer.size()+n>12_000_000)throw new IOException("HTML exceeds 12 MB");
          buffer.write(data,0,n);
        }
      }
      String type=c.getContentType();
      if(type!=null&&type.toLowerCase(Locale.ROOT).contains("charset=")){
        String charset=type.replaceAll("(?i).*charset=\\s*([A-Za-z0-9_-]+).*","$1");
        try{return buffer.toString(charset);}catch(Exception ignored){}
      }
      return buffer.toString("UTF-8");
    }finally{c.disconnect();}
  }
  void sync(){
    if(syncing){toast("Sync လုပ်နေဆဲပါ");return;}
    syncing=true;radioSyncDone=0;radioSyncTotal=monks.size();
    show("Radio");
    toast("MP3 Catalog "+catalog.size()+" ပုဒ် Offline ပါရှိပြီးပါပြီ · Website ကို စစ်နေသည်");
    background.execute(()->{
      int okay=0,failed=0,added=0;
      ArrayList<String> faults=new ArrayList<>();
      for(Monk monk:monks){
        try{
          String html=fetch(monk.source);
          List<Track> found=CatalogParser.parse(html,monk.source,monk.id,monk.name);
          int old; synchronized(catalog){old=catalog.size();}
          for(Track t:found){t.category=group(t.title);addCatalog(t);}
          synchronized(catalog){added+=catalog.size()-old;}
          okay++;
        }catch(Exception ex){
          failed++;
          if(faults.size()<7)faults.add(monk.name+": "+ex.getClass().getSimpleName()+" "+ex.getMessage());
        }
        final int progress=okay+failed,total=monks.size(),failCount=failed,count;
        radioSyncDone=progress;
        if(progress==1||progress%3==0||progress==total)
          settings.edit().putInt("radio_sync_last_checked",progress).apply();
        synchronized(catalog){count=catalog.size();}
        handler.post(()->{
          if(screen.equals("Radio")){
            if(information!=null)information.setText("MP3 "+count+" Offline Catalog Links");
            if(radioSyncProgress!=null){radioSyncProgress.setMax(total);radioSyncProgress.setProgress(progress);}
            if(radioSyncStatus!=null)radioSyncStatus.setText("CHECKING "+progress+"/"+total+"  •  "+(100*progress/Math.max(1,total))+"%  •  Errors "+failCount);
          }
        });
      }
      if(added>0)saveCache();
      syncing=false;
      final int successes=okay,errors=failed,newTracks=added;
      settings.edit().putString("radio_sync_summary","MP3 SYNC: "+successes+"/"+monks.size()+" page(s) OK • "+errors+" errors • +"+newTracks+" links")
        .putLong("radio_sync_last_at",System.currentTimeMillis()).apply();
      handler.post(()->{
        String message="Offline MP3 စာရင်း "+catalog.size()+" ပုဒ်\\nWebsite Pages: "+successes+" OK, "+errors+" failed\\nNew links: "+newTracks;
        if(!faults.isEmpty())message+="\\n\\nErrors:\\n"+android.text.TextUtils.join("\\n",faults);
        new AlertDialog.Builder(this).setTitle("DAMA RADIO • Sync Result").setMessage(message)
          .setPositiveButton("OK",null).setNeutralButton("Website",(d,w)->open("https://www.dhammadownload.com/AudioInMyanmar.htm")).show();
        if(screen.equals("Radio"))show("Radio");
      });
    });
  }
  @Override public void onDestroy(){
  if(ticker!=null)handler.removeCallbacks(ticker);
  if(sleepJob!=null)handler.removeCallbacks(sleepJob);
  release();background.shutdownNow();super.onDestroy();
}
}