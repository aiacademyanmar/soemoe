package asia.aaii.learningpublic;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.text.*;
import org.json.*;
import java.io.*;
import java.util.*;
public class MainActivity extends Activity {
 private JSONArray courses=new JSONArray(),roads=new JSONArray(),certs=new JSONArray();
 private SharedPreferences prefs;private LinearLayout root,body;private int page=0,limit=50;private String term="";
 private final String[] tabs={"Home","Courses","Roadmaps","Certificates","AI Support"};
 private final int navy=Color.rgb(16,38,60),blue=Color.rgb(32,101,145),gold=Color.rgb(214,169,76);
 private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
 private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);t.setPadding(dp(7),dp(7),dp(7),dp(7));return t;}
 private JSONArray asset(String file){try(InputStream in=getAssets().open(file)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return new JSONArray(out.toString("UTF-8"));}catch(Exception e){return new JSONArray();}}
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("aaii_local",MODE_PRIVATE);courses=asset("courses.json");roads=asset("roadmaps.json");try{certs=new JSONArray(prefs.getString("certs","[]"));}catch(Exception ignored){}draw();}
 private void go(int n){page=n;term="";limit=50;draw();}
 private LinearLayout card(LinearLayout parent){LinearLayout x=new LinearLayout(this);x.setOrientation(1);x.setPadding(dp(13),dp(13),dp(13),dp(13));x.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(10));parent.addView(x,p);return x;}
 private void button(LinearLayout parent,String s,Runnable act){TextView v=text(s,14,Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setPadding(dp(12),dp(10),dp(12),dp(10));v.setBackground(bg(blue,10));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(8),0,0);parent.addView(v,p);v.setOnClickListener(q->act.run());}
 private void open(String s){try{Uri u=Uri.parse(s);if(!"https".equals(u.getScheme())||!"www.coursera.org".equals(u.getHost()))throw new Exception();startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){Toast.makeText(this,"Official course URL unavailable",Toast.LENGTH_SHORT).show();}}
 private void draw(){root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xfff4f7fa);setContentView(root);
  LinearLayout head=new LinearLayout(this);head.setPadding(dp(15),dp(10),dp(15),dp(10));head.setOrientation(1);head.setBackgroundColor(navy);root.addView(head);
  head.addView(text("AAII  •  PROFESSIONAL CERTIFICATE",12,gold,true));head.addView(text("Soe Moe  |  Teacher Training + AI Support",18,Color.WHITE,true));
  head.addView(text("PUBLIC-SAFE BUILD  •  PRIVATE CERTIFICATES NOT INCLUDED",10,0xffd5dce5,false));
  ScrollView sc=new ScrollView(this);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(13),dp(14),dp(13),dp(15));sc.addView(body);
  if(page==0)home();else if(page==1)catalog();else if(page==2)roadmaps();else if(page==3)wallet();else ai();
  HorizontalScrollView bar=new HorizontalScrollView(this);bar.setHorizontalScrollBarEnabled(false);bar.setBackgroundColor(Color.WHITE);
  LinearLayout nav=new LinearLayout(this);bar.addView(nav);for(int i=0;i<tabs.length;i++){final int id=i;TextView label=text(tabs[i],12,id==page?blue:navy,id==page);label.setGravity(Gravity.CENTER);label.setMinWidth(dp(84));nav.addView(label,new LinearLayout.LayoutParams(-2,dp(56)));label.setOnClickListener(v->go(id));}
  root.addView(bar,new LinearLayout.LayoutParams(-1,dp(56)));
 }
 private void title(String v){body.addView(text(v,23,navy,true));}
 private void home(){title("AAII Learning Academy");LinearLayout x=card(body);x.addView(text(courses.length()+" curated courses • "+roads.length()+" professional roadmaps",17,navy,true));
 x.addView(text("Independent guided study with Teacher Soe Moe. Official Coursera certificates are issued by Coursera and its partner institutions. This is a native Android public build, not a verified completion record.",14,navy,false));
 button(x,"Open course catalog",()->go(1));button(x,"Professional certificate roadmaps",()->go(2));
 x=card(body);x.addView(text("International certificate privacy",18,navy,true));x.addView(text("No personal certificate numbers, completion dates or account login information are stored in public GitHub source. Import your own earned credentials on your device.",13,navy,false));button(x,"Private certificate wallet",()->go(3));
 x=card(body);x.addView(text("Learning process",17,navy,true));x.addView(text("Select roadmap → Study course → Practice with teacher → AI study guidance → Complete official Coursera assessments → Verify official credentials.",14,navy,false));}
 private void catalog(){title("Courses • "+courses.length());body.addView(text("Course titles are public catalog information. These listings do not claim user completion.",12,navy,false));
 EditText input=new EditText(this);input.setSingleLine(true);input.setHint("Search course, university, topic");input.setText(term);body.addView(input);
 LinearLayout list=new LinearLayout(this);list.setOrientation(1);body.addView(list);
 Runnable fill=()->{list.removeAllViews();int matches=0,shown=0;
 for(int i=0;i<courses.length();i++){JSONObject c=courses.optJSONObject(i);if(c==null)continue;String title=c.optString("title"),provider=c.optString("provider");
 if(!(title+" "+provider+" "+c.optString("category")+" "+c.optString("program")).toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT).trim()))continue;matches++;if(shown++>=limit)continue;
 LinearLayout x=card(list);x.addView(text(title,16,navy,true));x.addView(text(provider+" • "+c.optString("category"),12,blue,false));String link=c.optString("url");button(x,"Open original Coursera course",()->open(link));
 String id="p_"+i;CheckBox p=new CheckBox(this);p.setText("Personal study review");p.setChecked(prefs.getBoolean(id,false));x.addView(p);p.setOnCheckedChangeListener((a,b)->prefs.edit().putBoolean(id,b).apply());}
 list.addView(text("Showing "+Math.min(limit,matches)+" of "+matches,12,blue,true),0);
 if(matches>limit)button(list,"Show more",()->{limit+=50;draw();});};
 input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){term=s.toString();limit=50;fill.run();}public void afterTextChanged(Editable e){}});fill.run();}
 private void roadmaps(){title("Professional Roadmaps • "+roads.length());body.addView(text("Study durations are AAII estimates, not official Coursera requirements.",12,navy,false));
 for(int i=0;i<roads.length();i++){JSONObject c=roads.optJSONObject(i);if(c==null)continue;LinearLayout x=card(body);x.addView(text((i+1)+". "+c.optString("title"),17,navy,true));x.addView(text(c.optString("provider")+" • "+c.optInt("weeks")+" suggested weeks",13,blue,false));x.addView(text("Project: "+c.optString("project"),13,navy,false));String url=c.optString("url");button(x,"View on Coursera",()->open(url));}}
 private void wallet(){title("International Certificate Wallet");LinearLayout x=card(body);x.addView(text("Private by design",17,navy,true));
 x.addView(text("Your personal 55 certificate records are NOT uploaded to this public GitHub repository. Import the private programs.json file from your AAII V2.9 build package on your device.",14,navy,false));
 button(x,"Import private programs.json",()->{Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.setType("*/*");intent.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(intent,55);});
 x=card(body);x.addView(text("Imported credentials: "+certs.length(),17,navy,true));for(int i=0;i<certs.length()&&i<100;i++){JSONObject p=certs.optJSONObject(i);if(p==null)continue;String url=p.optString("certificateUrl");
 if(!url.startsWith("https://www.coursera.org/account/accomplishments/"))continue;LinearLayout item=card(body);item.addView(text(p.optString("name",p.optString("title")),16,navy,true));item.addView(text(p.optString("provider"),12,blue,false));button(item,"Verify original certificate",()->open(url));}
 button(x,"Clear private imported records",()->new AlertDialog.Builder(this).setTitle("Clear imported certificate records?")
  .setPositiveButton("Clear",(d,w)->{prefs.edit().remove("certs").apply();certs=new JSONArray();draw();}).setNegativeButton("Cancel",null).show());}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=55||result!=RESULT_OK||data==null||data.getData()==null)return;
 try(InputStream in=getContentResolver().openInputStream(data.getData())){ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){b.write(buf,0,n);if(b.size()>1000000)throw new Exception("File too large");}
 String json=b.toString("UTF-8");JSONArray a=new JSONArray(json);if(a.length()>150)throw new Exception("Too many records");
 for(int i=0;i<a.length();i++){JSONObject c=a.optJSONObject(i);if(c==null||!c.optString("certificateUrl").startsWith("https://www.coursera.org/account/accomplishments/"))throw new Exception("Invalid official certificate URL");}
 prefs.edit().putString("certs",json).apply();certs=a;draw();
 }catch(Exception ex){new AlertDialog.Builder(this).setTitle("Import failed").setMessage(ex.getMessage()).setPositiveButton("OK",null).show();}}
 private void ai(){title("Offline AI Study Support");LinearLayout x=card(body);x.addView(text("Teacher Soe Moe • Guided Training",18,navy,true));x.addView(text("This is a rule-based offline study guide; no live AI model or API connected.",14,navy,false));
 x.addView(text("1. Select a topic.\n2. Define the core concepts.\n3. Develop a relevant practical example.\n4. Discuss with a human teacher.\n5. Review strengths and limitations.\n6. Complete official Coursera assessments separately.",14,navy,false));button(x,"Choose course",()->go(1));}
}
