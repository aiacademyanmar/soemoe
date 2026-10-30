package asia.aaii.tipitaka;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class LearningActivity extends Activity {
  private final int royal=0xff30234d, ink=0xff292337, muted=0xff746c7b;
  private LinearLayout list;
  private final String[] titles={"ဓမ္မပဒ (Dhammapada)","မင်္ဂလသုတ် (Maṅgala Sutta)","နိဗ္ဗာန် (Nibbāna)","သတိပဋ္ဌာန် (Satipaṭṭhāna)","နိကာယ် ၅ မျိုး (Five Nikāyas)"};
  private final String[] descriptions={
    "ဂါထာ ၄၂၃ ပုဒ်ကို ဝဂ် ၂၆ ခုဖြင့် စုစည်းထားသော ခုဒ္ဒကနိကာယ်ကျမ်း။ စိတ်၊ သတိ၊ အကျင့်၊ ပညာနှင့် လွတ်မြောက်မှုဆိုင်ရာ ဓမ္မအကြောင်းအရာများကို လေ့လာနိုင်သည်။",
    "မင်္ဂလာ ၃၈ ပါးကို ဖော်ပြသော သုတ်တော်။ လူမှုဘဝ၊ မိသားစု၊ ကိုယ်ကျင့်တရား၊ ပညာနှင့် စိတ်တည်ငြိမ်မှုဆိုင်ရာ မင်္ဂလာအချက်များကို စုစည်းထားသည်။",
    "နိဗ္ဗာန်သည် ဒုက္ခချုပ်ငြိမ်းခြင်းနှင့် ကိလေသာမှ လွတ်မြောက်ခြင်းဆိုင်ရာ ဓမ္မခေါင်းစဉ်ဖြစ်သည်။ ဤနေရာရှိ စာသားသည် လေ့လာရေးအကျဉ်းသာဖြစ်ပြီး သီးခြားသုတ်တစ်ပုဒ်၏ အစားထိုးမဟုတ်ပါ။",
    "သတိတည်ရာ ၄ ပါးမှာ ကာယာနုပဿနာ၊ ဝေဒနာနုပဿနာ၊ စိတ္တာနုပဿနာ၊ ဓမ္မာနုပဿနာတို့ ဖြစ်သည်။ မဟာသတိပဋ္ဌာန်သုတ်ကို ဒီဃနိကာယ် အတွဲ ၁၀ တွင် လေ့လာနိုင်သည်။",
    "သုတ္တပိဋကတ်ကို ဒီဃ၊ မဇ္ဈိမ၊ သံယုတ္တ၊ အင်္ဂုတ္တရ၊ ခုဒ္ဒက ဟူသော နိကာယ် ၅ မျိုးဖြင့် ဖွဲ့စည်းထားသည်။ အတွဲ ၉ မှ ၃၃ အတွင်း တည်ရှိသည်။"
  };
  private final String[] urls={
    "https://tipitaka.cloud/en/learn/suttas/dhammapada",
    "https://tipitaka.cloud/learn/suttas/mangala",
    "https://tipitaka.cloud/en/learn/topics/nibbana",
    "https://tipitaka.cloud/en/learn/topics/satipatthana",
    "https://tipitaka.cloud/learn/structure/suttanta"
  };
  private final String[] nikayaNames={"ဒီဃနိကာယ် · Dīgha Nikāya","မဇ္ဈိမနိကာယ် · Majjhima Nikāya","သံယုတ္တနိကာယ် · Saṃyutta Nikāya","အင်္ဂုတ္တရနိကာယ် · Aṅguttara Nikāya","ခုဒ္ဒကနိကာယ် · Khuddaka Nikāya"};
  private final String[] ranges={"အတွဲ ၉–၁၁ · ရှည်လျားသော သုတ်တော်များ","အတွဲ ၁၂–၁၄ · အလယ်အလတ် သုတ်တော်များ","အတွဲ ၁၅–၁၉ · အကြောင်းအရာတူ သုတ်စုများ","အတွဲ ၂၀–၂၄ · တရားအရေအတွက်အလိုက် သုတ်များ","အတွဲ ၂၅–၃၃ · ဓမ္မပဒ၊ ဇာတကနှင့် ခုဒ္ဒကကျမ်းများ"};
  @Override public void onCreate(Bundle b){super.onCreate(b);show();}
  private int d(int x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable rounded(int color){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(13));return g;}
  private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  private TextView txt(String s,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(d(6),d(6),d(6),d(6));t.setLineSpacing(d(3),1.1f);if(bold)t.setTypeface(null,Typeface.BOLD);return t;
  }
  private LinearLayout card(){
    LinearLayout c=column();c.setPadding(d(13),d(10),d(13),d(10));c.setBackground(rounded(Color.WHITE));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(d(12),d(7),d(12),d(5));list.addView(c,p);return c;
  }
  private void action(LinearLayout p,String label,Runnable r){
    TextView b=txt(label,14,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setBackground(rounded(royal));b.setMinHeight(d(43));b.setOnClickListener(v->r.run());
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,d(6),0,d(4));p.addView(b,lp);
  }
  private void open(String url){
    try{Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse(url));startActivity(i);}
    catch(Exception e){android.widget.Toast.makeText(this,"Browser ဖြင့် လင့်ခ်မဖွင့်နိုင်ပါ။",android.widget.Toast.LENGTH_LONG).show();}
  }
  private void show(){
    getWindow().setStatusBarColor(royal);getWindow().setNavigationBarColor(royal);
    LinearLayout root=column();root.setBackgroundColor(0xfff8f6f2);setContentView(root);
    TextView header=txt("☸  ဓမ္မလေ့လာရန် · Dhamma Learning",20,Color.WHITE,true);header.setPadding(d(17),d(17),d(12),d(17));header.setBackgroundColor(royal);root.addView(header);
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));list=column();scroll.addView(list);
    LinearLayout intro=card();
    intro.addView(txt("Tipiṭaka · 45 ကျမ်း · အခမဲ့",18,ink,true));
    intro.addView(txt("Tipitaka.cloud တွင် MCU ထိုင်းဘာသာပြန်နှင့် ပါဠိစာသားများကို အွန်လိုင်း အခမဲ့ဖတ်ရှုနိုင်သည်။ ဤ Native App ထဲတွင် အောက်ပါ မြန်မာလေ့လာရေးအကျဉ်းများကို Offline ဖတ်နိုင်သည်။ မူရင်းစာမျက်နှာများကို ဖွင့်ရန် အင်တာနက်လိုအပ်သည်။",13,muted,false));
    action(intro,"📚 ကျမ်းဖတ်ခန်း (45 ကျမ်း) ဖွင့်ရန်",()->open("https://tipitaka.cloud/library"));
    action(intro,"📖 ပါဠိ / MCU Reader ဖွင့်ရန်",()->open("https://tipitaka.cloud/?panes=en_translation"));
    LinearLayout feature=card();feature.addView(txt("အထူးလေ့လာရန်",18,ink,true));
    for(int i=0;i<titles.length;i++){
      final int k=i;LinearLayout c=card();
      c.addView(txt(titles[i],16,ink,true));
      c.addView(txt(descriptions[i],14,ink,false));
      if(i==0)c.addView(txt("ဂါထာ ၄၂၃ ပုဒ် · ဝဂ် ၂၆ ခု · ခုဒ္ဒကနိကာယ် အတွဲ ၂၅",12,muted,false));
      if(i==1)c.addView(txt("မင်္ဂလာ ၃၈ ပါး · ခုဒ္ဒကနိကာယ် အတွဲ ၂၅",12,muted,false));
      if(i==3)c.addView(txt("ကိုယ် · ဝေဒနာ · စိတ် · ဓမ္မ",12,muted,false));
      if(i==4)c.addView(txt("Dīgha 9–11 · Majjhima 12–14 · Saṃyutta 15–19 · Aṅguttara 20–24 · Khuddaka 25–33",12,muted,false));
      action(c,"မူရင်း Website တွင် ဖတ်ရန် ↗",()->open(urls[k]));
    }
    LinearLayout nk=card();nk.addView(txt("သုတ္တပိဋကတ် — နိကာယ် ၅ မျိုး",18,ink,true));
    for(int i=0;i<nikayaNames.length;i++){
      nk.addView(txt(nikayaNames[i],15,ink,true));
      nk.addView(txt(ranges[i],13,muted,false));
    }
    action(nk,"နိကာယ်ဖွဲ့စည်းပုံ မူရင်းတွင် ဖတ်ရန် ↗",()->open(urls[4]));
    LinearLayout note=card();
    note.addView(txt("Offline / Online ခွဲခြားချက်",15,ink,true));
    note.addView(txt("မြန်မာအကျဉ်းချုပ်၊ ကျမ်းတွဲအမည်နှင့် နိကာယ်အညွှန်းများကို Offline ဖတ်နိုင်သည်။ Tipitaka.cloud ရှိ မူရင်း Pāli/MCU စာသားများကို ဖွင့်ဖတ်ရန် Internet လိုအပ်သည်။ ထိုစာသားများကို APK ထဲ အပြည့်အစုံကူးထည့်ထားခြင်း မဟုတ်ပါ။",13,muted,false));
  }
}