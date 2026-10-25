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
  private final int NAVY=0xff102a43, TEAL=0xff087e86, GOLD=0xffd5a74f, INK=0xff17324c, MUTED=0xff64748b, BG=0xfff4f8fb, WHITE=0xffffffff;
  private final String[] LABELS={"ပင်မ","သင်တန်း","အထူးပြု","Professional","ဆရာလမ်းကြောင်း"};
  private final String[] ICONS={"⌂","▤","✦","★","♙"};
  private JSONArray courses=new JSONArray(), programs=new JSONArray(),roadmaps=new JSONArray(),teachers=new JSONArray(),certs=new JSONArray();
  private SharedPreferences prefs;
  private LinearLayout root,body,footer;
  private int page=0,detailType=0,detailIndex=0,limit=35;
  private String search="",category="အားလုံး";
  private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
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
  private LinearLayout card(LinearLayout container){
    LinearLayout x=vertical();x.setPadding(dp(14),dp(12),dp(14),dp(12));x.setBackground(shape(WHITE,16));
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
  private void render(){
    root=vertical();root.setBackgroundColor(BG);root.setFitsSystemWindows(true);setContentView(root);
    LinearLayout head=vertical();head.setPadding(dp(16),dp(8),dp(16),dp(8));head.setBackgroundColor(NAVY);root.addView(head);
    line(head,"AAII LEARNING   •   COURSERA ROADMAP",11,GOLD,true);
    line(head,"ပညာရေး  •  Training with Soe Moe",16,WHITE,true);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    body=vertical();body.setPadding(dp(13),dp(12),dp(13),dp(14));scroll.addView(body);
    if(page==0)home();else if(page==1)courseList();else if(page==2)programList("Specialization");else if(page==3)programList("Professional Certificate");else if(page==4)teacher();else if(page==5)wallet();else detail();
    footer=new LinearLayout(this);footer.setOrientation(LinearLayout.HORIZONTAL);footer.setBackgroundColor(WHITE);
    footer.setPadding(dp(2),dp(3),dp(2),dp(5));
    int highlight=page==6?(detailType==1?1:detailType==2?2:detailType==3?3:4):page;
    for(int i=0;i<5;i++){final int tab=i;LinearLayout item=vertical();item.setGravity(Gravity.CENTER);
      if(highlight==i)item.setBackground(shape(0xffedf8f8,12));
      TextView icon=txt(ICONS[i],20,i==highlight?TEAL:MUTED,true);icon.setGravity(Gravity.CENTER);item.addView(icon);
      TextView label=txt(LABELS[i],i==3?9:10,i==highlight?TEAL:MUTED,true);label.setGravity(Gravity.CENTER);item.addView(label);
      footer.addView(item,new LinearLayout.LayoutParams(0,dp(65),1));item.setOnClickListener(v->openTab(tab));}
    root.addView(footer,new LinearLayout.LayoutParams(-1,dp(73)));
  }
  private void home(){
    LinearLayout hero=card(body);hero.setBackground(shape(NAVY,18));
    line(hero,"Learn • Practice • Get Certified",13,GOLD,true);
    line(hero,"နိုင်ငံတကာ သင်ယူမှုလမ်းကြောင်း",21,WHITE,true);
    line(hero,"Coursera ၏ သင်တန်းစာမျက်နှာများဆီသို့ တိုက်ရိုက်သွားရောက်လေ့လာနိုင်ပြီး AAII မှ မြန်မာဘာသာဖြင့် အကြောင်းအရာများကို ရှင်းပြထားသည်။",14,0xffe3f2f2,false);
    line(hero,courses.length()+" Courses   •   37 Specializations   •   18 Professional",14,GOLD,true);
    button(hero,"သင်ယူရန် Course ရွေးချယ်မည်",()->openTab(1),true);
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
    heading("လက်မှတ်ရဆရာဖြစ်ရန် လမ်းကြောင်း","Official Coursera သင်ကြားရေးအခြေခံ Course နှင့် Professional Certificate များကို ချိတ်ထားပါသည်။");
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
  private void wallet(){
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
