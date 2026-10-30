package asia.aaii.tipitaka;

import android.app.Activity;
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

public class ResearchActivity extends Activity {
  private final String[] titles={
    "မဟာဝိဘင်္ဂ (ပထမပိုင်း)","မဟာဝိဘင်္ဂ (ဒုတိယပိုင်း)","ဘိက္ခုနီဝိဘင်္ဂ",
    "မဟာဝဂ္ဂ (ပထမပိုင်း)","မဟာဝဂ္ဂ (ဒုတိယပိုင်း)","စူဠဝဂ္ဂ (ပထမပိုင်း)",
    "စူဠဝဂ္ဂ (ဒုတိယပိုင်း)","ပရိဝါရ",
    "ဒီဃနိကာယ် — သီလက္ခန္ဓဝဂ္ဂ","ဒီဃနိကာယ် — မဟာဝဂ္ဂ","ဒီဃနိကာယ် — ပါဋိကဝဂ္ဂ",
    "မဇ္ဈိမနိကာယ် — မူလပဏ္ဏာသ","မဇ္ဈိမနိကာယ် — မဇ္ဈိမပဏ္ဏာသ","မဇ္ဈိမနိကာယ် — ဥပရိပဏ္ဏာသ",
    "သံယုတ္တနိကာယ် — သဂါထာဝဂ္ဂ","သံယုတ္တနိကာယ် — နိဒါနဝဂ္ဂ","သံယုတ္တနိကာယ် — ခန္ဓဝဂ္ဂ",
    "သံယုတ္တနိကာယ် — သဠာယတနဝဂ္ဂ","သံယုတ္တနိကာယ် — မဟာဝဂ္ဂ",
    "အင်္ဂုတ္တရ — ဧကက၊ ဒုက၊ တိကနိပါတ်","အင်္ဂုတ္တရ — စတုက္ကနိပါတ်",
    "အင်္ဂုတ္တရ — ပဉ္စက၊ ဆက္ကနိပါတ်","အင်္ဂုတ္တရ — သတ္တက၊ အဋ္ဌက၊ နဝက",
    "အင်္ဂုတ္တရ — ဒသက၊ ဧကာဒသက",
    "ခုဒ္ဒကပါဌ၊ ဓမ္မပဒ၊ ဥဒါန၊ ဣတိဝုတ္တက၊ သုတ္တနိပါတ",
    "ဝိမာနဝတ္ထု၊ ပေတဝတ္ထု၊ ထေရဂါထာ၊ ထေရီဂါထာ",
    "ဇာတက (ပထမပိုင်း)","ဇာတက (ဒုတိယပိုင်း)","မဟာနိဒ္ဒေသ","စူဠနိဒ္ဒေသ",
    "ပဋိသမ္ဘိဒါမဂ္ဂ","အပဒါန် (ပထမပိုင်း)",
    "အပဒါန် (ဒုတိယပိုင်း)၊ ဗုဒ္ဓဝံသ၊ စရိယာပိဋက",
    "ဓမ္မသင်္ဂဏီ","ဝိဘင်္ဂ","ဓာတုကထာ၊ ပုဂ္ဂလပညတ္တိ",
    "ကထာဝတ္ထု","ယမက (ပထမပိုင်း)","ယမက (ဒုတိယပိုင်း)",
    "ပဋ္ဌာန်း (ပထမပိုင်း)","ပဋ္ဌာန်း (ဒုတိယပိုင်း)","ပဋ္ဌာန်း (တတိယပိုင်း)",
    "ပဋ္ဌာန်း (စတုတ္ထပိုင်း)","ပဋ္ဌာန်း (ပဉ္စမပိုင်း)","ပဋ္ဌာန်း (ဆဋ္ဌမပိုင်း)"
  };
  private final String[] pali={
    "Mahāvibhaṅga I","Mahāvibhaṅga II","Bhikkhunīvibhaṅga",
    "Mahāvagga I","Mahāvagga II","Cūḷavagga I","Cūḷavagga II","Parivāra",
    "Dīgha: Sīlakkhandhavagga","Dīgha: Mahāvagga","Dīgha: Pāṭikavagga",
    "Majjhima: Mūlapaṇṇāsa","Majjhima: Majjhimapaṇṇāsa","Majjhima: Uparipaṇṇāsa",
    "Saṃyutta: Sagāthāvagga","Saṃyutta: Nidānavagga","Saṃyutta: Khandhavagga",
    "Saṃyutta: Saḷāyatanavagga","Saṃyutta: Mahāvagga",
    "Aṅguttara: Ekaka–Tika","Aṅguttara: Catukkanipāta","Aṅguttara: Pañcaka–Chakka",
    "Aṅguttara: Sattaka–Navaka","Aṅguttara: Dasaka–Ekādasaka",
    "Khuddakapāṭha, Dhammapada, Udāna, Itivuttaka, Suttanipāta",
    "Vimānavatthu, Petavatthu, Theragāthā, Therīgāthā",
    "Jātaka I","Jātaka II","Mahāniddesa","Cūḷaniddesa",
    "Paṭisambhidāmagga","Apadāna I","Apadāna II, Buddhavaṃsa, Cariyāpiṭaka",
    "Dhammasaṅgaṇī","Vibhaṅga","Dhātukathā, Puggalapaññatti","Kathāvatthu",
    "Yamaka I","Yamaka II","Paṭṭhāna I","Paṭṭhāna II","Paṭṭhāna III",
    "Paṭṭhāna IV","Paṭṭhāna V","Paṭṭhāna VI"
  };
  private final String[] summary={
    "ရဟန်းတော်များ၏ ပါတိမောက်စည်းကမ်း အစပိုင်းနှင့် စည်းကမ်းဖြစ်ပေါ်လာပုံ နောက်ခံဝတ္ထုများ ပါဝင်သည်။",
    "ရဟန်းဝိနည်း စည်းကမ်းများ၏ ဆက်ပိုင်းနှင့် အပြစ်အမျိုးအစားများ ပါဝင်သည်။",
    "ဘိက္ခုနီသံဃာနှင့် သက်ဆိုင်သော စည်းကမ်းများ ပါဝင်သည်။",
    "သံဃာ့ဘဝ စတင်တည်ထောင်မှုနှင့် ဥပသမ္ပဒါဆိုင်ရာ လုပ်ထုံးလုပ်နည်းများ ပါဝင်သည်။",
    "သံဃာ့ဘဝ၏ နေ့စဉ်လုပ်ထုံးလုပ်နည်းများကို ဆက်လက်ဖော်ပြသည်။",
    "သံဃာ့အမှုကိစ္စ၊ စည်းကမ်းအရေးယူမှုနှင့် အဖွဲ့အစည်းဆိုင်ရာ လုပ်ငန်းစဉ်များ ပါဝင်သည်။",
    "ဝိနည်းဆိုင်ရာ သမိုင်းနှင့် လုပ်ထုံးလုပ်နည်း ဆက်ပိုင်း ဖြစ်သည်။",
    "ဝိနည်းကို မေးဖြေစနစ်ဖြင့် ပြန်လည်သုံးသပ်ရာ ကျမ်းဖြစ်သည်။",
    "ရှည်လျားသော သုတ်များအနက် သီလနှင့် ဆက်စပ်သည့် သင်ကြားချက်များ ပါဝင်သည်။",
    "ဒီဃနိကာယ်၏ အရေးပါသော သုတ်တော်များနှင့် သာသနာသမိုင်းဆိုင်ရာ အကြောင်းအရာများ ပါဝင်သည်။",
    "အယူဝါဒ၊ လူမှုဆက်ဆံရေးနှင့် ကျင့်ဝတ်ဆိုင်ရာ သုတ်တော်များ ပါဝင်သည်။",
    "အလယ်အလတ်ရှည်လျားသော သုတ်တော်များ၏ ပထမ ၅၀ သုတ်အုပ်စု ဖြစ်သည်။",
    "မဇ္ဈိမနိကာယ်၏ ဒုတိယ ၅၀ သုတ်အုပ်စု ဖြစ်သည်။",
    "မဇ္ဈိမနိကာယ်၏ နောက်ဆုံး ၅၂ သုတ်အုပ်စု ဖြစ်သည်။",
    "ဂါထာပါဝင်သည့် ဆက်စပ်သုတ်တော်အုပ်စုများ ပါဝင်သည်။",
    "ပဋိစ္စသမုပ္ပါဒ်နှင့် အကြောင်းအကျိုးဆက်စပ်မှုကို အဓိက လေ့လာနိုင်သည်။",
    "ခန္ဓာငါးပါး၊ အနိစ္စ၊ ဒုက္ခ၊ အနတ္တဆိုင်ရာ သုတ်များ ပါဝင်သည်။",
    "အာယတနခြောက်ပါး၊ အာရုံသိမှုနှင့် ဝေဒနာဆိုင်ရာ သုတ်များ ပါဝင်သည်။",
    "မဂ္ဂင်ရှစ်ပါး၊ ဗောဇ္ဈင်နှင့် ဘာဝနာဆိုင်ရာ သုတ်များ ပါဝင်သည်။",
    "တရားတစ်ပါး၊ နှစ်ပါး၊ သုံးပါးအလိုက် စုစည်းထားသော သုတ်များ ဖြစ်သည်။",
    "တရားလေးပါးစီအလိုက် စုစည်းထားသော သုတ်များ ဖြစ်သည်။",
    "တရားငါးပါးနှင့် ခြောက်ပါးအလိုက် စုစည်းထားသော သုတ်များ ဖြစ်သည်။",
    "တရားခုနစ်ပါး၊ ရှစ်ပါးနှင့် ကိုးပါးအလိုက် သုတ်များ ဖြစ်သည်။",
    "တရားဆယ်ပါးနှင့် တစ်ဆယ့်တစ်ပါးအလိုက် သုတ်များ ဖြစ်သည်။",
    "ဓမ္မပဒဂါထာ၊ မင်္ဂလသုတ်၊ မေတ္တာသုတ်စသည့် ထင်ရှားသော သုတ်များ ပါဝင်သည်။",
    "ကံအကျိုးဆိုင်ရာ ဝတ္ထုများနှင့် ထေရ/ထေရီတို့၏ ဂါထာများ ပါဝင်သည်။",
    "ဘုရားလောင်း၏ အတိတ်ဘဝ ဇာတကဝတ္ထုများ ပထမပိုင်း ဖြစ်သည်။",
    "ဘုရားလောင်း၏ အတိတ်ဘဝ ဇာတကဝတ္ထုများ ဒုတိယပိုင်း ဖြစ်သည်။",
    "သုတ္တနိပါတဆိုင်ရာ ဂါထာများကို အနက်ဖွင့်ဆိုသည့် ကျမ်းဖြစ်သည်။",
    "သုတ္တနိပါတဆိုင်ရာ ဆက်လက်အနက်ဖွင့်ကျမ်း ဖြစ်သည်။",
    "ဉာဏ်နှင့် တရားသဘောခွဲခြားမှုဆိုင်ရာ ကျမ်းဖြစ်သည်။",
    "သာသနာ့ပုဂ္ဂိုလ်များ၏ အတိတ်ကောင်းမှု ဝတ္ထုများ ပါဝင်သည်။",
    "အပဒါန်ဆက်ပိုင်း၊ ဗုဒ္ဓဝံသနှင့် ပါရမီကျင့်စဉ်ဆိုင်ရာ ကျမ်းများ ပါဝင်သည်။",
    "စိတ်၊ စေတသိက်၊ ရုပ် စသည့် တရားအမျိုးအစားများကို ခွဲခြားဖော်ပြသည်။",
    "ခန္ဓာ၊ အာယတန စသည့် အကြောင်းအရာအလိုက် တရားသဘောများကို ခွဲခြားသည်။",
    "ဓာတ်၊ တရားဆက်စပ်မှုနှင့် ပုဂ္ဂိုလ်အမျိုးအစားဆိုင်ရာ အကြောင်းအရာများ ပါဝင်သည်။",
    "အဘိဓမ္မာဆိုင်ရာ အယူအဆများကို မေးဖြေစနစ်ဖြင့် ဆန်းစစ်သည်။",
    "တရားသဘောများကို အပြန်အလှန်မေးခွန်းတွဲနည်းဖြင့် စစ်ဆေးသည်။",
    "ယမကကျမ်း၏ ဆက်ပိုင်း ဖြစ်သည်။",
    "အကြောင်းအကျိုးဆက်စပ်မှုနှင့် ပစ္စည်း ၂၄ ပါးဆိုင်ရာ အဓိကကျမ်း ဖြစ်သည်။",
    "ပဋ္ဌာန်းတရား၏ ပစ္စည်းဆက်စပ်မှုများကို ဆက်လက်ဖော်ပြသည်။",
    "ပစ္စည်းဆက်နွယ်မှုခွဲခြမ်းစိတ်ဖြာချက်များ၏ ဆက်ပိုင်း ဖြစ်သည်။",
    "ပဋ္ဌာန်းကျမ်း၏ အကြောင်းတရားဆိုင်ရာ ခွဲခြမ်းဖော်ပြချက်များ ပါဝင်သည်။",
    "ပဋ္ဌာန်းကျမ်း၏ အကျယ်ချဲ့ဆက်ပိုင်း ဖြစ်သည်။",
    "ပဋ္ဌာန်းကျမ်း၏ နောက်ဆုံးပိုင်း ဖြစ်သည်။"
  };
  private LinearLayout list;
  private String searchText="";
  private int selected=-1;
  private final int royal=0xff30234d, ink=0xff272236, muted=0xff766e7c;

