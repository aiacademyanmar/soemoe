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
public class MainActivity extends Activity {
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
  String screen="Home",query="",libraryMode="Favorites";
  int monkFilter=0, categoryFilter=0, seekInterval=1000;
  String[] cat={"အားလုံး","ပဋ္ဌာန်း","ဝိပဿနာ","ပါဠိ / ပရိတ်","အခြား"};
  LinearLayout outer,body,miniBar,listArea;
  TextView counter,information,miniLabel;
  EditText searchBox;
  MediaPlayer player; Track selected;
  boolean ready=false,syncing=false,repeat=false;
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
    for(String f:new String[]{"seed_tracks.json","full_catalog.json"}){JSONArray rows=assetArray(f);for(int i=0;i<rows.length();i++){JSONObject j=rows.optJSONObject(i);if(j!=null)addCatalog(new Track(j));}}
    File cache=new File(getFilesDir(),CACHE_FILE);if(cache.exists())try(FileInputStream in=new FileInputStream(cache);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);JSONArray rows=new JSONArray(out.toString("UTF-8"));for(int i=0;i<rows.length();i++){JSONObject j=rows.optJSONObject(i);if(j!=null)addCatalog(new Track(j));}}catch(Exception ignored){}
  }
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(DEEP);getWindow().setNavigationBarColor(DEEP);initData();show("Home");}
  void show(String next){screen=next;outer=vert();outer.setBackgroundColor(BG);setContentView(outer);
    createTop(); ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);outer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=vert();body.setPadding(dp(15),dp(13),dp(15),dp(14));scroll.addView(body);
    if(next.equals("Home"))home();else if(next.equals("Radio"))browse();else if(next.equals("Playing"))nowPlaying();else if(next.equals("Collection"))collection();else if(next.equals("Library")){startActivity(new Intent(this,LibraryActivity.class));screen="Home";home();}else if(next.equals("TV"))other("DAMA TV","▶ YouTube တရားတော်", "https://www.dhammadownload.com/VideoInMyanmar.htm");else if(next.equals("Worship"))other("DAILY WORSHIP","✿ နေ့စဉ်ဝတ်ပြုခြင်း", "https://www.dhammadownload.com/");
    if(selected!=null&&!next.equals("Playing"))miniPlayer();bottomBar();
  }
  void createTop(){
  LinearLayout bar=row();bar.setPadding(dp(16),dp(11),dp(16),dp(12));
  bar.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xff092b29,0xff123c36,0xff092925}));
  TextView menu=text("☰",23,GOLD,true);menu.setGravity(Gravity.CENTER);
  menu.setBackground(shape(0xff19453b,14,0));bar.addView(menu,lp(42,42));
  menu.setOnClickListener(v->menu());
  LinearLayout title=vert();title.setGravity(Gravity.CENTER);
  TextView main=text("✿   DAMA RADIO",21,GOLD,true);main.setGravity(Gravity.CENTER);title.addView(main);
  TextView sub=text("ဓမ္မအသံလွှင့် · LISTEN TO WISDOM",10,MUTED,false);sub.setGravity(Gravity.CENTER);title.addView(sub);
  bar.addView(title,new LinearLayout.LayoutParams(0,dp(49),1));
  TextView setting=text("⌕",27,GOLD,false);setting.setGravity(Gravity.CENTER);setting.setBackground(shape(0xff19453b,14,0));
  bar.addView(setting,lp(42,42));setting.setOnClickListener(v->show("Radio"));outer.addView(bar);
}
  void menu(){new AlertDialog.Builder(this).setTitle("DAMA APP").setItems(new String[]{"⌂  Home","♫  DAMA RADIO","▤  Favorites / Downloads","📚  DAMA LIBRARY","▶  DAMA TV","✿  Daily Worship","↻  Sync MP3 Catalog","Source Website"},(d,w)->{switch(w){case 0:show("Home");break;case 1:show("Radio");break;case 2:show("Collection");break;case 3:show("Library");break;case 4:show("TV");break;case 5:show("Worship");break;case 6:sync();break;case 7:open("https://www.dhammadownload.com/AudioInMyanmar.htm");break;}}).show();}
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
  TextView cta=click("▶  စတင်နားထောင်ရန်",()->show("Radio"));cta.setTextColor(DEEP);cta.setBackground(shape(GOLD,24,0));
  overlay.addView(cta,lp(178,43));frame.addView(overlay,new android.widget.FrameLayout.LayoutParams(-1,-1));
  body.addView(frame,lp(-1,218));body.addView(space(17));
}
  void summary(){LinearLayout r=row();r.setBackground(shape(CARD,14,GOLDDIM));r.setPadding(dp(12),dp(12),dp(8),dp(12));
    TextView qty=text("♫  "+catalog.size()+" MP3 links",14,GOLD,true);r.addView(qty,new LinearLayout.LayoutParams(0,-2,1));TextView n=text(monks.size()+" ဆရာတော်",12,MUTED,false);r.addView(n);body.addView(r);body.addView(space(10));}
  void catPills(){LinearLayout row=row();String[] titles={"☸ ပဋ္ဌာန်း","✿ ဝိပဿနာ","♬ ပရိတ်"};for(int i=0;i<3;i++){final int t=i;TextView bt=text(titles[i],12,GOLD,true);bt.setGravity(Gravity.CENTER);bt.setPadding(dp(7),dp(12),dp(7),dp(12));bt.setBackground(shape(CARD,13,GOLDDIM));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(3),0,dp(3),0);row.addView(bt,p);bt.setOnClickListener(v->{categoryFilter=t==0?1:t==1?2:3;show("Radio");});}body.addView(row);body.addView(space(15));}
  void monkTiles(){
  heading("ဆရာတော်ကြီးများ  ·  SAYADAWS");
  HorizontalScrollView horiz=new HorizontalScrollView(this);horiz.setHorizontalScrollBarEnabled(false);
  LinearLayout r=row();
  for(int i=0;i<Math.min(monks.size(),20);i++){
    Monk monk=monks.get(i);LinearLayout tile=vert();tile.setGravity(Gravity.CENTER);
    tile.setPadding(dp(8),dp(10),dp(8),dp(9));tile.setBackground(shape(0xff143f38,17,GOLDDIM));
    ImageView photo=monkPic(monk.id,66);tile.addView(photo,lp(66,66));tile.addView(space(7));
    TextView tx=text(monk.name,11,IVORY,true);tx.setMaxLines(2);tx.setGravity(Gravity.CENTER);tile.addView(tx,lp(100,45));
    LinearLayout.LayoutParams param=lp(116,146);param.setMargins(0,0,dp(10),0);r.addView(tile,param);
    tile.setOnClickListener(v->{monkFilter=monk.id;show("Radio");});
  }
  horiz.addView(r);body.addView(horiz);body.addView(space(18));
}
  ImageView monkPic(int id,int dim){ImageView img=new ImageView(this);int drawable=id==1?R.drawable.mogok_sayadaw:id==2?R.drawable.mahasi_sayadaw:id==4?R.drawable.nandamala_sayadaw:R.drawable.monk_symbol;img.setImageResource(drawable);img.setScaleType(ImageView.ScaleType.CENTER_CROP);img.setBackground(shape(CARD,dim/2,GOLDDIM));img.setClipToOutline(true);return img;}
  void home(){hero();summary();catPills();monkTiles();
    heading("တရားတော် နားထောင်ရန်");quickTrackList(6,false);body.addView(space(12));
    LinearLayout r=row();r.addView(click("↻  Sync MP3",()->sync()),new LinearLayout.LayoutParams(0,-2,1));r.addView(spaceX(8));r.addView(click("စာရင်းအားလုံး  ›",()->show("Radio")),new LinearLayout.LayoutParams(0,-2,1));body.addView(r);
    body.addView(space(15));heading("Featured Collections");addCollectionCard("ပဋ္ဌာန်းတရားတော်",R.drawable.radio_pattana,1);addCollectionCard("ဝိပဿနာတရားတော်",R.drawable.radio_meditation,2);body.addView(space(10));footnote();
  }
  View spaceX(int w){View v=new View(this);v.setLayoutParams(lp(w,1));return v;}
  void addCollectionCard(String s,int img,int category){LinearLayout r=row();ImageView pic=picture(img,75);r.addView(pic,lp(104,75));LinearLayout text=vert();text.setPadding(dp(14),dp(8),dp(8),dp(8));text.addView(text(s,16,IVORY,true));text.addView(text("တရားတော်များ ရှာဖွေကြည့်ရှုရန်  ›",12,MUTED,false));r.addView(text,new LinearLayout.LayoutParams(0,dp(75),1));r.setBackground(shape(CARD,13,0));body.addView(r,lp(-1,78));body.addView(space(8));r.setOnClickListener(v->{categoryFilter=category;show("Radio");});}
  void footnote(){TextView n=text("မူရင်း DhammaDownload MP3 လင့်ခ်များကို Index လုပ်ထားပါသည်။ Offline = စာရင်း/ပုံများ; အသံကို Stream လုပ်ရန် Internet လိုအပ်သည်။ ကိုယ်တိုင် Download လုပ်ထားသော MP3 ကိုသာ Offline နားဆင်နိုင်သည်။",11,MUTED,false);n.setPadding(dp(4),dp(10),dp(4),dp(12));body.addView(n);}
  void browse(){heading("တရားတော် စာကြည့်တိုက်");summary();
    searchBox=new EditText(this);searchBox.setSingleLine(true);searchBox.setTextSize(14);searchBox.setTextColor(IVORY);searchBox.setHintTextColor(MUTED);searchBox.setHint("⌕  တရားတော် / ဆရာတော် ရှာရန်");searchBox.setBackground(shape(CARD,13,GOLDDIM));searchBox.setPadding(dp(14),0,dp(14),0);searchBox.setText(query);body.addView(searchBox,lp(-1,50));body.addView(space(10));
    LinearLayout toolbar=row();Spinner monk=new Spinner(this);ArrayList<String> monkNames=new ArrayList<>();monkNames.add("ဆရာတော်အားလုံး");for(Monk m:monks)monkNames.add(m.name);initSpinner(monk,monkNames,monkFilter,i->{monkFilter=i;filter();});toolbar.addView(monk,new LinearLayout.LayoutParams(0,dp(48),1));toolbar.addView(spaceX(9));Spinner catSpin=new Spinner(this);initSpinner(catSpin,Arrays.asList(cat),categoryFilter,i->{categoryFilter=i;filter();});toolbar.addView(catSpin,new LinearLayout.LayoutParams(0,dp(48),1));body.addView(toolbar);body.addView(space(10));
    LinearLayout buttons=row();TextView sync=click("↻  Sync Catalog",()->sync());buttons.addView(sync);buttons.addView(spaceX(9));buttons.addView(click("Website  ↗",()->open("https://www.dhammadownload.com/AudioInMyanmar.htm")));body.addView(buttons);body.addView(space(9));
    information=text("Offline catalog "+catalog.size()+" • Playback မစစ်ရသေးပါ",11,MUTED,false);body.addView(information);body.addView(space(11));
    listArea=vert();body.addView(listArea);searchBox.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int n,int a){}public void afterTextChanged(Editable e){}public void onTextChanged(CharSequence s,int st,int b,int c){query=s.toString();filter();}});filter();
  }
  interface Select {void chose(int index);}
  void initSpinner(Spinner s,List<String> values,int current,Select listener){ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values){@Override public View getView(int p,View old,ViewGroup parent){TextView t=(TextView)super.getView(p,old,parent);t.setTextSize(12);t.setTextColor(GOLD);t.setPadding(dp(8),dp(8),dp(4),dp(8));return t;}};adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);s.setAdapter(adapter);s.setBackground(shape(CARD,12,GOLDDIM));s.setSelection(Math.max(0,Math.min(current,values.size()-1)));s.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){listener.chose(pos);}public void onNothingSelected(android.widget.AdapterView<?> p){}});}
  String group(String s){String l=s.toLowerCase(Locale.ROOT);if(s.contains("ပဋ္ဌာန်း")||l.contains("pahtan")||l.contains("patan")||l.contains("patthana"))return cat[1];if(s.contains("ဝိပဿနာ")||s.contains("ပဋိစ္စ")||l.contains("vipassana"))return cat[2];if(s.contains("ပါဠိ")||s.contains("ပရိတ်")||s.contains("သုတ်")||l.contains("paritta"))return cat[3];return cat[4];}
  void filter(){if(listArea==null||!screen.equals("Radio"))return;results.clear();String q=query.toLowerCase(Locale.ROOT).trim();synchronized(catalog){for(Track t:catalog.values()){if(monkFilter>0&&t.monkId!=monkFilter)continue;if(categoryFilter>0&&!cat[categoryFilter].equals(group(t.title)))continue;if(!q.isEmpty()&&!(t.title+" "+t.monk).toLowerCase(Locale.ROOT).contains(q))continue;results.add(t);}}
    listArea.removeAllViews();listArea.addView(text("တရားတော် "+results.size()+" ပုဒ်",17,GOLD,true));listArea.addView(space(8));if(results.isEmpty()){listArea.addView(text("တရားတော်မတွေ့ပါ — အခြား Keyword ဖြင့်ရှာပါ / Sync လုပ်ပါ။",13,MUTED,false));return;}
    ListView list=new ListView(this);list.setDividerHeight(dp(3));list.setBackgroundColor(BG);list.setCacheColorHint(BG);list.setVerticalScrollBarEnabled(false);list.setAdapter(new BaseAdapter(){public int getCount(){return results.size();}public Object getItem(int i){return results.get(i);}public long getItemId(int i){return i;}public View getView(int i,View reuse,ViewGroup parent){return trackRow(results.get(i));}});list.setOnItemClickListener((p,v,index,id)->trackActions(results.get(index)));listArea.addView(list,lp(-1,530));listArea.addView(space(10));}
  LinearLayout trackRow(Track t){
  LinearLayout r=row();r.setPadding(dp(10),dp(11),dp(10),dp(11));r.setBackground(shape(0xff133b36,15,0));
  ImageView photo=monkPic(t.monkId,54);r.addView(photo,lp(54,54));
  LinearLayout labels=vert();labels.setPadding(dp(12),0,dp(6),0);
  TextView name=text(t.title,14,IVORY,true);name.setMaxLines(2);labels.addView(name);
  TextView who=text(t.monk,11,MUTED,false);who.setMaxLines(1);labels.addView(who);
  TextView status=text(downloadFile(t).exists()?"● OFFLINE":"♫  MP3  ·  ONLINE",10,GOLD,false);labels.addView(status);
  r.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
  TextView play=text("▶",16,DEEP,true);play.setGravity(Gravity.CENTER);play.setBackground(shape(GOLD,26,0));
  r.addView(play,lp(40,40));return r;
}
  void quickTrackList(int max,boolean onlyFav){int i=0;ArrayList<Track> data; synchronized(catalog){data=new ArrayList<>(catalog.values());}for(Track t:data){if(onlyFav&&!favorites.contains(t.url))continue;if(i++>=max)break;LinearLayout r=trackRow(t);body.addView(r,lp(-1,77));body.addView(space(7));r.setOnClickListener(v->trackActions(t));}if(i==0)body.addView(text("စာရင်းမရှိသေးပါ",12,MUTED,false));}
  void trackActions(Track t){String fav=favorites.contains(t.url)?"♥ Favorites မှဖယ်":"♡ Favorites ထည့်";String downloaded=downloadFile(t).exists()?"Offline နားထောင်":"↓ Offline Download";
    new AlertDialog.Builder(this).setTitle(t.title).setMessage(t.monk+"\n"+group(t.title)).setItems(new String[]{"▶  နားထောင်ရန်",fav,downloaded,"🔗 မူရင်း MP3 Link","↗ မူရင်းစာမျက်နှာ"},(d,i)->{switch(i){case 0:play(t);break;case 1:toggleFavorite(t);break;case 2:download(t);break;case 3:open(t.url);break;case 4:open(t.source==null||t.source.isEmpty()?"https://www.dhammadownload.com/":t.source);break;}}).show();}
  void toggleFavorite(Track t){if(!favorites.add(t.url))favorites.remove(t.url);settings.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();toast(favorites.contains(t.url)?"Favorites ထဲထည့်ပြီးပါပြီ":"Favorites မှဖယ်ရှားပြီးပါပြီ");if(screen.equals("Collection"))show("Collection");}
  void rememberRecent(Track t){recent.remove(t.url);recent.add(0,t.url);while(recent.size()>50)recent.remove(recent.size()-1);settings.edit().putString("recent",new JSONArray(recent).toString()).apply();}
  void play(Track t){selected=t;ready=false;release();rememberRecent(t);File f=downloadFile(t);try{player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).setUsage(AudioAttributes.USAGE_MEDIA).build());if(f.exists())player.setDataSource(f.getAbsolutePath());else player.setDataSource(this,Uri.parse(t.url));player.setOnPreparedListener(p->{ready=true;try{p.start();if(speed!=1f)p.setPlaybackParams(p.getPlaybackParams().setSpeed(speed));}catch(Exception ignored){}show("Playing");});player.setOnCompletionListener(p->{if(repeat){p.seekTo(0);p.start();}else show("Playing");});player.setOnErrorListener((p,code,extra)->{ready=false;handler.post(()->new AlertDialog.Builder(this).setTitle("Audio ဖွင့်မရပါ").setMessage("Link ကို မူရင်း Website တွင် ဖွင့်စမ်းပါ။ (code "+code+")").setPositiveButton("Website",(d,w)->open(t.url)).setNegativeButton("OK",null).show());return true;});player.prepareAsync();show("Playing");}catch(Exception ex){toast("MP3 ဖွင့်မရပါ");show("Radio");}}
  void pause(){if(player==null||!ready)return;try{if(player.isPlaying())player.pause();else player.start();show(screen);}catch(Exception ignored){}}
  void release(){try{if(player!=null){player.reset();player.release();}}catch(Exception ignored){}player=null;ready=false;}
  void miniPlayer(){miniBar=row();miniBar.setPadding(dp(14),dp(5),dp(12),dp(5));miniBar.setBackground(shape(CARD,13,GOLDDIM));TextView label=text("♫  "+selected.title,12,IVORY,true);label.setMaxLines(1);miniBar.addView(label,new LinearLayout.LayoutParams(0,-1,1));miniBar.addView(click("❯",()->show("Playing")));miniBar.addView(spaceX(6));miniBar.addView(click(player!=null&&ready&&player.isPlaying()?"Ⅱ":"▶",()->pause()));outer.addView(miniBar,lp(-1,58));miniBar.setOnClickListener(v->show("Playing"));}
  void nowPlaying(){heading("✦  NOW PLAYING  ·  တရားတော်နားဆင်ရန်");if(selected==null){body.addView(text("နားထောင်ရန် တရားတော် ရွေးချယ်ပါ",15,MUTED,false));return;}
    ImageView artwork=picture(R.drawable.radio_pattana,255);artwork.setBackground(shape(CARD,24,GOLD));body.addView(artwork,lp(-1,255));body.addView(space(18));TextView title=text(selected.title,20,IVORY,true);title.setGravity(Gravity.CENTER);body.addView(title);TextView monk=text(selected.monk,13,GOLD,false);monk.setGravity(Gravity.CENTER);body.addView(monk);body.addView(space(24));
    playSeek=new SeekBar(this);playSeek.setMax(1000);playSeek.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));playSeek.setThumbTintList(android.content.res.ColorStateList.valueOf(GOLD));body.addView(playSeek);playSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){if(player!=null&&ready)try{player.seekTo((int)(player.getDuration()*(s.getProgress()/1000f)));}catch(Exception ignored){}}public void onProgressChanged(SeekBar s,int p,boolean user){}});
    timeLabel=text("00:00 / --:--",12,MUTED,false);timeLabel.setGravity(Gravity.CENTER);body.addView(timeLabel);handler.removeCallbacksAndMessages(null);ticker=()->{if(screen.equals("Playing")&&player!=null&&ready){try{int duration=player.getDuration(),current=player.getCurrentPosition();playSeek.setProgress(duration>0?Math.round(current*1000f/duration):0);timeLabel.setText(durationTime(current)+" / "+durationTime(duration));}catch(Exception ignored){}handler.postDelayed(ticker,1000);}};handler.postDelayed(ticker,400);
    body.addView(space(21));LinearLayout actions=row();actions.setGravity(Gravity.CENTER);TextView previous=click("❮❮",()->skip(-1));TextView play=click(player!=null&&ready&&player.isPlaying()?"Ⅱ  PAUSE":"▶  PLAY",()->pause());TextView next=click("❯❯",()->skip(1));actions.addView(previous);actions.addView(spaceX(12));actions.addView(play);actions.addView(spaceX(12));actions.addView(next);body.addView(actions);body.addView(space(15));
    LinearLayout actions2=row();actions2.addView(click(favorites.contains(selected.url)?"♥":"♡",()->toggleFavorite(selected)));actions2.addView(spaceX(7));actions2.addView(click(speed+"x",()->{speed=speed==1f?1.25f:speed==1.25f?1.5f:speed==1.5f?2f:1f;if(player!=null&&ready)try{player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));}catch(Exception ignored){}show("Playing");}));actions2.addView(spaceX(7));actions2.addView(click(repeat?"↻ On":"↻ Off",()->{repeat=!repeat;show("Playing");}));actions2.addView(spaceX(7));actions2.addView(click("↓ Offline",()->download(selected)));body.addView(actions2);
    body.addView(space(21));addCollectionCard("စာရင်း ပြန်ကြည့်ရန်",R.drawable.radio_meditation,2);footnote();
  }
  String durationTime(int ms){int seconds=Math.max(0,ms/1000);return String.format(Locale.US,"%02d:%02d",seconds/60,seconds%60);}
  void skip(int delta){if(selected==null)return;ArrayList<Track> choices=new ArrayList<>();synchronized(catalog){choices.addAll(catalog.values());}int at=-1;for(int i=0;i<choices.size();i++)if(choices.get(i).url.equals(selected.url)){at=i;break;}if(at>=0&&!choices.isEmpty())play(choices.get((at+delta+choices.size())%choices.size()));}
  void collection(){heading("သိမ်းထားသော တရားတော်များ");LinearLayout tabs=row();for(String mode:new String[]{"Favorites","Downloads","Recent"}){TextView b=click(mode,()->{libraryMode=mode;show("Collection");});tabs.addView(b,new LinearLayout.LayoutParams(0,-2,1));}body.addView(tabs);body.addView(space(15));
    int found=0;for(Track t:new ArrayList<>(catalog.values())){boolean yes=libraryMode.equals("Favorites")?favorites.contains(t.url):libraryMode.equals("Downloads")?downloadFile(t).exists():recent.contains(t.url);if(!yes)continue;LinearLayout r=trackRow(t);body.addView(r,lp(-1,72));body.addView(space(8));r.setOnClickListener(v->trackActions(t));found++;}if(found==0)body.addView(text("ဤကဏ္ဍတွင် မရှိသေးပါ။ တရားတော်ကို နားထောင်ပြီး Favorites/Download ပြုလုပ်နိုင်ပါတယ်။",13,MUTED,false));footnote();
  }
  File downloadFile(Track t){String key=Integer.toHexString(t.url.hashCode());File dir=new File(getExternalFilesDir(null),"DhammaMP3");if(!dir.exists())dir.mkdirs();return new File(dir,key+".mp3");}
  void download(Track t){File target=downloadFile(t);if(target.exists()){play(t);return;}
    new AlertDialog.Builder(this).setTitle("Offline MP3 ရယူရန်").setMessage("တရားတော်အသံဖိုင်ကို မူရင်း Website မှ ကိုယ်ပိုင်ဖုန်းထဲ Download လုပ်ပြီး Offline နားထောင်ရန် ဖြစ်ပါသည်။ Download ပမာဏကြီးနိုင်ပါတယ်။\n\n"+t.title).setPositiveButton("Download",(d,w)->{
    toast("Download စတင်နေပါပြီ…");background.execute(()->{File tmp=new File(target.getPath()+".part");try{URL url=new URL(t.url);HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(18000);c.setReadTimeout(45000);c.setInstanceFollowRedirects(true);if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());long length=c.getContentLengthLong();if(length>180L*1024L*1024L)throw new IOException("Audio file too large");try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(tmp)){byte[] bytes=new byte[32768];int n;long total=0;while((n=in.read(bytes))!=-1){total+=n;if(total>180L*1024L*1024L)throw new IOException("Audio limit exceeded");out.write(bytes,0,n);}if(total<10000)throw new IOException("File is unexpectedly small");}if(!tmp.renameTo(target))throw new IOException("File move failed");handler.post(()->{toast("MP3 Download ပြီးပါပြီ · Offline ဖွင့်နိုင်ပါပြီ");if(screen.equals("Collection"))show("Collection");});}catch(Exception e){tmp.delete();handler.post(()->toast("Download မအောင်မြင်: "+e.getMessage()));}});
    }).setNegativeButton("မလုပ်တော့ပါ",null).show();}
  void other(String title,String b,String url){heading(title);ImageView img=picture(R.drawable.radio_hero,220);body.addView(img,lp(-1,220));body.addView(space(14));body.addView(text(b,18,IVORY,true));body.addView(space(14));body.addView(click("မူရင်းဓမ္မစာမျက်နှာသို့ သွားရန် ↗",()->open(url)));body.addView(space(16));body.addView(text("DAMA Radio V2 အဓိက MP3 Library ကို ပြန်သွားနိုင်ပါတယ်။",12,MUTED,false));body.addView(click("♫  DAMA RADIO",()->show("Radio")));}
  void bottomBar(){
  LinearLayout nav=row();nav.setPadding(dp(4),dp(7),dp(4),dp(7));
  nav.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xff081d1d,0xff12312e,0xff081d1d}));
  String[] screens={"Home","Radio","Library","TV","Worship"};
  String[] icons={"⌂","♫","▤","▣","✿"};
  String[] labels={"Home","Radio","Books","TV","Worship"};
  for(int i=0;i<screens.length;i++){
    final String target=screens[i];boolean active=screen.equals(target)||(screen.equals("Playing")&&i==1);
    LinearLayout cell=vert();cell.setGravity(Gravity.CENTER);
    if(active)cell.setBackground(shape(0xff285042,17,GOLDDIM));
    TextView icon=text(icons[i],23,active?GOLD:MUTED,true);icon.setGravity(Gravity.CENTER);cell.addView(icon);
    TextView caption=text(labels[i],10,active?GOLD:MUTED,active);caption.setGravity(Gravity.CENTER);cell.addView(caption);
    LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(0,dp(57),1);pp.setMargins(dp(2),0,dp(2),0);
    nav.addView(cell,pp);cell.setOnClickListener(v->show(target));
  }outer.addView(nav,lp(-1,74));
}
  void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception ex){toast("Website မဖွင့်နိုင်ပါ");}}
  void saveCache(){File out=new File(getFilesDir(),CACHE_FILE),tmp=new File(getFilesDir(),CACHE_FILE+".tmp");try(FileOutputStream f=new FileOutputStream(tmp)){JSONArray a=new JSONArray();synchronized(catalog){for(Track t:catalog.values())a.put(t.json());}f.write(a.toString().getBytes(StandardCharsets.UTF_8));f.getFD().sync();if(!tmp.renameTo(out))throw new IOException("rename failed");}catch(Exception e){android.util.Log.e("DAMA","Unable to cache catalog",e);}}
  String fetch(String ref)throws Exception{URL url=new URL(ref);String host=url.getHost().toLowerCase(Locale.ROOT);if(!host.equals("dhammadownload.com")&&!host.equals("www.dhammadownload.com"))throw new IOException("host not allowed");HttpURLConnection conn=(HttpURLConnection)url.openConnection();conn.setConnectTimeout(16000);conn.setReadTimeout(26000);conn.setRequestProperty("User-Agent","DAMA-RADIO/2.0 Audio link index (no audio downloaded)");try{if(conn.getResponseCode()!=200)throw new IOException("HTTP "+conn.getResponseCode());ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream in=conn.getInputStream()){byte[] array=new byte[8192];int n;while((n=in.read(array))!=-1){if(b.size()+n>5_000_000)throw new IOException("HTML too large");b.write(array,0,n);}}String enc=conn.getContentEncoding();String contentType=conn.getContentType();if(contentType!=null&&contentType.toLowerCase(Locale.ROOT).contains("charset=")){String ch=contentType.replaceAll("(?i).*charset=\\s*([A-Za-z0-9_-]+).*","$1");try{return b.toString(ch);}catch(Exception ignored){}}return b.toString("UTF-8");}finally{conn.disconnect();}}
  void sync(){if(syncing){toast("Sync လုပ်နေဆဲပါ");return;}syncing=true;toast("ဆရာတော် MP3 စာရင်း Sync စတင်နေပါပြီ");background.execute(()->{int ok=0,errors=0,count=0;for(Monk m:monks){try{String html=fetch(m.source);List<Track> added=CatalogParser.parse(html,m.source,m.id,m.name);int previous=catalog.size();for(Track t:added){t.category=group(t.title);addCatalog(t);}count+=catalog.size()-previous;ok++;}catch(Exception e){errors++;}final int progress=ok+errors;handler.post(()->{if(information!=null&&screen.equals("Radio"))information.setText("Sync "+progress+"/"+monks.size()+" · "+catalog.size()+" MP3 Links");});try{Thread.sleep(800);}catch(Exception ignored){}}
    saveCache();syncing=false;final int add=count,fail=errors;handler.post(()->{toast("Sync ပြီးပါပြီ · MP3 "+catalog.size()+" · အသစ် "+add+" · Page Errors "+fail);show("Radio");});});}
  @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);release();background.shutdownNow();super.onDestroy();}
}