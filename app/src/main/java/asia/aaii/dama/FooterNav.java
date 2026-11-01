package asia.aaii.dama;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared bottom navigation across DAMA's three media modules and offline Reader. */
public final class FooterNav {
    private FooterNav(){}
    private static long lastNavigationAt=0L;
    private static synchronized boolean allowNavigation(){long now=android.os.SystemClock.elapsedRealtime();if(now-lastNavigationAt<350L)return false;lastNavigationAt=now;return true;}
    private static final int BG=0xff092b27, GOLD=0xffedc375, MUTED=0xffb8c5bb;
    private static int dp(Activity a,int n){return (int)(n*a.getResources().getDisplayMetrics().density+.5f);}
    public static void attach(Activity activity,LinearLayout root,String active){
        LinearLayout footer=new LinearLayout(activity);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(dp(activity,4),dp(activity,6),dp(activity,4),dp(activity,8));
        footer.setBackgroundColor(BG);
        String[] names={"Home","Radio","Library","Reader","TV","Voice","AAII"};
        String[] glyphs={"⌂","♫","▤","▣","▶","☀","✦"};
        Class<?>[] targets={HomeActivity.class,MainActivity.class,LibraryActivity.class,ReaderShelfActivity.class,TvActivity.class,DailyVoiceActivity.class,null};
        for(int i=0;i<names.length;i++){
            final int index=i;
            boolean selected=names[i].equals(active);
            LinearLayout item=new LinearLayout(activity);item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);item.setPadding(0,dp(activity,4),0,dp(activity,4));
            if(selected){
                GradientDrawable bg=new GradientDrawable();bg.setColor(0xff225244);
                bg.setCornerRadius(dp(activity,12));bg.setStroke(dp(activity,1),0xffa48445);
                item.setBackground(bg);
            }
            TextView icon=new TextView(activity);icon.setText(glyphs[i]);
            icon.setTextColor(selected?GOLD:MUTED);icon.setTextSize(23);
            icon.setGravity(Gravity.CENTER);item.addView(icon);
            TextView title=new TextView(activity);title.setText(names[i]);
            title.setTextColor(selected?GOLD:MUTED);title.setTextSize(10);
            if(selected)title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            title.setGravity(Gravity.CENTER);item.addView(title);
            LinearLayout.LayoutParams param=new LinearLayout.LayoutParams(0,dp(activity,61),1);
            param.setMargins(dp(activity,2),0,dp(activity,2),0);
            footer.addView(item,param);
            item.setContentDescription(names[i]+" tab");
            item.setOnClickListener(view->{
                if(names[index].equals("AAII")){
                    showAaiiPopup(activity);
                    return;
                }
                if(names[index].equals(active)||!allowNavigation())return;
                Intent route=new Intent(activity,targets[index]);
                route.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(route);
            });
        }
        root.addView(footer,new LinearLayout.LayoutParams(-1,dp(activity,79)));
    private static void showAaiiPopup(Activity activity){
        final android.app.Dialog dialog=new android.app.Dialog(activity);
        LinearLayout panel=new LinearLayout(activity);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(activity,18),dp(activity,18),dp(activity,18),dp(activity,16));
        GradientDrawable shell=new GradientDrawable();
        shell.setColor(0xff092b27);shell.setCornerRadius(dp(activity,22));
        shell.setStroke(dp(activity,1),0xffa48445);panel.setBackground(shell);

        TextView heading=new TextView(activity);
        heading.setText("✦   AAII");
        heading.setTextColor(GOLD);heading.setTextSize(21);
        heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        panel.addView(heading,new LinearLayout.LayoutParams(-1,dp(activity,34)));

        TextView subtitle=new TextView(activity);
        subtitle.setText("AAII EDUCATION & DHAMMA SERVICES");
        subtitle.setTextColor(MUTED);subtitle.setTextSize(10);
        subtitle.setPadding(0,dp(activity,3),0,dp(activity,13));
        panel.addView(subtitle);

        addAaiiOption(activity,panel,"01   ☸  BUDDHA PSYCHOLOGY",
            "ဗုဒ္ဓ၏ စိတ်ပညာ • App Information & APK Download",
            "https://aaii.asia/dama/psychology/",dialog);
        addAaiiOption(activity,panel,"02   ▣  AAII E-BOOK LIBRARY",
            "စာအုပ်ပေါင်း ၅ သောင်းကျော် • 50,000+ Books",
            "https://www.aaii.asia/edubooks/",dialog);
        addAaiiOption(activity,panel,"03   ✦  AAII ONLINE SCHOOL",
            "သင်ခန်းစာပေါင်း ၁ သိန်းကျော် • 100,000+ Lessons",
            "https://aaii.asia/eduhome/",dialog);

        TextView close=new TextView(activity);
        close.setText("CLOSE   ✕");close.setTextColor(GOLD);close.setTextSize(12);
        close.setTypeface(Typeface.DEFAULT,Typeface.BOLD);close.setGravity(Gravity.CENTER);
        close.setPadding(0,dp(activity,12),0,dp(activity,3));
        panel.addView(close,new LinearLayout.LayoutParams(-1,dp(activity,38)));
        close.setOnClickListener(v->dialog.dismiss());

        dialog.setContentView(panel);
        dialog.setCancelable(true);
        dialog.show();
        android.view.Window window=dialog.getWindow();
        if(window!=null){
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            int width=activity.getResources().getDisplayMetrics().widthPixels-dp(activity,34);
            window.setLayout(Math.min(width,dp(activity,430)),android.view.WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
    }

    private static void addAaiiOption(Activity activity,LinearLayout parent,String title,String description,
                                      String url,android.app.Dialog dialog){
        LinearLayout card=new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity,14),dp(activity,12),dp(activity,12),dp(activity,12));
        GradientDrawable bg=new GradientDrawable();bg.setColor(0xff17443b);
        bg.setCornerRadius(dp(activity,14));bg.setStroke(dp(activity,1),0xff5e785f);
        card.setBackground(bg);
        TextView name=new TextView(activity);name.setText(title);name.setTextColor(GOLD);
        name.setTextSize(13);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        card.addView(name);
        TextView detail=new TextView(activity);detail.setText(description);detail.setTextColor(0xfff1eee3);
        detail.setTextSize(11);detail.setPadding(0,dp(activity,5),0,0);
        card.addView(detail);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,dp(activity,70));
        params.bottomMargin=dp(activity,9);parent.addView(card,params);
        card.setContentDescription(title+" ဖွင့်ရန်");
        card.setOnClickListener(v->{
            try{
                Intent intent=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url));
                activity.startActivity(intent);
                dialog.dismiss();
            }catch(android.content.ActivityNotFoundException e){
                android.widget.Toast.makeText(activity,"Link ဖွင့်၍မရပါ။ Internet နှင့် Browser ကို စစ်ဆေးပါ။",
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    }
}
