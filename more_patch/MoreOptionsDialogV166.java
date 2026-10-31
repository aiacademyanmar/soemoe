package asia.aaii.buddhapsychology;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.function.IntConsumer;

/** Visual-only V1.6.6 More menu. Original eleven click actions are passed through unchanged. */
public final class MoreOptionsDialog {
  private MoreOptionsDialog() {}
  static final int BG=0xff161f20, CARD=0xff273333, BORDER=0xff48534d,
    GOLD=0xffe9bd68, LIGHT=0xffffe8b7, WHITE=0xfffcf7ed, MUTED=0xffb7c2bc;
  static final String[] ICONS={"▤","☸","♡","▣","▶","♫","◷","✦","◈","✧","⚙"};
  static final String[] TITLES={
    "Native စာသားဖတ်ရန်","အခန်း ၉ ခန်း ကြည့်ရန်","စိတ်ခံစားမှု အကြံပြုချက်",
    "PDF မူရင်း • ၈၄ မျက်နှာ","Video Course • Online","Audio Book • Offline",
    "စာဖတ်မှု အခြေအနေ","ChatGPT / Gemini Live AI","စိတ်အခြေအနေ ၂၄၀",
    "AI Module • 500 Chapters","User Account / Settings"};
  static final String[] SUB={
    "Native text reader","All book chapters","Mindfulness & emotional guidance",
    "Open the original PDF","Watch your video lessons","Listen to nine audio tracks",
    "Continue from your last page","Connect to the AI assistant","Explore sixteen categories",
    "Explore learning modules","Profile, subscription & preferences"};
  static int dp(Activity a,float n){return (int)(a.getResources().getDisplayMetrics().density*n+.5f);}
  static GradientDrawable bg(Activity a,int color,int radius,int line){
    GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(a,radius));
    if(line!=0)d.setStroke(dp(a,1),line);return d;
  }
  static TextView text(Activity a,String s,int size,int color,boolean bold){
    TextView t=new TextView(a);t.setText(s);t.setTextSize(size);t.setTextColor(color);
    t.setGravity(Gravity.CENTER_VERTICAL);t.setIncludeFontPadding(true);
    if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
  }
  static LinearLayout col(Activity a){LinearLayout l=new LinearLayout(a);l.setOrientation(1);return l;}
  static void category(Activity a,LinearLayout into,String title,String my){
    LinearLayout r=new LinearLayout(a);r.setGravity(Gravity.CENTER_VERTICAL);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(a,31));
    p.setMargins(dp(a,4),dp(a,11),dp(a,4),dp(a,4));into.addView(r,p);
    TextView h=text(a,title,12,LIGHT,true);h.setLetterSpacing(.04f);
    r.addView(h,new LinearLayout.LayoutParams(0,-1,1));
    r.addView(text(a,my,10,MUTED,false));
  }
  static void option(Activity a,LinearLayout parent,int i,IntConsumer action,Dialog dialog){
    LinearLayout item=new LinearLayout(a);item.setGravity(Gravity.CENTER_VERTICAL);
    item.setPadding(dp(a,12),dp(a,8),dp(a,12),dp(a,8));
    item.setBackground(new RippleDrawable(ColorStateList.valueOf(0x39e9bd68),bg(a,CARD,15,BORDER),null));
    item.setFocusable(true);item.setClickable(true);item.setContentDescription(TITLES[i]);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,69));lp.bottomMargin=dp(a,7);
    parent.addView(item,lp);
    TextView symbol=text(a,ICONS[i],24,GOLD,true);symbol.setGravity(Gravity.CENTER);
    symbol.setBackground(bg(a,0xff39433c,12,0));
    LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(a,45),dp(a,45));
    ip.rightMargin=dp(a,12);item.addView(symbol,ip);
    LinearLayout names=col(a);names.setGravity(Gravity.CENTER_VERTICAL);
    item.addView(names,new LinearLayout.LayoutParams(0,-1,1));
    TextView title=text(a,TITLES[i],14,WHITE,true);title.setMaxLines(1);
    title.setEllipsize(android.text.TextUtils.TruncateAt.END);names.addView(title);
    TextView sub=text(a,SUB[i],11,MUTED,false);sub.setMaxLines(1);
    sub.setEllipsize(android.text.TextUtils.TruncateAt.END);names.addView(sub);
    TextView arrow=text(a,"›",27,GOLD,false);arrow.setGravity(Gravity.CENTER);
    item.addView(arrow,new LinearLayout.LayoutParams(dp(a,24),-1));
    final int idx=i;item.setOnClickListener(v->{dialog.dismiss();action.accept(idx);});
  }
  public static void show(Activity a,IntConsumer action){
    if(a.isFinishing()||a.isDestroyed())return;
    Dialog dialog=new Dialog(a);
    LinearLayout shell=col(a);shell.setBackground(bg(a,BG,25,0xff675537));
    shell.setPadding(dp(a,16),dp(a,17),dp(a,16),dp(a,12));
    LinearLayout top=new LinearLayout(a);top.setGravity(Gravity.CENTER_VERTICAL);
    shell.addView(top,new LinearLayout.LayoutParams(-1,dp(a,65)));
    TextView emblem=text(a,"☸",26,GOLD,true);emblem.setGravity(Gravity.CENTER);
    emblem.setBackground(bg(a,0xff3a3426,15,0xff775a31));
    LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(dp(a,52),dp(a,52));
    ep.rightMargin=dp(a,12);top.addView(emblem,ep);
    LinearLayout heading=col(a);heading.setGravity(Gravity.CENTER_VERTICAL);
    top.addView(heading,new LinearLayout.LayoutParams(0,-1,1));
    heading.addView(text(a,"More Options",23,LIGHT,true));
    heading.addView(text(a,"နောက်ထပ် လုပ်ဆောင်ချက်များ",12,MUTED,false));
    TextView close=text(a,"✕",19,WHITE,true);close.setGravity(Gravity.CENTER);
    close.setContentDescription("Close More Options");close.setFocusable(true);
    close.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44e9bd68),bg(a,0xff32403f,12,0),null));
    top.addView(close,new LinearLayout.LayoutParams(dp(a,43),dp(a,43)));
    close.setOnClickListener(v->dialog.dismiss());
    LinearLayout intro=new LinearLayout(a);intro.setGravity(Gravity.CENTER_VERTICAL);
    intro.setPadding(dp(a,10),dp(a,8),dp(a,10),dp(a,8));
    intro.setBackground(bg(a,0xff302b22,11,0xff665335));
    LinearLayout.LayoutParams introP=new LinearLayout.LayoutParams(-1,-2);
    introP.topMargin=dp(a,10);shell.addView(intro,introP);
    TextView mini=text(a,"BUDDHA PSYCHOLOGY",10,LIGHT,true);mini.setLetterSpacing(.12f);
    intro.addView(mini,new LinearLayout.LayoutParams(0,-2,1));
    intro.addView(text(a,"11 FEATURES",10,GOLD,true));
    ScrollView scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);
    int max=(int)(a.getResources().getDisplayMetrics().heightPixels*.61f);
    LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,Math.min(dp(a,600),max));
    sp.topMargin=dp(a,5);shell.addView(scroll,sp);
    LinearLayout items=col(a);scroll.addView(items,new ScrollView.LayoutParams(-1,-2));
    category(a,items,"READ • WATCH • LISTEN","သင်ယူရန်");
    for(int i=0;i<6;i++)option(a,items,i,action,dialog);
    category(a,items,"PROGRESS • AI","လေ့လာမှုနှင့် AI");
    for(int i=6;i<10;i++)option(a,items,i,action,dialog);
    category(a,items,"MY ACCOUNT","အကောင့်");option(a,items,10,action,dialog);
    TextView footer=text(a,"ဗုဒ္ဓ၏ စိတ်ပညာ  ·  V1.6.6",11,MUTED,false);
    footer.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(a,31));
    fp.topMargin=dp(a,5);shell.addView(footer,fp);
    dialog.setContentView(shell);
    Window w=dialog.getWindow();if(w!=null)w.setBackgroundDrawableResource(android.R.color.transparent);
    dialog.show();w=dialog.getWindow();
    if(w!=null){w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
      WindowManager.LayoutParams at=w.getAttributes();at.dimAmount=.63f;at.gravity=Gravity.CENTER;
      w.setAttributes(at);int width=a.getResources().getDisplayMetrics().widthPixels-2*dp(a,13);
      w.setLayout(width,WindowManager.LayoutParams.WRAP_CONTENT);}
  }
}
