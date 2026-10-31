package asia.aaii.buddhapsychology;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.function.IntConsumer;
/** Visual update for More popup; original UiKit actions are preserved verbatim. */
public final class MoreOptionsDialog {
  private MoreOptionsDialog(){}
  static int dp(Activity a,int p){return (int)(p*a.getResources().getDisplayMetrics().density+.5f);}
  static GradientDrawable bg(Activity a,int color,int r,int line){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(a,r));if(line!=0)d.setStroke(dp(a,1),line);return d;}
  static TextView txt(Activity a,String s,int size,int color,boolean strong){TextView v=new TextView(a);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(strong)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
  static final String[] LABELS={"Native စာသားဖတ်ရန်","အခန်း ၉ ခန်း ကြည့်ရန်","စိတ်ခံစားမှုအကြံပြုချက်","PDF မူရင်း • ၈၄ မျက်နှာ","Video Course • Online","Audio Book • Offline","စာဖတ်မှုအခြေအနေ","ChatGPT / Gemini Live AI","စိတ်အခြေအနေ ၂၄၀","AI Module ၁ ခု • 500 Chapters","User Account / Settings"};
  static final String[] ICONS={"▤","☸","♡","▣","▶","♫","◷","✦","◈","✧","⚙"};
  static final String[] DESCRIPTIONS={"Native Book Reader","Book Chapters","Buddha Guidance","Original PDF Reader","Online video learning","Offline audio listening","Reading progress","AI Chat Assistant","Mood catalog","Learning modules","Profile and preferences"};
  public static void show(Activity a, IntConsumer originalAction){
    final int GOLD=0xffecc589, MUTED=0xffb5c1c4;
    final Dialog dialog=new Dialog(a);
    LinearLayout shell=new LinearLayout(a);shell.setOrientation(LinearLayout.VERTICAL);shell.setBackground(bg(a,0xff151f24,24,0xff6c5b40));
    shell.setPadding(dp(a,14),dp(a,16),dp(a,14),dp(a,12));
    LinearLayout header=new LinearLayout(a);header.setGravity(Gravity.CENTER_VERTICAL);shell.addView(header);
    LinearLayout names=new LinearLayout(a);names.setOrientation(LinearLayout.VERTICAL);header.addView(names,new LinearLayout.LayoutParams(0,-2,1));
    names.addView(txt(a,"More Options",23,GOLD,true)); names.addView(txt(a,"နောက်ထပ် လုပ်ဆောင်ချက်များ",12,MUTED,false));
    TextView close=txt(a,"✕",20,Color.WHITE,true);close.setGravity(Gravity.CENTER);close.setContentDescription("Close More menu");close.setBackground(bg(a,0xff23333a,12,0xff44555a));
    header.addView(close,new LinearLayout.LayoutParams(dp(a,42),dp(a,42)));close.setOnClickListener(v->dialog.dismiss());
    View line=new View(a);line.setBackgroundColor(0xff5b513e);
    LinearLayout.LayoutParams divider=new LinearLayout.LayoutParams(-1,dp(a,1));divider.topMargin=dp(a,12);divider.bottomMargin=dp(a,10);shell.addView(line,divider);
    ScrollView scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);
    int height=Math.min(dp(a,600),(int)(a.getResources().getDisplayMetrics().heightPixels*.68f));
    shell.addView(scroll,new LinearLayout.LayoutParams(-1,height));
    LinearLayout list=new LinearLayout(a);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);
    for(int i=0;i<LABELS.length;i++){
      final int index=i;
      LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(a,11),dp(a,10),dp(a,10),dp(a,10));row.setBackground(bg(a,0xff23333a,15,0xff44555a));
      LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=dp(a,7);list.addView(row,rp);
      TextView sym=txt(a,ICONS[i],22,GOLD,true);sym.setGravity(Gravity.CENTER);sym.setBackground(bg(a,0xff37464a,12,0));
      LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(a,44),dp(a,44));ip.rightMargin=dp(a,11);row.addView(sym,ip);
      LinearLayout copy=new LinearLayout(a);copy.setOrientation(LinearLayout.VERTICAL);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
      copy.addView(txt(a,LABELS[i],14,Color.WHITE,true));copy.addView(txt(a,DESCRIPTIONS[i],11,MUTED,false));
      TextView arrow=txt(a,"›",25,GOLD,false);row.addView(arrow);row.setContentDescription(LABELS[i]);
      row.setOnClickListener(v->{dialog.dismiss();originalAction.accept(index);});
    }
    TextView footer=txt(a,"Buddha Psychology • V1.6.5",11,MUTED,false);footer.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);fp.topMargin=dp(a,8);shell.addView(footer,fp);
    dialog.setContentView(shell);dialog.show();
    Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams at=w.getAttributes();at.dimAmount=.55f;w.setAttributes(at);w.setLayout((int)(a.getResources().getDisplayMetrics().widthPixels*.93f),-2);}
  }
}
