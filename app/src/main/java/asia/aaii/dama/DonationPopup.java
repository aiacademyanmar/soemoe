package asia.aaii.dama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** DAMA LIGHT official contact popup. Only user-initiated links; no payment collection. */
public final class DonationPopup {
    private DonationPopup(){}
    private static final int BG=0xff062723, GOLD=0xffedc576, IVORY=0xfffff3dd, MUTED=0xffc4d2c7;
    private static int dp(Activity a,int n){return (int)(n*a.getResources().getDisplayMetrics().density+.5f);}
    private static LinearLayout col(Activity a){LinearLayout l=new LinearLayout(a);l.setOrientation(1);return l;}
    private static TextView text(Activity a,String str,int size,int color,boolean bold){
        TextView t=new TextView(a);t.setText(str);t.setTextColor(color);t.setTextSize(size);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private static GradientDrawable round(Activity a,int background,int radius,int stroke){
        GradientDrawable d=new GradientDrawable();d.setColor(background);d.setCornerRadius(dp(a,radius));
        if(stroke!=0)d.setStroke(dp(a,1),stroke);return d;
    }
    private static void open(Activity a,String url){
        try{
            Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
            a.startActivity(i);
        }catch(Exception e){
            new AlertDialog.Builder(a).setMessage("ဤလင့်ခ်ကို ဖွင့်နိုင်သော App မရှိပါ။\n"+url)
                .setPositiveButton("OK",null).show();
        }
    }
    private static void viber(Activity a){
        // Attempt the direct Viber chat URI, then fall back to Android Dialer.
        try{a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("viber://chat?number=%2B959252566954")));}
        catch(Exception notInstalled){
            try{a.startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:+959252566954")));}
            catch(Exception e){open(a,"https://www.viber.com/");}
        }
    }
    private static void contact(Activity a,LinearLayout box,String symbol,String title,String value,Runnable click){
        LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(a,12),dp(a,11),dp(a,12),dp(a,11));
        row.setBackground(round(a,0xff17433a,15,0xff7d6c49));
        TextView icon=text(a,symbol,22,GOLD,true);icon.setGravity(Gravity.CENTER);
        row.addView(icon,new LinearLayout.LayoutParams(dp(a,38),dp(a,48)));
        LinearLayout words=col(a);words.setPadding(dp(a,8),0,0,0);
        words.addView(text(a,title,13,IVORY,true));
        words.addView(text(a,value,13,GOLD,false));
        row.addView(words,new LinearLayout.LayoutParams(0,-2,1));
        TextView right=text(a,"›",29,GOLD,true);
        row.addView(right,new LinearLayout.LayoutParams(dp(a,18),-2));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,0,0,dp(a,9));box.addView(row,lp);
        PremiumMedia.tap(row,click);
    }
    public static void show(Activity a){
        LinearLayout body=col(a);body.setBackground(round(a,BG,22,GOLD));
        body.setPadding(dp(a,17),dp(a,17),dp(a,17),dp(a,14));
        LinearLayout branding=new LinearLayout(a);branding.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon=new ImageView(a);icon.setImageResource(R.drawable.dama_official_logo_icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        branding.addView(icon,new LinearLayout.LayoutParams(dp(a,86),dp(a,86)));
        LinearLayout header=col(a);header.setPadding(dp(a,10),0,0,0);
        header.addView(text(a,"DAMA LIGHT",20,GOLD,true));
        header.addView(text(a,"ဓမ္မအလင်း",19,GOLD,true));
        header.addView(text(a,"DONATE BY AAII.ASIA",11,MUTED,true));
        branding.addView(header);body.addView(branding);
        TextView heading=text(a,"ဓမ္မအလှူ ပံ့ပိုးကူညီရန်",17,IVORY,true);
        heading.setPadding(0,dp(a,13),0,dp(a,7));body.addView(heading);
        TextView message=text(a,"ဓမ္မစာပေ၊ တရားတော်များနှင့် ပညာရေးအကြောင်းအရာများကို အများပြည်သူ လေ့လာနိုင်ရန် ပံ့ပိုးကူညီပေးသော AAII.ASIA ကို ဆက်သွယ်နိုင်ပါသည်။\nအလှူငွေပေးရန် မဖြစ်မနေ မလိုအပ်ပါ။",12,MUTED,false);
        message.setLineSpacing(dp(a,3),1f);
        LinearLayout.LayoutParams ml=new LinearLayout.LayoutParams(-1,-2);
        ml.bottomMargin=dp(a,12);body.addView(message,ml);
        contact(a,body,"◎","AAII.ASIA","aaii.asia",()->open(a,"https://aaii.asia/"));
        contact(a,body,"◉","SOEMOE.AAII.ASIA","soemoe.aaii.asia",()->open(a,"https://soemoe.aaii.asia/"));
        contact(a,body,"☏","Viber • Open App","+95 9 252566954",()->viber(a));
        // LINE URL is user-supplied generic add-friends link; it may not identify the phone number directly.
        contact(a,body,"◉","LINE • Open App","+66 9 30409759",()->open(a,"https://line.me/R/nv/addFriends"));
        contact(a,body,"➤","Telegram • Open App","@myanmardata",()->open(a,"https://t.me/myanmardata"));
        TextView note=text(a,"LINE ခလုတ်သည် Add Friends စာမျက်နှာသို့ ဖွင့်ပေးပါမည်။ အကောင့်နှင့် ဖုန်းနံပါတ်ကို ထပ်မံအတည်ပြုရန် လိုနိုင်သည်။",10,MUTED,false);
        note.setPadding(0,dp(a,6),0,0);body.addView(note);
        ScrollView sc=new ScrollView(a);sc.setFillViewport(false);sc.addView(body);
        AlertDialog dialog=new AlertDialog.Builder(a).setView(sc)
            .setNegativeButton("ပိတ်မည်",null).create();
        dialog.show();
        if(dialog.getWindow()!=null){
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int)(a.getResources().getDisplayMetrics().widthPixels*.95f),-2);
        }
    }
}
