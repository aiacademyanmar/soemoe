package asia.aaii.learningpublic;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
  private final int NAVY=0xff082b53, TEAL=0xff0969e8, GOLD=0xfff5be42, INK=0xff17324c, MUTED=0xff64748b, BG=0xfff3f8fe, WHITE=0xffffffff;
  private final String[] LABELS={"Home","Courses","Specialize","Professional","Teachers","Services","Profile"};
  private final String[] ICONS={"⌂","▣","◈","✦","♙","▦","◉"};
  private final int[] FOOTER_ROUTES={0,1,2,3,9,14,11};
  private JSONArray courses=new JSONArray(), programs=new JSONArray(),roadmaps=new JSONArray(),teachers=new JSONArray(),certs=new JSONArray();
  private SharedPreferences prefs;
  private LinearLayout root,body,footer;
  private int page=0,detailType=0,detailIndex=0,limit=35;
  private String bookingDate="ရက်ရွေးပါ", bookingTime="အချိန်ရွေးပါ";
  private String search="",category="အားလုံး";
  private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  private GradientDrawable gradient(int a,int b,int r){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(r));return d;}
  private LinearLayout coloredCard(LinearLayout parent,int a,int b){
    LinearLayout c=card(parent);c.setBackground(gradient(a,b,18));return c;
  }
  private void screenLabel(String title,String subtitle){
    LinearLayout c=coloredCard(body,0xff0c3763,0xff0f64a5);
    line(c,title,21,WHITE,true);line(c,subtitle,12,0xffdeecfb,false);
  }
  private void actionTile(LinearLayout parent,String bigIcon,String title,String subtitle,int tint,Runnable action){
    LinearLayout box=vertical();box.setPadding(dp(10),dp(11),dp(10),dp(11));box.setBackground(shape(tint,15));
    TextView icon=txt(bigIcon,25,NAVY,true);box.addView(icon);
    line(box,title,14,NAVY,true);line(box,subtitle,11,INK,false);
    parent.addView(box,new LinearLayout.LayoutParams(0,-2,1));box.setOnClickListener(v->action.run());
  }
  private TextView txt(String s,int size,int color,boolean strong){
    TextView v=new TextView(this);v.setText(s);v.setTextColor(color);v.setTextSize(size);v.setIncludeFontPadding(true);
    if(strong)v.setTypeface(null,Typeface.BOLD);v.setPadding(dp(5),dp(5),dp(5),dp(5));return v;
  }
  private JSONArray load(String path){try(InputStream in=getAssets().open(path)){
    ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;
    while((n=in.read(buf))!=-1)b.write(buf,0,n);return new JSONArray(b.toString("UTF-8"));
  }catch(Exception ex){return new JSONArray();}}
  @Override public void onCreate(Bundle b){super.onCreate(b);
    getWindow().setStatusBarColor(NAVY);getWindow().setNavigationBarColor(NAVY);
    prefs=getSharedPreferences("AAII_Learning_Study_V31",MODE_PRIVATE);
    courses=load("courses.json");programs=load("programs_public.json");roadmaps=load("roadmaps.json");teachers=load("teacher_paths.json");
    try{certs=new JSONArray(prefs.getString("personal_credentials","[]"));}catch(Exception ignored){}render();
  }
  private LinearLayout vertical(){LinearLayout x=new LinearLayout(this);x.setOrientation(1);return x;}
  private void openTab(int p){page=p;search="";category="အားလုံး";limit=35;render();}
  private void inspect(int type,int index){detailType=type;detailIndex=index;page=6;render();}
  private void pillRow(LinearLayout target,String[] names,int[] pages){
    HorizontalScrollView h=new HorizontalScrollView(this);h.setHorizontalScrollBarEnabled(false);
    LinearLayout row=new LinearLayout(this);row.setOrientation(0);h.addView(row);
    for(int i=0;i<names.length;i++){final int destination=pages[i];TextView t=txt(names[i],12,TEAL,true);
      t.setPadding(dp(13),dp(11),dp(13),dp(11));t.setBackground(shape(0xffeaf3ff,18));
      LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,0,dp(7),0);row.addView(t,p);
      t.setOnClickListener(v->openTab(destination));
    }
    target.addView(h);
  }
  private void statTiles(LinearLayout parent){
    LinearLayout row=new LinearLayout(this);row.setOrientation(0);parent.addView(row);
    String[] nums={"337","37","18","6"};String[] captions={"Courses","Specialize","Professional","Teacher Paths"};
    int[] backgrounds={0xffe0f8ef,0xffe5f0ff,0xffffefd1,0xfff0e4ff};
    for(int i=0;i<4;i++){LinearLayout tile=vertical();tile.setPadding(dp(4),dp(9),dp(4),dp(9));
      tile.setGravity(Gravity.CENTER);tile.setBackground(shape(backgrounds[i],12));
      line(tile,nums[i],19,NAVY,true);line(tile,captions[i],9,INK,false);
      LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(2),0,dp(2),0);row.addView(tile,p);
    }
  }
  private LinearLayout card(LinearLayout container){
    LinearLayout x=vertical();x.setPadding(dp(14),dp(12),dp(14),dp(12));x.setBackground(shape(WHITE,16));x.setElevation(dp(2));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(11));container.addView(x,lp);return x;
  }
  private void line(LinearLayout target,String content,int size,int color,boolean bold){target.addView(txt(content,size,color,bold));}
  private void button(LinearLayout target,String label,Runnable onClick,boolean primary){
    TextView b=txt(label,14,primary?WHITE:TEAL,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(11),dp(12),dp(11),dp(12));
    b.setBackground(shape(primary?TEAL:0xffe5f5f4,12));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(7),0,0);target.addView(b,p);
    b.setOnClickListener(v->onClick.run());
  }
  private void browse(String url){
    try{Uri u=Uri.parse(url);if(!"https".equalsIgnoreCase(u.getScheme()) || !"www.coursera.org".equalsIgnoreCase(u.getHost()))throw new Exception("URL invalid");
      startActivity(new Intent(Intent.ACTION_VIEW,u));
    }catch(Exception ex){Toast.makeText(this,"Coursera Official Link မဖွင့်နိုင်ပါ",Toast.LENGTH_LONG).show();}
  }
  private void heading(String h,String sub){body.addView(txt(h,22,NAVY,true));if(!sub.isEmpty())body.addView(txt(sub,13,MUTED,false));}
  private String typeOf(int type){return type==1?"Course":type==2?"Specialization":type==3?"Professional Certificate":"Teacher Training";}
  private String overview(String category,String title){
    String s=(category+" "+title).toLowerCase(Locale.ROOT);
    if(s.contains("cyber")||s.contains("security"))return "Cybersecurity ဆိုသည်မှာ ကွန်ပျူတာ၊ Network နှင့် Data ကို အန္တရာယ်ရှိသောတိုက်ခိုက်မှုမှ ကာကွယ်ခြင်းဖြစ်သည်။ သင်ယူရာတွင် Risk Assessment၊ Security Controls၊ Incident Response နှင့် တာဝန်ယူမှုရှိသော လုံခြုံရေးစီမံခန့်ခွဲမှုကို အလေးထားပါ။";
    if(s.contains("artificial intelligence")||s.contains("ai ")||s.contains("prompt")||s.contains("generative"))return "Artificial Intelligence (AI) သည် Data မှ Pattern နှင့် ဆက်စပ်မှုများကိုအသုံးပြုကာ လုပ်ငန်းများကို ကူညီလုပ်ဆောင်ပေးသည့်နည်းပညာဖြစ်သည်။ Prompt ရေးခြင်း၊ အချက်အလက်တိကျမှုစစ်ခြင်း၊ AI မမှန်ကန်မှုနှင့် Privacy ကိုထိန်းခြင်းတို့ကို အဓိကထား လေ့လာပါ။";
    if(s.contains("analytics")||s.contains("data")||s.contains("intelligence"))return "Data Analysis ဆိုသည်မှာ အချက်အလက်စုဆောင်းခြင်း၊ သန့်စင်ခြင်း၊ SQL/Spreadsheet ဖြင့်ခွဲခြမ်းစိတ်ဖြာခြင်း၊ Chart/Dashboard များဖြင့် ဆုံးဖြတ်ချက်ချရန် ထောက်ပံ့ခြင်းတို့ဖြစ်သည်။";
    if(s.contains("project")||s.contains("agile")||s.contains("scrum"))return "Project Management သည် ရည်မှန်းချက်၊ အချိန်၊ ဘတ်ဂျက်၊ တာဝန်ခွဲဝေမှု၊ Risk နှင့် Quality ကို စီမံသည့်ပညာရပ်ဖြစ်သည်။ Project Charter၊ Timeline၊ Risk Register နှင့် Agile/Scrum လေ့ကျင့်ခန်းများ လုပ်ပါ။";
    if(s.contains("ux")||s.contains("design")||s.contains("user interface"))return "UI/UX Design သည် အသုံးပြုသူလိုအပ်ချက်ကို လေ့လာပြီး User Flow၊ Wireframe၊ Prototype နှင့် Usability Test ဖြင့် အသုံးပြုရလွယ်ကူသော Product တည်ဆောက်သည့်နည်းလမ်း ဖြစ်သည်။";
    if(s.contains("cloud")||s.contains("azure")||s.contains("aws"))return "Cloud Computing ဆိုသည်မှာ Internet မှတစ်ဆင့် Server၊ Storage နှင့် Computing Service များကို စီမံအသုံးပြုခြင်းဖြစ်သည်။ Access Control၊ Billing၊ Security၊ Deployment နှင့် Monitoring ကို လေ့လာပါ။";
    if(s.contains("marketing")||s.contains("e-commerce")||s.contains("advert"))return "Digital Marketing သည် Online Customer များထံ သင့်တော်သည့် Content နှင့် Campaign များဖြင့် ရောက်ရှိစေရန် လုပ်ဆောင်ခြင်းဖြစ်သည်။ Customer Journey၊ SEO၊ Ads၊ Analytics နှင့် Ethics ကို နားလည်ရပါမည်။";
    if(s.contains("python")||s.contains("program")||s.contains("developer")||s.contains("coding"))return "Programming သည် ပြဿနာဖြေရှင်းရန် Algorithm နှင့် Code များရေးသားခြင်း ဖြစ်သည်။ Variable၊ Function၊ Data Structure၊ Testing နှင့် Automation ကို လေ့ကျင့်ပါ။";
    if(s.contains("teach")||s.contains("education")||s.contains("learning"))return "သင်ကြားရေးပညာရပ်တွင် Learning Objectives၊ Lesson Plan၊ သင်ယူသူလိုအပ်ချက်၊ Classroom Interaction၊ Assessment နှင့် Feedback များကို အခြေခံ၍ အရည်အသွေးမြင့် သင်ခန်းစာဖန်တီးပေးရသည်။";
    if(s.contains("business")||s.contains("entrepren")||s.contains("leadership"))return "Business Management သည် လုပ်ငန်းရည်မှန်းချက်၊ လူအင်အား၊ ငွေကြေး၊ Customer နှင့် Performance ကို စနစ်တကျ စီမံခန့်ခွဲခြင်းဖြစ်သည်။ Business Plan နှင့် KPI လေ့ကျင့်ခန်း ပြုလုပ်ပါ။";
    return "ဤဘာသာရပ်၏ အဓိကသဘောတရား၊ အသုံးပြုပုံ၊ လုပ်ငန်းခွင်ဥပမာနှင့် လက်တွေ့ Project တို့ကို စနစ်တကျလေ့လာပါ။ သင်ခန်းစာအစိတ်အပိုင်းအတိအကျကို မူရင်း Coursera စာမျက်နှာမှ ထပ်မံစစ်ဆေးပါ။";
  }
  private String exercise(String cat,String title){
    String s=(cat+" "+title).toLowerCase(Locale.ROOT);
    if(s.contains("data")||s.contains("analyt"))return "CSV Data ၁၀၀ Row ကို သန့်စင်ပြီး Chart တစ်ခုထုတ်ပါ။ ရလာဒ်ကို မြန်မာလို ၅ ကြောင်းရှင်းပြပါ။";
    if(s.contains("project")||s.contains("agile"))return "Project Charter၊ Task List၊ Timeline နှင့် Risk Register တစ်ခုစီရေးပါ။";
    if(s.contains("cyber")||s.contains("security"))return "ကိုယ်ပိုင် Lab အတွက် Risk Assessment နှင့် Incident Response Checklist တည်ဆောက်ပါ။";
    if(s.contains("design")||s.contains("ux"))return "Student App Screen ၃ ခုကို Wireframe ဆွဲပြီး User Test ၃ ဦး၏ Feedback မှတ်တမ်းတင်ပါ။";
    if(s.contains("ai")||s.contains("prompt"))return "AI Prompt ၃ မျိုးနှိုင်းယှဉ်ပြီး Source Verification Checklist ဖြင့် အဖြေမှန်ကန်မှုကို စစ်ဆေးပါ။";
    if(s.contains("teach")||s.contains("educat"))return "Learning Objective၊ Activity၊ Quiz၊ Rubric ပါဝင်သော 30-minute Lesson Plan ရေးပါ။";
    return "အဓိကအသုံးအနှုန်း ၅ ခုကို သတ်မှတ်ပြီး ကိုယ်ပိုင်လုပ်ငန်းခွင်ဥပမာတစ်ခုနှင့် ရှင်းပြပါ။";
  }
  
  private void saveLocalDraft(String kind,String subject){
    try{
      String key="aaii_request_drafts";
      JSONArray arr=new JSONArray(prefs.getString(key,"[]"));
      JSONObject obj=new JSONObject();
      obj.put("type",kind);obj.put("subject",subject);obj.put("state","Local draft • not submitted");
      obj.put("date",new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()));
      arr.put(obj);
      if(arr.length()>100){
        JSONArray trimmed=new JSONArray();for(int i=Math.max(0,arr.length()-100);i<arr.length();i++)trimmed.put(arr.getJSONObject(i));arr=trimmed;
      }
      prefs.edit().putString(key,arr.toString()).apply();
    }catch(Exception ex){android.util.Log.w("AAII","Draft not saved",ex);}
  }
  private JSONArray drafts(){
    try{return new JSONArray(prefs.getString("aaii_request_drafts","[]"));}catch(Exception e){return new JSONArray();}
  }
  private int reviewedCourses(){
    int n=0;for(int i=0;i<courses.length();i++)if(prefs.getBoolean("course_"+i,false))n++;return n;
  }
  private void progressStrip(LinearLayout parent,String label,int count,int total){
    LinearLayout card=card(parent);
    line(card,label+"   "+count+" / "+total,14,INK,true);
    ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
    bar.setMax(Math.max(1,total));bar.setProgress(count);card.addView(bar,new LinearLayout.LayoutParams(-1,dp(6)));
  }
  private android.widget.ImageView portrait(int width,int height){
    android.widget.ImageView v=new android.widget.ImageView(this);
    v.setImageResource(R.drawable.soe_moe_original);
    v.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
    v.setLayoutParams(new LinearLayout.LayoutParams(dp(width),dp(height)));
    v.setBackground(shape(0xffebf4fa,14));
    v.setClipToOutline(true);
    return v;
  }
  private void portraitCard(LinearLayout target, String headline, String description){
    LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);
    row.addView(portrait(100,112));
    LinearLayout bodyText=vertical();bodyText.setPadding(dp(12),0,0,0);
    row.addView(bodyText,new LinearLayout.LayoutParams(0,-2,1));
    line(bodyText,headline,18,NAVY,true);
    line(bodyText,description,12,INK,false);
    target.addView(row);
  }
  private void render(){
    root=vertical();root.setBackgroundColor(BG);root.setFitsSystemWindows(true);setContentView(root);
    LinearLayout head=vertical();head.setPadding(dp(16),dp(8),dp(16),dp(8));head.setBackgroundColor(NAVY);root.addView(head);
    line(head,"✦  AAII Learning",23,GOLD,true);
    line(head,"LEARN TODAY • BUILD TOMORROW",10,0xffcfe8fe,false);
    LinearLayout links=new LinearLayout(this);links.setOrientation(0);head.addView(links);
    TextView learningLink=txt("◉ My Learning",12,GOLD,true);links.addView(learningLink,new LinearLayout.LayoutParams(0,dp(36),1));
    learningLink.setOnClickListener(v->openTab(12));
    TextView requestLink=txt("▤ Requests",12,WHITE,true);links.addView(requestLink,new LinearLayout.LayoutParams(0,dp(36),1));
    requestLink.setOnClickListener(v->openTab(13));
    TextView profileLink=txt("⚙ Profile",12,WHITE,true);links.addView(profileLink,new LinearLayout.LayoutParams(0,dp(36),1));
    profileLink.setOnClickListener(v->openTab(11));
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    body=vertical();body.setPadding(dp(13),dp(12),dp(13),dp(14));scroll.addView(body);
    if(page==0)home();else if(page==1)courseList();else if(page==2)programList("Specialization");else if(page==3)programList("Professional Certificate");else if(page==4)teacher();else if(page==5)wallet();else if(page==7)enrollmentService();else if(page==8)hireTeacherService();else if(page==9)teacherProfile();else if(page==10)bookingForm();else if(page==11)settingsPage();else if(page==12)myLearning();else if(page==13)myRequests();else if(page==14)servicesHub();else detail();
    footer=new LinearLayout(this);footer.setOrientation(LinearLayout.HORIZONTAL);footer.setBackgroundColor(WHITE);
    footer.setPadding(dp(5),dp(4),dp(5),dp(9));footer.setElevation(dp(8));
    int highlight=page==6?(detailType==1?1:(detailType==2?2:3)):
      (page==9||page==10||page==4?4:(page==7||page==8||page==13||page==14?5:
      (page==11||page==12||page==5?6:page)));
    for(int i=0;i<LABELS.length;i++){
      final int tab=FOOTER_ROUTES[i];LinearLayout item=vertical();item.setGravity(Gravity.CENTER);
      if(highlight==i)item.setBackground(shape(0xffe8f3ff,12));
      TextView icon=txt(ICONS[i],19,i==highlight?TEAL:MUTED,true);icon.setGravity(Gravity.CENTER);
      icon.setPadding(0,0,0,0);item.addView(icon);
      TextView label=txt(LABELS[i],9,i==highlight?TEAL:MUTED,i==highlight);
      label.setGravity(Gravity.CENTER);label.setPadding(0,dp(3),0,0);label.setMaxLines(1);label.setSingleLine(true);
      item.addView(label);footer.addView(item,new LinearLayout.LayoutParams(0,dp(62),1));item.setOnClickListener(v->openTab(tab));
    }
    root.addView(footer,new LinearLayout.LayoutParams(-1,dp(76)));
  }
  private void home(){
    LinearLayout hero=coloredCard(body,NAVY,0xff075e9e);
    line(hero,"✦  GLOBAL SKILLS  •  BURMESE GUIDANCE",12,GOLD,true);
    line(hero,"နိုင်ငံတကာ သင်ယူမှုလမ်းကြောင်း",21,WHITE,true);
    line(hero,"Coursera ၏ သင်တန်းစာမျက်နှာများဆီသို့ တိုက်ရိုက်သွားရောက်လေ့လာနိုင်ပြီး AAII မှ မြန်မာဘာသာဖြင့် အကြောင်းအရာများကို ရှင်းပြထားသည်။",14,0xffe3f2f2,false);
    line(hero,"Explore • Learn • Practice • Build your career",13,GOLD,true);
    button(hero,"🔎  Courses / Skills ရှာဖွေလေ့လာမည်",()->openTab(1),true);
    statTiles(body);
    LinearLayout quickServices=new LinearLayout(this);quickServices.setOrientation(0);
    LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,-2);q.setMargins(0,dp(12),0,dp(8));body.addView(quickServices,q);
    actionTile(quickServices,"✉","Coursera Enrollment","လျှောက်ထားရေးအကူအညီ",0xffe7f1ff,()->openTab(7));
    android.widget.Space between=new android.widget.Space(this);quickServices.addView(between,new LinearLayout.LayoutParams(dp(8),dp(1)));
    actionTile(quickServices,"♙","Hire a Teacher","ဆရာငှားရန် Request",0xffe2f7ed,()->openTab(8));
    LinearLayout teacherHero=card(body);
    portraitCard(teacherHero,"Teacher Soe Moe • ဆရာ စိုးမိုး","Original portrait • Coursera learning guidance • AI, Data, Business, IT");
    line(teacherHero,"AI • Data Science • Python • IT • Business • Digital Education",12,TEAL,true);
    button(teacherHero,"Teacher Profile & Portfolio",()->openTab(9),false);
    heading("SERVICES • AAII ဝန်ဆောင်မှုများ","Enrollment Help  •  Hire a Teacher");
    pillRow(body,new String[]{"Enrollment Assistance","Hire Teacher","My Certificates","My Learning"},new int[]{7,8,5,12});
    LinearLayout enrollment=card(body);
    line(enrollment,"01  COURSERA ENROLLMENT ASSISTANCE",15,TEAL,true);
    line(enrollment,"မြန်မာလို သင်တန်းရွေးချယ်မှု၊ Course/Specialization/Professional Certificate ကွာခြားချက်၊ သင်တန်းကြေး၊ Financial Aid ရရှိနိုင်မှုနှင့် ကိုယ်တိုင် Enrollment လုပ်နည်းကို ကူညီလမ်းညွှန်ပေးရန် Request တင်နိုင်သည်။",13,INK,false);
    button(enrollment,"Coursera သင်တန်းလျှောက်ရန် အကူအညီတောင်းမည်",()->openTab(7),true);
    LinearLayout hiring=card(body);
    line(hiring,"02  HIRE A TEACHER • ဆရာငှားရမ်းရန်",15,TEAL,true);
    line(hiring,"ဘာသာရပ်၊ Online/Offline၊ အတန်းအဆင့်၊ အချိန်ဇယားနှင့် Budget အလိုက် ဆရာရှာဖွေရန် Request ပြုလုပ်နိုင်သည်။ ဆရာ/ကျောင်းသား Matching ကို AAII က သီးခြားအတည်ပြုပေးရမည်။",13,INK,false);
    button(hiring,"ဆရာငှားရန် Request တင်မည်",()->openTab(8),true);
    button(hiring,"Teacher Soe Moe — Profile နှင့် Booking",()->openTab(9),false);
    LinearLayout personal=card(body);line(personal,"My Learning • Certificate Wallet • Requests",17,NAVY,true);
    line(personal,"သင်ယူပြီးစီးမှု၊ ပညာရေးလက်မှတ်များနှင့် ကိုယ်တိုင်ပေးပို့ရန် ပြင်ထားသော Service Request Draft များကို ဖုန်းထဲမှာ ကြည့်နိုင်သည်။",13,INK,false);
    button(personal,"My Learning Dashboard",()->openTab(12),false);
    button(personal,"Profile & Settings",()->openTab(11),false);
    heading("POPULAR LEARNING PATHS","ဘာသာရပ်အလိုက် သင်ယူမှုလမ်းကြောင်း");
    pillRow(body,new String[]{"AI & Data","Business","Teaching","IT & Computer"},new int[]{1,3,4,1});
    sectionHome("01","COURSE / သင်တန်း","ဘာသာရပ်တစ်ခုချင်းကို သီးခြားလေ့လာခြင်း၊ အကြောင်းအရာ၊ Practical Task၊ Coursera Link",()->openTab(1));
    sectionHome("02","SPECIALIZATION / အထူးပြု","ဆက်စပ်ဘာသာရပ်အစုံကို လေ့လာ၍ Project နှင့် နည်းပညာဆိုင်ရာအထူးပြုကျွမ်းကျင်မှု တည်ဆောက်ခြင်း",()->openTab(2));
    sectionHome("03","PROFESSIONAL CERTIFICATE","အလုပ်အကိုင်အတွက် Skill Pathway၊ Project၊ လေ့ကျင့်ခန်းနှင့် Official Program Link",()->openTab(3));
    sectionHome("04","TEACHER TRAINING","ဆရာဖြစ်ရန်သင်ယူရမည့် Online Teaching / TESOL / Pedagogy သင်တန်းများ",()->openTab(4));
    LinearLayout x=card(body);line(x,"ကိုယ်ပိုင်လက်မှတ်များ",17,INK,true);
    line(x,"ယခု Public APK ထဲတွင် ကိုယ်ပိုင် Coursera Certificate ID မထည့်ထားပါ။ Private Wallet သို့ ဖုန်းထဲမှ Import လုပ်နိုင်သည်။",13,MUTED,false);
    button(x,"My Certificate Wallet",()->openTab(5),false);
  }
  private void sectionHome(String no,String name,String desc,Runnable open){
    LinearLayout x=card(body);line(x,no+"    "+name,17,NAVY,true);line(x,desc,13,INK,false);button(x,"အသေးစိတ်ကြည့်မည်",open,false);
  }
  private void courseList(){
    screenLabel("COURSES • 337","ဘာသာရပ်အလိုက် မြန်မာလိုရှင်းပြချက် • Search & Practice");
    LinearLayout titleBanner=card(body);titleBanner.setBackground(shape(0xffe4f1ff,15));
    line(titleBanner,"337 COURSES • EXPLORE YOUR FUTURE",17,NAVY,true);
    line(titleBanner,"Browse courses by interest, then tap a card for Myanmar explanations and guided exercises.",12,INK,false);
    pillRow(titleBanner,new String[]{"AI & Data","Business","Teacher","Professional"},new int[]{1,3,4,3});
    heading("COURSES • "+courses.length(),"ဘာသာရပ်တစ်ခုချင်းကို မြန်မာလို အဓိပ္ပာယ်၊ လေ့ကျင့်ခန်းနှင့်တကွ ရှင်းပြထားသည်။");
    EditText searchBox=new EditText(this);searchBox.setSingleLine(true);searchBox.setTextSize(15);searchBox.setHint("Search Course / ဘာသာရပ်ရှာရန်");
    searchBox.setText(search);searchBox.setBackground(shape(WHITE,12));searchBox.setPadding(dp(14),dp(14),dp(14),dp(14));
    body.addView(searchBox,new LinearLayout.LayoutParams(-1,dp(52)));
    LinearLayout list=vertical();body.addView(list);
    Runnable fill=()->{
      list.removeAllViews();int matched=0,shown=0;
      for(int i=0;i<courses.length();i++){JSONObject c=courses.optJSONObject(i);if(c==null)continue;
        String name=c.optString("title"),cat=c.optString("category"),provider=c.optString("provider");
        if(!(name+" "+cat+" "+provider+" "+c.optString("program")).toLowerCase(Locale.ROOT).contains(search.trim().toLowerCase(Locale.ROOT)))continue;
        matched++;if(shown>=limit)continue;shown++;
        final int id=i;LinearLayout x=card(list);line(x,name,16,NAVY,true);line(x,provider+"  •  "+cat,12,TEAL,false);
        String preview=overview(cat,name);line(x,preview.length()>95?preview.substring(0,95)+"…":preview,13,INK,false);
        button(x,"မြန်မာလို အသေးစိတ်ရှင်းပြချက်",()->inspect(1,id),false);
      }
      if(matched>limit)button(list,"နောက်ထပ် Courses ပြရန် ("+(matched-limit)+")",()->{limit+=35;render();},true);
      list.addView(txt("တွေ့ရှိမှု "+matched+" ခု",12,MUTED,false),0);
    };
    searchBox.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}
      public void onTextChanged(CharSequence s,int st,int before,int count){search=s.toString();limit=35;fill.run();}
      public void afterTextChanged(Editable e){}});
    fill.run();
  }
  private void programList(String type){
    if("Specialization".equals(type))screenLabel("SPECIALIZATIONS • 37","Multiple connected courses • Guided learning paths");
    else screenLabel("PROFESSIONAL CERTIFICATES • 18","Job-ready skills • Real-world project roadmaps");
    boolean spec="Specialization".equals(type);heading(spec?"SPECIALIZATIONS • 37":"PROFESSIONAL CERTIFICATES • 18",
      spec?"ဆက်စပ် Courses များဖြင့် နက်ရှိုင်းစွာ အထူးပြုသင်ယူနိုင်သည်။":"လုပ်ငန်းခွင်နှင့် Career အတွက် ရည်ရွယ်သည့် Professional Learning Pathway များ။");
    int j=0;for(int i=0;i<programs.length();i++){JSONObject p=programs.optJSONObject(i);
      if(p==null||!type.equals(p.optString("type")))continue;final int id=i;j++;
      LinearLayout x=card(body);line(x,String.format(Locale.US,"%02d",j)+"   "+p.optString("title"),17,NAVY,true);
      line(x,p.optString("provider")+"  •  "+type,12,TEAL,true);
      line(x,overview(type+" "+p.optString("title"),p.optString("title")),13,INK,false);
      button(x,"သင်ရိုးနှင့် မြန်မာရှင်းလင်းချက်",()->inspect(spec?2:3,id),false);
    }
  }
  private void detail(){
    if(detailType==1){JSONObject c=courses.optJSONObject(detailIndex);if(c==null)return;
      heading(c.optString("title"),"COURSE  •  "+c.optString("provider")+"  •  "+c.optString("category"));
      LinearLayout x=card(body);line(x,"သင်ခန်းစာအဓိပ္ပာယ် / မြန်မာလို",18,NAVY,true);
      line(x,overview(c.optString("category"),c.optString("title")),15,INK,false);
      line(x,"ဘာတွေသင်ယူရမလဲ?",17,TEAL,true);
      line(x,"၁။ အဓိကဝေါဟာရနှင့် သဘောတရားကို နားလည်ခြင်း။\n၂။ လုပ်ငန်းခွင်တွင် အသုံးချပုံကို ဥပမာဖြင့်သိခြင်း။\n၃။ စမ်းသပ်ပြီး ရလာဒ်အမှားများကို ပြန်လည်သုံးသပ်ခြင်း။\n၄။ သက်ဆိုင်ရာ Official Coursera Module နှင့် Assignment ကို လိုက်နာခြင်း။",14,INK,false);
      line(x,"လက်တွေ့လေ့ကျင့်ခန်း",17,TEAL,true);line(x,exercise(c.optString("category"),c.optString("title")),14,INK,false);
      String key="course_"+detailIndex;CheckBox done=new CheckBox(this);done.setText("ကိုယ်ပိုင်လေ့ကျင့်ခန်း ပြီးဆုံးပြီ");done.setChecked(prefs.getBoolean(key,false));
      x.addView(done);done.setOnCheckedChangeListener((b,checked)->prefs.edit().putBoolean(key,checked).apply());
      button(x,"Coursera.org မှ မူရင်း Course ဖွင့်မည်",()->browse(c.optString("url")),true);
      line(x,"မှတ်ချက် — ဤမြန်မာလိုအကြောင်းအရာများသည် AAII ၏ သင်ကြားရေးရှင်းလင်းချက်ဖြစ်ပြီး Official Coursera သင်ရိုးကို စာလုံးချင်း ပြန်ထုတ်ထားခြင်း မဟုတ်ပါ။",12,MUTED,false);
      button(x,"Offline AI Study Support",()->new AlertDialog.Builder(this).setTitle("AAII Study Guidance")
        .setMessage("သင်ခန်းစာ: "+c.optString("title")+"\n\n"+overview(c.optString("category"),c.optString("title"))+"\n\nလေ့ကျင့်ရန်: "+exercise(c.optString("category"),c.optString("title"))+"\n\nLive AI API မချိတ်ထားသေးပါ။")
        .setPositiveButton("OK",null).show(),false);
      button(x,"← Courses သို့ပြန်ရန်",()->openTab(1),false);return;}
    JSONObject p=programs.optJSONObject(detailIndex);if(p==null)return;
    boolean spec=detailType==2;heading(p.optString("title"),(spec?"SPECIALIZATION":"PROFESSIONAL CERTIFICATE")+"  •  "+p.optString("provider"));
    LinearLayout x=card(body);
    line(x,"ဘာသာရပ်အကြောင်း — မြန်မာလို",18,NAVY,true);
    line(x,overview(p.optString("type")+" "+p.optString("title"),p.optString("title")),15,INK,false);
    line(x,"လေ့လာရန် အစီအစဉ်",17,TEAL,true);
    line(x,"အဆင့် ၁။ Foundation (အခြေခံသဘောတရား)\nအဆင့် ၂။ Core Skills (အဓိကကျွမ်းကျင်မှု)\nအဆင့် ၃။ Guided Practice (လက်တွေ့လေ့ကျင့်ခန်း)\nအဆင့် ၄။ Portfolio/Project (လုပ်ငန်းနမူနာ)\nအဆင့် ၅။ Coursera Official Assessment (မူရင်းစာမေးပွဲ)",14,INK,false);
    if(p.optString("title").equalsIgnoreCase("Google Project Management"))
      line(x,"Coursera မှ အဓိကလေ့လာရသည့်အချက်များ — Project Charter, Project Plan, Risk Management, Stakeholder Communication, Agile, Scrum, Capstone။",14,TEAL,false);
    if(p.optString("title").equalsIgnoreCase("Google Data Analytics"))
      line(x,"Coursera မှ အဓိကလေ့လာရသည့်အချက်များ — Data Cleaning, Spreadsheets, SQL, Python, Visualization, Tableau, Case Study။",14,TEAL,false);
    if(p.optString("title").equalsIgnoreCase("Google AI"))
      line(x,"Coursera မှ အဓိကလေ့လာရသည့်အချက်များ — AI Fundamentals, Responsible Prompting, Research, Planning, Content, Data Analysis နှင့် App Prototyping။",14,TEAL,false);
    String target=p.optString("title");int related=0;
    for(int i=0;i<courses.length();i++){JSONObject c=courses.optJSONObject(i);if(c==null)continue;String names=c.optString("program");
      if(names.toLowerCase(Locale.ROOT).contains(target.toLowerCase(Locale.ROOT)) || target.toLowerCase(Locale.ROOT).contains(names.toLowerCase(Locale.ROOT))&&!names.isEmpty()){
        related++;LinearLayout y=card(body);line(y,"Module "+related+"  •  "+c.optString("title"),14,NAVY,true);line(y,c.optString("category"),11,MUTED,false);
        final int courseIndex=i;button(y,"Module ရှင်းလင်းချက်",()->inspect(1,courseIndex),false);
      }
    }
    if(related==0)line(x,"သင်တန်းများ၏ အတိအကျအစီအစဉ်ကို Official Coursera Program Page တွင်ကြည့်ပါ။",13,MUTED,false);
    String completeKey="program_"+detailIndex;CheckBox done=new CheckBox(this);done.setText("AAII Self-study Review ပြီးပြီ (Official Certificate မဟုတ်ပါ)");done.setChecked(prefs.getBoolean(completeKey,false));x.addView(done);
    done.setOnCheckedChangeListener((v,value)->prefs.edit().putBoolean(completeKey,value).apply());
    button(x,"Coursera Original Program & Curriculum",()->browse(p.optString("url")),true);
    line(x,"AAII သင်ကြားရေးအတွက် ရေးသားထားသည့် Roadmap ဖြစ်ပြီး Course Series နှင့် Module များကို Coursera က ပြောင်းလဲနိုင်ပါသည်။",12,MUTED,false);
    button(x,"← ပြန်ရန်",()->openTab(spec?2:3),false);
  }
  private void teacher(){
    screenLabel("TEACHER TRAINING","Teaching methodology • TESOL • Online classroom tools");
    heading("လက်မှတ်ရဆရာဖြစ်ရန် လမ်းကြောင်း","Official Coursera သင်ကြားရေးအခြေခံ Course နှင့် Professional Certificate များကို ချိတ်ထားပါသည်။");
    LinearLayout service=card(body);line(service,"ဆရာလိုအပ်နေပါသလား?",17,NAVY,true);
    line(service,"Online / Offline ဆရာငှားရန် AAII Teacher Matching Request တင်နိုင်ပါသည်။",13,INK,false);
    button(service,"Hire a Teacher • ဆရာရှာမည်",()->openTab(8),true);
    button(service,"Teacher Soe Moe — Profile Detail",()->openTab(9),false);
    button(service,"Teacher Booking Request Form",()->openTab(10),false);
    LinearLayout x=card(body);line(x,"TEACHER CAREER ROADMAP",19,NAVY,true);
    line(x,"01  Teaching Foundations — သင်ယူသူနှင့် သင်ကြားနည်း သီအိုရီ\n02  Lesson Planning — ရည်မှန်းချက်၊ သင်ခန်းစာအစီအစဉ်\n03  Classroom Management — တက်ကြွစွာပါဝင်မှု\n04  Digital Teaching — Zoom, LMS, Digital Activities\n05  Assessment — Quiz, Rubric, Feedback\n06  Teaching Portfolio — Lesson Plan, Video, Sample Work\n07  Official Course / TESOL Certificate ကို သင်တန်းပေးသူထံမှ ရယူခြင်း\n08  Teacher License လိုပါက နိုင်ငံအလိုက် အစိုးရအသိအမှတ်ပြု လမ်းကြောင်းသီးခြား စစ်ဆေးခြင်း",14,INK,false);
    line(x,"Coursera/TESOL Certificate ရရှိရုံဖြင့် နိုင်ငံအလိုက် Teacher License အလိုအလျောက် မရပါ။",13,0xffa3471b,true);
    for(int i=0;i<teachers.length();i++){JSONObject c=teachers.optJSONObject(i);if(c==null)continue;final int index=i;LinearLayout a=card(body);
      line(a,c.optString("name"),17,NAVY,true);line(a,c.optString("provider")+"   •   "+c.optString("type"),12,TEAL,true);
      line(a,c.optString("mm"),14,INK,false);line(a,"သင်တန်းဖွဲ့စည်းမှု: "+c.optString("modules"),12,MUTED,false);
      line(a,"လုပ်ဆောင်ရန်: "+c.optString("practice"),13,TEAL,false);
      String key="teacher_"+i;CheckBox done=new CheckBox(this);done.setText("Self-study အတွက် မှတ်သားပြီး");done.setChecked(prefs.getBoolean(key,false));a.addView(done);
      done.setOnCheckedChangeListener((v,value)->prefs.edit().putBoolean(key,value).apply());
      button(a,"Coursera.org မှ သင်တန်းအသေးစိတ်",()->browse(c.optString("url")),true);
    }
    x=card(body);line(x,"AAII Teacher Soe Moe Training Support",17,NAVY,true);
    line(x,"သင်ယူသူအား သင်ခန်းစာရွေးချယ်မှု၊ လက်တွေ့ Lesson Plan ပြုစုမှုနှင့် Portfolio Review ကို ကူညီလမ်းညွှန်နိုင်ရန် ရည်ရွယ်ထားသည်။ Coursera တရားဝင် Trainer ဖြစ်ကြောင်း မဆိုလိုပါ။",13,INK,false);
  }
  
  private EditText requestInput(LinearLayout parent,String label,String hint){
    line(parent,label,13,NAVY,true);
    EditText field=new EditText(this);field.setSingleLine(true);field.setTextSize(15);field.setTextColor(INK);
    field.setHint(hint);field.setPadding(dp(13),dp(11),dp(13),dp(11));field.setBackground(shape(0xfff1f6f9,10));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(4),0,dp(7));parent.addView(field,lp);
    return field;
  }
  private Spinner requestSelect(LinearLayout parent,String label,String[] choices){
    line(parent,label,13,NAVY,true);
    Spinner spinner=new Spinner(this);
    ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,choices);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    parent.addView(spinner,new LinearLayout.LayoutParams(-1,dp(49)));
    return spinner;
  }
  private static final String VIBER_PHONE="+959252566954";
  private static final String LINE_PHONE="+66930409759";
  private static final String TELEGRAM_LINK="https://t.me/myanmardata";
  private void clip(String caption,String message){
    android.content.ClipboardManager cb=(android.content.ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
    if(cb!=null)cb.setPrimaryClip(android.content.ClipData.newPlainText(caption,message));
  }
  private void channel(int choice,String draft){
    try{
      if(choice==0){
        if(draft!=null)clip("AAII Viber Request",draft);
        Intent go=new Intent(Intent.ACTION_VIEW,Uri.parse("viber://chat?number=%2B959252566954"));
        go.setPackage("com.viber.voip");startActivity(go);
        if(draft!=null)Toast.makeText(this,"Viber: Request copied • Paste into chat and tap Send",Toast.LENGTH_LONG).show();
      }else if(choice==1){
        if(draft!=null){
          clip("AAII LINE Request",draft);
          startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://line.me/R/share?text="+Uri.encode(draft))));
          Toast.makeText(this,"LINE: Select the AAII recipient and tap Send",Toast.LENGTH_LONG).show();
        }else{
          clip("AAII LINE Phone",LINE_PHONE);
          startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://line.me/R/nv/addFriends")));
          Toast.makeText(this,"LINE: Add friend by searching +66 9 30409759",Toast.LENGTH_LONG).show();
        }
      }else if(choice==2){
        if(draft!=null)clip("AAII Telegram Request",draft);
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(TELEGRAM_LINK)));
        if(draft!=null)Toast.makeText(this,"Telegram: Paste request into @myanmardata chat, then Send",Toast.LENGTH_LONG).show();
      }else if(choice==3){
        clip("AAII LINE Phone",LINE_PHONE);
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://line.me/R/nv/addFriends")));
        Toast.makeText(this,"LINE: Search for +66 9 30409759",Toast.LENGTH_LONG).show();
      }
    }catch(android.content.ActivityNotFoundException ex){
      String id=choice==0?VIBER_PHONE:(choice==1||choice==3?LINE_PHONE:"@myanmardata");
      clip("AAII Contact",id);
      new AlertDialog.Builder(this).setTitle("Messaging app not available")
        .setMessage("App ကို Install လုပ်ပါ။ Contact ကို Copy လုပ်ထားသည်: "+id+"\n\nRequest ကို အလိုအလျောက် မပေးပို့ပါ။")
        .setPositiveButton("OK",null).show();
    }catch(Exception ex){
      Toast.makeText(this,"Messaging app မဖွင့်နိုင်ပါ။ အခြား Share နည်းလမ်းရွေးပါ။",Toast.LENGTH_LONG).show();
    }
  }
  private void genericShare(String title,String message){
    try{
      Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");
      send.putExtra(Intent.EXTRA_SUBJECT,title);send.putExtra(Intent.EXTRA_TEXT,message);
      startActivity(Intent.createChooser(send,"Share AAII Request"));
    }catch(Exception ex){Toast.makeText(this,"No supported sharing app found",Toast.LENGTH_LONG).show();}
  }
  private void contactButtons(LinearLayout parent){
    LinearLayout c=card(parent);
    line(c,"AAII CONTACT • ဆက်သွယ်ရန်",17,NAVY,true);
    line(c,"Viber +95 9 252566954 • LINE +66 9 30409759 • Telegram @myanmardata",12,INK,false);
    button(c,"☏ Viber • Open App",()->channel(0,null),false);
    button(c,"◉ LINE • Add Friends",()->channel(1,null),false);
    button(c,"➤ Telegram • Open Chat",()->channel(2,null),false);
    line(c,"Request Form ဖြည့်ပြီးမှ Channel ရွေးလျှင် စာသားကို Copy/Share လုပ်နိုင်သည်။ Auto-send မရှိပါ။",12,MUTED,false);
  }
  private void shareRequest(String title,String summary,String contact){
    if(summary==null||summary.trim().length()<2||contact==null||contact.trim().length()<3){
      new AlertDialog.Builder(this).setTitle("အချက်အလက်လိုအပ်သည်")
        .setMessage("သင်တန်း/ဘာသာရပ်နှင့် ဆက်သွယ်ရန်ဖုန်း/အီးမေးလ်ကို ဖြည့်ပါ။")
        .setPositiveButton("OK",null).show();return;
    }
    String payload=title+"\n\n"+summary+"\n\nContact: "+contact+
      "\n\nAAII Learning (user-initiated request)";
    new AlertDialog.Builder(this).setTitle("Request ပို့ရန် App ရွေးပါ")
      .setMessage("Viber: +95 9 252566954\nLINE: +66 9 30409759\nTelegram: @myanmardata\n\nPassword, OTP, Bank PIN မပို့ပါနှင့်။ Message ကို သင်ကိုယ်တိုင် Send နှိပ်ရပါမည်။")
      .setItems(new String[]{
        "Viber • +95 9 252566954",
        "LINE • Share Request",
        "Telegram • @myanmardata",
        "Other Apps • Share Menu",
        "LINE • Add Friend via Phone"
      },(dialog,which)->{
        saveLocalDraft(title,summary.length()>160?summary.substring(0,160):summary);
        if(which==0||which==1||which==2)channel(which,payload);
        else if(which==3)genericShare(title,payload);
        else if(which==4)channel(3,null);
      })
      .setNegativeButton("Cancel",null).show();
  }
  private void servicesHub(){
    screenLabel("AAII LEARNING SERVICES","Learn • Enroll • Hire Teacher • Track Requests");
    heading("ပညာရေး ဝန်ဆောင်မှုများ","Choose a service below. Forms are submitted using your phone's Share Menu.");
    LinearLayout a=card(body);line(a,"01  COURSERA ENROLLMENT",18,NAVY,true);
    line(a,"Courses၊ Specializations နှင့် Professional Certificates ရွေးချယ်ခြင်း၊ Financial Aid ရရှိနိုင်မှုကို Official Website မှ စစ်ဆေးခြင်း။",13,INK,false);
    button(a,"Enrollment Assistance Request",()->openTab(7),true);
    LinearLayout b=card(body);line(b,"02  TEACHER MARKETPLACE",18,NAVY,true);
    portraitCard(b,"Teacher Soe Moe","Learning mentor • Online / Offline Request");
    line(b,"ဘာသာရပ်၊ Learning Level၊ Location၊ Budget နှင့် Schedule ရွေးပြီး ဆရာငှားရန် Request တင်နိုင်သည်။",13,INK,false);
    button(b,"Hire a Teacher",()->openTab(8),true);
    button(b,"Teacher Soe Moe Profile",()->openTab(9),false);
    LinearLayout c=card(body);line(c,"03  MY LEARNING & CERTIFICATES",17,NAVY,true);
    button(c,"View Learning Dashboard",()->openTab(12),false);
    button(c,"My Private Certificates",()->openTab(5),false);
    LinearLayout d=card(body);line(d,"04  REQUEST HISTORY",17,NAVY,true);
    line(d,"Saved local drafts: "+drafts().length()+". Booking အတည်ပြုမှု သို့မဟုတ် Coursera Enrollment အတည်ပြုမှု မဟုတ်သေးပါ။",13,MUTED,false);
    button(d,"My Request Drafts",()->openTab(13),false);
    contactButtons(body);
    line(d,"AAII Enrollment Assistance သည် Coursera ၏ Official Agency မဟုတ်ပါ။ Account Password/OTP မမျှဝေပါနှင့်။",12,MUTED,false);
  }

  private void enrollmentService(){
    screenLabel("ENROLLMENT ASSISTANCE","Coursera Course • Specialization • Professional Certificate");
    heading("Coursera Enrollment Assistance","AAII ၏ သီးခြားသင်တန်းရွေးချယ်/Enrollment အကြံပေးဝန်ဆောင်မှု ဖြစ်ပါသည်။ Coursera ၏ တရားဝင်ကိုယ်စားလှယ်မဟုတ်ပါ။");
    LinearLayout intro=card(body);
    line(intro,"ဘာတွေကူညီပေးမလဲ?",18,NAVY,true);
    line(intro,"၁။ Course / Specialization / Professional Certificate များကို လိုအပ်ချက်နှင့် ကိုက်ညီအောင် ရွေးချယ်ပေးရန်။\n၂။ Official Course Page မှ သင်ရိုး၊ သင်တန်းကြေးနှင့် သင်ယူချိန်ကို စစ်ဆေးရန်။\n၃။ သက်ဆိုင်ရာ Course မှာ Financial Aid ရနိုင်/မရနိုင် ကို လမ်းညွှန်ရန်။\n၄။ သင်ယူသူကိုယ်တိုင် Coursera Account ဖွင့်ပြီး Enrollment ပြုလုပ်ရန် လမ်းညွှန်ရန်။\n၅။ သင်တန်းပြီးစီးရန် Study Plan နှင့် Teacher Support တိုင်ပင်ရန်။",14,INK,false);
    line(intro,"တရားဝင်သင်တန်းကြေးကို Coursera ပေါ်တွင်သာ စစ်ဆေးပါ။ AAII Service Fee ရှိပါက သီးခြားရှင်းလင်းအတည်ပြုပါမည်။ Enrollment၊ Financial Aid သို့ Certificate ရရှိမည်ဟု အာမမခံပါ။",12,MUTED,false);
    button(intro,"Coursera Official Programs",()->browse("https://www.coursera.org/professional-certificates"),false);
    LinearLayout form=card(body);line(form,"သင်တန်းလျှောက်ရေး အကူအညီတောင်းရန်",18,NAVY,true);
    EditText topic=requestInput(form,"လိုချင်သောသင်တန်း / Subject *","ဥပမာ: Google Project Management");
    Spinner type=requestSelect(form,"သင်တန်းအမျိုးအစား",new String[]{"မရွေးရသေး","Course","Specialization","Professional Certificate","သင့်တော်သောသင်တန်း ရွေးပေးရန်"});
    EditText purpose=requestInput(form,"သင်ယူရခြင်းရည်ရွယ်ချက်","ဥပမာ: Career Change, Teaching");
    EditText contact=requestInput(form,"ဖုန်း / Email / ဆက်သွယ်ရန် *","ဆက်သွယ်ရန် သင်ရွေးချယ်သည့်နည်း");
    CheckBox agree=new CheckBox(this);agree.setText("AAII သည် Coursera ၏ တရားဝင်ကိုယ်စားလှယ် မဟုတ်ကြောင်း နားလည်ပါသည်။ Password, OTP နှင့် ငွေပေးချေမှုအချက်အလက်များ မမျှဝေပါ။");
    agree.setTextSize(12);form.addView(agree);
    button(form,"Request → Viber / LINE / Telegram",()->{
      if(!agree.isChecked()){Toast.makeText(this,"အသိပေးချက်ကို ဦးစွာဖတ်ပြီး အတည်ပြုပါ",Toast.LENGTH_LONG).show();return;}
      String bodyText="Requested Course: "+topic.getText().toString()+"\nProgram Type: "+String.valueOf(type.getSelectedItem())+"\nGoal: "+purpose.getText().toString();
      shareRequest("AAII Coursera Enrollment Assistance",bodyText,contact.getText().toString());
    },true);
    line(form,"Submit လုပ်ပြီးကြောင်း မဆိုလိုပါ။ Share Menu မှ သင်ရွေးချယ်သည့်ဆက်သွယ်ရေး App ဖြင့် ကိုယ်တိုင်ပေးပို့ရန် လိုပါသည်။",12,MUTED,false);
    button(form,"← Home",()->openTab(0),false);
    contactButtons(body);
  }
  private void hireTeacherService(){
    screenLabel("HIRE A TEACHER","Find a learning mentor • Online / Offline Request");
    heading("Hire a Teacher • ဆရာငှားရမ်းရန်","AAII Teacher Matching Request • ဆရာအရည်အချင်းနှင့် လစ်လပ်ချိန်ကို လူကိုယ်တိုင် အတည်ပြုရန် လိုအပ်သည်။");
    LinearLayout intro=card(body);
    portraitCard(intro,"Teacher Soe Moe","AI • Data • Business • Online / Offline");
    button(intro,"View Full Teacher Profile",()->openTab(9),false);
    line(intro,"ဆရာငှားနိုင်သော အကြောင်းအရာများ",18,NAVY,true);
    line(intro,"• English & Language Learning\n• IT, Programming & AI\n• Business & Management\n• Mathematics & Science\n• Coursera Course Mentoring\n• TESOL, Teaching & Lesson Planning",14,INK,false);
    line(intro,"သင်ကြားမှုပုံစံ: Online / Offline / Hybrid\nSession: တစ်ဦးချင်း / အုပ်စု\nကြေးနှုန်း: ဆရာ၏အတွေ့အကြုံနှင့် သင်တန်းအချိန်အလိုက် သဘောတူညီရန်",13,MUTED,false);
    LinearLayout form=card(body);line(form,"Teacher Request Form",18,NAVY,true);
    EditText subject=requestInput(form,"သင်ကြားလိုသော ဘာသာရပ် *","ဥပမာ: English IELTS, Python");
    Spinner format=requestSelect(form,"သင်ကြားမှုအမျိုးအစား",new String[]{"Online","Offline","Hybrid"});
    Spinner level=requestSelect(form,"သင်ယူသူ အဆင့်",new String[]{"Beginner","Intermediate","Advanced","Professional","Children"});
    EditText schedule=requestInput(form,"သင်ကြားလိုသော အချိန် / ရက်များ","ဥပမာ: Sat/Sun 9–11 AM");
    EditText duration=requestInput(form,"သင်ကြားမည့် ကာလ / Budget","ဥပမာ: 3 months / MMK budget");
    EditText location=requestInput(form,"Offline ဖြစ်ပါက မြို့ / နေရာ","ဥပမာ: Yangon");
    EditText contact=requestInput(form,"ဖုန်း / Email / ဆက်သွယ်ရန် *","ဆက်သွယ်ရန် သင်ရွေးချယ်သည့်နည်း");
    CheckBox agree=new CheckBox(this);agree.setText("AAII က ဆရာကို စိစစ်ပြီး အတည်ပြုမှ Matching/Booking ဖြစ်ကြောင်းနှင့် ကြေးနှုန်းကို သီးခြားသဘောတူရန် လိုကြောင်း နားလည်ပါသည်။");
    agree.setTextSize(12);form.addView(agree);
    button(form,"Teacher Request → Viber / LINE / Telegram",()->{
      if(!agree.isChecked()){Toast.makeText(this,"Matching အချက်အလက်ကို ဖတ်ပြီး အတည်ပြုပါ",Toast.LENGTH_LONG).show();return;}
      String bodyText="Subject: "+subject.getText().toString()+"\nLearning Mode: "+String.valueOf(format.getSelectedItem())+
      "\nLevel: "+String.valueOf(level.getSelectedItem())+"\nSchedule: "+schedule.getText().toString()+
      "\nDuration/Budget: "+duration.getText().toString()+"\nLocation: "+location.getText().toString();
      shareRequest("AAII Hire a Teacher Request",bodyText,contact.getText().toString());
    },true);
    line(form,"ယခု Version သည် Request Draft / Share UI ဖြစ်သည်။ Teacher Database, Admin Approval, Booking Confirmation နှင့် Online Payment မချိတ်ရသေးပါ။",12,MUTED,false);
    button(form,"← Home",()->openTab(0),false);
    contactButtons(body);
  }

  
  private void teacherProfile(){
    LinearLayout teacherCover=coloredCard(body,NAVY,0xff075b91);
    line(teacherCover,"TEACHER SOE MOE  •  AAII MENTOR",15,GOLD,true);
    line(teacherCover,"COURSE GUIDANCE • PROJECT COACHING • PORTFOLIO",12,WHITE,false);
    LinearLayout ribbon=card(body);ribbon.setBackground(shape(NAVY,17));
    line(ribbon,"AAII LEARNING • TEACHER MARKETPLACE",13,GOLD,true);
    line(ribbon,"Teacher Profile • Portfolio • Booking Request",16,WHITE,true);
    heading("Teacher Soe Moe • ဆရာ စိုးမိုး","AAII Learning Mentor • Course Guidance, Portfolio Support");
    LinearLayout bio=card(body);bio.setBackground(shape(0xffe9f5ff,18));
    portraitCard(bio,"SOE MOE • ဆရာ စိုးမိုး","AAII Learning Mentor • Original Portrait");
    line(bio,"TEACHER PROFILE",21,NAVY,true);
    line(bio,"AI • Data Science • Python • IT • Digital Education • Business",14,TEAL,true);
    line(bio,"AAII သင်ကြားရေး Mentor အဖြစ် Course ရွေးချယ်မှု၊ လေ့လာမည့်အစီအစဉ်၊ Project Design နှင့် Learning Portfolio တည်ဆောက်မှုကို ကူညီဆောင်ရွက်နိုင်ရန် ရည်ရွယ်ပါသည်။",14,INK,false);
    line(bio,"Verification: ကိုယ်ပိုင် Coursera Program Certificates ကို ဖုန်း၏ Private Wallet မှသာ လင့်ခ်ဖြင့်စစ်ဆေးနိုင်သည်။ မျက်နှာပြင်တွင် ပြသထားသည့်လက်မှတ်များကို Teacher License ဟု မယူဆပါနှင့်။",12,MUTED,false);
    button(bio,"View Private Certificate Wallet",()->openTab(5),false);
    LinearLayout topics=card(body);line(topics,"Teaching Subjects • သင်ကြားနိုင်သော ဘာသာရပ်များ",17,NAVY,true);
    String[] subjects={"Artificial Intelligence / Prompt Engineering","Python Programming","Data Science & Analytics","Project Management","IT & Digital Skills","Digital Teaching & Portfolio"};
    for(int i=0;i<subjects.length;i++)line(topics,(i+1)+". "+subjects[i],14,INK,false);
    LinearLayout modes=card(body);line(modes,"Learning Modes • သင်ကြားမှုပုံစံ",17,NAVY,true);
    line(modes,"Online (Zoom / Google Meet အစရှိသည်)\nOffline (ညှိနှိုင်းအတည်ပြုရန်)\nIndividual / Group / Project Mentoring",14,INK,false);
    line(modes,"ဆရာ၏ လစ်လပ်ချိန်၊ သင်တန်းကြေးနှင့် အရည်အချင်းအတည်ပြုမှုကို Request လက်ခံပြီးမှ ထပ်မံစစ်ဆေးရန် လိုပါသည်။",12,MUTED,false);
    LinearLayout p=card(body);line(p,"Teaching Portfolio • Learning Journey",17,NAVY,true);
    line(p,"① Skills Assessment → ② Course & Lesson Plan → ③ Live Coaching → ④ Practical Project → ⑤ Portfolio Review",14,INK,false);
    button(p,"Request Booking with Soe Moe",()->openTab(10),true);
    button(p,"Browse Courses with Mentor Support",()->openTab(1),false);
  }
  private void bookingForm(){
    screenLabel("REQUEST TEACHER BOOKING","Teacher Soe Moe • Subject • Date • Time • Budget");
    LinearLayout ribbon=card(body);ribbon.setBackground(shape(0xffe8f6fe,16));
    line(ribbon,"01 Choose Subject   →   02 Schedule   →   03 Share Request",13,TEAL,true);
    heading("Teacher Booking Request","ဆရာ စိုးမိုးနှင့် သင်တန်းညှိနှိုင်းရန် (လက်ရှိ Draft/Share ဝန်ဆောင်မှု)");
    LinearLayout head=card(body);
    portraitCard(head,"Teacher Soe Moe","Course guidance • Online / Offline Request");
    line(head,"ဘာသာရပ်အလိုက် Online / Offline သင်ကြားမှုနှင့် Project Coaching ကို ညှိနှိုင်းနိုင်ပါသည်။",13,INK,false);
    LinearLayout steps=card(body);line(steps,"1. Choose Subject   →   2. Pick Schedule   →   3. Send Request",13,TEAL,true);
    LinearLayout f=card(body);
    Spinner subject=requestSelect(f,"သင်ကြားလိုသော Subject *",new String[]{
      "AI & Data Science","Python Programming","Machine Learning","IT & Computer",
      "Digital Education","Project Management","Business","English for IT","Other"});
    Spinner level=requestSelect(f,"Learning Level",new String[]{"Beginner","Intermediate","Advanced","Professional"});
    Spinner mode=requestSelect(f,"Online / Offline",new String[]{"Online","Offline","Hybrid"});
    EditText name=requestInput(f,"Student Full Name *","Your name");
    EditText contact=requestInput(f,"Contact Email / Phone *","How AAII can contact you");
    EditText location=requestInput(f,"Location (for offline class)","Yangon / Mandalay / Other");
    TextView date=txt("Date: "+bookingDate,14,INK,true);f.addView(date);
    button(f,"Select Preferred Date",()->{
      java.util.Calendar c=java.util.Calendar.getInstance();
      android.app.DatePickerDialog dialog=new android.app.DatePickerDialog(this,(view,y,m,d)->{
        bookingDate=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);date.setText("Date: "+bookingDate);
      },c.get(java.util.Calendar.YEAR),c.get(java.util.Calendar.MONTH),c.get(java.util.Calendar.DAY_OF_MONTH));
      dialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000L);dialog.show();
    },false);
    TextView time=txt("Time: "+bookingTime,14,INK,true);f.addView(time);
    button(f,"Select Preferred Time",()->{
      java.util.Calendar c=java.util.Calendar.getInstance();
      new android.app.TimePickerDialog(this,(v,h,min)->{
        bookingTime=String.format(Locale.US,"%02d:%02d",h,min);time.setText("Time: "+bookingTime);
      },c.get(java.util.Calendar.HOUR_OF_DAY),0,false).show();
    },false);
    EditText budget=requestInput(f,"Budget (MMK) • Optional","e.g. 50000 MMK per course");
    EditText notes=requestInput(f,"Learning Goals / Other Notes","What would you like to learn?");
    CheckBox agree=new CheckBox(this);agree.setText("Booking Request ပေးပို့ခြင်းသာဖြစ်ပြီး ဆရာ၏အချိန်နှင့်ကြေးနှုန်းကို AAII အတည်ပြုမှ Booking Confirm ဖြစ်ပါမည်။");
    agree.setTextSize(12);f.addView(agree);
    button(f,"Booking Request → Viber / LINE / Telegram",()->{
      if(name.getText().toString().trim().isEmpty()||bookingDate.equals("ရက်ရွေးပါ")||bookingTime.equals("အချိန်ရွေးပါ")){
        Toast.makeText(this,"Student Name, Date, Time ဖြည့်ပါ",Toast.LENGTH_LONG).show();return;
      }
      if(!agree.isChecked()){Toast.makeText(this,"Request သဘောတူချက်ကို အတည်ပြုပါ",Toast.LENGTH_LONG).show();return;}
      String summary="Teacher: Soe Moe\nSubject: "+subject.getSelectedItem()+
        "\nLevel: "+level.getSelectedItem()+"\nMode: "+mode.getSelectedItem()+
        "\nStudent: "+name.getText().toString()+"\nDate: "+bookingDate+" Time: "+bookingTime+
        "\nLocation: "+location.getText().toString()+"\nBudget: "+budget.getText().toString()+"\nGoals: "+notes.getText().toString();
      shareRequest("AAII Teacher Booking — Soe Moe",summary,contact.getText().toString());
    },true);
    line(f,"No live scheduling API or payment is connected. Availability is NOT confirmed. Request only.",12,MUTED,false);
    button(f,"View My Local Request Drafts",()->openTab(13),false);
    contactButtons(body);
  }
  private void myLearning(){
    screenLabel("MY LEARNING","Course review progress • local offline dashboard");
    heading("My Learning • သင်ယူမှုမှတ်တမ်း","ဤဖုန်းမှာ ကိုယ်တိုင်နှိပ်ထားသော Study Progress ကိုသာ သိမ်းထားပါသည်။");
    progressStrip(body,"ကိုယ်ပိုင် Course Review",reviewedCourses(),courses.length());
    LinearLayout intro=card(body);
    line(intro,"Learning Dashboard",19,NAVY,true);
    line(intro,"Course Catalog: "+courses.length()+" • Specializations: 37 • Professional: 18",13,TEAL,true);
    line(intro,"ကိုယ်တိုင်လေ့လာပြီးကြောင်း မှတ်သားထားသည့် Course များကို အောက်တွင်ပြထားပါသည်။ Coursera Official Completion မဟုတ်ပါ။",13,INK,false);
    button(intro,"Browse Courses",()->openTab(1),true);
    int displayed=0;
    for(int i=0;i<courses.length();i++){
      if(!prefs.getBoolean("course_"+i,false))continue;
      JSONObject c=courses.optJSONObject(i);if(c==null)continue;
      LinearLayout x=card(body);line(x,c.optString("title"),15,NAVY,true);
      line(x,c.optString("provider")+" • Self-reviewed on device",12,MUTED,false);
      final int id=i;button(x,"Course Details",()->inspect(1,id),false);displayed++;
      if(displayed>=60)break;
    }
    if(displayed==0)line(intro,"No course reviews marked yet.",13,MUTED,false);
    button(intro,"Certificate Wallet",()->openTab(5),false);
  }
  private void myRequests(){
    screenLabel("MY REQUESTS","Enrollment and booking drafts • not yet confirmed");
    heading("My Service Requests","Local request drafts • Not sent to an AAII server");
    LinearLayout notice=card(body);
    line(notice,"Important: Local drafts ≠ AAII Booking Confirmation",16,NAVY,true);
    line(notice,"ဖုန်း၏ Share Menu ကနေ မိမိကိုယ်တိုင် ပို့ပြီး AAII မှ ပြန်လည်အတည်ပြုပြီးမှသာ လျှောက်ထားမှု / Booking အတည်ဖြစ်ပါသည်။",13,INK,false);
    JSONArray a=drafts();line(notice,"Saved request drafts: "+a.length(),15,TEAL,true);
    for(int i=a.length()-1;i>=0;i--){
      JSONObject obj=a.optJSONObject(i);if(obj==null)continue;
      LinearLayout row=card(body);line(row,obj.optString("type"),16,NAVY,true);
      line(row,obj.optString("date")+" • "+obj.optString("state"),12,MUTED,false);
      line(row,obj.optString("subject"),12,INK,false);
    }
    if(a.length()==0)line(notice,"No local requests saved yet.",13,MUTED,false);
    button(notice,"New Coursera Enrollment Request",()->openTab(7),false);
    button(notice,"New Teacher Booking Request",()->openTab(10),false);
    button(notice,"Clear Local Request Drafts",()->new AlertDialog.Builder(this)
      .setTitle("Clear request drafts from this phone?")
      .setPositiveButton("Clear",(d,w)->{prefs.edit().remove("aaii_request_drafts").apply();render();})
      .setNegativeButton("Cancel",null).show(),false);
  }
  private void settingsPage(){
    screenLabel("PROFILE & SETTINGS","Personal learning • Certificates • Requests • Privacy");
    heading("Profile & Settings • ကိုယ်ရေးအချက်အလက်","AAII Learning Native Android • Offline-first preferences");
    LinearLayout profile=card(body);portraitCard(profile,"Teacher Soe Moe • ဆရာ စိုးမိုး","AAII Profile • Settings");
    line(profile,"AAII Course Guidance & Teaching Mentor Profile",13,TEAL,true);
    button(profile,"View Teacher Profile",()->openTab(9),false);
    LinearLayout learning=card(body);line(learning,"My Learning & Progress",17,NAVY,true);
    line(learning,"Self-reviewed Courses: "+reviewedCourses()+" / "+courses.length(),14,INK,false);
    button(learning,"Open Learning Dashboard",()->openTab(12),false);
    LinearLayout cert=card(body);line(cert,"My Certificates",17,NAVY,true);
    line(cert,"Imported private certificates: "+certs.length(),13,INK,false);
    button(cert,"Open Private Wallet",()->openTab(5),false);
    LinearLayout services=card(body);line(services,"Enrollment & Teacher Booking Requests",17,NAVY,true);
    line(services,"Local drafts: "+drafts().length(),13,INK,false);
    button(services,"View Request Drafts",()->openTab(13),false);
    LinearLayout language=card(body);line(language,"Language Preference • ဘာသာစကား",17,NAVY,true);
    line(language,"သင်ယူမှုအထောက်အကူပြု preferred language သာဖြစ်ပြီး App UI တစ်ခုလုံး Localization မပြီးသေးပါ။",12,MUTED,false);
    Spinner spinner=requestSelect(language,"Preferred Learning Language",new String[]{"Myanmar (မြန်မာ)","English"});
    spinner.setSelection("en".equals(prefs.getString("study_lang","my"))?1:0);
    button(language,"Save Language Preference",()->{
      prefs.edit().putString("study_lang",spinner.getSelectedItemPosition()==1?"en":"my").apply();
      Toast.makeText(this,"Language preference saved locally",Toast.LENGTH_SHORT).show();
    },true);
    LinearLayout account=card(body);line(account,"Account & Privacy",17,NAVY,true);
    line(account,"No server login, payments, live teacher calendar or notifications are connected to this build. Private credentials stay on this device.",13,INK,false);
    button(account,"Clear My Local Course Progress",()->new AlertDialog.Builder(this).setTitle("Clear course reviews on this phone?")
      .setPositiveButton("Clear",(d,w)->{SharedPreferences.Editor edit=prefs.edit();for(int i=0;i<courses.length();i++)edit.remove("course_"+i);edit.apply();render();})
      .setNegativeButton("Cancel",null).show(),false);
    button(account,"View Request Drafts",()->openTab(13),false);
  }

  private void wallet(){
    screenLabel("MY CERTIFICATE WALLET","Private credentials stored on your device");
    heading("My Private Certificates","Public APK ထဲမှာ အခြားသူ၏ Certificate ID မပါဝင်ပါ။");
    LinearLayout x=card(body);line(x,"Personal Credential Wallet",17,NAVY,true);
    line(x,"ကိုယ်ပိုင် programs.json ကို ဖုန်းအတွင်း၌သာ ထည့်သွင်းနိုင်ပြီး GitHub Public Source ထဲ မတင်ပါ။ Coursera ကသာ Official Certificate ထုတ်ပေးပါသည်။",14,INK,false);
    button(x,"Private JSON Import",()->{Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.setType("*/*");intent.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(intent,404);},true);
    line(x,"Imported Certificates: "+certs.length(),13,TEAL,true);
    for(int i=0;i<certs.length()&&i<100;i++){JSONObject c=certs.optJSONObject(i);if(c==null)continue;String url=c.optString("certificateUrl");
      if(!url.startsWith("https://www.coursera.org/account/accomplishments/"))continue;LinearLayout y=card(body);
      line(y,c.optString("title",c.optString("name")),16,NAVY,true);String link=url;
      button(y,"Certificate Verify",()->browse(link),false);
    }
    button(x,"Imported Records ဖျက်မည်",()->new AlertDialog.Builder(this).setTitle("Clear imported credentials?")
      .setPositiveButton("Clear",(d,w)->{prefs.edit().remove("personal_credentials").apply();certs=new JSONArray();render();})
      .setNegativeButton("Cancel",null).show(),false);
  }
  @Override protected void onActivityResult(int req,int result,Intent data){
    super.onActivityResult(req,result,data);
    if(req!=404||result!=RESULT_OK||data==null||data.getData()==null)return;
    try(InputStream in=getContentResolver().openInputStream(data.getData())){
      if(in==null)throw new Exception("File not available");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;
      while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>1000000)throw new Exception("File too large");}
      JSONArray imported=new JSONArray(out.toString("UTF-8"));if(imported.length()>150)throw new Exception("Too many records");
      for(int i=0;i<imported.length();i++){JSONObject p=imported.optJSONObject(i);if(p==null||!p.optString("certificateUrl").startsWith("https://www.coursera.org/account/accomplishments/"))throw new Exception("Unrecognized certificate URL");}
      prefs.edit().putString("personal_credentials",imported.toString()).apply();certs=imported;render();
    }catch(Exception ex){new AlertDialog.Builder(this).setTitle("Import error").setMessage(ex.getMessage()).setPositiveButton("OK",null).show();}
  }
  @Override public void onBackPressed(){if(page==6)openTab(detailType==1?1:detailType==2?2:3);else if(page!=0)openTab(0);else super.onBackPressed();}
}
