package asia.aaii.learningpublic;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

/** V5.7 fully native, reflowing, small-screen AAII Learning landing page. */
public final class MobileHomeActivity extends Activity {
  private static final int NAVY=0xff03162d, PANEL=0xff082c4b, GOLD=0xffffd477,
      WHITE=0xfff3f8ff, MUTED=0xffbed0e2, STROKE=0xff94723b;
  private LinearLayout content;
  private JSONArray courses=new JSONArray(),programs=new JSONArray();
  private SoundPool sounds;
  private int soundId, screenDp, imported;
  private SharedPreferences prefs;
  private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
  private LinearLayout col(){LinearLayout x=new LinearLayout(this);x.setOrientation(1);return x;}
  private LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(0);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
  private GradientDrawable box(int bg,int stroke,int rad){
    GradientDrawable d=new GradientDrawable();d.setColor(bg);d.setCornerRadius(dp(rad));
    if(stroke!=0)d.setStroke(dp(1),stroke);return d;
  }
  private GradientDrawable gradient(){
    GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
      new int[]{0xff103d65,0xff06223f,0xff03152c});d.setCornerRadius(dp(18));
    d.setStroke(dp(1),STROKE);return d;
  }
  private GradientDrawable gold(){
    GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
      new int[]{0xffffefaf,0xffffcb5b,0xffd5a044});d.setCornerRadius(dp(13));return d;
  }
  private TextView text(String s,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);
    t.setGravity(Gravity.CENTER_VERTICAL);t.setIncludeFontPadding(true);
    if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
  }
  private void tap(View v,Runnable done){
    v.setClickable(true);v.setFocusable(true);v.setSoundEffectsEnabled(false);
    v.setOnClickListener(x->{
      x.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
      if(sounds!=null && soundId>0 && prefs.getBoolean("click_enabled",true))
        sounds.play(soundId,0.35f,0.35f,1,0,1);
      x.animate().cancel();x.setScaleX(1);x.setScaleY(1);
      x.animate().scaleX(.96f).scaleY(.96f).setDuration(70)
        .withEndAction(()->x.animate().scaleX(1).scaleY(1).setDuration(115).start()).start();
      x.postDelayed(()->{if(!isFinishing()&&!isDestroyed())done.run();},120);
    });
  }
  private TextView button(String label,boolean primary,Runnable action){
    TextView b=text(label,13,primary?NAVY:WHITE,true);
    b.setGravity(Gravity.CENTER);b.setPadding(dp(10),dp(9),dp(10),dp(9));
    b.setMinHeight(dp(48));b.setBackground(primary?gold():box(0xff124366,0xff54809b,13));
    b.setContentDescription(label);tap(b,action);return b;
  }
  private JSONArray load(String name){
    try(InputStream in=getAssets().open(name);ByteArrayOutputStream out=new ByteArrayOutputStream()){
      byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
      return new JSONArray(out.toString("UTF-8"));
    }catch(Exception ex){return new JSONArray();}
  }
  private void go(int page){go(page,0,"","",-1,0);}
  private void go(int page,int group,String provider){go(page,group,provider,"",-1,0);}
  private void go(int page,int group,String provider,String query,int index,int type){
    Intent intent=new Intent(this,MainActivity.class);
    intent.putExtra("aaii_page",index>=0?6:page);
    intent.putExtra("aaii_provider_group",group);
    intent.putExtra("aaii_provider",provider);
    intent.putExtra("aaii_search",query);
    if(index>=0){intent.putExtra("aaii_detail_index",index);intent.putExtra("aaii_detail_type",type);}
    startActivity(intent);
  }
  private void search(){
    EditText input=new EditText(this);input.setSingleLine(true);input.setHint("Course / Provider...");
    LinearLayout pane=col();pane.setPadding(dp(18),0,dp(18),0);
    pane.addView(input,new LinearLayout.LayoutParams(-1,dp(54)));
    new AlertDialog.Builder(this).setTitle("AAII Learning Search").setView(pane)
      .setPositiveButton("Search",(d,w)->go(1,0,"",input.getText().toString().trim(),-1,0))
      .setNeutralButton("Categories",(d,w)->go(16))
      .setNegativeButton("Cancel",null).show();
  }
  private void profile(){
    String[] names={"My Profile / Settings","Teacher Soe Moe Profile",
      "My Certificate Gallery","My Learning","Teacher Request"};
    int[] dest={11,9,17,12,8};
    new AlertDialog.Builder(this).setTitle("AAII Learning • Profile")
      .setItems(names,(d,n)->go(dest[n])).setNegativeButton("Close",null).show();
  }
  private LinearLayout panel(LinearLayout host){
    LinearLayout p=col();p.setPadding(dp(12),dp(12),dp(12),dp(12));
    p.setBackground(box(PANEL,STROKE,15));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
    lp.bottomMargin=dp(11);host.addView(p,lp);return p;
  }
  private void section(String title,String subtitle,Runnable action){
    LinearLayout bar=row();
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
    lp.setMargins(0,dp(11),0,dp(7));content.addView(bar,lp);
    LinearLayout head=col();bar.addView(head,new LinearLayout.LayoutParams(0,-2,1));
    head.addView(text(title,18,GOLD,true));
    if(!subtitle.isEmpty())head.addView(text(subtitle,11,MUTED,false));
    if(action!=null){
      TextView all=text("View All  ›",12,WHITE,true);all.setPadding(dp(5),dp(8),0,dp(8));
      bar.addView(all);tap(all,action);
    }
  }
  private void header(){
    LinearLayout head=panel(content);head.setBackground(gradient());
    LinearLayout brand=row();head.addView(brand);
    ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.aaii_logo_v47);
    logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
    int side=screenDp<=360?70:83;
    LinearLayout.LayoutParams image=new LinearLayout.LayoutParams(dp(side),dp(side));
    image.rightMargin=dp(8);brand.addView(logo,image);
    LinearLayout labels=col();brand.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
    labels.addView(text("AAII Learning",screenDp<=360?21:25,GOLD,true));
    labels.addView(text("Your Path to Global Learning",11,WHITE,false));
    labels.addView(text("V5.7 • Responsive Native",10,GOLD,true));
    LinearLayout controls=row();LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(48));
    cp.topMargin=dp(8);head.addView(controls,cp);
    TextView notice=button("Notifications",false,()->new AlertDialog.Builder(this)
      .setTitle("Notifications").setMessage("Server notification မချိတ်ရသေးပါ။ Local request history ကို စစ်နိုင်ပါတယ်။")
      .setPositiveButton("Requests",(d,w)->go(13)).setNegativeButton("Close",null).show());
    LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(0,-1,1);np.rightMargin=dp(8);
    controls.addView(notice,np);controls.addView(button("Profile",false,this::profile),new LinearLayout.LayoutParams(0,-1,1));
  }
  private void filters(){
    LinearLayout.LayoutParams searchParams=new LinearLayout.LayoutParams(-1,dp(55));
    searchParams.bottomMargin=dp(8);
    TextView searchButton=button("⌕   Search courses, providers, certificates...",false,this::search);
    searchButton.setBackground(box(0xff0b3554,STROKE,15));
    content.addView(searchButton,searchParams);
    HorizontalScrollView scroll=new HorizontalScrollView(this);
    scroll.setHorizontalScrollBarEnabled(false);content.addView(scroll);
    LinearLayout chips=row();scroll.addView(chips);
    String[] names={"All","Corporate","University","Learning Partners"};
    for(int i=0;i<4;i++){
      final int selected=i;
      TextView chip=button(names[i],i==0,()->go(16,selected,""));
      LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(48));p.rightMargin=dp(7);
      chips.addView(chip,p);
    }
  }
  private void metrics(){
    LinearLayout host=panel(content);
    int columns=screenDp>=600?4:2;
    String[] counts={String.valueOf(courses.length()),"37","18",String.valueOf(imported)};
    String[] labels={"Courses","Specializations","Professional","My Certificates"};
    int[] pages={1,2,3,17};
    for(int base=0;base<4;base+=columns){
      LinearLayout r=row();host.addView(r);
      for(int i=base;i<Math.min(base+columns,4);i++){
        int index=i;
        LinearLayout c=col();c.setPadding(dp(9),dp(10),dp(6),dp(7));
        c.setBackground(box(0xff103a59,0xff346482,11));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(83),1);
        if((i%columns)!=columns-1)p.rightMargin=dp(7);
        if(base>0)p.topMargin=dp(7);
        r.addView(c,p);
        c.addView(text(counts[i],21,GOLD,true));c.addView(text(labels[i],11,WHITE,false));
        tap(c,()->go(pages[index]));
      }
    }
  }
  private int logo(String provider){
    switch(provider){
      case "Google":return R.drawable.provider_google;
      case "Meta":return R.drawable.provider_meta;
      case "Microsoft":return R.drawable.provider_microsoft;
      case "Adobe":return R.drawable.provider_adobe;
      default:return 0;
    }
  }
  private int courseCount(String provider){
    int n=0;for(int i=0;i<courses.length();i++){
      JSONObject c=courses.optJSONObject(i);
      if(c!=null&&provider.equals(c.optString("provider")))n++;
    }return n;
  }
  private void providers(){
    section("Top Learning Partners","Company / Corporate သင်တန်းများ",()->go(16));
    HorizontalScrollView scroller=new HorizontalScrollView(this);
    scroller.setHorizontalScrollBarEnabled(false);content.addView(scroller);
    LinearLayout cards=row();scroller.addView(cards);
    for(String provider:new String[]{"Google","Meta","Microsoft","IBM","Amazon Web Services","Adobe"}){
      LinearLayout c=col();c.setGravity(Gravity.CENTER_HORIZONTAL);
      c.setPadding(dp(9),dp(10),dp(9),dp(10));c.setBackground(box(PANEL,STROKE,14));
      LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(117),dp(166));
      p.rightMargin=dp(7);cards.addView(c,p);
      int res=logo(provider);
      if(res!=0){
        ImageView img=new ImageView(this);img.setImageResource(res);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        c.addView(img,new LinearLayout.LayoutParams(dp(45),dp(45)));
      }else{
        TextView brand=text(provider.equals("IBM")?"IBM":"aws",23,GOLD,true);
        brand.setGravity(Gravity.CENTER);c.addView(brand,new LinearLayout.LayoutParams(-1,dp(45)));
      }
      TextView label=text(provider.equals("Amazon Web Services")?"AWS":provider,13,WHITE,true);
      label.setGravity(Gravity.CENTER);c.addView(label,new LinearLayout.LayoutParams(-1,dp(31)));
      TextView count=text(courseCount(provider)+" courses",11,GOLD,true);
      count.setGravity(Gravity.CENTER);c.addView(count);
      TextView open=button("View Courses",true,()->go(1,1,provider));
      LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(43));bp.topMargin=dp(8);
      c.addView(open,bp);tap(c,()->go(1,1,provider));
    }
  }
  private void quickAction(LinearLayout row,String glyph,String name,String hint,Runnable next){
    LinearLayout box=col();box.setPadding(dp(9),dp(9),dp(9),dp(9));
    box.setBackground(box(PANEL,STROKE,14));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(94),1);
    p.rightMargin=dp(7);row.addView(box,p);
    box.addView(text(glyph+"  "+name,13,GOLD,true));
    box.addView(text(hint,11,MUTED,false));tap(box,next);
  }
  private void quickLinks(){
    section("Quick Access","အမြန်ဖွင့်နိုင်သော Services",null);
    String[] names={"All Courses","My Certificates","Teacher Request",
      "Universities","Corporate","Partners"};
    String[] glyph={"▣","▤","♙","▥","▦","♧"};
    String[] hints={"337 courses","Gallery & Verify","Hire a Teacher",
      "University courses","Company courses","Learning providers"};
    int[] pages={1,17,8,16,16,16}, groups={0,0,0,2,1,3};
    int cols=screenDp>=600?3:2;
    for(int base=0;base<names.length;base+=cols){
      LinearLayout r=row();LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);
      rp.bottomMargin=dp(7);content.addView(r,rp);
      for(int i=base;i<Math.min(base+cols,names.length);i++){
        final int n=i;
        quickAction(r,glyph[i],names[i],hints[i],()->go(pages[n],groups[n],""));
      }
    }
  }
  private void ai(){
    LinearLayout p=panel(content);
    p.addView(text("✦ AI Learning Assistant",17,GOLD,true));
    p.addView(text("Study guidance • Exercises • Learning support",12,WHITE,false));
    LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(-1,dp(48));l.topMargin=dp(9);
    p.addView(button("Start Learning  ›",true,()->go(15)),l);
    p.addView(text("Some AI tools are local demonstrations; live AI API may be unavailable.",10,MUTED,false));
  }
  private void programs(){
    section("Featured Coursera Programs","Program Detail နှင့် Teacher Certificate Links",()->go(3));
    String[] chosen={"Google AI","Google Digital Marketing & E-commerce",
      "Google Project Management","Digital Transformation Using AI/ML with Google Cloud"};
    for(String title:chosen){
      for(int i=0;i<programs.length();i++){
        JSONObject p=programs.optJSONObject(i);
        if(p==null||!title.equalsIgnoreCase(p.optString("title")))continue;
        int index=i,type="Specialization".equalsIgnoreCase(p.optString("type"))?2:3;
        LinearLayout card=row();card.setPadding(dp(10),dp(8),dp(8),dp(8));
        card.setBackground(box(PANEL,0xff305c7c,13));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(8);content.addView(card,lp);
        ImageView logo=new ImageView(this);
        logo.setImageResource("Google Cloud".equals(p.optString("provider"))?
          R.drawable.provider_googlecloud:R.drawable.provider_google);
        logo.setPadding(dp(6),dp(6),dp(6),dp(6));
        logo.setBackground(box(WHITE,0,9));
        LinearLayout.LayoutParams im=new LinearLayout.LayoutParams(dp(47),dp(47));
        im.rightMargin=dp(10);card.addView(logo,im);
        LinearLayout desc=col();card.addView(desc,new LinearLayout.LayoutParams(0,-2,1));
        desc.addView(text(p.optString("title"),13,WHITE,true));
        desc.addView(text(p.optString("provider")+" • "+p.optString("type"),10,MUTED,false));
        card.addView(text("  ›",24,GOLD,true));
        tap(card,()->go(6,0,"","",index,type));break;
      }
    }
  }
  private void teacher(){
    LinearLayout panel=panel(content);
    panel.addView(text("Teacher Soe Moe • Certificates",17,GOLD,true));
    panel.addView(text("Course တစ်ခုချင်းစီတွင် ဆရာစိုးမိုး၏ Private Certificate Wallet မှ ကိုက်ညီသည့်လင့်ခ်ကိုသာ ချိတ်ပြပါမည်။",12,WHITE,false));
    panel.addView(text(imported+" certificate links imported on this phone",11,MUTED,false));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(49));p.topMargin=dp(9);
    panel.addView(button("My Certificate Wallet / Import",true,()->go(5)),p);
    LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,dp(49));q.topMargin=dp(8);
    panel.addView(button("Teacher Profile / Booking",false,()->go(9)),q);
  }
  private void footer(LinearLayout root){
    LinearLayout bar=row();bar.setPadding(dp(3),dp(3),dp(3),dp(5));
    bar.setBackground(box(0xff04182e,STROKE,13));
    root.addView(bar,new LinearLayout.LayoutParams(-1,dp(70)));
    String[] title={"Home","Categories","AI Tools","Services","Profile"};
    String[] icon={"⌂","▦","♟","♧","◉"};
    int[] dest={0,16,15,14,11};
    for(int i=0;i<5;i++){
      int n=i;LinearLayout tab=col();tab.setGravity(Gravity.CENTER);
      if(i==0)tab.setBackground(box(0xff493922,0xffa37d43,12));
      tab.setMinimumHeight(dp(48));
      bar.addView(tab,new LinearLayout.LayoutParams(0,-1,1));
      TextView symbol=text(icon[i],24,i==0?GOLD:WHITE,true);
      symbol.setGravity(Gravity.CENTER);tab.addView(symbol);
      TextView word=text(title[i],screenDp<360?10:11,i==0?GOLD:WHITE,i==0);
      word.setGravity(Gravity.CENTER);tab.addView(word);
      tab.setContentDescription(title[i]+" navigation");
      tap(tab,()->{if(n==4)profile();else if(n>0)go(dest[n]);});
    }
  }
  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().setStatusBarColor(NAVY);getWindow().setNavigationBarColor(NAVY);
    screenDp=Math.round(getResources().getDisplayMetrics().widthPixels/
      getResources().getDisplayMetrics().density);
    prefs=getSharedPreferences("AAII_Learning_Study_V31",MODE_PRIVATE);
    courses=load("courses.json");programs=load("programs_public.json");
    try{imported=new JSONArray(prefs.getString("personal_credentials","[]")).length();}
    catch(Exception ignored){}
    sounds=new SoundPool.Builder().setMaxStreams(2).build();
    soundId=sounds.load(this,R.raw.aaii_click,1);
    LinearLayout root=col();root.setFitsSystemWindows(true);
    root.setBackgroundColor(NAVY);setContentView(root);
    ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    content=col();content.setPadding(dp(12),dp(9),dp(12),dp(14));
    scroll.addView(content);
    header();filters();metrics();providers();quickLinks();ai();programs();teacher();
    footer(root);
  }
  @Override protected void onDestroy(){
    if(sounds!=null){sounds.release();sounds=null;}
    super.onDestroy();
  }
}