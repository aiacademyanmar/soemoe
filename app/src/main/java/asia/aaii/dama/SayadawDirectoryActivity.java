package asia.aaii.dama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

/** V20 offline Sayadaw directory. PHOTO and GENERIC ILLUSTRATION must never be confused. */
public final class SayadawDirectoryActivity extends DamaActivity {
  private static final int BG=0xff061d1c, DEEP=0xff092e28, PANEL=0xff154236;
  private static final int GOLD=0xffedc578, IVORY=0xfffff5df, MUTED=0xffbbd2c5;
  private final ArrayList<JSONObject> people=new ArrayList<>();
  private final HashSet<Integer> saved=new HashSet<>();
  private LinearLayout root, cards, tabs;
  private TextView count;
  private EditText query;
  private String selectedFilter="all";
  private SharedPreferences prefs;
  private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
  private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}
  private LinearLayout horizontal(){LinearLayout v=new LinearLayout(this);v.setOrientation(0);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
  private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
  private GradientDrawable bg(int color,int radius,int stroke){GradientDrawable x=new GradientDrawable();x.setColor(color);x.setCornerRadius(dp(radius));if(stroke!=0)x.setStroke(dp(1),stroke);return x;}
  private TextView text(String content,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(content);t.setTextColor(color);t.setTextSize(size);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
  private String str(JSONObject p,String key){return p.optString(key,"");}
  private int count(JSONObject p,String key){return p.optInt(key,0);}
  private boolean photo(JSONObject p){return "photo".equals(p.optString("status"));}
  private ImageView portrait(JSONObject p,int size){ImageView v=new ImageView(this);String name=p.optString("android_drawable");int id=getResources().getIdentifier(name,"drawable",getPackageName());if(id==0)id=R.drawable.radio_meditation;v.setImageResource(id);v.setScaleType(ImageView.ScaleType.CENTER_CROP);v.setBackground(bg(PANEL,12,0xff98804a));v.setClipToOutline(true);v.setContentDescription(photo(p)?"မူရင်းဓာတ်ပုံ":"AI Illustration — ရုပ်ပုံတူမဟုတ်");return v;}
  @Override public void onCreate(Bundle savedState){super.onCreate(savedState);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);prefs=getSharedPreferences("dama_sayadaw_v20",MODE_PRIVATE);
    try{Set<String> set=prefs.getStringSet("favorite_profiles",Collections.emptySet());for(String value:set)saved.add(Integer.parseInt(value));}catch(Exception ignored){}
    try(InputStream in=getAssets().open("sayadaw_profiles_v20.json");ByteArrayOutputStream buffer=new ByteArrayOutputStream()){
      byte[] tmp=new byte[16384];int n;while((n=in.read(tmp))!=-1){if(buffer.size()>600_000)break;buffer.write(tmp,0,n);}JSONArray a=new JSONArray(buffer.toString("UTF-8"));for(int i=0;i<a.length();i++){JSONObject p=a.optJSONObject(i);if(p!=null)people.add(p);}
    }catch(Exception e){android.util.Log.e("DAMA_PROFILE","Directory asset missing",e);}
    draw();
  }
  private void draw(){
    root=vertical();root.setBackgroundColor(BG);setContentView(root);
    if(android.os.Build.VERSION.SDK_INT>=35){root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bar=insets.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(bar.left,bar.top,bar.right,bar.bottom);return insets;});root.requestApplyInsets();}
    LinearLayout header=horizontal();header.setPadding(dp(16),dp(12),dp(13),dp(12));header.setBackgroundColor(DEEP);
    TextView back=text("‹",31,GOLD,true);back.setGravity(Gravity.CENTER);header.addView(back,lp(43,49));PremiumMedia.tap(back,()->finish());
    LinearLayout id=vertical();id.setPadding(dp(7),0,0,0);id.addView(text("DAMA SAYADAW",21,GOLD,true));id.addView(text("Photo & Illustration Gallery",11,MUTED,false));header.addView(id,new LinearLayout.LayoutParams(0,-2,1));
    TextView about=text("ⓘ",23,GOLD,true);about.setGravity(Gravity.CENTER);about.setBackground(bg(PANEL,16,0xff706544));header.addView(about,lp(42,40));PremiumMedia.tap(about,()->new AlertDialog.Builder(this).setTitle("DAMA SAYADAW · About").setMessage("Photo: မူရင်းဓာတ်ပုံနှင့် Attribution\nIllustration: AI ဖန်တီးထားသော ယေဘုယျပုံ — အမှန်တကယ် ရုပ်ပုံတူ မဟုတ်ပါ။\n\nစာအုပ်/MP3/TV အရေအတွက်သည် APK Catalog Snapshot ဖြစ်ပြီး Media Link အားလုံး အလုပ်လုပ်ကြောင်း အာမခံချက်မဟုတ်ပါ။").setPositiveButton("OK",null).show());
    root.addView(header,lp(-1,76));
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout content=vertical();content.setPadding(dp(14),dp(10),dp(14),dp(16));scroll.addView(content);
    LinearLayout hero=vertical();hero.setPadding(dp(19),dp(17),dp(19),dp(14));hero.setBackground(bg(PANEL,18,0xff846d47));
    hero.addView(text("✦  DHAMMA KNOWLEDGE COLLECTION",10,GOLD,true));
    TextView title=text("ဆရာတော်ကြီးများ၏\nဓမ္မအလင်းရောင်",24,IVORY,true);title.setPadding(0,dp(12),0,dp(8));hero.addView(title);
    hero.addView(text("မှန်ကန်သောဓာတ်ပုံရှိသူများအတွက် Photo၊ မရသေးသူများအတွက် Illustration ပြထားပါတယ်။",12,MUTED,false));
    content.addView(hero,lp(-1,-2));
    LinearLayout metrics=horizontal();metrics.setPadding(0,dp(12),0,dp(9));
    metric(metrics,"22","SAYADAWS");metric(metrics,"3","PHOTOS");metric(metrics,"19","ILLUSTRATED");content.addView(metrics);
    query=new EditText(this);query.setSingleLine(true);query.setTextSize(14);query.setTextColor(IVORY);query.setHintTextColor(MUTED);query.setHint("⌕  ဆရာတော်အမည် ရှာရန်...");query.setPadding(dp(14),0,dp(13),0);query.setBackground(bg(0xff183e35,13,0xff9b7c4d));
    LinearLayout.LayoutParams qp=lp(-1,50);qp.setMargins(0,dp(8),0,dp(8));content.addView(query,qp);
    HorizontalScrollView rail=new HorizontalScrollView(this);rail.setHorizontalScrollBarEnabled(false);tabs=horizontal();rail.addView(tabs);content.addView(rail);addTabs();
    LinearLayout result=horizontal();result.setPadding(0,dp(15),0,dp(9));result.addView(text("✦  ဆရာတော်စာရင်း",15,GOLD,true));
    count=text("",11,MUTED,false);count.setGravity(Gravity.RIGHT);result.addView(count,new LinearLayout.LayoutParams(0,-2,1));content.addView(result);
    cards=vertical();content.addView(cards);
    TextView note=text("Illustration များသည် ဆရာတော်တစ်ပါးချင်း၏ အမှန်တကယ် ရုပ်ပုံတူ မဟုတ်ပါ။ Photo Credit တွေကို Profile Popup မှ ကြည့်နိုင်ပါတယ်။",11,MUTED,false);note.setPadding(0,dp(11),0,dp(10));content.addView(note);
    query.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence v,int s,int c,int a){}public void onTextChanged(CharSequence v,int s,int b,int c){showCards();}public void afterTextChanged(Editable e){}});
    showCards();
    FooterNav.attach(this,root,"Sayadaw");
  }
  private void metric(LinearLayout outer,String number,String title){LinearLayout tile=vertical();tile.setGravity(Gravity.CENTER);tile.setPadding(dp(2),dp(10),dp(2),dp(10));tile.setBackground(bg(0xff153a31,12,0xff675d43));TextView big=text(number,20,GOLD,true);big.setGravity(Gravity.CENTER);tile.addView(big);TextView small=text(title,9,MUTED,false);small.setGravity(Gravity.CENTER);tile.addView(small);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(66),1);p.setMargins(dp(2),0,dp(2),0);outer.addView(tile,p);}
  private void addTabs(){tabs.removeAllViews();String[][] opts={{"all","အားလုံး"},{"photo","PHOTO"},{"illustration","ILLUSTRATION"},{"favorites","♡ Favorites"}};for(String[] pair:opts){boolean chosen=selectedFilter.equals(pair[0]);TextView t=text(pair[1],12,chosen?BG:GOLD,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(chosen?GOLD:PANEL,18,chosen?GOLD:0xff6e754c));LinearLayout.LayoutParams p=lp(-2,37);p.setMargins(0,0,dp(8),0);t.setPadding(dp(12),0,dp(12),0);tabs.addView(t,p);PremiumMedia.tap(t,()->{selectedFilter=pair[0];addTabs();showCards();});}}
  private void showCards(){if(cards==null)return;cards.removeAllViews();String q=query==null?"":query.getText().toString().trim().toLowerCase(Locale.ROOT);ArrayList<JSONObject> shown=new ArrayList<>();for(JSONObject p:people){int id=p.optInt("id");if(selectedFilter.equals("photo")&&!photo(p))continue;if(selectedFilter.equals("illustration")&&photo(p))continue;if(selectedFilter.equals("favorites")&&!saved.contains(id))continue;if(!q.isEmpty()&&!str(p,"name").toLowerCase(Locale.ROOT).contains(q)&&!str(p,"school").toLowerCase(Locale.ROOT).contains(q))continue;shown.add(p);}
    count.setText(shown.size()+" profiles");if(shown.isEmpty()){TextView t=text("သတ်မှတ်ချက်နှင့်ကိုက်ညီသော ဆရာတော် မတွေ့ပါ။",13,MUTED,false);t.setPadding(0,dp(30),0,dp(30));cards.addView(t);return;}
    for(int index=0;index<shown.size();index+=2){LinearLayout line=horizontal();JSONObject p=shown.get(index);addCard(line,p);if(index+1<shown.size())addCard(line,shown.get(index+1));else{View spacer=new View(this);line.addView(spacer,new LinearLayout.LayoutParams(0,1,1));}LinearLayout.LayoutParams ap=lp(-1,-2);ap.setMargins(0,0,0,dp(10));cards.addView(line,ap);}
  }
  private void addCard(LinearLayout line,JSONObject p){LinearLayout card=vertical();card.setPadding(dp(8),dp(8),dp(8),dp(8));card.setBackground(bg(0xff154336,14,0xff857449));card.setElevation(dp(2));
    ImageView pic=portrait(p,145);card.addView(pic,lp(-1,145));
    TextView kind=text(photo(p)?"● PHOTO":"◈ ILLUSTRATION",10,photo(p)?0xffa5eed2:GOLD,true);kind.setPadding(dp(3),dp(8),0,dp(3));card.addView(kind);
    TextView name=text(str(p,"name"),12,IVORY,true);name.setMaxLines(2);card.addView(name,lp(-1,48));
    TextView info=text(String.format(Locale.US,"%,d MP3 · %d Books",count(p,"mp3_count"),count(p,"book_title_matches")),10,GOLD,false);card.addView(info);
    if(!photo(p)){TextView disclaimer=text("ရုပ်ပုံတူမဟုတ်",10,MUTED,false);card.addView(disclaimer);}
    int id=p.optInt("id");TextView fav=text(saved.contains(id)?"♥":"♡",22,GOLD,true);fav.setGravity(Gravity.RIGHT);card.addView(fav);
    PremiumMedia.tap(fav,()->{if(!saved.add(id))saved.remove(id);HashSet<String> set=new HashSet<>();for(int number:saved)set.add(String.valueOf(number));prefs.edit().putStringSet("favorite_profiles",set).apply();showCards();});
    PremiumMedia.tap(card,()->showProfile(p));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(3),0,dp(3),0);line.addView(card,lp);
  }
  private void popupButton(LinearLayout target,String label,Runnable action){TextView button=text(label,13,IVORY,true);button.setGravity(Gravity.CENTER);button.setPadding(dp(12),dp(12),dp(12),dp(12));button.setBackground(bg(0xff265344,12,0xff92774c));LinearLayout.LayoutParams bp=lp(-1,-2);bp.setMargins(0,0,0,dp(8));target.addView(button,bp);PremiumMedia.tap(button,action);}
  private void showProfile(JSONObject p){int id=p.optInt("id"),books=count(p,"book_title_matches"),videos=count(p,"video_name_matches"),audio=count(p,"mp3_count");
    LinearLayout box=vertical();box.setPadding(dp(17),dp(14),dp(17),dp(12));box.setBackground(bg(BG,19,0xffb08b52));
    LinearLayout top=horizontal();ImageView pic=portrait(p,104);top.addView(pic,lp(100,114));LinearLayout t=vertical();t.setPadding(dp(12),0,0,0);t.addView(text("✦  SAYADAW PROFILE",10,GOLD,true));TextView name=text(str(p,"name"),17,IVORY,true);name.setPadding(0,dp(5),0,dp(3));t.addView(name);t.addView(text(photo(p)?"SOURCE PHOTO":"AI ILLUSTRATION · NOT A LIKENESS",10,GOLD,true));top.addView(t,new LinearLayout.LayoutParams(0,-2,1));box.addView(top);
    LinearLayout stats=horizontal();stats.setPadding(0,dp(14),0,dp(11));metric(stats,String.format(Locale.US,"%,d",books),"BOOKS");metric(stats,String.format(Locale.US,"%,d",audio),"MP3");metric(stats,String.format(Locale.US,"%,d",videos),"TV");box.addView(stats);
    TextView note=text("ဤအရေအတွက်များသည် Offline Catalog မှ ခေါင်းစဉ်/အမည်ကိုက်ညီမှုစာရင်းဖြစ်ပြီး လင့်ခ်များ အလုပ်လုပ်ကြောင်း အာမခံချက်မဟုတ်ပါ။",11,MUTED,false);note.setPadding(0,0,0,dp(12));box.addView(note);
    final AlertDialog dialog=new AlertDialog.Builder(this).setView(box).create();
    popupButton(box,"▣   စာအုပ်စာရင်း ကြည့်ရန်",()->{dialog.dismiss();Intent go=new Intent(this,LibraryActivity.class);go.putExtra("dama_profile_author_key",str(p,"alias"));startActivity(go);});
    popupButton(box,"♫   MP3 တရားတော်များ",()->{dialog.dismiss();Intent go=new Intent(this,MainActivity.class);go.putExtra("dama_profile_monk_id",id);startActivity(go);});
    popupButton(box,"▶   TV ဗီဒီယိုများ",()->{dialog.dismiss();Intent go=new Intent(this,TvActivity.class);go.putExtra("dama_profile_sayadaw_key",str(p,"alias"));startActivity(go);});
    String credit=str(p,"photo_credit"),license=str(p,"photo_license");String original=str(p,"photo_source");
    if(photo(p)){TextView attr=text("Photo © "+credit+" · "+license+"  ↗",10,GOLD,false);attr.setPadding(0,dp(5),0,dp(6));box.addView(attr);PremiumMedia.tap(attr,()->{if(original.startsWith("https://")){Intent v=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(original));startActivity(v);}});}else{TextView label=text("Illustration သည် ယေဘုယျဖန်တီးထားသော ပုံဖြစ်ပြီး ဆရာတော်၏ ဓာတ်ပုံမဟုတ်ပါ။",10,MUTED,false);box.addView(label);}
    dialog.show();if(dialog.getWindow()!=null)dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
  }
}