package asia.aaii.dama;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * DAMA V9 launcher. Three independent native modules: RADIO V7, LIBRARY V7, TV V6.
 * Catalog metadata is bundled offline; streaming requires connection.
 */
public final class HomeActivity extends DamaActivity {
  private static final int NIGHT=0xff061e1d, DEEP=0xff082b28,
      GOLD=0xffedc375, WHITE=0xfffff5dd, SOFT=0xffbfcec0, PANEL=0xff143d36;
  private LinearLayout root;
  private int d(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
  private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}
  private LinearLayout horizontal(){LinearLayout r=new LinearLayout(this);r.setOrientation(0);r.setGravity(Gravity.CENTER_VERTICAL);return r;}
  private LinearLayout.LayoutParams size(int w,int h){return new LinearLayout.LayoutParams(w<0?w:d(w),h<0?h:d(h));}
  private TextView label(String words,int pts,int color,boolean bold){
    TextView t=new TextView(this);t.setText(words);t.setTextColor(color);t.setTextSize(pts);
    if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
  }
  private GradientDrawable fill(int color,int radius,int stroke){
    GradientDrawable b=new GradientDrawable();b.setColor(color);b.setCornerRadius(d(radius));
    if(stroke!=0)b.setStroke(d(1),stroke);return b;
  }
  private void padding(View v,int x,int y){v.setPadding(d(x),d(y),d(x),d(y));}
  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    getWindow().setStatusBarColor(NIGHT);getWindow().setNavigationBarColor(NIGHT);
    showHome();
  }
  private void showHome(){
    root=vertical();root.setBackgroundColor(NIGHT);setContentView(root);
    if(android.os.Build.VERSION.SDK_INT>=35){root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets safe=insets.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(safe.left,safe.top,safe.right,safe.bottom);return insets;});}
    LinearLayout bar=horizontal();bar.setBackgroundColor(NIGHT);bar.setPadding(d(21),d(12),d(18),d(13));
    ImageView mark=new ImageView(this);mark.setImageResource(R.drawable.dama_official_logo_icon);mark.setScaleType(ImageView.ScaleType.FIT_CENTER);bar.addView(mark,size(49,49));
    LinearLayout identity=vertical();identity.setPadding(d(10),0,0,0);
    identity.addView(label("DAMA LIGHT",22,GOLD,true));
    identity.addView(label("ဓမ္မအလင်း",15,SOFT,true));
    bar.addView(identity,new LinearLayout.LayoutParams(0,-2,1));
    TextView index=label("V22",12,GOLD,true);index.setGravity(Gravity.CENTER);
    index.setBackground(fill(PANEL,11,0xff755d35));bar.addView(index,size(43,34));
    root.addView(bar,size(-1,78));

    ScrollView sv=new ScrollView(this);sv.setVerticalScrollBarEnabled(false);
    root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout content=vertical();content.setPadding(d(17),d(10),d(17),d(18));
    sv.addView(content);

    FrameLayout hero=new FrameLayout(this);hero.setBackground(fill(DEEP,20,0xff85663a));hero.setClipToOutline(true);
    ImageView backdrop=new ImageView(this);backdrop.setImageResource(R.drawable.dama_header);
    backdrop.setScaleType(ImageView.ScaleType.CENTER_CROP);hero.addView(backdrop,new FrameLayout.LayoutParams(-1,-1));
    View shade=new View(this);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
       new int[]{0xf5082625,0xcf092a28,0x55082a2a}));
    hero.addView(shade,new FrameLayout.LayoutParams(-1,-1));
    LinearLayout h=vertical();h.setPadding(d(21),d(18),d(14),d(14));
    h.addView(label("WELCOME TO DAMA",10,GOLD,true));
    h.addView(label("ဓမ္မနှင့်အတူ\nနေ့သစ်တစ်နေ့",23,WHITE,true));
    TextView kicker=label("LISTEN · READ · WATCH",10,GOLD,true);kicker.setPadding(0,d(8),0,0);h.addView(kicker);
    hero.addView(h,new FrameLayout.LayoutParams(-1,-1));
    content.addView(hero,size(-1,154));

    TextView header=label("သင်လေ့လာလိုသော ဓမ္မကဏ္ဍကို ရွေးပါ",15,WHITE,true);
    header.setPadding(d(3),d(21),0,d(12));content.addView(header);

    addModule(content,"01","DAMA RADIO","ဆရာတော်ကြီးများ၏ တရားတော် MP3\nနားထောင်ရန် • Player • Favorites",
      "MP3 Streaming · Offline Link Catalog",R.drawable.radio_hero,MainActivity.class,"♫");
    addModule(content,"02","DAMA LIBRARY","ဓမ္မစာအုပ်၊ ပိဋကတ်နှင့် PDF များ\nစာရေးသူအလိုက်ရှာရန် • Offline Reader",
      "2,204+ Book Links · Native PDF",R.drawable.radio_pattana,LibraryActivity.class,"▣");
    addModule(content,"03","DAMA TV","YouTube တရားတော်ဗီဒီယိုများ\nဆရာတော်၊ Category၊ Playlist ရွေးကြည့်ရန်",
      "3,264 Video Links · In-App Player",R.drawable.tv_banner,TvActivity.class,"▶");

    addModule(content,"04","DAILY DAMA VOICE","နေ့စဉ် ဓမ္မအခြေပြု စိတ်ခွန်အားပေး Reflection ၃၆၅ ပုဒ်\nToday’s Voice • Calendar • Offline TTS",
      "365 Days · Offline Reading · Burmese TTS",R.drawable.radio_pattana,DailyVoiceActivity.class,"☀");

    addModule(content,"05","SAYADAW GALLERY","ဆရာတော် ၂၂ ပါး • Photo / Illustration\nProfile • စာအုပ် • MP3 • TV", "22 Profiles · 3 Source Photos · 19 Illustration Slots",R.drawable.radio_meditation,SayadawDirectoryActivity.class,"☸");
    TextView donate=label("♡  ဓမ္မအလှူ ပံ့ပိုးကူညီရန် • DONATE BY AAII.ASIA",13,GOLD,true);
    donate.setGravity(Gravity.CENTER);donate.setPadding(d(10),d(14),d(10),d(14));donate.setBackground(fill(PANEL,13,0xffa7844b));
    LinearLayout.LayoutParams dp=size(-1,-2);dp.bottomMargin=d(12);content.addView(donate,dp);
    donate.setOnClickListener(v->DonationPopup.show(this));
    LinearLayout note=vertical();note.setBackground(fill(PANEL,13,0xff526b54));padding(note,15,14);
    note.addView(label("✦   DAMA Learning Collection",13,GOLD,true));
    TextView explanation=label("စာအုပ်နှင့် ဗီဒီယိုလင့်ခ်စာရင်းများကို Offline ကြည့်ရှုနိုင်သည်။ အသံ/ဗီဒီယို Stream လုပ်ရန် Internet လိုအပ်ပါသည်။",11,SOFT,false);
    explanation.setPadding(0,d(7),0,0);note.addView(explanation);
    LinearLayout.LayoutParams np=size(-1,-2);np.topMargin=d(13);content.addView(note,np);

    // V13: User-visible sync health; "not checked" is distinct from live verification.
    LinearLayout health=vertical();health.setPadding(d(15),d(13),d(15),d(13));
    health.setBackground(fill(PANEL,15,0xff686349));
    TextView ht=label("↻  CATALOG SYNC PROGRESS",14,GOLD,true);
    health.addView(ht);
    android.content.SharedPreferences radio=getSharedPreferences("dama_radio_preferences",MODE_PRIVATE);
    android.content.SharedPreferences library=getSharedPreferences("dama_books",MODE_PRIVATE);
    TextView mp3=label("♫  MP3 LIST  •  12,973 Offline Links",12,WHITE,true);
    mp3.setPadding(0,d(10),0,d(4));health.addView(mp3);
    TextView mp3Status=label(radio.getString("radio_sync_summary","Last Live Sync: not checked"),11,SOFT,false);
    health.addView(mp3Status);
    TextView ebook=label("▣  EBOOK LIST  •  2,204 Offline Links",12,WHITE,true);
    ebook.setPadding(0,d(10),0,d(4));health.addView(ebook);
    TextView ebookStatus=label(library.getString("ebook_sync_summary","Last Live Sync: not checked"),11,SOFT,false);
    health.addView(ebookStatus);
    TextView launch=label("MP3 ကို Sync လုပ်ရန် Radio သို့ ဝင်ပါ • EBook ကို Sync လုပ်ရန် Library သို့ ဝင်ပါ",10,GOLD,false);
    launch.setPadding(0,d(9),0,0);health.addView(launch);
    LinearLayout.LayoutParams statWrap=size(-1,-2);statWrap.topMargin=d(13);
    content.addView(health,statWrap);

    // V22 touch sound preference: animation always responds; audio is optional.
    LinearLayout interaction=horizontal();interaction.setPadding(d(14),d(12),d(14),d(12));
    interaction.setBackground(fill(PANEL,15,0xff806a49));
    LinearLayout touchInfo=vertical();
    touchInfo.addView(label("✦  TOUCH ANIMATION + SOFT SOUND",13,GOLD,true));
    touchInfo.addView(label("နှိပ်လျှင် Pop Animation • အသံပိတ်/ဖွင့်နိုင်သည်",11,SOFT,false));
    interaction.addView(touchInfo,new LinearLayout.LayoutParams(0,-2,1));
    android.widget.Switch audioSwitch=new android.widget.Switch(this);
    audioSwitch.setContentDescription("အသံအထူးပြုချက် ဖွင့် / ပိတ်");
    audioSwitch.setChecked(DamaTouchFeedback.enabled(this));
    audioSwitch.setOnCheckedChangeListener((button,enabled)->{
        DamaTouchFeedback.setEnabled(this,enabled);
        android.widget.Toast.makeText(this,enabled?"Click Sound ဖွင့်ပြီးပါပြီ":"Click Sound ပိတ်ပြီးပါပြီ",
            android.widget.Toast.LENGTH_SHORT).show();
    });
    interaction.addView(audioSwitch);
    LinearLayout.LayoutParams touchCard=size(-1,-2);touchCard.topMargin=d(13);
    content.addView(interaction,touchCard);
    FooterNav.attach(this,root,"Home");

  }
  private void addModule(LinearLayout into,String number,String title,String explanation,
      String footer,int background,Class<?> target,String glyph){
    FrameLayout frame=new FrameLayout(this);frame.setBackground(fill(PANEL,17,0xff8c744c));frame.setClipToOutline(true);
    ImageView art=new ImageView(this);art.setImageResource(background);art.setScaleType(ImageView.ScaleType.CENTER_CROP);
    frame.addView(art,new FrameLayout.LayoutParams(-1,-1));
    View overlay=new View(this);overlay.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
      new int[]{0xfb062c2b,0xe509302c,0x6a164337}));frame.addView(overlay,new FrameLayout.LayoutParams(-1,-1));
    LinearLayout info=vertical();info.setPadding(d(18),d(12),d(12),d(9));
    info.addView(label(number+"   "+glyph+"   "+title,17,GOLD,true));
    TextView body=label(explanation,12,WHITE,false);body.setLineSpacing(d(3),1f);body.setPadding(0,d(7),0,d(3));info.addView(body);
    TextView sub=label(footer,10,SOFT,false);sub.setPadding(0,d(5),0,0);info.addView(sub);
    frame.addView(info,new FrameLayout.LayoutParams(-1,-1));
    LinearLayout.LayoutParams fp=size(-1,144);fp.bottomMargin=d(12);into.addView(frame,fp);
    frame.setOnClickListener(v->startActivity(new Intent(this,target)));
    frame.setContentDescription(title+" ဖွင့်ရန်");
  }
  private void go(int index){
    if(index==1)startActivity(new Intent(this,MainActivity.class));
    else if(index==2)startActivity(new Intent(this,LibraryActivity.class));
    else if(index==3)startActivity(new Intent(this,TvActivity.class));
    else showHome();
  }
}
