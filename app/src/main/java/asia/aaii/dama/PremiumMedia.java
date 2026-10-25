package asia.aaii.dama;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.*;
import android.widget.*;
import android.view.animation.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/** Premium media affordances; actual YouTube thumbnails from YouTube's thumbnail CDN,
 * decorative book-cover placeholders, and optional first-page preview of an offline PDF.
 * Never presents decorative artwork as the real published cover.
 */
public final class PremiumMedia {
  private PremiumMedia(){}
  public static final int GOLD=0xffefc67b, IVORY=0xfffff4d9, NIGHT=0xff062721;
  private static final ExecutorService WORKERS=Executors.newFixedThreadPool(3);
  private static final Handler MAIN=new Handler(Looper.getMainLooper());
  private static final LruCache<String,Bitmap> IMAGES=new LruCache<String,Bitmap>(48){
    @Override protected int sizeOf(String key, Bitmap bmp){return Math.max(1,bmp.getByteCount()/1024);}
  };
  public static int dp(Context c,int n){return (int)(c.getResources().getDisplayMetrics().density*n+.5f);}
  public static GradientDrawable round(int color,Context c,int radius,int stroke){
    GradientDrawable b=new GradientDrawable();b.setColor(color);b.setCornerRadius(dp(c,radius));
    if(stroke!=0)b.setStroke(dp(c,1),stroke);return b;
  }
  /** Android click sound follows system preference, no surprise loud audio or permissions. */
  /** Use the same micro-pop feedback for popup dialogs and on-page cards. */
  public static void tap(View v,Runnable action){
    DamaTouchFeedback.bind(v,action);
  }
  /** Local branded book jacket, not the publisher's cover. Category and title are real metadata. */
  public static View decorativeBookCover(Context context,String title,String category,int variant,int w,int h){
    int[] shades={0xff234f3d,0xff7a352b,0xff213e5c,0xff866a3e,0xff463956,0xff315d5a,0xff734c27};
    int bg=shades[Math.floorMod(variant,shades.length)];
    LinearLayout cover=new LinearLayout(context);cover.setGravity(Gravity.CENTER);
    cover.setOrientation(LinearLayout.VERTICAL);
    cover.setPadding(dp(context,5),dp(context,5),dp(context,5),dp(context,5));
    GradientDrawable border=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
      new int[]{bg,shade(bg,.7f)});
    border.setCornerRadius(dp(context,8));border.setStroke(dp(context,2),0xffbc965a);
    cover.setBackground(border);cover.setElevation(dp(context,3));
    TextView small=new TextView(context);small.setText("✦ DAMA ✦");small.setTextColor(GOLD);
    small.setTextSize(8);small.setGravity(Gravity.CENTER);cover.addView(small);
    TextView ornament=new TextView(context);ornament.setText("☸");ornament.setTextColor(0xfffbdf9f);
    ornament.setTextSize(h>130?34:26);ornament.setGravity(Gravity.CENTER);
    cover.addView(ornament,new LinearLayout.LayoutParams(-1,0,1f));
    TextView t=new TextView(context);t.setText(title==null?"ဓမ္မစာအုပ်":title);
    t.setTextSize(10);t.setTextColor(IVORY);t.setTypeface(null,android.graphics.Typeface.BOLD);
    t.setGravity(Gravity.CENTER);t.setMaxLines(3);t.setEllipsize(android.text.TextUtils.TruncateAt.END);
    cover.addView(t,new LinearLayout.LayoutParams(-1,0,1.15f));
    TextView categoryLabel=new TextView(context);
    categoryLabel.setText(category==null?"EBOOK":category);categoryLabel.setTextSize(8);
    categoryLabel.setTextColor(GOLD);categoryLabel.setGravity(Gravity.CENTER);
    categoryLabel.setMaxLines(1);cover.addView(categoryLabel);
    cover.setContentDescription("Design cover for "+title+" (not a publisher cover)");
    cover.setLayoutParams(new LinearLayout.LayoutParams(dp(context,w),dp(context,h)));
    return cover;
  }
  private static int shade(int c,float f){
    return Color.rgb((int)(Color.red(c)*f),(int)(Color.green(c)*f),(int)(Color.blue(c)*f));
  }
  public static void youtubeThumbnail(ImageView image,String videoId){
    if(videoId==null||!videoId.matches("^[a-zA-Z0-9_-]{11}$"))return;
    final String k="yt_"+videoId;
    image.setTag(k);
    image.setImageDrawable(null);
    image.setBackground(round(0xff123c34,image.getContext(),10,0xff847145));
    image.setScaleType(ImageView.ScaleType.CENTER_CROP);
    Bitmap known=IMAGES.get(k);
    if(known!=null){image.setImageBitmap(known);return;}
    WORKERS.execute(()->{
      Bitmap loaded=null;
      HttpURLConnection c=null;
      try{
        URL url=new URL("https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg");
        c=(HttpURLConnection)url.openConnection();
        c.setConnectTimeout(4500);c.setReadTimeout(6500);
        c.setInstanceFollowRedirects(false);
        c.setRequestProperty("User-Agent","DAMA-TV/17 (Android)");
        if(c.getResponseCode()==200&&c.getContentLengthLong()<350_000){
          try(InputStream in=c.getInputStream()){
            byte[] bytes=readBounded(in,350_000);
            BitmapFactory.Options opts=new BitmapFactory.Options();opts.inSampleSize=1;
            loaded=BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
            if(loaded!=null)IMAGES.put(k,loaded);
          }
        }
      }catch(Exception ignored){}finally{if(c!=null)c.disconnect();}
      final Bitmap result=loaded;
      MAIN.post(()->{if(k.equals(image.getTag())&&result!=null)image.setImageBitmap(result);});
    });
  }
  private static byte[] readBounded(InputStream in,int limit)throws IOException{
    ByteArrayOutputStream out=new ByteArrayOutputStream();
    byte[] tmp=new byte[8192];int n;
    while((n=in.read(tmp))!=-1){if(out.size()+n>limit)throw new IOException("Asset limit");out.write(tmp,0,n);}
    return out.toByteArray();
  }
  public static void offlinePdfFirstPage(ImageView image,File file){
    if(file==null||!file.isFile())return;
    String k="pdf_"+file.getAbsolutePath()+"_"+file.lastModified();
    image.setTag(k);
    Bitmap known=IMAGES.get(k);
    if(known!=null){image.setImageBitmap(known);return;}
    WORKERS.execute(()->{
      Bitmap img=null; android.os.ParcelFileDescriptor handle=null;
      android.graphics.pdf.PdfRenderer renderer=null;
      try{
        handle=android.os.ParcelFileDescriptor.open(file,android.os.ParcelFileDescriptor.MODE_READ_ONLY);
        renderer=new android.graphics.pdf.PdfRenderer(handle);
        if(renderer.getPageCount()>0){
          try(android.graphics.pdf.PdfRenderer.Page page=renderer.openPage(0)){
            int height=Math.max(90,Math.min(300,300*page.getHeight()/Math.max(1,page.getWidth())));
            img=Bitmap.createBitmap(220,height,Bitmap.Config.ARGB_8888);
            img.eraseColor(Color.WHITE);
            page.render(img,null,null,android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            IMAGES.put(k,img);
          }
        }
      }catch(Exception ignored){}finally{
        if(renderer!=null)try{renderer.close();}catch(Exception ignored){}
        else if(handle!=null)try{handle.close();}catch(Exception ignored){}
      }
      Bitmap ready=img;
      MAIN.post(()->{if(k.equals(image.getTag())&&ready!=null){image.setImageBitmap(ready);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);}});
    });
  }
}
