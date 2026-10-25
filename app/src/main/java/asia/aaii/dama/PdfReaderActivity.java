package asia.aaii.dama;
import android.app.Activity;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.graphics.pdf.PdfRenderer;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.io.File;

/** On-device PDF rendering (no external WebView/Google viewer; no internet required). */
public class PdfReaderActivity extends Activity {
  PdfRenderer pdf;ParcelFileDescriptor input; ImageView page;TextView counter;int current=0;
  int dp(int x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff071d1b);getWindow().setNavigationBarColor(0xff071d1b);String path=getIntent().getStringExtra("path"),title=getIntent().getStringExtra("title");try{
      File f=new File(path);File dir=new File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS),"DAMA-Library");if(!f.getCanonicalPath().startsWith(dir.getCanonicalPath()+File.separator)||!f.exists())throw new SecurityException("Wrong file path");
      input=ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);pdf=new PdfRenderer(input);
      LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xff001b19);setContentView(root);
      if(android.os.Build.VERSION.SDK_INT>=35){
        root.setOnApplyWindowInsetsListener((v,insets)->{
          android.graphics.Insets safe=insets.getInsets(android.view.WindowInsets.Type.systemBars() |
            android.view.WindowInsets.Type.displayCutout());
          v.setPadding(0,safe.top,0,safe.bottom);return insets;
        });root.requestApplyInsets();
      }
      TextView h=new TextView(this);h.setText("‹  "+title);h.setTextColor(0xffe9c179);h.setTextSize(16);h.setPadding(dp(13),dp(14),dp(13),dp(10));h.setOnClickListener(v->finish());root.addView(h);
      ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));page=new ImageView(this);page.setAdjustViewBounds(true);page.setBackgroundColor(Color.WHITE);page.setScaleType(ImageView.ScaleType.FIT_CENTER);scroll.addView(page);
      LinearLayout controls=new LinearLayout(this);controls.setPadding(dp(13),dp(8),dp(13),dp(10));controls.setGravity(Gravity.CENTER);TextView before=new TextView(this);before.setText("◀ Prev");before.setTextColor(0xffe9c179);before.setTextSize(16);controls.addView(before);before.setOnClickListener(v->render(current-1));counter=new TextView(this);counter.setTextColor(Color.WHITE);counter.setPadding(dp(20),0,dp(20),0);controls.addView(counter);TextView after=new TextView(this);after.setText("Next ▶");after.setTextColor(0xffe9c179);after.setTextSize(16);controls.addView(after);after.setOnClickListener(v->render(current+1));root.addView(controls);render(0);
    }catch(Exception e){Toast.makeText(this,"PDF ဖွင့်မရပါ: "+e.getMessage(),Toast.LENGTH_LONG).show();finish();}}
  void render(int index){if(pdf==null||index<0||index>=pdf.getPageCount())return;current=index;try(PdfRenderer.Page p=pdf.openPage(index)){int width=Math.min(1800,Math.max(600,getResources().getDisplayMetrics().widthPixels));int height=Math.max(1,(int)(((float)p.getHeight())/p.getWidth()*width));if((long)width*height>9_000_000L){double k=Math.sqrt(9_000_000.0/(width*(double)height));width=(int)(width*k);height=(int)(height*k);}Bitmap bmp=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);bmp.eraseColor(Color.WHITE);p.render(bmp,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);page.setImageBitmap(bmp);counter.setText((index+1)+" / "+pdf.getPageCount());}catch(Exception e){Toast.makeText(this,"Page Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();}}
  @Override public void onDestroy(){try{if(pdf!=null)pdf.close();}catch(Exception ignored){}try{if(input!=null)input.close();}catch(Exception ignored){}super.onDestroy();}
}