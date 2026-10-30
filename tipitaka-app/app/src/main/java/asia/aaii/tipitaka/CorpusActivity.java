package asia.aaii.tipitaka;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class CorpusActivity extends Activity {
  private final int royal=0xff30234d, ink=0xff292337, muted=0xff746c7b;
  private SQLiteDatabase db;
  private LinearLayout list;
  private String query="", category="";
  private int selected=-1, offset=0, font=18;
  private static final int PAGE_SIZE=45;
  private TextWatcher watcher;

  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    font=getPreferences(0).getInt("font",18);
    if(state!=null){selected=state.getInt("selected",-1);offset=state.getInt("offset",0);category=state.getString("category","");}
    try{openCorpus();}catch(Exception e){Toast.makeText(this,"Offline ကျမ်းစာ Database ဖွင့်မရပါ: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    show();
  }
  @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putInt("selected",selected);b.putInt("offset",offset);b.putString("category",category);}
  @Override public void onDestroy(){if(db!=null&&db.isOpen())db.close();super.onDestroy();}
  @Override public void onBackPressed(){if(selected>=0){selected=-1;offset=0;show();}else super.onBackPressed();}
  private int d(int x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable rounded(int color){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(13));return g;}
  private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  private TextView txt(String s,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(d(7),d(7),d(7),d(7));t.setLineSpacing(d(4),1.12f);
    if(bold)t.setTypeface(null,Typeface.BOLD);return t;
  }
  private LinearLayout card(){
    LinearLayout c=column();c.setPadding(d(12),d(10),d(12),d(10));c.setBackground(rounded(Color.WHITE));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(d(11),d(6),d(11),d(5));list.addView(c,p);return c;
  }
  private void action(LinearLayout p,String label,Runnable run){
    TextView b=txt(label,14,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setBackground(rounded(royal));b.setMinHeight(d(43));b.setOnClickListener(v->run.run());
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,d(5),0,d(5));p.addView(b,lp);
  }
  private void openCorpus() throws Exception{
    File file=getDatabasePath("tipitaka_myanmar.db");
    if(!file.exists()){
      File parent=file.getParentFile();if(parent!=null&&!parent.exists())parent.mkdirs();
      try(InputStream in=getAssets().open("tipitaka_myanmar.db");FileOutputStream out=new FileOutputStream(file)){
        byte[] buf=new byte[32768];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);out.flush();
      }
    }
    db=SQLiteDatabase.openDatabase(file.getAbsolutePath(),null,SQLiteDatabase.OPEN_READONLY);
  }
  private void show(){
    getWindow().setStatusBarColor(royal);getWindow().setNavigationBarColor(royal);
    LinearLayout root=column();root.setBackgroundColor(0xfff8f6f2);setContentView(root);
    TextView head=txt(selected<0?"☸  မူရင်းကျမ်းစာဖတ်ခန်း · Offline":"☸  Native Reader",20,Color.WHITE,true);
    head.setPadding(d(16),d(16),d(12),d(16));head.setBackgroundColor(royal);root.addView(head);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    list=column();scroll.addView(list);
    if(db==null){LinearLayout c=card();c.addView(txt("ကျမ်းစာ Database မရရှိပါ။ APK Build ထဲတွင် Corpus asset ထည့်သွင်းထားမှုကို စစ်ဆေးပါ။",15,ink,false));return;}
    if(selected<0)catalog();else reader();
  }
  private void catalog(){
    LinearLayout intro=card();
    intro.addView(txt("မြန်မာအက္ခရာဖြင့် ပါဠိမူရင်းကျမ်းစာ",18,ink,true));
    intro.addView(txt("ကျမ်းစာဒေတာကို APK ထဲ တစ်ခါတည်း ထည့်ထားပါသည်။ Internet၊ Browser၊ WebView၊ Import မလိုပါ။ ဤမူသည် မြန်မာဘာသာပြန်မဟုတ်ဘဲ ပါဠိစာသားကို မြန်မာအက္ခရာဖြင့် ဖော်ပြထားခြင်း ဖြစ်သည်။",13,muted,false));
    EditText search=new EditText(this);search.setSingleLine(true);search.setHint("ကျမ်းအမည် သို့မဟုတ် စာသား ရှာရန်");search.setText(query);intro.addView(search);
    if(watcher!=null)search.removeTextChangedListener(watcher);
    watcher=new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();} public void afterTextChanged(Editable e){}};
    search.addTextChangedListener(watcher);
    action(intro,"ရှာဖွေမည်",()->show());
    LinearLayout tabs=card();
    action(tabs,"ကျမ်းအားလုံး",()->{category="";show();});
    action(tabs,"ဝိနည်းပိဋကတ်",()->{category="ဝိနည်းပိဋကတ်";show();});
    action(tabs,"သုတ္တပိဋကတ်",()->{category="သုတ္တပိဋကတ်";show();});
    action(tabs,"အဘိဓမ္မာပိဋကတ်",()->{category="အဘိဓမ္မာပိဋကတ်";show();});
    String sql;
    String[] args;
    if(query.trim().length()>=2){
      sql="SELECT DISTINCT d.id,d.title,d.category,d.paragraph_count FROM documents d JOIN passages p ON p.doc_id=d.id WHERE (d.title LIKE ? OR p.body LIKE ?)"+(category.isEmpty()?"":" AND d.category=?")+" ORDER BY d.category,d.id LIMIT 100";
      args=category.isEmpty()?new String[]{"%"+query+"%","%"+query+"%"}:new String[]{"%"+query+"%","%"+query+"%",category};
    }else{
      sql="SELECT id,title,category,paragraph_count FROM documents"+(category.isEmpty()?"":" WHERE category=?")+" ORDER BY CASE category WHEN 'ဝိနည်းပိဋကတ်' THEN 1 WHEN 'သုတ္တပိဋကတ်' THEN 2 ELSE 3 END,id";
      args=category.isEmpty()?null:new String[]{category};
    }
    int count=0;
    try(Cursor c=db.rawQuery(sql,args)){
      while(c.moveToNext()){
        count++;final int id=c.getInt(0);String title=c.getString(1),cat=c.getString(2);int paragraphs=c.getInt(3);
        LinearLayout item=card();item.addView(txt(title,16,ink,true));item.addView(txt(cat+" · စာပိုဒ် "+paragraphs+" ပိုဒ်",12,muted,false));
        action(item,"ဖတ်ရှုရန်  ›",()->{selected=id;offset=0;show();});
      }
    }
    if(count==0){LinearLayout c=card();c.addView(txt("ရှာဖွေမှုရလဒ် မတွေ့ပါ။",14,muted,false));}
    LinearLayout credit=card();credit.addView(txt("Source & Attribution",14,ink,true));credit.addView(txt("Vipassana Research Institute (VRI) — Tipitaka.org XML / mymr Mula files. Source: github.com/VipassanaTech/tipitaka-xml. Free for non-commercial use; VRI attribution retained. This is Pāli in Myanmar script, not a Burmese translation.",11,muted,false));
  }
  private void reader(){
    String title="ကျမ်းစာ";
    String categoryName="";
    try(Cursor c=db.rawQuery("SELECT title,category,paragraph_count FROM documents WHERE id=?",new String[]{""+selected})){
      if(c.moveToFirst()){title=c.getString(0);categoryName=c.getString(1);list.addView(txt(categoryName+" · "+c.getInt(2)+" စာပိုဒ်",12,muted,false));}
    }
    LinearLayout top=card();top.addView(txt(title,20,ink,true));
    action(top,"← ကျမ်းစာရင်းသို့ ပြန်ရန်",()->{selected=-1;offset=0;show();});
    action(top,"A− စာလုံးသေး",()->{font=Math.max(14,font-2);getPreferences(0).edit().putInt("font",font).apply();show();});
    action(top,"A+ စာလုံးကြီး",()->{font=Math.min(32,font+2);getPreferences(0).edit().putInt("font",font).apply();show();});
    int total=0;
    try(Cursor c=db.rawQuery("SELECT paragraph_count FROM documents WHERE id=?",new String[]{""+selected})){if(c.moveToFirst())total=c.getInt(0);}
    try(Cursor c=db.rawQuery("SELECT seq,kind,body FROM passages WHERE doc_id=? ORDER BY seq LIMIT ? OFFSET ?",new String[]{""+selected,""+PAGE_SIZE,""+offset})){
      while(c.moveToNext()){
        String kind=c.getString(1),body=c.getString(2);int passageNo=c.getInt(0);
        TextView t=txt(body,font,ink,kind.equals("heading"));
        if(kind.equals("heading"))t.setTextColor(royal);
        list.addView(t);
        if(kind.equals("paragraph")){
          TextView explain=txt("မြန်မာလို ရှင်းလင်းချက် ဖတ်ရန်  ▾",13,royal,true);
          explain.setOnClickListener(v->{
            String note=MyanmarExplanation.explain(body,categoryName,title,passageNo);
            new android.app.AlertDialog.Builder(this).setTitle("မြန်မာဘာသာ လေ့လာရေးရှင်းလင်းချက်").setMessage(note).setPositiveButton("နားလည်ပါပြီ",null).show();
          });list.addView(explain);
        }
      }
    }
    LinearLayout nav=card();
    nav.addView(txt("စာပိုဒ် "+(offset+1)+" – "+Math.min(offset+PAGE_SIZE,total)+" / "+total,12,muted,false));
    action(nav,"◀ ယခင်စာပိုဒ်များ",()->{offset=Math.max(0,offset-PAGE_SIZE);show();});
    final int pageTotal=total;
    action(nav,"နောက်စာပိုဒ်များ ▶",()->{if(offset+PAGE_SIZE<pageTotal){offset+=PAGE_SIZE;show();}});
  }
}