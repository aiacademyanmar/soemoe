package asia.aaii.tipitaka;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
  private static final int IMPORT_FILE = 19;
  private final int royal=Color.rgb(48,35,76), gold=Color.rgb(187,126,46);
  private DB helper;
  private LinearLayout root, body;
  private String tab="home", filter="", keyword="";
  private int opened=0, page=1, size=19;
  private boolean night=false;
  private TextToSpeech speaker;
  private boolean speechReady=false;

  private static class DB extends SQLiteOpenHelper {
    DB(Activity activity) { super(activity,"tipitaka_reader.db",null,1); }
    @Override public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE books(no INTEGER PRIMARY KEY,title TEXT,pitaka TEXT,source TEXT,pages INTEGER)");
      db.execSQL("CREATE TABLE pages(no INTEGER,page INTEGER,body TEXT,PRIMARY KEY(no,page))");
      db.execSQL("CREATE TABLE notes(no INTEGER,page INTEGER,note TEXT,PRIMARY KEY(no,page))");
      for (int i=1;i<=45;i++) {
        String category=i<=8?"ဝိနည်းပိဋကတ်":(i<=33?"သုတ္တပိဋကတ်":"အဘိဓမ္မပိဋကတ်");
        db.execSQL("INSERT INTO books VALUES(?,?,?,?,0)",new Object[]{i,"ကျမ်းတွဲ "+i,category,""});
      }
    }
    @Override public void onUpgrade(SQLiteDatabase db,int a,int b) { }
  }

  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    helper=new DB(this);
    night=getPreferences(0).getBoolean("night",false);
    size=getPreferences(0).getInt("size",19);
    if (b!=null) {tab=b.getString("tab","home");opened=b.getInt("opened");page=b.getInt("page",1);}
    try {speaker=new TextToSpeech(this,s->{
      if(s==TextToSpeech.SUCCESS && speaker!=null) {
        int result=speaker.setLanguage(Locale.forLanguageTag("my-MM"));
        speechReady=result!=TextToSpeech.LANG_NOT_SUPPORTED && result!=TextToSpeech.LANG_MISSING_DATA;
      }
    });} catch (Exception ignore) {}
    render();
  }
  @Override public void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putString("tab",tab);b.putInt("opened",opened);b.putInt("page",page);}
  @Override public void onBackPressed(){if(opened!=0){opened=0;render();}else if(!tab.equals("home")){tab="home";filter="";render();}else super.onBackPressed();}
  @Override public void onDestroy(){if(speaker!=null){speaker.stop();speaker.shutdown();}helper.close();super.onDestroy();}

  private int dp(int v){return (int)(getResources().getDisplayMetrics().density*v+.5f);}
  private int bg(){return night?0xff181721:0xfffaf8f4;}
  private int surface(){return night?0xff292634:Color.WHITE;}
  private int ink(){return night?0xfff4eadd:0xff282233;}
  private int muted(){return night?0xffb9afbe:0xff6c6972;}
  private GradientDrawable rounded(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(14));return d;}
  private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  private TextView label(String value,int sp,boolean bold,int color){
    TextView t=new TextView(this);t.setText(value);t.setTextSize(sp);t.setTextColor(color);t.setPadding(dp(3),dp(6),dp(3),dp(6));t.setLineSpacing(dp(3),1.12f);
    if(bold)t.setTypeface(null,Typeface.BOLD);return t;
  }
  private LinearLayout card(){
    LinearLayout l=vertical();l.setBackground(rounded(surface()));l.setPadding(dp(14),dp(13),dp(14),dp(13));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(14),dp(6),dp(14),dp(6));body.addView(l,p);return l;
  }
  private void line(LinearLayout l,String s,int sp,boolean bold){l.addView(label(s,sp,bold,ink()));}
  private TextView action(String s,boolean primary,Runnable r){
    TextView a=label(s,14,true,primary?Color.WHITE:ink());a.setGravity(Gravity.CENTER);a.setBackground(rounded(primary?royal:surface()));
    a.setMinHeight(dp(46));a.setPadding(dp(10),dp(8),dp(10),dp(8));a.setOnClickListener(v->r.run());return a;
  }
  private void button(LinearLayout parent,String s,Runnable r){
    TextView a=action(s,true,r);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(46));p.setMargins(0,dp(8),0,0);parent.addView(a,p);
  }
  private void row(LinearLayout parent,String left,Runnable a,String right,Runnable b){
    LinearLayout r=new LinearLayout(this);r.setOrientation(0);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(3),dp(5),dp(3),0);
    r.addView(action(left,true,a),p);r.addView(action(right,false,b),new LinearLayout.LayoutParams(0,dp(48),1));
    parent.addView(r);
  }
  private void render(){
    getWindow().setStatusBarColor(royal);getWindow().setNavigationBarColor(royal);
    root=vertical();root.setBackgroundColor(bg());setContentView(root);
    LinearLayout head=vertical();head.setPadding(dp(19),dp(13),dp(18),dp(13));head.setBackgroundColor(royal);root.addView(head);
    head.addView(label("☸  ပိဋကတ်သုံးပုံ",24,true,Color.WHITE));
    head.addView(label("MYANMAR TIPITAKA  ·  NATIVE READER",11,false,0xffe4cb9a));
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=vertical();body.setPadding(0,dp(13),0,dp(12));scroll.addView(body);
    if(opened!=0)reader();else if(tab.equals("home"))home();else if(tab.equals("books"))books();else if(tab.equals("search"))search();else if(tab.equals("marks"))marks();else settings();
    footer();
  }
  private void footer(){
    LinearLayout bar=new LinearLayout(this);bar.setPadding(dp(2),dp(7),dp(2),dp(7));bar.setBackgroundColor(royal);
    String[] titles={"⌂  ပင်မ","▣  ကျမ်း","⌕  ရှာရန်","★  စာမှတ်","⚙  ဆက်တင်"};
    String[] ids={"home","books","search","marks","settings"};
    for(int i=0;i<titles.length;i++){
      final String id=ids[i];TextView t=label(titles[i],12,true,tab.equals(id)?0xffffd88d:Color.WHITE);
      t.setGravity(Gravity.CENTER);t.setOnClickListener(v->{opened=0;tab=id;filter="";render();});
      bar.addView(t,new LinearLayout.LayoutParams(0,dp(50),1));
    }
    root.addView(bar);
  }
  private int scalar(String sql,String...args){try(Cursor c=helper.getReadableDatabase().rawQuery(sql,args)){return c.moveToFirst()?c.getInt(0):0;}}
  private void home(){
    LinearLayout c=card();line(c,"တရားတော်ကို လေ့လာဖတ်ရှုရန်",20,true);
    line(c,"ဝိနည်း · သုတ္တန် · အဘိဓမ္မာ",14,false);
    line(c,"ထည့်သွင်းပြီးသော ကျမ်းတွဲ "+scalar("SELECT COUNT(*) FROM books WHERE pages>0")+" / 45",16,true);
    if(scalar("SELECT COUNT(*) FROM books WHERE pages>0")==0)line(c,"ကျမ်းစာသားအပြည့်အစုံ မထည့်ရသေးပါ။ အသုံးပြုခွင့်ရှိသော JSON ကျမ်းစာများကို ကိုယ်တိုင် ထည့်သွင်းနိုင်ပါသည်။",13,false);
    button(c,"🎨 UI/UX Screens 20 · Native Navigation",()->startActivity(new Intent(this,DesignScreensActivity.class)));
    button(c,"🎧 တရားအသံ · Offline Sync Center",()->startActivity(new Intent(this,AudioLibraryActivity.class)));
    button(c,"📚 မူရင်း ပါဠိကျမ်းစာ — Offline Native Reader",()->startActivity(new Intent(this,CorpusActivity.class)));
    button(c,"☸ Offline မြန်မာသုတေသန ကျမ်းတွဲ ၄၅ တွဲ",()->startActivity(new Intent(this,ResearchActivity.class)));
    button(c,"📖 ဓမ္မလေ့လာရန် — ဓမ္မပဒ / မင်္ဂလသုတ် / နိဗ္ဗာန် / သတိပဋ္ဌာန်",()->startActivity(new Intent(this,LearningActivity.class)));
    button(c,"ကျမ်းစာ JSON ထည့်သွင်းမည်",this::pickJson);
    button(c,"☸ Tipitaka Cloud — Offline Link Library",()->startActivity(new Intent(this,OfflineActivity.class)));
    String[] sections={"ဝိနည်းပိဋကတ်","သုတ္တပိဋကတ်","အဘိဓမ္မပိဋကတ်"};
    String[] captions={"ကျမ်းတွဲ ၈ တွဲ","ကျမ်းတွဲ ၂၅ တွဲ","ကျမ်းတွဲ ၁၂ တွဲ"};
    for(int i=0;i<3;i++){final String f=sections[i];LinearLayout p=card();line(p,"☸  "+f,18,true);line(p,captions[i],14,false);button(p,"ကျမ်းများကြည့်ရန်",()->{filter=f;tab="books";render();});}
  }
  private void books(){
    LinearLayout c=card();line(c,filter.isEmpty()?"ကျမ်းတွဲ ၄၅ တွဲ":filter,20,true);
    if(!filter.isEmpty())button(c,"ကျမ်းအားလုံးပြရန်",()->{filter="";render();});
    String sql="SELECT no,title,pitaka,pages FROM books"+(filter.isEmpty()?"":" WHERE pitaka=?")+" ORDER BY no";
    try(Cursor cur=helper.getReadableDatabase().rawQuery(sql,filter.isEmpty()?null:new String[]{filter})){
      while(cur.moveToNext()){
        int n=cur.getInt(0),count=cur.getInt(3);
        LinearLayout p=card();line(p,"ကျမ်းတွဲ "+n+" — "+cur.getString(1),16,true);
        line(p,cur.getString(2)+" · "+(count==0?"စာသားမရှိသေး":count+" မျက်နှာ"),13,false);
        button(p,"ဖတ်ရှုရန်",()->{opened=n;page=getPreferences(0).getInt("page_"+n,1);render();});
      }
    }
  }
  private String title(int no){try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT title FROM books WHERE no=?",new String[]{""+no})){return c.moveToFirst()?c.getString(0):"ကျမ်းတွဲ "+no;}}
  private int pageCount(int no){return scalar("SELECT pages FROM books WHERE no=?",""+no);}
  private String pageText(int no,int pg){
    try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT body FROM pages WHERE no=? AND page=?",new String[]{""+no,""+pg})){return c.moveToFirst()?c.getString(0):null;}
  }
  private String markKey(){return opened+":"+page;}
  private Set<String> markSet(){return new HashSet<>(getPreferences(0).getStringSet("marks",new HashSet<>()));}
  private void toast(String v){Toast.makeText(this,v,Toast.LENGTH_LONG).show();}
  private void reader(){
    int count=pageCount(opened);String content=pageText(opened,page);
    LinearLayout c=card();line(c,title(opened),20,true);
    line(c,"စာမျက်နှာ "+page+" / "+count,13,false);
    TextView text=label(content==null?"ဤကျမ်းတွဲအတွက် ကျမ်းစာသား မထည့်သွင်းရသေးပါ။ Settings မှ JSON Import ကို အသုံးပြုပါ။":content,size,false,ink());
    text.setTextIsSelectable(true);c.addView(text);
    if(count>0){
      row(c,"◀ ရှေ့",()->{page=Math.max(1,page-1);savePage();render();},"နောက် ▶",()->{page=Math.min(count,page+1);savePage();render();});
      row(c,markSet().contains(markKey())?"★ စာမှတ်ဖြုတ်":"☆ စာမှတ်",this::toggleMark,"✎ မှတ်စု",this::noteDialog);
      button(c,"🔊 မြန်မာအသံဖြင့် ဖတ်ရန်",()->{
        if(speechReady && speaker!=null)speaker.speak(pageText(opened,page),TextToSpeech.QUEUE_FLUSH,null,"tipitaka_reader");
        else toast("မြန်မာ TTS Engine မရရှိသေးပါ။");
      });
    }
    button(c,"← ကျမ်းစာရင်းသို့ ပြန်သွားရန်",()->{opened=0;tab="books";render();});
  }
  private void savePage(){getPreferences(0).edit().putInt("page_"+opened,page).apply();}
  private void toggleMark(){Set<String> marks=markSet();if(marks.contains(markKey()))marks.remove(markKey());else marks.add(markKey());getPreferences(0).edit().putStringSet("marks",marks).apply();render();}
  private void noteDialog(){
    EditText e=new EditText(this);e.setMinLines(3);
    try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT note FROM notes WHERE no=? AND page=?",new String[]{""+opened,""+page})){if(c.moveToFirst())e.setText(c.getString(0));}
    new AlertDialog.Builder(this).setTitle("မှတ်စု").setView(e).setPositiveButton("သိမ်းမည်",(d,w)->{
      helper.getWritableDatabase().execSQL("INSERT OR REPLACE INTO notes VALUES(?,?,?)",new Object[]{opened,page,e.getText().toString()});toast("မှတ်စုသိမ်းပြီးပါပြီ။");
    }).setNegativeButton("ပိတ်",null).show();
  }
  private void marks(){
    Set<String> marks=markSet();LinearLayout c=card();line(c,"သိမ်းထားသော စာမှတ်များ ("+marks.size()+")",20,true);
    if(marks.isEmpty())line(c,"စာမှတ်များ မရှိသေးပါ။",14,false);
    for(String m:marks){String[] parts=m.split(":");if(parts.length!=2)continue;
      try{int n=Integer.parseInt(parts[0]),pg=Integer.parseInt(parts[1]);LinearLayout p=card();
        line(p,title(n)+" — စာမျက်နှာ "+pg,16,true);button(p,"ဆက်ဖတ်မည်",()->{opened=n;page=pg;render();});}catch(Exception ignore){}
    }
  }
  private void search(){
    LinearLayout c=card();line(c,"ကျမ်းစာသားရှာဖွေရန်",20,true);
    EditText e=new EditText(this);e.setSingleLine(true);e.setText(keyword);e.setHint("စကားလုံး ရိုက်ထည့်ပါ");
    e.setTextColor(ink());e.setHintTextColor(muted());c.addView(e);
    button(c,"ရှာဖွေမည်",()->{keyword=e.getText().toString().trim();render();});
    if(keyword.length()<2){line(c,"ဒေတာထည့်သွင်းပြီးသော ကျမ်းစာသားများမှသာ ရှာဖွေနိုင်ပါသည်။",13,false);return;}
    int hits=0;
    try(Cursor cur=helper.getReadableDatabase().rawQuery("SELECT p.no,p.page,b.title,p.body FROM pages p JOIN books b ON p.no=b.no WHERE p.body LIKE ? ORDER BY p.no,p.page LIMIT 80",new String[]{"%"+keyword+"%"})){
      while(cur.moveToNext()){
        int no=cur.getInt(0),pg=cur.getInt(1);String data=cur.getString(3);int ix=data.indexOf(keyword);int st=Math.max(0,ix-25);
        LinearLayout p=card();line(p,cur.getString(2)+" · စာမျက်နှာ "+pg,15,true);
        line(p,data.substring(st,Math.min(data.length(),st+120)),13,false);
        button(p,"ဖွင့်ဖတ်ရန်",()->{opened=no;page=pg;render();});hits++;
      }
    }
    if(hits==0){LinearLayout p=card();line(p,"ရှာဖွေမှုရလဒ် မတွေ့ပါ။",15,false);}
  }
  private void settings(){
    LinearLayout c=card();line(c,"စာဖတ်စနစ် ဆက်တင်",20,true);
    button(c,"ကျမ်းစာ JSON Import",this::pickJson);
    line(c,"မြန်မာ Unicode / Native Android Reader",14,false);
    row(c,"A− စာလုံးသေး",()->{size=Math.max(14,size-2);getPreferences(0).edit().putInt("size",size).apply();render();},
      "A+ စာလုံးကြီး",()->{size=Math.min(34,size+2);getPreferences(0).edit().putInt("size",size).apply();render();});
    button(c,night?"☼ Light Mode":"☾ Dark Mode",()->{night=!night;getPreferences(0).edit().putBoolean("night",night).apply();render();});
    line(c,"ကျမ်းစာသားမူပိုင်ခွင့်နှင့် အရင်းအမြစ်ကို စစ်ဆေးပြီးမှ Import လုပ်ပါ။",13,false);
    line(c,"Version 1.0 · Offline · No WebView",13,false);
  }
  private void pickJson(){
    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);
    try{startActivityForResult(i,IMPORT_FILE);}catch(Exception ex){toast("JSON ဖိုင်ကို မရွေးနိုင်ပါ။");}
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(request!=IMPORT_FILE||result!=RESULT_OK||data==null||data.getData()==null)return;
    try(InputStream input=getContentResolver().openInputStream(data.getData())){
      ByteArrayOutputStream buf=new ByteArrayOutputStream();byte[] chunk=new byte[8192];int read;
      while((read=input.read(chunk))!=-1){buf.write(chunk,0,read);if(buf.size()>10_000_000)throw new IllegalArgumentException("ဖိုင်အရွယ်အစား 10MB ထက်ကြီးနေပါသည်။");}
      JSONObject json=new JSONObject(buf.toString("UTF-8"));
      JSONArray vols=json.has("volumes")?json.getJSONArray("volumes"):new JSONArray().put(json.has("volume")?json.getJSONObject("volume"):json);
      if(vols.length()<1||vols.length()>45)throw new IllegalArgumentException("ကျမ်းတွဲ ၁ မှ ၄၅ အတွင်းသာ ထည့်ပါ။");
      SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();
      try{
        Set<Integer> used=new HashSet<>();
        for(int j=0;j<vols.length();j++){
          JSONObject v=vols.getJSONObject(j);int no=v.getInt("number");
          if(no<1||no>45||!used.add(no))throw new IllegalArgumentException("ကျမ်းတွဲနံပါတ် မှားယွင်း/ထပ်နေပါသည်။");
          String name=v.getString("title").trim(),source=v.getString("source").trim();
          JSONArray pages=v.getJSONArray("pages");
          if(name.length()<2||source.length()<3||pages.length()<1||pages.length()>2500)throw new IllegalArgumentException("ခေါင်းစဉ်/ရင်းမြစ်/စာမျက်နှာဒေတာ မမှန်ပါ။");
          db.execSQL("DELETE FROM pages WHERE no=?",new Object[]{no});
          for(int k=0;k<pages.length();k++){
            Object entry=pages.get(k);String body=entry instanceof JSONObject?((JSONObject)entry).getString("text"):pages.getString(k);
            if(body.trim().isEmpty()||body.length()>50000)throw new IllegalArgumentException("စာမျက်နှာစာသား မမှန်ပါ။");
            db.execSQL("INSERT INTO pages VALUES(?,?,?)",new Object[]{no,k+1,body});
          }
          db.execSQL("UPDATE books SET title=?,source=?,pages=? WHERE no=?",new Object[]{name,source,pages.length(),no});
        }
        db.setTransactionSuccessful();
      }finally{db.endTransaction();}
      toast("ကျမ်းစာဒေတာ ထည့်သွင်းပြီးပါပြီ။");opened=0;tab="home";render();
    }catch(Exception err){toast("Import မအောင်မြင်ပါ — "+err.getMessage());}
  }
}