  @Override public void onCreate(Bundle saved){super.onCreate(saved);show();}
  @Override public void onBackPressed(){if(selected>=0){selected=-1;show();}else super.onBackPressed();}
  private int d(int px){return (int)(px*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable rounded(int fill){
    GradientDrawable b=new GradientDrawable();b.setColor(fill);b.setCornerRadius(d(12));return b;
  }
  private TextView txt(String value,int pt,int color,boolean bold){
    TextView t=new TextView(this);t.setText(value);t.setTextSize(pt);t.setTextColor(color);
    t.setPadding(d(8),d(7),d(8),d(7));t.setLineSpacing(d(3),1.1f);
    if(bold)t.setTypeface(null,Typeface.BOLD);return t;
  }
  private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  private LinearLayout card(){
    LinearLayout c=column();c.setPadding(d(12),d(10),d(12),d(10));c.setBackground(rounded(Color.WHITE));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
    lp.setMargins(d(12),d(7),d(12),d(5));list.addView(c,lp);return c;
  }
  private String pitaka(int n){return n<=8?"ဝိနည်းပိဋကတ်":(n<=33?"သုတ္တန်ပိဋကတ်":"အဘိဓမ္မာပိဋကတ်");}
  private void show(){
    LinearLayout root=column();root.setBackgroundColor(0xfff9f7f3);setContentView(root);
    getWindow().setStatusBarColor(royal);getWindow().setNavigationBarColor(royal);
    TextView bar=txt("☸  ပိဋကတ် ၄၅ တွဲ · Offline သုတေသန",20,Color.WHITE,true);
    bar.setPadding(d(17),d(17),d(12),d(17));bar.setBackgroundColor(royal);root.addView(bar);
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    list=column();scroll.addView(list);
    if(selected>=0){detail(selected);return;}
    LinearLayout info=card();
    info.addView(txt("ဝိနည်း ၈ တွဲ · သုတ္တန် ၂၅ တွဲ · အဘိဓမ္မာ ၁၂ တွဲ",16,ink,true));
    info.addView(txt("APK ထဲတွင် တစ်ခါတည်းပါဝင်သော မြန်မာဘာသာ သုတေသနအညွှန်း ဖြစ်ပါသည်။ အင်တာနက်မလိုပါ။ ကျမ်းစာသားမူရင်းအပြည့်အစုံ မဟုတ်ပါ။",13,muted,false));
    info.addView(txt("Source: tipitaka.cloud/library/ · Tipitaka.cloud ၏ ထိုင်း ၄၅ တွဲစနစ်",12,muted,false));
    EditText search=new EditText(this);
    search.setSingleLine(true);search.setHint("ကျမ်းတွဲ၊ နိကာယ်၊ ပါဠိအမည် ရှာဖွေရန်");search.setText(searchText);
    info.addView(search);
    search.addTextChangedListener(new TextWatcher(){
      @Override public void beforeTextChanged(CharSequence s,int a,int c,int f){}
      @Override public void onTextChanged(CharSequence s,int a,int before,int count){
        searchText=s.toString();list.removeViews(1,list.getChildCount()-1);renderItems();
      }
      @Override public void afterTextChanged(Editable e){}
    });
    renderItems();
  }
  private void renderItems(){
    int count=0;String q=searchText.toLowerCase(java.util.Locale.ROOT);
    for(int i=0;i<45;i++){
      final int number=i;
      String searchable=titles[i]+" "+pali[i]+" "+summary[i]+" "+pitaka(i+1);
      if(!searchable.toLowerCase(java.util.Locale.ROOT).contains(q))continue;
      count++;LinearLayout c=card();
      c.addView(txt(String.format(java.util.Locale.ROOT,"%02d",i+1)+" · "+titles[i],16,ink,true));
      c.addView(txt(pitaka(i+1)+"   ·   "+pali[i],12,muted,false));
      c.addView(txt(summary[i],13,ink,false));
      TextView open=txt("အသေးစိတ် Offline ဖတ်ရန်  ›",13,royal,true);
      open.setOnClickListener(v->{selected=number;show();});c.addView(open);
    }
    if(count==0){
      LinearLayout c=card();c.addView(txt("ရှာဖွေမှုရလဒ် မတွေ့ပါ။",15,muted,false));
    }
  }
  private void detail(int n){
    LinearLayout c=card();c.addView(txt(pitaka(n+1)+"   ·   ကျမ်းတွဲ "+(n+1),15,muted,true));
    c.addView(txt(titles[n],23,ink,true));
    c.addView(txt(pali[n],15,royal,true));
    c.addView(txt("ကျမ်းအကြောင်းအရာ",16,ink,true));
    c.addView(txt(summary[n],18,ink,false));
    c.addView(txt("မူရင်းလင့်ခ်: https://tipitaka.cloud/vol/"+(n+1)+"/",12,muted,false));
    c.addView(txt("အရေးကြီး: ဤစာသည် သုတေသနအကျဉ်းဖြစ်သည်။ ဘုရားဟော ကျမ်းစာသား မဟုတ်ပါ။ မူရင်းစာသား၏ တည်းဖြတ်မူ/လိုင်စင်ကို အတည်ပြုပြီးမှ full offline content ထည့်သွင်းနိုင်ပါမည်။",13,0xff9a5b39,false));
    TextView back=txt("← ကျမ်းတွဲစာရင်းသို့ ပြန်ရန်",16,royal,true);
    back.setOnClickListener(v->{selected=-1;show();});c.addView(back);
  }
}