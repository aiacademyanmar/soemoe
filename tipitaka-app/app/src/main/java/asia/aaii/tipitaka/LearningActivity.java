package asia.aaii.tipitaka;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class LearningActivity extends Activity {
  private final int royal=0xff30234d,ink=0xff292337,muted=0xff746c7b;
  private LinearLayout list;
  private int d(int x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
  private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  private TextView txt(String s,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(d(7),d(7),d(7),d(7));t.setLineSpacing(d(4),1.12f);if(bold)t.setTypeface(null,Typeface.BOLD);return t;
  }
  private GradientDrawable rounded(int color){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(13));return g;}
  private LinearLayout card(){
    LinearLayout c=column();c.setPadding(d(12),d(10),d(12),d(10));c.setBackground(rounded(Color.WHITE));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(d(11),d(6),d(11),d(5));list.addView(c,p);return c;
  }
  private void action(LinearLayout p,String label,Runnable r){
    TextView b=txt(label,14,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setBackground(rounded(royal));b.setMinHeight(d(43));b.setOnClickListener(v->r.run());
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,d(5),0,d(5));p.addView(b,lp);
  }
  private void open(String code,int start,int end){
    Intent i=new Intent(this,CorpusActivity.class);
    if(code!=null)i.putExtra("open_code",code);
    i.putExtra("start_seq",start);i.putExtra("end_seq",end);
    startActivity(i);
  }
  private void openNikaya(String prefix){
    Intent i=new Intent(this,CorpusActivity.class);
    i.putExtra("filter_category","သုတ္တပိဋကတ်");
    i.putExtra("initial_query",prefix);
    startActivity(i);
  }
  @Override public void onCreate(Bundle b){super.onCreate(b);show();}
  private void show(){
    getWindow().setStatusBarColor(royal);getWindow().setNavigationBarColor(royal);
    LinearLayout root=column();root.setBackgroundColor(0xfff8f6f2);setContentView(root);
    TextView header=txt("☸  Offline ဓမ္မလေ့လာခန်း · Native Reader",20,Color.WHITE,true);header.setPadding(d(16),d(16),d(12),d(16));header.setBackgroundColor(royal);root.addView(header);
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));list=column();scroll.addView(list);
    LinearLayout intro=card();
    intro.addView(txt("Website မလိုဘဲ App အတွင်း တိုက်ရိုက်ဖတ်ရှုရန်",18,ink,true));
    intro.addView(txt("အောက်ပါ ကျမ်းစာများသည် APK ထဲပါ Offline SQLite ကျမ်းဒေတာမှ ဖွင့်ပေးသည်။ မူရင်းစာသားသည် ပါဠိကို မြန်မာအက္ခရာဖြင့် ဖော်ပြထားခြင်းဖြစ်ပြီး မြန်မာဘာသာပြန်မဟုတ်ပါ။",13,muted,false));
    LinearLayout dhamma=card();dhamma.addView(txt("၁။ ဓမ္မပဒ (Dhammapada)",17,ink,true));
    dhamma.addView(txt("ဂါထာ ၄၂၃ · ဝဂ် ၂၆ ခု · ခုဒ္ဒကနိကာယ်",13,muted,false));
    dhamma.addView(txt("ယမကဝဂ်မှ ဗြာဟ္မဏဝဂ်အထိ မူရင်းပါဠိကို Offline ဖတ်နိုင်သည်။",14,ink,false));
    action(dhamma,"ဓမ္မပဒ အပြည့်ဖတ်ရန်",()->open("s0502m",1,-1));

    LinearLayout mangala=card();mangala.addView(txt("၂။ မင်္ဂလသုတ် (Maṅgala Sutta)",17,ink,true));
    mangala.addView(txt("မင်္ဂလာ ၃၈ ပါး · ခုဒ္ဒကပါဌ",13,muted,false));
    mangala.addView(txt("ခုဒ္ဒကပါဌပါဠိထဲရှိ မင်္ဂလသုတ္တံ အပိုင်းကို ရွေးဖတ်နိုင်သည်။",14,ink,false));
    action(mangala,"မင်္ဂလသုတ် ဖတ်ရန်",()->open("s0501m",45,81));

    LinearLayout nibbana=card();nibbana.addView(txt("၃။ နိဗ္ဗာန် (Nibbāna)",17,ink,true));
    nibbana.addView(txt("ဥဒါနပါဠိ · နိဗ္ဗာန်နှင့် ဆက်စပ်သော သုတ် ၄ ပုဒ်",13,muted,false));
    nibbana.addView(txt("အဇာတံ အဘူတံ အကတံ အသင်္ခတံ စသော ပါဠိစာပိုဒ်များကို ရှေ့နောက်အကြောင်းအရာနှင့် တွဲဖတ်ရန်။",14,ink,false));
    action(nibbana,"နိဗ္ဗာန်ဆိုင်ရာ သုတ်များ ဖတ်ရန်",()->open("s0503m",814,830));

    LinearLayout sati=card();sati.addView(txt("၄။ သတိပဋ္ဌာန် (Satipaṭṭhāna)",17,ink,true));
    sati.addView(txt("မဟာသတိပဋ္ဌာန်သုတ် · မဇ္ဈိမနိကာယ်",13,muted,false));
    sati.addView(txt("ကာယ၊ ဝေဒနာ၊ စိတ်၊ ဓမ္မ ဟူသော သတိပဋ္ဌာန် ၄ ပါးကို မူရင်းသုတ်တော်တွင် ဖတ်ရှုနိုင်သည်။",14,ink,false));
    action(sati,"မဟာသတိပဋ္ဌာန်သုတ် ဖတ်ရန်",()->open("s0201m",280,410));

    LinearLayout nk=card();nk.addView(txt("၅။ နိကာယ် ၅ မျိုး (Five Nikāyas)",17,ink,true));
    nk.addView(txt("နိကာယ်တစ်မျိုးစီကို ရွေး၍ သက်ဆိုင်ရာ Offline ကျမ်းတွဲစာရင်းကို ကြည့်နိုင်သည်။",13,muted,false));
    String[] names={"ဒီဃနိကာယ် · Dīgha Nikāya","မဇ္ဈိမနိကာယ် · Majjhima Nikāya","သံယုတ္တနိကာယ် · Saṃyutta Nikāya","အင်္ဂုတ္တရနိကာယ် · Aṅguttara Nikāya","ခုဒ္ဒကနိကာယ် · Khuddaka Nikāya"};
    String[] ranges={"အတွဲ ၉–၁၁ · ရှည်လျားသော သုတ်တော်များ","အတွဲ ၁၂–၁၄ · အလယ်အလတ် သုတ်တော်များ","အတွဲ ၁၅–၁၉ · အကြောင်းအရာအလိုက် သုတ်စုများ","အတွဲ ၂၀–၂၄ · အရေအတွက်အလိုက် သုတ်စုများ","အတွဲ ၂၅–၃၃ · ခုဒ္ဒကကျမ်းများ"};
    for(int x=0;x<5;x++){final int k=x;LinearLayout item=column();item.addView(txt(names[x],15,ink,true));item.addView(txt(ranges[x],12,muted,false));action(item,"Offline ကျမ်းစာများ ကြည့်ရန်",()->openNikaya("s0"+(k+1)));nk.addView(item);}
    LinearLayout footer=card();footer.addView(txt("Offline ရှင်းလင်းချက်",16,ink,true));
    footer.addView(txt("ဤအက်ပ်တွင် ပါဠိမူရင်းကို မြန်မာအက္ခရာဖြင့် ထည့်သွင်းထားသည်။ မြန်မာဘာသာဖြင့် အယူအဆရှင်းလင်းချက်များကို သီးခြားလေ့လာရေးမှတ်စုအဖြစ် ဖော်ပြမည်။ ပါဠိမူရင်းနှင့် ဘာသာပြန်ကို မရောထွေးစေရန် အမှတ်အသားပြထားသည်။",13,muted,false));
  }
}