package asia.aaii.dama;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

/**
 * DAMA V19: Sayadaw portrait -> native profile popup -> relevant books/MP3/videos.
 * Numbers are derived from bundled metadata, not confirmed working links.
 * No invented biography or fabricated portrait attribution.
 */
public final class SayadawProfilePopup {
    private SayadawProfilePopup() {}
    private static final int GOLD=0xffe9be74, IVORY=0xfffff7e5, MUTED=0xffc4d2c5,
      BG=0xff082b26, PANEL=0xff143b32, BUTTON=0xff255445;
    public static final String[] KEYS={"မိုးကုတ်","မဟာစည်","နန္ဒမာလာ"};
    public static final String[] NAMES={"မိုးကုတ်ဆရာတော် ဦးဝိမလ",
      "မဟာစည်ဆရာတော် ဦးသောဘန","ပါမောက္ခချုပ်ဆရာတော် နန္ဒမာလာဘိဝံသ"};
    public static final int[] RADIO_MONK_IDS={1,2,4};
    public static final int[] PHOTOS={
      R.drawable.mogok_sayadaw,R.drawable.mahasi_sayadaw,R.drawable.nandamala_sayadaw
    };

    private static final ExecutorService WORK=Executors.newSingleThreadExecutor();
    private static final Handler UI=new Handler(Looper.getMainLooper());
    private static final Map<Integer,int[]> CACHED=new HashMap<>();

