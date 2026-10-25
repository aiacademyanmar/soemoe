package asia.aaii.dama;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Offline first-party PdfRenderer; pinch/double-tap zoom, panning, page navigation.
 * PDFs remain within the app's private document directory; no external viewer needed.
 */
public class PdfReaderActivity extends Activity {
    private final int BG = 0xff031e1c, GOLD = 0xfff4c475, PALE = 0xfffff6de;
    private final Object rendererLock = new Object();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private ParcelFileDescriptor input;
    private PdfRenderer pdf;
    private ZoomPage view;
    private TextView counter;
    private TextView zoomLabel;
    private int current = 0, generation = 0, pageCount = 0;
    private volatile boolean closed = false;
    private int dp(int v) {return (int)(v * getResources().getDisplayMetrics().density + .5f);}
    private TextView control(String text, Runnable action) {
        TextView b = new TextView(this);
        b.setText(text); b.setTextColor(GOLD); b.setTextSize(15);
        b.setGravity(Gravity.CENTER); b.setPadding(dp(10),dp(9),dp(10),dp(9));
        b.setOnClickListener(x -> action.run());
        return b;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        String path = getIntent().getStringExtra("path");
        String title = getIntent().getStringExtra("title");
        if (state != null) current = state.getInt("page", 0);
        else if(path!=null)current=getSharedPreferences("dama_reader",MODE_PRIVATE).getInt("page_"+path.hashCode(),0);
        try {
            if (path == null) throw new IllegalArgumentException("No PDF selected");
            File file = new File(path).getCanonicalFile();
            File root = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (root == null) root = getFilesDir();
            File safe = new File(root,"DAMA-Library").getCanonicalFile();
            if (!file.getPath().startsWith(safe.getPath()+File.separator) || !file.isFile())
                throw new SecurityException("PDF not in DAMA Library");
            input=ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);
            pdf = new PdfRenderer(input);
            pageCount = pdf.getPageCount();
            if (pageCount < 1) throw new IllegalArgumentException("Empty PDF");
            LinearLayout rootLayout = new LinearLayout(this); rootLayout.setOrientation(1);
            rootLayout.setBackgroundColor(BG); setContentView(rootLayout);
            if(android.os.Build.VERSION.SDK_INT>=35)rootLayout.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
            TextView header=control("‹    "+(title == null ? "DAMA PDF READER" : title),this::finish);
            header.setTextSize(16); header.setMaxLines(2);header.setGravity(Gravity.CENTER_VERTICAL);
            header.setBackgroundColor(0xff143d33);
            rootLayout.addView(header,new LinearLayout.LayoutParams(-1,dp(67)));
            TextView readerTag=control("DAMA  ✦  EBOOK READER    •    Native PDF",()->{});
            readerTag.setTextSize(11);readerTag.setTextColor(0xffbbcebc);readerTag.setBackgroundColor(0xff0c2926);
            rootLayout.addView(readerTag,new LinearLayout.LayoutParams(-1,dp(30)));
            TextView hint = new TextView(this); hint.setText("လက်နှစ်ချောင်းဖြင့် Zoom • ပုံကို နှစ်ချက်ထိ၍ Zoom • ဆွဲရွှေ့ကြည့်နိုင်သည်");
            hint.setTextColor(PALE);hint.setGravity(Gravity.CENTER);hint.setTextSize(11);
            rootLayout.addView(hint,new LinearLayout.LayoutParams(-1,dp(35)));
            view = new ZoomPage(); rootLayout.addView(view,new LinearLayout.LayoutParams(-1,0,1));
            LinearLayout zoomRow = new LinearLayout(this);zoomRow.setGravity(Gravity.CENTER);
            zoomRow.addView(control("−",()->view.changeZoom(.75f)),new LinearLayout.LayoutParams(dp(56),dp(49)));
            zoomLabel = control("100%",()->view.resetZoom());
            zoomRow.addView(zoomLabel,new LinearLayout.LayoutParams(0,dp(49),1));
            zoomRow.addView(control("+",()->view.changeZoom(1.33f)),new LinearLayout.LayoutParams(dp(56),dp(49)));
            zoomRow.addView(control("Reset",()->view.resetZoom()),new LinearLayout.LayoutParams(dp(75),dp(49)));
            rootLayout.addView(zoomRow);
            LinearLayout pager = new LinearLayout(this);pager.setGravity(Gravity.CENTER);pager.setBackgroundColor(0xff163f36);
            pager.addView(control("◀  ရှေ့",()->render(current-1)),new LinearLayout.LayoutParams(0,dp(58),1));
            counter=new TextView(this);counter.setTextColor(PALE);counter.setGravity(Gravity.CENTER);
            counter.setTextSize(14);pager.addView(counter,new LinearLayout.LayoutParams(0,dp(58),1));
            pager.addView(control("နောက်  ▶",()->render(current+1)),new LinearLayout.LayoutParams(0,dp(58),1));
            rootLayout.addView(pager);
            FooterNav.attach(this,rootLayout,"Reader");
            render(Math.min(current,pageCount-1));
        } catch(Exception ex) {
            Toast.makeText(this,"PDF ဖွင့်မရပါ: "+ex.getMessage(),Toast.LENGTH_LONG).show();
            finish();
        }
    }
    @Override protected void onSaveInstanceState(Bundle out) {out.putInt("page",current);super.onSaveInstanceState(out);}
    private void render(int index) {
        if (closed || pdf == null || index<0 || index>=pageCount) return;
        current=index;final int request=++generation;
        String filePath=getIntent().getStringExtra("path");
        if(filePath!=null)getSharedPreferences("dama_reader",MODE_PRIVATE).edit()
            .putString("last_path",filePath).putString("last_title",getIntent().getStringExtra("title"))
            .putInt("page_"+filePath.hashCode(),index).apply();
        if (counter != null) counter.setText(""+(index+1)+" / "+pageCount);
        if (zoomLabel != null) zoomLabel.setText("Loading …");
        worker.execute(()->{
            Bitmap bitmap=null;String error=null;
            try {
                synchronized(rendererLock) {
                    if (closed) return;
                    try(PdfRenderer.Page pg=pdf.openPage(index)) {
                        // Render at ~2x viewport for sharp text when zoomed, bounded to 7 million pixels.
                        int w=Math.max(800,Math.min(2200,getResources().getDisplayMetrics().widthPixels*2));
                        int h=Math.max(1,Math.round(w*(float)pg.getHeight()/Math.max(1,pg.getWidth())));
                        long area=(long)w*h;
                        if (area>7_000_000L){double f=Math.sqrt(7_000_000d/area);w=Math.max(1,(int)(w*f));h=Math.max(1,(int)(h*f));}
                        bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
                        bitmap.eraseColor(Color.WHITE);
                        pg.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    }
                }
            } catch(Exception e){error=e.getMessage();}
            final Bitmap result=bitmap; final String err=error;
            runOnUiThread(()->{
                if (closed || isFinishing() || isDestroyed() || request!=generation) {
                    if(result!=null && !result.isRecycled()) result.recycle();return;
                }
                if (result!=null) {view.setBitmap(result);zoomLabel.setText("100%");}
                else Toast.makeText(this,"Page Error: "+err,Toast.LENGTH_LONG).show();
            });
        });
    }
    @Override protected void onDestroy() {
        closed=true;++generation;worker.shutdownNow();
        synchronized(rendererLock) {
            try {if(pdf!=null) pdf.close();} catch(Exception ignored) {}
            try {if(input!=null) input.close();} catch(Exception ignored) {}
        }
        if(view!=null) view.releaseBitmap();
        super.onDestroy();
    }
    private final class ZoomPage extends View {
        private final Paint paint=new Paint(Paint.FILTER_BITMAP_FLAG);
        private final ScaleGestureDetector scaleDetector;
        private final GestureDetector gestureDetector;
        private Bitmap bitmap;
        private float zoom=1f,panX=0,panY=0;
        ZoomPage() {
            super(PdfReaderActivity.this);setBackgroundColor(0xff313835);
            scaleDetector = new ScaleGestureDetector(PdfReaderActivity.this,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
                @Override public boolean onScale(ScaleGestureDetector detector){
                    float next=Math.max(1f,Math.min(5f,zoom*detector.getScaleFactor()));
                    updateZoom(next,detector.getFocusX(),detector.getFocusY());return true;
                }
            });
            gestureDetector = new GestureDetector(PdfReaderActivity.this,new GestureDetector.SimpleOnGestureListener(){
                @Override public boolean onDown(MotionEvent e){return true;}
                @Override public boolean onDoubleTap(MotionEvent e){updateZoom(zoom>1.15f?1f:2.5f,e.getX(),e.getY());return true;}
                @Override public boolean onScroll(MotionEvent e1,MotionEvent e2,float dx,float dy){
                    if(zoom<=1f) return false;
                    panX-=dx;panY-=dy;clamp();invalidate();return true;
                }
            });
        }
        void setBitmap(Bitmap b){Bitmap old=bitmap;bitmap=b;zoom=1;panX=0;panY=0;invalidate();if(old!=null && old!=b && !old.isRecycled())old.recycle();}
        void releaseBitmap(){Bitmap old=bitmap;bitmap=null;if(old!=null && !old.isRecycled())old.recycle();}
        void resetZoom(){updateZoom(1f,getWidth()/2f,getHeight()/2f);}
        void changeZoom(float factor){updateZoom(zoom*factor,getWidth()/2f,getHeight()/2f);}
        float fit(){if(bitmap==null||getWidth()<=0||getHeight()<=0)return 1;return Math.min((float)getWidth()/bitmap.getWidth(),(float)getHeight()/bitmap.getHeight());}
        void updateZoom(float requested,float fx,float fy){
            if(bitmap==null)return;
            float next=Math.max(1f,Math.min(5f,requested));
            float oldW=bitmap.getWidth()*fit()*zoom,oldH=bitmap.getHeight()*fit()*zoom;
            float sx=(fx-(getWidth()-oldW)/2f-panX)/(fit()*zoom);
            float sy=(fy-(getHeight()-oldH)/2f-panY)/(fit()*zoom);
            zoom=next;
            float nw=bitmap.getWidth()*fit()*zoom,nh=bitmap.getHeight()*fit()*zoom;
            panX=fx-sx*fit()*zoom-(getWidth()-nw)/2f;
            panY=fy-sy*fit()*zoom-(getHeight()-nh)/2f;
            clamp();invalidate();zoomLabel.setText((int)(zoom*100)+"% • Zoom");
        }
        void clamp(){
            if(bitmap==null)return;
            float w=bitmap.getWidth()*fit()*zoom,h=bitmap.getHeight()*fit()*zoom;
            float maxX=Math.max(0,(w-getWidth())/2f),maxY=Math.max(0,(h-getHeight())/2f);
            panX=Math.max(-maxX,Math.min(maxX,panX));panY=Math.max(-maxY,Math.min(maxY,panY));
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);if(bitmap==null||bitmap.isRecycled())return;
            float fact=fit()*zoom;
            canvas.save();canvas.translate((getWidth()-bitmap.getWidth()*fact)/2f+panX,(getHeight()-bitmap.getHeight()*fact)/2f+panY);
            canvas.scale(fact,fact);canvas.drawBitmap(bitmap,0,0,paint);canvas.restore();
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            scaleDetector.onTouchEvent(e);
            if(!scaleDetector.isInProgress())gestureDetector.onTouchEvent(e);
            if(e.getActionMasked()==MotionEvent.ACTION_UP)performClick();
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}