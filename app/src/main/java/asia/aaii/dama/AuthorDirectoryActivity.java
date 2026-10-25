package asia.aaii.dama;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.io.*;
import java.util.zip.GZIPInputStream;

/** DAMA Library - real, navigable author/source-collection directory. */
public final class AuthorDirectoryActivity extends Activity {
    private static final int BG=0xff031d1b, CARD=0xff10372f, PANEL=0xff092824;
    private static final int GOLD=0xffe8bd76, IVORY=0xfffff5df, MUTED=0xffa9bcb1, BORDER=0xff4d6451;
    private final ArrayList<Profile> profiles=new ArrayList<>();
    private LinearLayout cardList; private TextView status;
    private String searchText="", kind="all", chosenCategory="အားလုံး";
    private int bookTotal=0, attributed=0, grouped=0;
    private static final class Profile {
        String name, type; int count=0; final TreeMap<String,Integer> cat=new TreeMap<>();
        Profile(String name,String type){this.name=name;this.type=type;}
        void add(String category){count++;cat.put(category,cat.getOrDefault(category,0)+1);}
    }
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
    private LinearLayout vert(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    private LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable x=new GradientDrawable();x.setColor(color);x.setCornerRadius(dp(radius));if(stroke!=0)x.setStroke(dp(1),stroke);return x;}
    private TextView text(String s,int sp,int color,boolean bold){TextView x=new TextView(this);x.setText(s);x.setTextSize(sp);x.setTextColor(color);x.setGravity(Gravity.CENTER_VERTICAL);if(bold)x.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return x;}
    private TextView pill(String s,Runnable run){TextView v=text(s,11,GOLD,true);v.setPadding(dp(12),dp(8),dp(12),dp(8));v.setGravity(Gravity.CENTER);v.setBackground(shape(PANEL,22,BORDER));v.setOnClickListener(w->run.run());return v;}
    private void load(){
        LinkedHashMap<String,Profile> map=new LinkedHashMap<>();
        try(InputStream stream=new GZIPInputStream(getAssets().open("library_catalog.dat"))){
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] bytes=new byte[16384];int n;
            while((n=stream.read(bytes))>=0){if(n>0)out.write(bytes,0,n);}
            JSONArray arr=new JSONArray(out.toString("UTF-8"));
            for(int i=0;i<arr.length();i++){
                JSONObject record=arr.optJSONObject(i);if(record==null)continue;
                String name=record.optString("author","").trim();
                if(name.isEmpty())name="အမည်မဖော်ပြထားသော စာအုပ်များ";
                String type=record.optString("author_type", "person_label");
                String cat=record.optString("category","ဓမ္မစာအုပ်");
                Profile p=map.get(name);if(p==null){p=new Profile(name,type);map.put(name,p);}
                p.add(cat);bookTotal++;
                if("collection".equals(type))grouped++;else attributed++;
            }
        }catch(Exception e){Toast.makeText(this,"စာအုပ်စာရင်းဖွင့်မရပါ: "+e.getMessage(),Toast.LENGTH_LONG).show();}
        profiles.addAll(map.values());profiles.sort((a,b)->Integer.compare(b.count,a.count));
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff00100d);getWindow().setNavigationBarColor(0xff00100d);load();draw();}
    private void draw(){
        LinearLayout root=vert();root.setBackgroundColor(BG);setContentView(root);
        LinearLayout bar=row();bar.setPadding(dp(14),dp(13),dp(14),dp(11));bar.setBackgroundColor(0xff001914);
        TextView back=text("‹",32,GOLD,true);back.setGravity(Gravity.CENTER);bar.addView(back,new LinearLayout.LayoutParams(dp(42),dp(44)));back.setOnClickListener(v->finish());
        LinearLayout title=vert();title.addView(text("ဓမ္မစာရေးသူများ",19,GOLD,true));title.addView(text("DAMA LIBRARY · AUTHOR DIRECTORY",9,MUTED,true));bar.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView badge=text("✦",25,GOLD,true);badge.setGravity(Gravity.CENTER);bar.addView(badge,new LinearLayout.LayoutParams(dp(42),dp(42)));root.addView(bar);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout column=vert();column.setPadding(dp(14),dp(12),dp(14),dp(20));scroll.addView(column);
        LinearLayout hero=vert();hero.setPadding(dp(18),dp(15),dp(18),dp(16));hero.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff123e34,0xff05231f,0xff182b21}));
        hero.addView(text("စာရေးသူ · ဆရာတော် · ကျမ်းစုစည်းမှု",17,IVORY,true));
        hero.addView(text("ဓမ္မစာအုပ် ၁,၈၉၄ အုပ်ကို စာရေးသူအုပ်စု / Category အလိုက် လွယ်ကူစွာ ရွေးဖတ်နိုင်ပါသည်။",11,MUTED,false));
        column.addView(hero,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout stats=row();stats.setPadding(0,dp(12),0,dp(8));
        addStat(stats,String.valueOf(profiles.size()),"နာမည် / အုပ်စု");addStat(stats,String.valueOf(attributed),"စာရေးသူအမည်စာရင်း");addStat(stats,String.valueOf(grouped),"စာအုပ်စုစည်းမှု");column.addView(stats);
        LinearLayout progressBox=vert();progressBox.setPadding(dp(13),dp(12),dp(13),dp(14));progressBox.setBackground(shape(CARD,16,BORDER));
        progressBox.addView(text("CATALOG LINKING PROGRESS",12,GOLD,true));
        addProgress(progressBox,"စာရေးသူ / စုစည်းမှု Label ချိတ်ဆက်မှု",bookTotal,bookTotal);
        addProgress(progressBox,"Category ချိတ်ဆက်မှု",bookTotal,bookTotal);
        addProgress(progressBox,"စာရေးသူအမည်အောက် စုစည်းထားသောစာအုပ်",attributed,bookTotal);
        progressBox.addView(text("ဤ Progress က Catalog ချိတ်ဆက်မှုကိုသာ ဖော်ပြသည်။ ဆရာတော်အမည်မှန်ကန်မှု / PDF Link ဖွင့်နိုင်မှုကို သီးခြားအတည်ပြုရန် လိုသည်။",10,MUTED,false));
        LinearLayout.LayoutParams prog=new LinearLayout.LayoutParams(-1,-2);prog.setMargins(0,dp(6),0,dp(12));column.addView(progressBox,prog);
        EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(13);search.setTextColor(0xff14291e);search.setHintTextColor(0xff727b70);
        search.setPadding(dp(14),dp(7),dp(14),dp(7));search.setBackground(shape(IVORY,14,0));search.setHint("⌕ ဆရာတော် / စာရေးသူ အမည်ရှာရန်...");
        column.addView(search,new LinearLayout.LayoutParams(-1,dp(51)));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){searchText=s.toString();render();}public void afterTextChanged(Editable t){}});
        LinearLayout kindPills=row();kindPills.setPadding(0,dp(10),0,dp(8));
        addPill(kindPills,"အားလုံး", "all");addPill(kindPills,"ဆရာတော်/စာရေးသူ (၄၉)","person_label");addPill(kindPills,"ကျမ်းစုများ (၁၅)","collection");column.addView(kindPills);
        HorizontalScrollView cv=new HorizontalScrollView(this);cv.setHorizontalScrollBarEnabled(false);LinearLayout chips=row();
        ArrayList<String> cats=new ArrayList<>();cats.add("အားလုံး"); for(Profile p:profiles)for(String c:p.cat.keySet())if(!cats.contains(c))cats.add(c);
        for(String cat:cats){TextView b=pill(cat,()->{chosenCategory=cat;render();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.setMargins(0,0,dp(7),0);chips.addView(b,lp);}cv.addView(chips);column.addView(cv);
        status=text("",12,GOLD,true);status.setPadding(0,dp(14),0,dp(11));column.addView(status);
        cardList=vert();column.addView(cardList);
        render();
    }
    private void addStat(LinearLayout into,String count,String caption){
        LinearLayout b=vert();b.setPadding(dp(10),dp(11),dp(7),dp(12));b.setBackground(shape(CARD,11,0));
        b.addView(text(count,17,GOLD,true));b.addView(text(caption,9,MUTED,false));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(0,0,dp(5),0);into.addView(b,p);
    }
    private void addProgress(LinearLayout into,String label,int have,int total){
        LinearLayout caption=row();TextView name=text(label,11,IVORY,false);caption.addView(name,new LinearLayout.LayoutParams(0,-2,1));caption.addView(text(have+" / "+total,10,GOLD,true));into.addView(caption);
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(total>0?total:1);p.setProgress(have);p.setProgressTintList(android.content.res.ColorStateList.valueOf(0xffd9b272));p.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff2f4d44));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,dp(7));params.setMargins(0,dp(4),0,dp(9));into.addView(p,params);
    }
    private void addPill(LinearLayout into,String text,String kindValue){TextView b=pill(text,()->{kind=kindValue;render();});LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-2,1);params.setMargins(0,0,dp(4),0);into.addView(b,params);}
    private void choose(Profile author,String selected){
        Intent result=new Intent();result.putExtra("selected_author",author.name);result.putExtra("selected_category",selected.equals("အားလုံး")?"အားလုံး":selected);setResult(RESULT_OK,result);finish();
    }
    private void render(){if(cardList==null)return;cardList.removeAllViews();int match=0;
        for(Profile a:profiles){
            if(!kind.equals("all")&&!kind.equals(a.type))continue;
            if(!searchText.isEmpty()&&!a.name.toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT)))continue;
            if(!chosenCategory.equals("အားလုံး")&&!a.cat.containsKey(chosenCategory))continue;
            match++;
            LinearLayout card=vert();card.setPadding(dp(11),dp(12),dp(11),dp(12));card.setBackground(shape(CARD,17,BORDER));
            LinearLayout line=row();
            TextView avatar=text(a.type.equals("collection")?"▤":"☸",25,GOLD,true);avatar.setGravity(Gravity.CENTER);
            GradientDrawable circle=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff143c33,0xff736038});circle.setShape(GradientDrawable.OVAL);circle.setStroke(dp(2),0xffc79c5d);avatar.setBackground(circle);
            line.addView(avatar,new LinearLayout.LayoutParams(dp(62),dp(62)));
            LinearLayout info=vert();info.setPadding(dp(11),0,dp(3),0);TextView name=text(a.name,13,IVORY,true);name.setMaxLines(3);info.addView(name);
            info.addView(text((a.type.equals("collection")?"စာအုပ်စု / ကျမ်းစုစည်းမှု":"စာရေးသူ/ဆရာတော်အမည်စာရင်း")+" • "+a.count+" အုပ်",10,GOLD,false));
            line.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            TextView arrow=text("›",26,GOLD,true);line.addView(arrow);card.addView(line);
            LinearLayout chips=row();chips.setPadding(0,dp(9),0,dp(7));int max=0;
            for(Map.Entry<String,Integer> e:a.cat.entrySet()){
                if(!chosenCategory.equals("အားလုံး")&&!chosenCategory.equals(e.getKey()))continue;
                if(max++>=2)break;
                final String itemCat=e.getKey();TextView badge=text(e.getKey()+" "+e.getValue(),10,GOLD,false);badge.setGravity(Gravity.CENTER);
                badge.setPadding(dp(9),dp(6),dp(9),dp(6));badge.setBackground(shape(PANEL,11,BORDER));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,0,dp(6),0);chips.addView(badge,p);badge.setOnClickListener(v->choose(a,itemCat));
            }card.addView(chips);
            TextView browse=text("စာအုပ်များ ကြည့်ရန်    →",11,GOLD,true);browse.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);browse.setPadding(0,dp(3),dp(7),0);card.addView(browse);
            card.setOnClickListener(v->choose(a,chosenCategory));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,0,0,dp(10));cardList.addView(card,cp);
        }
        status.setText("စာရေးသူ / အုပ်စု "+match+" ခု တွေ့ရှိသည် • ကဏ္ဍ: "+chosenCategory);
        if(match==0){TextView empty=text("ရွေးထားသော Filter နှင့် ကိုက်ညီသည့် စာရေးသူ မရှိပါ။",12,IVORY,false);cardList.addView(empty);}
    }
}