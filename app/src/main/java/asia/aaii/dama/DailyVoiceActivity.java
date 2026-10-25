package asia.aaii.dama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.graphics.Color;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * DAILY DAMA VOICE: 365 offline Burmese original reflections.
 * Voice playback uses installed Android TTS; this app does NOT bundle prerecorded audio.
 * AI-drafted content is educational reflection, not a verbatim Buddhist scripture quotation.
 */
public final class DailyVoiceActivity extends Activity {
    private static final int DARK=0xff061e1c, DEEP=0xff092c27, PANEL=0xff133d35;
    private static final int GOLD=0xffefc576, PALE=0xfffff5df, MUTED=0xffb1c5b8, GREEN=0xff30644e;
    private final ArrayList<JSONObject> voices=new ArrayList<>();
    private LinearLayout root,content;
    private TextView playbackLabel;
    private SharedPreferences store;
    private Set<String> listened,favorites;
    private TextToSpeech tts;
    private boolean ttsReady=false,playing=false,calendarMode=false,favoritesMode=false;
    private int index=0,month=1;
    private float rate=1.0f;
    private int dp(float a){return Math.round(a*getResources().getDisplayMetrics().density);}
    private LinearLayout col(){LinearLayout x=new LinearLayout(this);x.setOrientation(1);return x;}
    private LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(0);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));if(stroke!=0)g.setStroke(dp(1),stroke);return g;}
    private void gap(int h){content.addView(new View(this),lp(1,h));}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private TextView button(String s,boolean bright,Runnable action){TextView t=txt(s,12,bright?DARK:GOLD,true);t.setGravity(Gravity.CENTER);t.setPadding(dp(10),dp(10),dp(10),dp(10));t.setBackground(shape(bright?GOLD:PANEL,14,bright?0:0xff7c714d));t.setOnClickListener(v->action.run());return t;}
    private void addCard(LinearLayout parent,View child){LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(12);parent.addView(child,p);}
    private String string(JSONObject v,String key){return v.optString(key,"");}
    private String todayKey(){Calendar now=Calendar.getInstance();return String.format(Locale.US,"%02d-%02d",now.get(Calendar.MONTH)+1,Math.min(now.get(Calendar.DAY_OF_MONTH),now.get(Calendar.MONTH)==1?28:31));}
    private int todayIndex(){String key=todayKey();for(int i=0;i<voices.size();i++)if(key.equals(string(voices.get(i),"date_label")))return i;return 0;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(DARK);getWindow().setNavigationBarColor(DARK);
        store=getSharedPreferences("dama_daily_voice",MODE_PRIVATE);
        listened=new HashSet<>(store.getStringSet("listened",new HashSet<>()));
        favorites=new HashSet<>(store.getStringSet("favorites",new HashSet<>()));
        load();index=todayIndex();month=voices.isEmpty()?1:voices.get(index).optInt("month",1);
        tts=new TextToSpeech(this,status->{
            if(status==TextToSpeech.SUCCESS){
                Locale locale=new Locale("my","MM");
                int support=tts.setLanguage(locale);
                ttsReady=support!=TextToSpeech.LANG_NOT_SUPPORTED&&support!=TextToSpeech.LANG_MISSING_DATA;
                if(ttsReady){
                    tts.setSpeechRate(rate);
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){
                        @Override public void onStart(String id){runOnUiThread(()->{playing=true;setPlayback("♫ မြန်မာအသံဖြင့် ဖတ်နေပါသည်");});}
                        @Override public void onDone(String id){runOnUiThread(()->{playing=false;markListened();setPlayback("✓ ဖတ်ပြီးပါပြီ");});}
                        @Override public void onError(String id){runOnUiThread(()->{playing=false;setPlayback("အသံဖွင့်မရပါ — စာသားဖတ်နိုင်ပါသည်");});}
                    });
                }
            }
            runOnUiThread(()->{if(!ttsReady)setPlayback("မြန်မာ TTS Voice မရှိပါ — စာသားကို Offline ဖတ်နိုင်ပါသည်");else setPlayback("Voice Engine Ready · Burmese TTS");});
        });
        render();
    }
    private void load(){
        try(InputStream raw=getAssets().open("daily_voice_365.dat");GZIPInputStream unzip=new GZIPInputStream(raw);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[32768];int n;
            while((n=unzip.read(buf))!=-1){if(out.size()+n>3_000_000)throw new IllegalStateException("data too large");out.write(buf,0,n);}
            JSONArray entries=new JSONArray(out.toString("UTF-8"));
            for(int i=0;i<entries.length();i++){JSONObject item=entries.optJSONObject(i);if(item!=null)voices.add(item);}
        }catch(Exception e){android.util.Log.e("DAMA_DAILY","No daily reflections",e);toast("Daily Voice Catalog မဖွင့်နိုင်ပါ");}
    }
    private JSONObject current(){return voices.get(Math.max(0,Math.min(index,voices.size()-1)));}
    private String key(){return String.valueOf(current().optInt("day"));}
    private void setPlayback(String state){if(playbackLabel!=null)playbackLabel.setText(state);}
    private void markListened(){if(voices.isEmpty())return;listened.add(key());store.edit().putStringSet("listened",new HashSet<>(listened)).apply();}
    private void toggleFavorite(){if(!favorites.add(key()))favorites.remove(key());store.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();render();}
    private void speak(){
        if(voices.isEmpty())return;
        if(!ttsReady){new AlertDialog.Builder(this).setTitle("မြန်မာအသံ မရရှိသေးပါ")
            .setMessage("ဒီ APK တွင် အသံဖိုင် ၃၆၅ ဖိုင် ထည့်မထားပါ။ သင့်ဖုန်းတွင် မြန်မာဘာသာ Text-to-Speech voice ပါမှ အသံဖတ်နိုင်ပါသည်။ Voice မရှိပါက Offline စာသားဖတ်နိုင်ပါတယ်။")
            .setPositiveButton("ဖတ်ပြီးအမှတ်အသားလုပ်",(d,w)->{markListened();render();})
            .setNegativeButton("OK",null).show();return;}
        if(tts.isSpeaking()){tts.stop();playing=false;setPlayback("ရပ်နားထားသည်");return;}
        try{
            tts.setSpeechRate(rate);
            int status=tts.speak(string(current(),"script"),TextToSpeech.QUEUE_FLUSH,null,"DAMA_DAY_"+key());
            if(status==TextToSpeech.ERROR){playing=false;setPlayback("TTS မအောင်မြင်ပါ");}
            else{playing=true;setPlayback("အသံစတင်ဖတ်နေပါသည် …");}
        }catch(Exception e){playing=false;setPlayback("အသံစနစ် မပံ့ပိုးပါ");}
    }
    private void openDay(int at){if(at<0||at>=voices.size())return;if(tts!=null)tts.stop();playing=false;index=at;month=current().optInt("month",1);calendarMode=false;favoritesMode=false;render();}
    private TextView chip(String name,boolean active,Runnable action){TextView t=button(name,active,action);LinearLayout.LayoutParams lp=lp(-2,42);lp.setMargins(dp(3),0,dp(3),0);t.setLayoutParams(lp);return t;}
    private void render(){
        root=col();root.setBackgroundColor(DARK);setContentView(root);
        if(android.os.Build.VERSION.SDK_INT>=35){root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});root.requestApplyInsets();}
        LinearLayout top=row();top.setPadding(dp(16),dp(10),dp(16),dp(11));top.setBackgroundColor(DEEP);
        TextView back=txt("‹",30,GOLD,true);back.setGravity(Gravity.CENTER);top.addView(back,lp(36,43));back.setOnClickListener(v->finish());
        LinearLayout brand=col();brand.setPadding(dp(8),0,0,0);brand.addView(txt("DAILY DAMA VOICE",19,GOLD,true));brand.addView(txt("365 DAYS · MINDFUL LIVING",10,MUTED,false));top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView viewCalendar=button("▦",false,()->{calendarMode=!calendarMode;favoritesMode=false;render();});top.addView(viewCalendar,lp(45,42));root.addView(top,lp(-1,67));
        ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=col();content.setPadding(dp(15),dp(12),dp(15),dp(12));scroll.addView(content);
        if(voices.isEmpty()){content.addView(txt("Daily Voice Data မရှိပါ",17,PALE,true));FooterNav.attach(this,root,"Voice");return;}
        hero();gap(12);statistics();gap(17);
        LinearLayout tabs=row();tabs.addView(button("☀ TODAY",!calendarMode&&!favoritesMode,()->openDay(todayIndex())),new LinearLayout.LayoutParams(0,dp(42),1));tabs.addView(button("▦ CALENDAR",calendarMode,()->{calendarMode=true;favoritesMode=false;render();}),new LinearLayout.LayoutParams(0,dp(42),1));tabs.addView(button("♥ SAVED",favoritesMode,()->{calendarMode=false;favoritesMode=true;render();}),new LinearLayout.LayoutParams(0,dp(42),1));content.addView(tabs);gap(15);
        if(calendarMode)calendar();else if(favoritesMode)favoritesView();else dailyCard();
        gap(12);TextView notice=txt("AI ရေးသားထားသော နေ့စဉ်ဓမ္မအခြေပြု Reflection များသာဖြစ်ပြီး ကျမ်းစာမူရင်းမှ တိုက်ရိုက်ကောက်နုတ်ချက် မဟုတ်ပါ။ စာပေအကြောင်းအရာကို ဓမ္မကျွမ်းကျင်သူ ပြန်လည်စိစစ်သင့်ပါသည်။",10,MUTED,false);content.addView(notice);gap(15);
        FooterNav.attach(this,root,"Voice");
    }
    private void hero(){
        FrameLayout frame=new FrameLayout(this);frame.setBackground(shape(PANEL,22,0xffc39a59));frame.setClipToOutline(true);
        ImageView image=new ImageView(this);image.setImageResource(R.drawable.radio_pattana);image.setScaleType(ImageView.ScaleType.CENTER_CROP);frame.addView(image,new FrameLayout.LayoutParams(-1,-1));
        View overlay=new View(this);overlay.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xf004231f,0xd1082b25,0x3a173e2e}));frame.addView(overlay,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout data=col();data.setPadding(dp(20),dp(18),dp(15),dp(10));
        data.addView(txt("✦  A MOMENT OF DHAMMA",10,0xffffdea7,true));
        TextView lead=txt("တစ်နေ့တာဓမ္မအသံ\nစိတ်အေးချမ်းမှုခရီး",22,PALE,true);lead.setPadding(0,dp(8),0,dp(6));data.addView(lead);
        data.addView(txt("365 GUIDED REFLECTIONS",10,GOLD,true));frame.addView(data,new FrameLayout.LayoutParams(-1,-1));content.addView(frame,lp(-1,168));
    }
    private void statistics(){
        LinearLayout r=row();stat(r,"365","DAYS");stat(r,String.valueOf(listened.size()),"COMPLETED");stat(r,String.valueOf(favorites.size()),"SAVED");content.addView(r);
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(365);p.setProgress(listened.size());p.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));p.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff335b49));
        LinearLayout.LayoutParams par=lp(-1,7);par.topMargin=dp(11);content.addView(p,par);
        TextView caption=txt("365-Day Journey  •  "+(listened.size()*100/365)+"% ပြီးစီး",11,MUTED,false);caption.setPadding(0,dp(5),0,0);content.addView(caption);
    }
    private void stat(LinearLayout r,String value,String subtitle){LinearLayout t=col();t.setGravity(Gravity.CENTER);t.setPadding(dp(4),dp(9),dp(4),dp(9));t.setBackground(shape(PANEL,13,0xff665d40));TextView a=txt(value,22,GOLD,true);a.setGravity(Gravity.CENTER);t.addView(a);TextView b=txt(subtitle,9,MUTED,false);b.setGravity(Gravity.CENTER);t.addView(b);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(75),1);p.setMargins(dp(3),0,dp(3),0);r.addView(t,p);}
    private void dailyCard(){
        JSONObject x=current();
        LinearLayout titleRow=row();titleRow.addView(txt("DAY "+String.format(Locale.US,"%03d",x.optInt("day"))+" / 365",12,GOLD,true),new LinearLayout.LayoutParams(0,dp(35),1));
        titleRow.addView(txt(string(x,"date_label")+"  •  "+string(x,"theme"),11,MUTED,false));content.addView(titleRow);
        LinearLayout card=col();card.setPadding(dp(16),dp(17),dp(16),dp(16));card.setBackground(shape(PANEL,19,0xff876c49));
        TextView title=txt(string(x,"title"),19,PALE,true);card.addView(title);TextView series=txt(string(x,"series"),12,GOLD,true);series.setPadding(0,dp(6),0,dp(13));card.addView(series);
        TextView body=txt(string(x,"script"),14,PALE,false);body.setLineSpacing(dp(9),1.2f);body.setTextIsSelectable(true);card.addView(body);
        addCard(content,card);
        LinearLayout control=col();control.setPadding(dp(14),dp(12),dp(14),dp(13));control.setBackground(shape(0xff0d302c,17,0xff806845));
        TextView now=txt("♫  NATIVE VOICE PLAYER",12,GOLD,true);control.addView(now);
        playbackLabel=txt(ttsReady?"Burmese Voice Ready":"မြန်မာ TTS Engine စစ်ဆေးနေပါသည် ...",11,MUTED,false);playbackLabel.setPadding(0,dp(8),0,dp(13));control.addView(playbackLabel);
        LinearLayout transport=row();TextView prev=button("‹",false,()->openDay(index-1));TextView play=button(playing?"Ⅱ STOP":"▶ LISTEN",true,()->speak());TextView next=button("›",false,()->openDay(index+1));
        transport.addView(prev,new LinearLayout.LayoutParams(0,dp(49),1));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(49),2);mp.setMargins(dp(8),0,dp(8),0);transport.addView(play,mp);transport.addView(next,new LinearLayout.LayoutParams(0,dp(49),1));control.addView(transport);
        LinearLayout speeds=row();speeds.setPadding(0,dp(12),0,0);
        for(float choice:new float[]{.8f,1.0f,1.2f}){
            TextView speed=button(String.format(Locale.US,"%.1f×",choice),Math.abs(rate-choice)<.01f,()->{rate=choice;if(tts!=null)tts.setSpeechRate(rate);render();});
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(39),1);p.setMargins(dp(3),0,dp(3),0);speeds.addView(speed,p);
        }
        control.addView(speeds);addCard(content,control);
        LinearLayout tools=row();TextView heart=button(favorites.contains(key())?"♥ SAVED":"♡ FAVORITE",false,()->toggleFavorite());TextView done=button(listened.contains(key())?"✓ COMPLETED":"✓ MARK DONE",false,()->{markListened();render();});tools.addView(heart,new LinearLayout.LayoutParams(0,dp(47),1));LinearLayout.LayoutParams btnp=new LinearLayout.LayoutParams(0,dp(47),1);btnp.leftMargin=dp(9);tools.addView(done,btnp);content.addView(tools);gap(16);
        LinearLayout practice=col();practice.setPadding(dp(16),dp(15),dp(16),dp(14));practice.setBackground(shape(0xff18443a,17,0xff647151));practice.addView(txt("☸  TODAY'S PRACTICE",13,GOLD,true));
        TextView activity=txt(string(x,"practice"),14,PALE,false);activity.setPadding(0,dp(8),0,dp(12));practice.addView(activity);
        practice.addView(txt("✦  REFLECT TONIGHT",12,GOLD,true));TextView reflection=txt(string(x,"reflection"),13,PALE,false);reflection.setPadding(0,dp(8),0,0);practice.addView(reflection);content.addView(practice);
    }
    private void calendar(){
        TextView header=txt("DAY BY DAY · 365 DAYS",16,GOLD,true);content.addView(header);gap(12);
        HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);LinearLayout months=row();
        String[] labels={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
        for(int i=0;i<12;i++){final int selected=i+1;months.addView(chip(labels[i],month==selected,()->{month=selected;render();}));}
        scroll.addView(months);content.addView(scroll);gap(14);
        GridLayout dates=new GridLayout(this);dates.setColumnCount(7);dates.setUseDefaultMargins(false);dates.setBackground(shape(PANEL,17,0xff5d644b));dates.setPadding(dp(5),dp(11),dp(5),dp(11));
        String[] weekdays={"S","M","T","W","T","F","S"};for(String wd:weekdays){TextView t=txt(wd,11,MUTED,true);t.setGravity(Gravity.CENTER);GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(34);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);dates.addView(t,p);}
        Calendar base=new GregorianCalendar(2027,month-1,1);int offset=base.get(Calendar.DAY_OF_WEEK)-1;
        int length=base.getActualMaximum(Calendar.DAY_OF_MONTH);
        for(int n=0;n<offset;n++){TextView empty=txt("",12,MUTED,false);GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(48);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);dates.addView(empty,p);}
        for(int day=1;day<=length;day++){
            int num=day;int at=-1;String key=String.format(Locale.US,"%02d-%02d",month,day);
            for(int i=0;i<voices.size();i++)if(key.equals(string(voices.get(i),"date_label"))){at=i;break;}
            final int choice=at;
            TextView cell=txt(String.valueOf(num)+(at>=0&&listened.contains(String.valueOf(at+1))?" ✓":""),12,GOLD,true);cell.setGravity(Gravity.CENTER);
            boolean today=key.equals(todayKey());cell.setBackground(shape(today?0xff775f35:at==index?0xff315a43:PANEL,9,today?GOLD:0));
            GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(48);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(1),dp(1),dp(1),dp(1));dates.addView(cell,p);
            cell.setOnClickListener(v->{if(choice>=0)openDay(choice);});
        }
        content.addView(dates);gap(13);
        TextView note=txt("ရွှေရောင်အမှတ် = ယနေ့ · ✓ = နားထောင်ပြီး/ဖတ်ပြီး · ရက်စွဲကို နှိပ်၍ နေ့စဉ်ဓမ္မဖတ်နိုင်သည်။",11,MUTED,false);content.addView(note);
    }
    private void favoritesView(){
        TextView title=txt("♡  MY SAVED DHAMMA REFLECTIONS",16,GOLD,true);content.addView(title);gap(11);
        int n=0;for(int i=0;i<voices.size();i++){
            JSONObject v=voices.get(i);if(!favorites.contains(String.valueOf(v.optInt("day"))))continue;
            final int position=i;
            LinearLayout card=col();card.setPadding(dp(14),dp(12),dp(14),dp(12));card.setBackground(shape(PANEL,13,0xff5d6e54));
            card.addView(txt("DAY "+v.optInt("day")+"  ·  "+string(v,"title"),13,PALE,true));card.addView(txt(string(v,"theme"),11,GOLD,false));
            content.addView(card);gap(9);card.setOnClickListener(w->openDay(position));n++;
        }
        if(n==0)content.addView(txt("သိမ်းထားသောစာသား မရှိသေးပါ။ Voice ဖတ်ရှုပြီး ♥ Favorite ထည့်နိုင်သည်။",13,MUTED,false));
    }
    @Override public void onBackPressed(){if(calendarMode||favoritesMode){calendarMode=false;favoritesMode=false;render();}else super.onBackPressed();}
    @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}