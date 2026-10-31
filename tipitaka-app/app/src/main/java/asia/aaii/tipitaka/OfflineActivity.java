package asia.aaii.tipitaka;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineActivity extends Activity {
  private static final String HOST="tipitaka.cloud";
  private static final int BLUE=0xff34285e;
  private static final String[] VOLUME_NAMES={
    "มหาวิภังค์ ภาค ๑","มหาวิภังค์ ภาค ๒","ภิกขุนีวิภังค์","มหาวรรค ภาค ๑",
    "มหาวรรค ภาค ๒","จุลวรรค ภาค ๑","จุลวรรค ภาค ๒","ปริวาร",
    "สีลขันธวรรค","มหาวรรค","ปาฏิกวรรค",
    "มูลปัณณาสก์","มัชฌิมปัณณาสก์","อุปริปัณณาสก์",
    "สคาถวรรค","นิทานวรรค","ขันธวารวรรค","สฬายตนวรรค","มหาวารวรรค",
    "เอก-ทุก-ติกนิบาต","จตุกกนิบาต","ปัญจก-ฉักกนิบาต","สัตตก-อัฏฐก-นวกนิบาต","ทสก-เอกาทสกนิบาต",
    "ขุททกปาฐะ ธรรมบท อุทาน อิติวุตตกะ สุตตนิบาต",
    "วิมานวัตถุ เปตวัตถุ เถรคาถา เถรีคาถา","ชาดก ภาค ๑","ชาดก ภาค ๒",
    "มหานิทเทส","จูฬนิทเทส","ปฏิสัมภิทามรรค","อปทาน ภาค ๑",
    "อปทาน ภาค ๒ พุทธวงศ์ จริยาปิฎก",
    "ธัมมสังคณี","วิภังค์","ธาตุกถา ปุคคลบัญญัติ","กถาวัตถุ",
    "ยมก ภาค ๑","ยมก ภาค ๒","ปัฏฐาน ภาค ๑","ปัฏฐาน ภาค ๒",
    "ปัฏฐาน ภาค ๓","ปัฏฐาน ภาค ๔","ปัฏฐาน ภาค ๕","ปัฏฐาน ภาค ๖"
  };
  private static class Store extends SQLiteOpenHelper {
    Store(Activity a){super(a,"tipitaka_offline_pages.db",null,1);}
    @Override public void onCreate(SQLiteDatabase db){
      db.execSQL("CREATE TABLE saved(url TEXT PRIMARY KEY,title TEXT NOT NULL,body TEXT NOT NULL,saved_at INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int old,int newer){}
  }
  private Store store;
  private LinearLayout main;
  private EditText urlField,searchField;
  private boolean catalog=false, busy=false;
  private String opened=null, query="";
  private int font=19;

  @Override public void onCreate(Bundle state){
    super.onCreate(state); store=new Store(this);
    font=getPreferences(0).getInt("offline_font",19);
    show();
  }
  @Override public void onDestroy(){store.close();super.onDestroy();}
  @Override public void onBackPressed(){if(opened!=null){opened=null;show();}else super.onBackPressed();}
  private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(12));return d;}
  private LinearLayout vbox(){LinearLayout b=new LinearLayout(this);b.setOrientation(1);return b;}
  private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setLineSpacing(dp(3),1.1f);t.setPadding(dp(5),dp(5),dp(5),dp(5));return t;}
  private void label(LinearLayout p,String s,int size){p.addView(text(s,size,0xff30273d));}
  private LinearLayout card(){
    LinearLayout c=vbox();c.setBackground(bg(Color.WHITE));c.setPadding(dp(12),dp(12),dp(12),dp(12));
    LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
    params.setMargins(dp(10),dp(6),dp(10),dp(6));main.addView(c,params);return c;
  }
  private void action(LinearLayout p,String text,Runnable r){
    TextView b=text(text,15,Color.WHITE);b.setGravity(Gravity.CENTER);b.setBackground(bg(BLUE));
    b.setMinHeight(dp(46));b.setOnClickListener(x->r.run());
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,dp(5));p.addView(b,lp);
  }
  private void toast(String s){android.widget.Toast.makeText(this,s,android.widget.Toast.LENGTH_LONG).show();}
  private void show(){
    getWindow().setStatusBarColor(BLUE);getWindow().setNavigationBarColor(BLUE);
    LinearLayout root=vbox();root.setBackgroundColor(0xfff7f4ef);setContentView(root);
    TextView heading=text("☸  Link-wise Offline Reader",22,Color.WHITE);
    heading.setPadding(dp(18),dp(18),dp(12),dp(18));heading.setBackgroundColor(BLUE);root.addView(heading);
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    main=vbox();main.setPadding(0,dp(12),0,dp(12));scroll.addView(main);
    if(opened!=null){showReader(opened);return;}
    LinearLayout info=card();
    label(info,"Tipitaka Cloud မှ စာမျက်နှာတစ်ခုချင်း သိမ်းဆည်းပြီး ဖုန်းအတွင်း Offline ဖတ်နိုင်ပါသည်။",16);
    label(info,"အရေးကြီး — ကျမ်းတွဲ ၄၅ တွဲရဲ့ မာတိကာ/ခေါင်းစဉ်သာ APK ထဲပါပြီး ကျမ်းစာသားအားလုံး မပါသေးပါ။ ဤစနစ်သည် လင့်ခ်ဖတ်နိုင်သော HTML စာမျက်နှာများအတွက်သာ ဖြစ်ပါသည်။",13);
    label(info,"Source: https://tipitaka.cloud/sources · အသုံးပြုခွင့်ရှိသော စာမျက်နှာများကိုသာ ကိုယ်ပိုင် Offline Cache အဖြစ် သိမ်းပါ။",12);
    LinearLayout form=card();
    label(form,"လင့်ခ်ထည့်ပြီး Offline သိမ်းရန်",18);
    urlField=new EditText(this);urlField.setSingleLine(true);
    urlField.setText("https://tipitaka.cloud/?vol=1&item=1&panes=mcu");
    urlField.setTextSize(13);urlField.setHint("https://tipitaka.cloud/?vol=1&item=1&panes=mcu");
    form.addView(urlField);
    action(form,"⬇ လင့်ခ်စာမျက်နှာကို Offline သိမ်းမည်",()->download(urlField.getText().toString().trim()));
    action(form,"ကျမ်းတွဲ ၄၅ တွဲ၏ အစစာမျက်နှာလင့်ခ်များ",()->{catalog=!catalog;show();});
    if(catalog) catalog();
    LinearLayout list=card();
    label(list,"Offline သိမ်းထားပြီးသော စာမျက်နှာများ",19);
    EditText search=new EditText(this);search.setSingleLine(true);search.setHint("Offline စာသားရှာဖွေရန်");
    search.setText(query);list.addView(search);
    action(list,"စာသားရှာရန်",()->{query=search.getText().toString().trim();show();});
    int n=0;
    String sql=query.isEmpty()?"SELECT url,title,length(body),saved_at FROM saved ORDER BY saved_at DESC":
       "SELECT url,title,length(body),saved_at FROM saved WHERE title LIKE ? OR body LIKE ? ORDER BY saved_at DESC";
    String[] args=query.isEmpty()?null:new String[]{"%"+query+"%","%"+query+"%"};
    try(Cursor cur=store.getReadableDatabase().rawQuery(sql,args)){
      while(cur.moveToNext()){
        n++;String url=cur.getString(0),title=cur.getString(1);int chars=cur.getInt(2);
        LinearLayout c=card();label(c,title,16);label(c,chars+" characters · သိမ်းထားသော Offline Data",12);
        label(c,url,11);
        action(c,"📖 Offline ဖတ်မည်",()->{opened=url;show();});
        action(c,"ဖျက်ရန်",()->confirmDelete(url));
      }
    }
    if(n==0)label(list,"Offline သိမ်းထားသော စာမျက်နှာ မရှိသေးပါ။",14);
  }
  private void catalog(){
    LinearLayout group=card();
    label(group,"Tipitaka Cloud — ၄၅ တွဲ (Entry 1 လင့်ခ်များ)",18);
    label(group,"Website URL Pattern ကို အသုံးပြုထားပြီး တချို့ Volume များ၏ Link/Content ကို စမ်းသပ်အတည်ပြုရန် လိုနိုင်ပါသည်။ ဤ Link များသည် ကျမ်းတစ်တွဲလုံး မဟုတ်ပါ။",13);
    for(int i=1;i<=45;i++){
      final String u=entryUrl(i);
      LinearLayout c=card();label(c,"တွဲ "+i+" · "+VOLUME_NAMES[i-1],14);
      action(c,"ပထမ Entry ကို Offline သိမ်းမည်",()->download(u));
    }
  }
  private static String entryUrl(int volume){
    return "https://tipitaka.cloud/?vol="+volume+"&item=1&panes=mcu";
  }
  private void showReader(String url){
    String title=null,body=null;
    try(Cursor c=store.getReadableDatabase().rawQuery("SELECT title,body FROM saved WHERE url=?",new String[]{url})){
      if(c.moveToFirst()){title=c.getString(0);body=c.getString(1);}
    }
    if(body==null){opened=null;show();return;}
    LinearLayout c=card();label(c,title,21);label(c,"Offline · "+url,12);
    final String content=body;
    label(c,"စာသားအရွယ်အစား: "+font,13);
    action(c,"A+ စာလုံးကြီး",()->{font=Math.min(34,font+2);getPreferences(0).edit().putInt("offline_font",font).apply();show();});
    action(c,"A− စာလုံးသေး",()->{font=Math.max(14,font-2);getPreferences(0).edit().putInt("offline_font",font).apply();show();});
    TextView reader=text(content,font,0xff26202f);reader.setTextIsSelectable(true);
    c.addView(reader);
    action(c,"← Offline Library သို့ပြန်ရန်",()->{opened=null;show();});
  }
  private void confirmDelete(String url){
    new AlertDialog.Builder(this).setTitle("Offline စာမျက်နှာ ဖျက်မည်လား")
      .setPositiveButton("ဖျက်မည်",(a,b)->{
        store.getWritableDatabase().delete("saved","url=?",new String[]{url});show();
      }).setNegativeButton("မဖျက်ပါ",null).show();
  }
  private void download(String text){
    if(busy){toast("လင့်ခ်တစ်ခု သိမ်းနေပါသည်။");return;}
    final URL parsed;
    try{
      parsed=new URL(text);
      if(!parsed.getProtocol().equalsIgnoreCase("https") || 
       !(HOST.equalsIgnoreCase(parsed.getHost()) || ("www."+HOST).equalsIgnoreCase(parsed.getHost()))
       || parsed.getPort()!=-1 || text.length()>2048)throw new Exception();
    }catch(Exception invalid){toast("tipitaka.cloud HTTPS Link ကိုသာ ထည့်ပါ။");return;}
    busy=true;toast("ဒေတာ စစ်ဆေးနေပါသည်…");
    new Thread(()->{
      try{
        HttpURLConnection conn=(HttpURLConnection)parsed.openConnection();
        try{
          conn.setInstanceFollowRedirects(false);
          conn.setConnectTimeout(12000);conn.setReadTimeout(15000);
          conn.setRequestProperty("User-Agent","MyanmarTipitakaNative/1.1 (personal offline reader)");
          conn.setRequestProperty("Accept","text/html");
          conn.setRequestProperty("Accept-Encoding","identity");
          int status=conn.getResponseCode();
          if(status!=200)throw new Exception("HTTP "+status);
          String mime=conn.getContentType();
          if(mime==null || !mime.toLowerCase(Locale.ROOT).contains("text/html"))throw new Exception("HTML စာမျက်နှာ မဟုတ်ပါ။");
          ByteArrayOutputStream out=new ByteArrayOutputStream();
          try(InputStream in=conn.getInputStream()){
            byte[] bytes=new byte[8192];int k;
            while((k=in.read(bytes))!=-1){
              out.write(bytes,0,k);
              if(out.size()>3*1024*1024)throw new Exception("စာမျက်နှာ 3 MB ထက်ကြီးသည်။");
            }
          }
          String html=out.toString("UTF-8");
          String heading=extract(html,"title");
          if(heading.isEmpty())heading=parsed.toString();
          String mainHtml=extract(html,"main");
          if(mainHtml.length()<200)mainHtml=extract(html,"article");
          if(mainHtml.length()<200)mainHtml=extract(html,"body");
          if(mainHtml.isEmpty())throw new Exception("စာသားမတွေ့ပါ။");
          mainHtml=mainHtml.replaceAll("(?is)<(script|style|nav|footer|header|aside|noscript)\\b[^>]*>.*?</\\1>"," ");
          String reading=Html.fromHtml(mainHtml,Html.FROM_HTML_MODE_LEGACY).toString()
            .replaceAll("[\\t ]+"," ").replaceAll("\\n{4,}","\n\n").trim();
          if(reading.length()<150)throw new Exception("HTML ထဲတွင် ဖတ်ရန်စာသား မလုံလောက်ပါ။");
          ContentValues values=new ContentValues();
          values.put("url",parsed.toString());values.put("title",Html.fromHtml(heading,Html.FROM_HTML_MODE_LEGACY).toString().trim());
          values.put("body",reading);values.put("saved_at",System.currentTimeMillis());
          store.getWritableDatabase().insertWithOnConflict("saved",null,values,SQLiteDatabase.CONFLICT_REPLACE);
          final int chars=reading.length();
          runOnUiThread(()->{busy=false;toast("Offline သိမ်းပြီးပါပြီ ("+chars+" စာလုံး)");opened=parsed.toString();show();});
        }finally{conn.disconnect();}
      }catch(Exception e){
        String msg=e.getMessage()==null?"Network/HTML Error":e.getMessage();
        runOnUiThread(()->{busy=false;new AlertDialog.Builder(this).setTitle("Offline Download မအောင်မြင်ပါ")
          .setMessage(msg+"\n\nSite သည် dynamic JavaScript အခြေခံဖြစ်ပါက HTML Cache ဖြင့်မရနိုင်ပါ။ မူရင်းအချက်အလက်ကို JSON အဖြစ် Import လုပ်နိုင်ပါသည်။")
          .setPositiveButton("ပိတ်",null).show();});
      }
    }).start();
  }
  private static String extract(String html,String tag){
    Pattern p=Pattern.compile("(?is)<"+tag+"\\b[^>]*>(.*?)</"+tag+"\\s*>");
    Matcher match=p.matcher(html);return match.find()?match.group(1):"";
  }
}