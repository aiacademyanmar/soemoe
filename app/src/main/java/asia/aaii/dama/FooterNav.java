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
    private static final int BG=0xff092b27, GOLD=0xffedc375, MUTED=0xffb8c5bb;
    private static int dp(Activity a,int n){return (int)(n*a.getResources().getDisplayMetrics().density+.5f);}
    public static void attach(Activity activity,LinearLayout root,String active){
        LinearLayout footer=new LinearLayout(activity);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(dp(activity,4),dp(activity,6),dp(activity,4),dp(activity,8));
        footer.setBackgroundColor(BG);
        String[] names={"Home","Radio","Library","Reader","TV"};
        String[] glyphs={"⌂","♫","▤","▣","▶"};
        Class<?>[] targets={HomeActivity.class,MainActivity.class,LibraryActivity.class,ReaderShelfActivity.class,TvActivity.class};
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
                if(names[index].equals(active))return;
                Intent route=new Intent(activity,targets[index]);
                route.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(route);
            });
        }
        root.addView(footer,new LinearLayout.LayoutParams(-1,dp(activity,79)));
    }
}