    private static int dp(Activity a,int n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
    private static LinearLayout col(Activity a){LinearLayout x=new LinearLayout(a);x.setOrientation(1);return x;}
    private static LinearLayout row(Activity a){LinearLayout x=new LinearLayout(a);x.setOrientation(0);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    private static TextView label(Activity a,String value,int size,int color,boolean bold){
       TextView x=new TextView(a);x.setText(value);x.setTextColor(color);x.setTextSize(size);
       if(bold)x.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return x;
    }
    private static GradientDrawable shape(Activity a,int bg,int r,int line){
       GradientDrawable g=new GradientDrawable();g.setColor(bg);g.setCornerRadius(dp(a,r));
       if(line!=0)g.setStroke(dp(a,1),line);return g;
    }
    private static void button(Activity a,LinearLayout layout,String value,Runnable action){
       TextView control=label(a,value,14,IVORY,true);
       control.setGravity(Gravity.CENTER);control.setPadding(dp(a,12),dp(a,14),dp(a,12),dp(a,14));
       control.setBackground(shape(a,BUTTON,14,0xff987c4e));
       LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
       p.setMargins(0,0,0,dp(a,9));layout.addView(control,p);
       PremiumMedia.tap(control,action);
    }
    private static JSONArray json(android.content.Context ctx,String file,boolean zip)throws IOException,org.json.JSONException {
       try(InputStream original=ctx.getAssets().open(file);
           InputStream in=zip?new GZIPInputStream(original):original;
           ByteArrayOutputStream b=new ByteArrayOutputStream()){
           byte[] tmp=new byte[32768];int n;
           while((n=in.read(tmp))!=-1){
               if(b.size()+n>25_000_000)throw new IOException("Catalog size limit");
               b.write(tmp,0,n);
           }
           return new JSONArray(b.toString("UTF-8"));
       }
    }
    // An indexed count is NOT an assurance that the audio or video is playable online.
    private static int[] countCatalog(android.content.Context context,int index){
        synchronized(CACHED){
            int[] cached=CACHED.get(index);
            if(cached!=null)return cached.clone();
        }
        int mp3=0,video=0;int radioId=RADIO_MONK_IDS[index];
        String key=KEYS[index];
        try{
            JSONArray tracks=json(context,"radio_catalog.dat",true);
            for(int i=0;i<tracks.length();i++){
               JSONObject t=tracks.optJSONObject(i);
               if(t!=null&&t.optInt("monk_id",-1)==radioId)mp3++;
            }
        }catch(Exception e){android.util.Log.w("DAMA_PROFILE","MP3 metadata count failed",e);mp3=-1;}
        try{
            JSONArray videos=json(context,"tv_catalog.json",false);
            for(int i=0;i<videos.length();i++){
               JSONObject v=videos.optJSONObject(i);
               if(v!=null&&v.optString("sayadaw").contains(key))video++;
            }
        }catch(Exception e){android.util.Log.w("DAMA_PROFILE","Video metadata count failed",e);video=-1;}
        int[] result={mp3,video};
        synchronized(CACHED){CACHED.put(index,result.clone());}
        return result;
    }
    public static void open(LibraryActivity a,int index,int bookCount,Runnable viewBooks){
        if(index<0||index>=KEYS.length)return;
        // Construct the profile synchronously; count large audio/video catalogs off-thread.
        LinearLayout panel=col(a);panel.setPadding(dp(a,18),dp(a,17),dp(a,18),dp(a,13));
        panel.setBackground(shape(a,BG,23,GOLD));
        LinearLayout identity=row(a);
        ImageView picture=new ImageView(a);picture.setImageResource(PHOTOS[index]);
        picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        picture.setBackground(shape(a,0xff335444,55,GOLD));picture.setClipToOutline(true);
        identity.addView(picture,new LinearLayout.LayoutParams(dp(a,95),dp(a,106)));
        LinearLayout heading=col(a);heading.setPadding(dp(a,13),dp(a,6),0,0);
        heading.addView(label(a,"✦  SAYADAW PROFILE",11,GOLD,true));
        TextView name=label(a,NAMES[index],17,IVORY,true);name.setPadding(0,dp(a,7),0,0);
        heading.addView(name);
        TextView sub=label(a,"ဓမ္မစာပေ • အသံတရား • ဗီဒီယို",11,MUTED,false);
        sub.setPadding(0,dp(a,5),0,0);heading.addView(sub);
        identity.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        panel.addView(identity);
        TextView divider=label(a,"────────────────────────",11,0xff847252,false);
        divider.setGravity(Gravity.CENTER);divider.setPadding(0,dp(a,8),0,dp(a,8));panel.addView(divider);
        LinearLayout stats=row(a);
        String[] titles={"စာအုပ်","MP3","VIDEO"};
        TextView[] countLabels=new TextView[3];
        for(int i=0;i<3;i++){
            LinearLayout tile=col(a);tile.setGravity(Gravity.CENTER);
            tile.setPadding(dp(a,4),dp(a,11),dp(a,4),dp(a,11));
            tile.setBackground(shape(a,PANEL,12,0xff52644d));
            countLabels[i]=label(a,i==0?String.format(Locale.US,"%,d",bookCount):"…",20,GOLD,true);
            countLabels[i].setGravity(Gravity.CENTER);tile.addView(countLabels[i]);
            TextView caption=label(a,titles[i],11,IVORY,true);caption.setGravity(Gravity.CENTER);tile.addView(caption);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(a,74),1);
            p.setMargins(dp(a,2),0,dp(a,2),0);stats.addView(tile,p);
        }
        panel.addView(stats);
        TextView note=label(a,"လက်ရှိ APK Catalog မှ အမည်ကိုက်ညီသော စာရင်းအရေအတွက်များဖြစ်ပြီး မူရင်းလင့်ခ်တိုင်း ဖွင့်နိုင်ကြောင်း အတည်မပြုရသေးပါ။",11,MUTED,false);
        note.setPadding(dp(a,2),dp(a,10),dp(a,2),dp(a,14));panel.addView(note);

        AlertDialog dialog=new AlertDialog.Builder(a).setView(panel).create();
        button(a,panel,"▣  စာအုပ်များ ကြည့်ရန်  ("+bookCount+" အုပ်)",()->{
            dialog.dismiss();viewBooks.run();
        });
        button(a,panel,"♫  MP3 တရားတော်များ နားထောင်ရန်",()->{
            dialog.dismiss();
            Intent intent=new Intent(a,MainActivity.class);
            intent.putExtra("dama_profile_monk_id",RADIO_MONK_IDS[index]);
            a.startActivity(intent);
        });
        button(a,panel,"▶  YouTube ဗီဒီယိုများ ကြည့်ရန်",()->{
            dialog.dismiss();
            Intent intent=new Intent(a,TvActivity.class);
            intent.putExtra("dama_profile_sayadaw_key",KEYS[index]);
            a.startActivity(intent);
        });
        TextView source=label(a,"ဓာတ်ပုံ: Wikimedia Commons • မူပိုင်ခွင့်/Attribution ကို App Credits တွင် ကြည့်နိုင်ပါသည်။",10,MUTED,false);
        source.setPadding(0,dp(a,3),0,dp(a,5));panel.addView(source);
        TextView close=label(a,"✕  ပိတ်မည်",13,GOLD,true);close.setGravity(Gravity.CENTER);
        close.setPadding(0,dp(a,8),0,dp(a,2));panel.addView(close);
        PremiumMedia.tap(close,dialog::dismiss);
        dialog.show();
        if(dialog.getWindow()!=null)dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        int[] existing;
        synchronized(CACHED){existing=CACHED.get(index);}
        if(existing!=null){
            countLabels[1].setText(existing[0]<0?"—":String.format(Locale.US,"%,d",existing[0]));
            countLabels[2].setText(existing[1]<0?"—":String.format(Locale.US,"%,d",existing[1]));
            return;
        }
        WORK.execute(()->{
            int[] counts=countCatalog(a.getApplicationContext(),index);
            UI.post(()->{
                if(a.isFinishing()||a.isDestroyed()||!dialog.isShowing())return;
                countLabels[1].setText(counts[0]<0?"—":String.format(Locale.US,"%,d",counts[0]));
                countLabels[2].setText(counts[1]<0?"—":String.format(Locale.US,"%,d",counts[1]));
            });
        });
    }
}
