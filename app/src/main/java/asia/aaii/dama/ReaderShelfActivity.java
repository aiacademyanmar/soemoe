package asia.aaii.dama;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

/**
 * Reader Shelf is a safe, offline list of PDFs deliberately downloaded from DAMA LIBRARY.
 * It launches the existing PdfReaderActivity and does not guess/forge file paths.
 */
public final class ReaderShelfActivity extends DamaActivity {
    private static final int BG=0xff041e1c, DEEP=0xff092b27, PANEL=0xff123c34;
    private static final int GOLD=0xffedc375, WHITE=0xfffff5dd, MUTED=0xffbecabd;
    private LinearLayout content,root;
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int color,int radius, int outline){
        GradientDrawable bg=new GradientDrawable();bg.setColor(color);bg.setCornerRadius(dp(radius));
        if(outline!=0)bg.setStroke(dp(1),outline);return bg;
    }
    private TextView label(String text,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(text);t.setTextColor(color);
        t.setTextSize(size);t.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private LinearLayout col(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);return c;}
    private LinearLayout.LayoutParams wh(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private void gap(int h){content.addView(new android.view.View(this),wh(1,h));}
    @Override public void onCreate(Bundle b){super.onCreate(b);
        getWindow().setStatusBarColor(DEEP);getWindow().setNavigationBarColor(DEEP);render();
    }
    @Override protected void onResume(){super.onResume();if(content!=null)render();}
    private File folder(){
        File root=getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(root==null)root=getFilesDir();
        return new File(root,"DAMA-Library");
    }
    private Map<String,String> titles(){
        Map<String,String> names=new HashMap<>();
        try(InputStream raw=getAssets().open("library_catalog.dat");
            java.util.zip.GZIPInputStream unzip=new java.util.zip.GZIPInputStream(raw);
            ByteArrayOutputStream stream=new ByteArrayOutputStream()){
            byte[] chunk=new byte[8192];int n;
            while((n=unzip.read(chunk))!=-1){if(stream.size()+n>12_000_000)break;stream.write(chunk,0,n);}
            JSONArray catalog=new JSONArray(stream.toString("UTF-8"));
            for(int i=0;i<catalog.length();i++){JSONObject item=catalog.optJSONObject(i);
                if(item!=null)names.put(item.optString("id"),item.optString("title"));}
        }catch(Exception e){android.util.Log.w("DAMA Reader","Book titles not available",e);}
        return names;
    }
    private void read(File file,String title){
        try{
            File canonical=file.getCanonicalFile(),safe=folder().getCanonicalFile();
            if(!canonical.getParentFile().equals(safe)||!canonical.isFile()||!canonical.getName().endsWith(".pdf"))return;
            Intent i=new Intent(this,PdfReaderActivity.class);
            i.putExtra("path",canonical.getAbsolutePath());i.putExtra("title",title);
            startActivity(i);
        }catch(IOException ignored){}
    }
    private void button(String title,Runnable action){
        TextView b=label(title,14,GOLD,true);b.setPadding(dp(16),dp(14),dp(16),dp(14));
        b.setGravity(Gravity.CENTER);b.setBackground(shape(PANEL,12,0xff8e7040));
        content.addView(b,wh(-1,-2));gap(11);b.setOnClickListener(v->action.run());
    }
    private void render(){
        root=col();root.setBackgroundColor(BG);setContentView(root);
        if(android.os.Build.VERSION.SDK_INT>=35)root.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;
        });
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20),dp(16),dp(15),dp(14));header.setBackgroundColor(DEEP);
        LinearLayout words=col();words.addView(label("▣  DAMA READER",21,GOLD,true));
        words.addView(label("Android PDF Reader • Offline Library",11,MUTED,false));
        header.addView(words);root.addView(header,wh(-1,80));
        ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=col();content.setPadding(dp(16),dp(13),dp(16),dp(16));scroll.addView(content);

        TextView headline=label("ဖုန်းထဲသိမ်းထားသော ဓမ္မစာအုပ်များ",16,WHITE,true);
        content.addView(headline);gap(9);
        TextView explain=label("DAMA LIBRARY မှ Download လုပ်ထားသော PDF များကို Internet မလိုဘဲ Native Android Reader ဖြင့် ဖတ်နိုင်ပါသည်။",12,MUTED,false);
        explain.setPadding(dp(4),dp(4),dp(4),dp(13));content.addView(explain);

        File dir=folder();File[] files=dir.listFiles(f->f.isFile()&&f.getName().endsWith(".pdf")&&f.length()>0);
        if(files==null)files=new File[0];
        Arrays.sort(files, (a,b)->Long.compare(b.lastModified(),a.lastModified()));
        Map<String,String> titles=titles();
        TextView total=label("OFFLINE BOOKS  •  "+files.length,13,GOLD,true);
        content.addView(total);gap(12);

        SharedPreferences prefs=getSharedPreferences("dama_reader",MODE_PRIVATE);
        String lastPath=prefs.getString("last_path",null);
        if(lastPath!=null){
            try{
                File last=new File(lastPath).getCanonicalFile();
                if(last.exists()&&last.getParentFile().equals(dir.getCanonicalFile())){
                    String old=prefs.getString("last_title",last.getName());
                    TextView recent=label("▶   ဆက်လက်ဖတ်ရှုရန် — "+old,13,WHITE,true);
                    recent.setPadding(dp(13),dp(14),dp(13),dp(14));
                    recent.setBackground(shape(0xff265443,13,0xffa78951));
                    content.addView(recent);gap(13);
                    recent.setOnClickListener(v->read(last,old));
                }
            }catch(IOException ignored){}
        }
        if(files.length==0){
            LinearLayout empty=col();empty.setPadding(dp(15),dp(20),dp(15),dp(20));
            empty.setBackground(shape(PANEL,16,0xff657457));
            empty.addView(label("▤  PDF စာအုပ် မသိမ်းရသေးပါ",17,GOLD,true));
            empty.addView(label("Library သို့ဝင်ပြီး PDF စာအုပ်တစ်အုပ်ကို Download လုပ်ပါ။ ပြီးနောက် Reader ထဲမှ Offline ဖတ်နိုင်ပါသည်။",12,WHITE,false));
            content.addView(empty);gap(16);
        }else{
            int visible=Math.min(files.length,150);
            for(int i=0;i<visible;i++){
                final File file=files[i];
                String fileId=file.getName().substring(0,file.getName().length()-4);
                final String title=titles.getOrDefault(fileId,fileId);
                LinearLayout cell=col();cell.setPadding(dp(14),dp(12),dp(14),dp(12));
                cell.setBackground(shape(PANEL,13,0xff466050));
                TextView a=label("▣  "+title,14,WHITE,true);a.setMaxLines(2);cell.addView(a);
                cell.addView(label("PDF • Offline  •  "+(file.length()/1024)+" KB  •  နှိပ်၍ဖတ်ရန်  ›",11,GOLD,false));
                content.addView(cell,wh(-1,-2));gap(9);
                cell.setOnClickListener(v->read(file,title));
            }
            if(files.length>150)content.addView(label("ပထမဆုံး စာအုပ် ၁၅၀ အုပ်ကို ပြထားပါသည်။",11,MUTED,false));
        }
        button("▤  DAMA LIBRARY — စာအုပ်ရှာဖွေရန်",()->startActivity(new Intent(this,LibraryActivity.class)));
        TextView note=label("Native PDF Reader: Zoom • Page Navigation • Resume Reading. PDF link catalog သည် Offline ဖြစ်သော်လည်း PDF ဖိုင်အပြည့်အစုံကို ကိုယ်တိုင် Download လုပ်ရန် လိုအပ်ပါသည်။",11,MUTED,false);
        content.addView(note);
        FooterNav.attach(this,root,"Reader");
    }
}
