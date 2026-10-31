package asia.aaii.tipitaka;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import java.util.*;

public class DesignScreensActivity extends Activity {
  private int page=2;
  private LinearLayout root, content;
  private final int navy=0xff090e2a,purple=0xff6224ac,gold=0xffffcc63;
  private final String[] names={"Splash Screen","Home Dashboard","Navigation Menu","Tipitaka Books","Dhammapada","Dhammapada Reader","Dhamma Audio Player","Audio Library","Offline Sync Center","Settings","My Library & Progress","Bookmarks & Notes","Reading History","Advanced Search","Abhidhamma Books","Abhidhamma Reader","Satipatthana Learning","Mangala Sutta & Nibbana","Five Nikayas","About & Help"};
  private final String[] mm={"စတင်မျက်နှာ","ပင်မစာမျက်နှာ","လမ်းညွှန်မီနူး","ပိဋကတ်ကျမ်းများ","ဓမ္မပဒ","ဓမ္မပဒဖတ်ခန်း","တရားအသံဖွင့်စက်","တရားအသံစာကြည့်တိုက်","အော့ဖ်လိုင်း Sync","ပြင်ဆင်မှုများ","ကိုယ်ရေးမှတ်တမ်း","စာမှတ်နှင့် မှတ်စုများ","ဖတ်ရှုမှတ်တမ်း","အဆင့်မြင့်ရှာဖွေရေး","အဘိဓမ္မာကျမ်းများ","အဘိဓမ္မာဖတ်ခန်း","သတိပဋ္ဌာန်","မင်္ဂလသုတ်နှင့် နိဗ္ဗာန်","နိကာယ်ငါးမျိုး","အကြောင်းနှင့်အကူအညီ"};
  @Override public void onCreate(Bundle b){super.onCreate(b);if(b!=null)page=b.getInt("page",2);render();}
  @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putInt("page",page);}
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  GradientDrawable shape(int c){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(18));g.setStroke(dp(1),gold);return g;}
  TextView text(String s,int sz,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sz);t.setTextColor(color);t.setPadding(dp(12),dp(10),dp(12),dp(10));if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
  LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  LinearLayout card(String title,String desc){
    LinearLayout l=column();l.setBackground(shape(0xff151938));l.setPadding(dp(12),dp(10),dp(12),dp(10));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(12),dp(7),dp(12),0);content.addView(l,p);
    l.addView(text(title,19,gold,true));if(desc!=null)l.addView(text(desc,14,Color.WHITE,false));return l;
  }
  void button(LinearLayout p,String s,Runnable run){TextView b=text(s,16,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setBackground(shape(purple));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,dp(3));p.addView(b,lp);b.setOnClickListener(v->run.run());}
  void open(Class<?> activity){startActivity(new Intent(this,activity));}
  void corpus(String category){Intent i=new Intent(this,CorpusActivity.class);if(category!=null)i.putExtra("filter_category",category);startActivity(i);}
  void read(String code,int st,int en){Intent i=new Intent(this,CorpusActivity.class);i.putExtra("open_code",code);i.putExtra("start_seq",st);i.putExtra("end_seq",en);startActivity(i);}
  void render(){
    getWindow().setStatusBarColor(navy);getWindow().setNavigationBarColor(navy);
    root=column();root.setBackgroundColor(navy);setContentView(root);
    TextView banner=text("☸  MYANMAR TIPITAKA\n"+mm[page-1]+" · "+names[page-1],22,gold,true);
    banner.setGravity(Gravity.CENTER);banner.setPadding(dp(15),dp(20),dp(15),dp(20));banner.setBackgroundColor(0xff251249);root.addView(banner);
    ScrollView sv=new ScrollView(this);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));content=column();sv.addView(content);
    if(page==1){LinearLayout c=card("ပိဋကတ်သုံးပုံ","Offline Native Reader · အင်တာနက်မလို");button(c,"စတင်ရန်",()->navigate(2));}
    else if(page==2||page==3||page==4){LinearLayout c=card("ပိဋကတ်သုံးပုံ","ဝိနည်း · သုတ္တန် · အဘိဓမ္မာ");button(c,"ဝိနည်းပိဋကတ်",()->corpus("ဝိနည်းပိဋကတ်"));button(c,"သုတ္တပိဋကတ်",()->corpus("သုတ္တပိဋကတ်"));button(c,"အဘိဓမ္မပိဋကတ်",()->corpus("အဘိဓမ္မပိဋကတ်"));button(c,"အထူးဓမ္မသင်ခန်းစာများ",()->open(LearningActivity.class));button(c,"တရားအသံ",()->open(AudioLibraryActivity.class));}
    else if(page==5||page==6){LinearLayout c=card("ဓမ္မပဒ (Dhammapada)","ဝဂ် ၂၆ ခု · ဂါထာ ၄၂၃ ပုဒ် · ပါဠိမူရင်း");button(c,"Native Offline ဖတ်ရန်",()->read("s0502m",1,-1));}
    else if(page==7||page==8||page==9){LinearLayout c=card("တရားအသံနှင့် Offline Sync","YouTube / Website Catalog + ခွင့်ရှိသော Local MP3 Playback");button(c,"Audio Library / Sync Center",()->open(AudioLibraryActivity.class));}
    else if(page==10){LinearLayout c=card("စာဖတ်ခန်း ပြင်ဆင်မှုများ","စာလုံးအရွယ်အစား · Offline Reader");button(c,"Native Reader Settings",()->corpus(null));}
    else if(page==11||page==12||page==13){LinearLayout c=card("မှတ်သားချက်နှင့် ဖတ်ရှုမှတ်တမ်း","Offline Reading၊ Bookmarks နှင့် Notes အတွက် UI");button(c,"ကျမ်းစာဖတ်ခန်း",()->corpus(null));}
    else if(page==14){LinearLayout c=card("အဆင့်မြင့်ရှာဖွေရေး","ပါဠိစာသား၊ ကျမ်းအမည်၊ နိကာယ်အလိုက် ရှာဖွေရန်");button(c,"Offline Search",()->corpus(null));}
    else if(page==15||page==16){LinearLayout c=card("အဘိဓမ္မာ ၇ ကျမ်း","ဓမ္မသင်္ဂဏီ · ဝိဘင်္ဂ · ဓာတုကထာ · ပုဂ္ဂလပညတ္တိ · ကထာဝတ္ထု · ယမက · ပဋ္ဌာန်း");button(c,"အဘိဓမ္မာ Offline Reader",()->corpus("အဘိဓမ္မပိဋကတ်"));}
    else if(page==17){LinearLayout c=card("သတိပဋ္ဌာန်လေးပါး","ကာယ · ဝေဒနာ · စိတ် · ဓမ္မ");button(c,"သတိပဋ္ဌာန် Offline",()->open(LearningActivity.class));}
    else if(page==18){LinearLayout c=card("မင်္ဂလသုတ်နှင့် နိဗ္ဗာန်","မင်္ဂလာ ၃၈ ပါး · နိဗ္ဗာန်သုတ်");button(c,"မင်္ဂလသုတ် Offline",()->read("s0501m",45,81));button(c,"နိဗ္ဗာန် Offline",()->read("s0503m",814,830));}
    else if(page==19){LinearLayout c=card("နိကာယ် ၅ မျိုး","ဒီဃ · မဇ္ဈိမ · သံယုတ္တ · အင်္ဂုတ္တရ · ခုဒ္ဒက");button(c,"နိကာယ်စာရင်း",()->open(LearningActivity.class));}
    else {LinearLayout c=card("Myanmar Tipitaka","Native Offline Library · Source VRI and licensed personal audio only");button(c,"Offline ကျမ်းဖတ်ခန်း",()->corpus(null));}
    LinearLayout nav=card("SCREEN "+page+" / 20","UI Navigation · Offline Corpus အပေါ် အခြေခံထားပါသည်။");
    button(nav,"◀ ယခင်မျက်နှာ",()->navigate(page==1?20:page-1));
    button(nav,"နောက်မျက်နှာ ▶",()->navigate(page==20?1:page+1));
    LinearLayout foot=new LinearLayout(this);foot.setBackgroundColor(0xff1b1243);
    String[] ts={"⌂ ပင်မ","▣ ကျမ်း","♫ အသံ","⌕ ရှာဖွေ","⚙ အကြောင်း"};
    int[] pages={2,4,8,14,20};
    for(int j=0;j<ts.length;j++){final int target=pages[j];TextView t=text(ts[j],11,gold,false);t.setGravity(Gravity.CENTER);foot.addView(t,new LinearLayout.LayoutParams(0,dp(58),1));t.setOnClickListener(v->navigate(target));}
    root.addView(foot);
  }
  void navigate(int p){page=p;render();}
}