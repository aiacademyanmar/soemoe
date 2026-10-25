package asia.aaii.dama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.media.AudioAttributes;
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
public final class DailyVoiceActivity extends DamaActivity {
    private static final int DARK=0xff061e1c, DEEP=0xff092c27, PANEL=0xff133d35;
    private static final int GOLD=0xffefc576, PALE=0xfffff5df, MUTED=0xffb1c5b8, GREEN=0xff30644e;
    private final ArrayList<JSONObject> voices=new ArrayList<>();
    private LinearLayout root,content;
    private TextView playbackLabel;
    private SharedPreferences store;
    private Set<String> listened,favorites;
    private TextToSpeech tts;
    private boolean ttsReady=false,playing=false,calendarMode=false,favoritesMode=false,ttsOffline=false,ttsChecked=false;
    private String ttsState="မြန်မာ Voice စစ်ဆေးနေပါသည်...",activeSpeechId=null,ttsEngine="Not initialized";
    private long speechSequence=0;
    private final Locale MYANMAR=Locale.forLanguageTag("my-MM");
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
        tts=new TextToSpeech(this,status->runOnUiThread(()->configureMyanmarTts(status)));
        render();
    }
    private void configureMyanmarTts(int initStatus){
        ttsChecked=true;
        ttsReady=false;ttsOffline=false;
        if(tts==null||initStatus!=TextToSpeech.SUCCESS){
            ttsState="TTS Engine မဖွင့်နိုင်ပါ • Android Voice Settings ကို စစ်ဆေးပါ";
            setPlayback(ttsState);return;
        }
        try{
            ttsEngine=tts.getDefaultEngine()==null?"Unknown TTS Engine":tts.getDefaultEngine();
            Voice bestOffline=null,bestOnline=null;
            Set<Voice> installed=tts.getVoices();
            if(installed!=null)for(Voice voice:installed){
                if(voice==null||voice.getLocale()==null)continue;
                if(!"my".equalsIgnoreCase(voice.getLocale().getLanguage()))continue;
                if(!voice.isNetworkConnectionRequired()&&bestOffline==null)bestOffline=voice;
                else if(bestOnline==null)bestOnline=voice;
            }
            int available=tts.isLanguageAvailable(MYANMAR);
            if(available==TextToSpeech.LANG_MISSING_DATA||available==TextToSpeech.LANG_NOT_SUPPORTED){
                // Some TTS engines report an incomplete language list. Prefer an explicitly
                // exposed Burmese voice when present instead of falling back to English.
                if(bestOffline==null&&bestOnline==null){
                    ttsState="မြန်မာ (my-MM) Voice မရှိပါ • ဖုန်း TTS Settings မှ Install လုပ်ပါ";
                    setPlayback(ttsState);return;
                }
            }
            int result;
            if(bestOffline!=null){result=tts.setVoice(bestOffline);ttsOffline=result==TextToSpeech.SUCCESS;}
            else if(bestOnline!=null){result=tts.setVoice(bestOnline);ttsOffline=false;}
            else{
                result=tts.setLanguage(MYANMAR);
                Voice selected=tts.getVoice();
                ttsOffline=selected!=null&&!selected.isNetworkConnectionRequired();
            }
            ttsReady=result!=TextToSpeech.ERROR&&result!=TextToSpeech.LANG_MISSING_DATA&&
                     result!=TextToSpeech.LANG_NOT_SUPPORTED;
            if(!ttsReady){ttsState="မြန်မာ Voice မတပ်ဆင်ရသေးပါ • Voice Data ထည့်ရန်လိုအပ်သည်";}
            else{
                tts.setSpeechRate(rate);
                tts.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());
                ttsState=ttsOffline?"✓ မြန်မာ Offline TTS အသင့်ဖြစ်ပါပြီ":
                                    "✓ မြန်မာ TTS ရရှိသည် • အသံအတွက် Internet လိုအပ်နိုင်သည်";
            }
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){
                @Override public void onStart(String id){
                    runOnUiThread(()->{
                        if(activeSpeechId==null||id==null||!id.startsWith(activeSpeechId+"#"))return;
                        playing=true;setPlayback("♫ မြန်မာအသံ ဖတ်နေပါသည် • "+(ttsOffline?"Offline":"Engine"));
                    });
                }
                @Override public void onDone(String id){
                    runOnUiThread(()->{
                        if(activeSpeechId==null||id==null||!id.equals(activeSpeechId+"#LAST"))return;
                        activeSpeechId=null;playing=false;markListened();
                        ttsState="✓ Today's Voice ဖတ်ပြီးပါပြီ";render();
                    });
                }
                @Override public void onError(String id){failSpeech(id,"TTS Audio Error");}
                @Override public void onError(String id,int code){failSpeech(id,"TTS Error "+code);}
                @Override public void onStop(String id,boolean interrupted){
                    runOnUiThread(()->{if(id!=null&&activeSpeechId!=null&&id.startsWith(activeSpeechId+"#")){
                        playing=false;setPlayback("အသံဖတ်ခြင်း ရပ်ထားသည်");}});
                }
            });
        }catch(Exception e){
            ttsReady=false;ttsState="TTS Setting ပြဿနာ: "+e.getClass().getSimpleName();
            android.util.Log.w("DAMA_TTS","Myanmar voice selection failed",e);
        }
        setPlayback(ttsState);
    }
    private void failSpeech(String id,String detail){
        runOnUiThread(()->{
            if(activeSpeechId==null||id==null||!id.startsWith(activeSpeechId+"#"))return;
            activeSpeechId=null;playing=false;ttsState=detail+" • စာသားဖတ်ရှုနိုင်ပါသည်";
            setPlayback(ttsState);
        });
    }
    private void stopSpeech(){
        // Invalidate callback IDs BEFORE stop. A cancelled utterance must NEVER
        // mark a different daily reflection as finished.
        activeSpeechId=null;playing=false;
        if(tts!=null)try{tts.stop();}catch(Exception ignored){}
    }
    private void showTtsDiagnostics(){
        String selected="Unknown";boolean network=false;
        try{
            if(tts!=null&&tts.getVoice()!=null){
                selected=tts.getVoice().getName();network=tts.getVoice().isNetworkConnectionRequired();
            }
        }catch(Exception ignored){}
        String message="TTS Engine: "+ttsEngine+"\\nLanguage: Myanmar (my-MM)"+
          "\\nAvailable: "+(ttsReady?"YES":"NO")+
          "\\nVoice: "+selected+
          "\\nNetwork Voice: "+(network?"YES":"NO / Unknown")+
          "\\n\\nမြန်မာ Voice မရှိပါက Android TTS Settings မှ Language Data ကို ထည့်ပါ။ "+
          "မြန်မာဘာသာကို Device TTS Engine က မပံ့ပိုးလျှင် အသံဖတ်ပြနိုင်မည် မဟုတ်ပါ။ "+
          "App ထဲတွင် ကြိုတင်ဖန်တီးထားသော MP3 အသံဖိုင် မပါရှိပါ။";
        new AlertDialog.Builder(this).setTitle("DAMA DAILY VOICE • TTS CHECK")
          .setMessage(message)
          .setPositiveButton("CHECK AGAIN",(d,w)->{configureMyanmarTts(TextToSpeech.SUCCESS);render();})
          .setNeutralButton("TTS SETTINGS",(d,w)->openAndroidTtsSettings())
          .setNegativeButton("CLOSE",null).show();
    }
    private void openAndroidTtsSettings(){
        try{
            Intent settings=new Intent("com.android.settings.TTS_SETTINGS");
            startActivity(settings);
        }catch(Exception unavailable){
            try{startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));}
            catch(Exception ignored){toast("TTS Settings: Android Settings → Accessibility → Text-to-Speech ကိုဖွင့်ပါ");}
        }
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
        if(!ttsChecked||!ttsReady||tts==null){
            showTtsDiagnostics();return;
        }
        if(playing||tts.isSpeaking()){
            stopSpeech();ttsState="Ⅱ ဖတ်ရှုမှု ရပ်ထားသည် • ပြန်နှိပ်၍ အစမှစနိုင်သည်";
            setPlayback(ttsState);return;
        }
        String script=string(current(),"script").trim();
        if(script.length()<2){setPlayback("ဖတ်ရန်စာသား မရှိပါ");return;}
        stopSpeech();
        try{
            tts.setSpeechRate(rate);
            String token="DAMA_DAY_"+key()+"_"+(++speechSequence);
            activeSpeechId=token;
            // Native Android limits each utterance's size; split longer talks and
            // mark completion ONLY when the LAST chunk's callback is delivered.
            int chunkMax=Math.max(500,Math.min(2800,TextToSpeech.getMaxSpeechInputLength()-200));
            int cursor=0,part=0;
            while(cursor<script.length()){
                int next=Math.min(script.length(),cursor+chunkMax);
                if(next<script.length()){
                    int lastSpace=script.lastIndexOf(' ',next);
                    int lastStop=script.lastIndexOf('။',next);
                    int boundary=Math.max(lastSpace,lastStop);
                    if(boundary>cursor+400)next=boundary+1;
                }
                boolean last=next>=script.length();
                String id=token+"#"+(last?"LAST":String.valueOf(part++));
                String piece=script.substring(cursor,next);
                int status=tts.speak(piece,cursor==0?TextToSpeech.QUEUE_FLUSH:TextToSpeech.QUEUE_ADD,null,id);
                if(status==TextToSpeech.ERROR)throw new IllegalStateException("Speech synthesis rejected");
                cursor=next;
            }
            playing=true;ttsState="♫ မြန်မာအသံ ဖတ်ရန် ပြင်ဆင်နေသည်...";
            setPlayback(ttsState);
        }catch(Exception e){
            stopSpeech();ttsState="TTS စတင်မရပါ: "+e.getClass().getSimpleName();
            setPlayback(ttsState);android.util.Log.e("DAMA_TTS","speak failed",e);
        }
    }
    private void openDay(int at){if(at<0||at>=voices.size())return;stopSpeech();index=at;month=current().optInt("month",1);calendarMode=false;favoritesMode=false;render();}
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
        playbackLabel=txt(ttsState,11,MUTED,false);playbackLabel.setPadding(0,dp(8),0,dp(13));control.addView(playbackLabel);
        LinearLayout transport=row();TextView prev=button("‹",false,()->openDay(index-1));TextView play=button(playing?"■ STOP":"▶ LISTEN",true,()->speak());TextView next=button("›",false,()->openDay(index+1));
        transport.addView(prev,new LinearLayout.LayoutParams(0,dp(49),1));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(49),2);mp.setMargins(dp(8),0,dp(8),0);transport.addView(play,mp);transport.addView(next,new LinearLayout.LayoutParams(0,dp(49),1));control.addView(transport);
        LinearLayout speeds=row();speeds.setPadding(0,dp(12),0,0);
        for(float choice:new float[]{.8f,1.0f,1.2f}){
            TextView speed=button(String.format(Locale.US,"%.1f×",choice),Math.abs(rate-choice)<.01f,()->{rate=choice;if(tts!=null)tts.setSpeechRate(rate);render();});
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(39),1);p.setMargins(dp(3),0,dp(3),0);speeds.addView(speed,p);
        }
        control.addView(speeds);
        TextView check=button("⚙  MYANMAR TTS CHECK / VOICE SETTINGS",false,()->showTtsDiagnostics());
        LinearLayout.LayoutParams cp=lp(-1,46);cp.topMargin=dp(11);
        control.addView(check,cp);
        addCard(content,control);
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
    @Override protected void onPause(){stopSpeech();super.onPause();}
    @Override protected void onResume(){
        super.onResume();
        if(ttsChecked&&tts!=null&&!playing){
            // Engine voice packs may have been installed in Android settings.
            configureMyanmarTts(TextToSpeech.SUCCESS);
        }
    }
    @Override protected void onDestroy(){stopSpeech();if(tts!=null)tts.shutdown();super.onDestroy();}
}