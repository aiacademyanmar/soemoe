package asia.aaii.musiccreator;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.RenderProcessGoneDetail;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import org.json.JSONObject;

public class MainActivity extends Activity {
  private static final String TAG="AAII_MUSIC";
  private WebView web;
  private ValueCallback<Uri[]> chooser;
  private static final int PICK_CODE=480;
  private String sharedText="";
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    getWindow().setStatusBarColor(Color.rgb(8,10,31));
    getWindow().setNavigationBarColor(Color.rgb(8,10,31));
    Intent incoming=getIntent();
    if(incoming!=null && Intent.ACTION_SEND.equals(incoming.getAction())) {
      sharedText=incoming.getStringExtra(Intent.EXTRA_TEXT);
      if(sharedText==null) sharedText="";
    }
    launch();
  }
  private void launch() {
    try {
      final WebView w=new WebView(this); web=w;
      w.setBackgroundColor(Color.rgb(8,10,31));
      WebSettings s=w.getSettings();
      s.setJavaScriptEnabled(true);
      s.setDomStorageEnabled(true);
      s.setDefaultTextEncodingName("UTF-8");
      s.setAllowFileAccess(true);
      s.setMediaPlaybackRequiresUserGesture(true);
      w.addJavascriptInterface(new AppBridge(),"AAII");
      w.setWebChromeClient(new WebChromeClient() {
        @Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> cb,FileChooserParams params) {
          if(chooser!=null) chooser.onReceiveValue(null); chooser=cb;
          try { startActivityForResult(params.createIntent(),PICK_CODE);return true; }
          catch(Exception e) { chooser=null; cb.onReceiveValue(null);return false; }
        }
      });
      w.setWebViewClient(new WebViewClient(){
        @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req){return route(req.getUrl());}
        @Override public boolean shouldOverrideUrlLoading(WebView v,String url){return route(Uri.parse(url));}
        @Override public boolean onRenderProcessGone(WebView view,RenderProcessGoneDetail detail){
          Log.e(TAG,"Renderer gone; crash="+detail.didCrash());
          web=null; try{view.destroy();}catch(Throwable ignored){}
          recovery("WebView ရပ်တန့်သွားပါတယ်။ Chrome / Android System WebView ကို Update လုပ်ပြီး Retry နှိပ်ပါ။");
          return true;
        }
        @Override public void onPageFinished(WebView view,String url) {
          if(!sharedText.isEmpty()) {
            view.evaluateJavascript("window.importShared("+JSONObject.quote(sharedText)+")",null);
            sharedText="";
          }
        }
      });
      setContentView(w);
      w.loadUrl("file:///android_asset/index.html");
    }catch(Throwable e) {Log.e(TAG,"Startup error",e);recovery("App စတင်မရပါ။ "+e.getClass().getSimpleName());}
  }
  private boolean route(Uri uri){
    if(uri==null)return true;
    String scheme=uri.getScheme();
    if("file".equals(scheme) && "android_asset".equals(uri.getHost()))return false;
    if("about".equals(scheme))return false;
    if("https".equals(scheme)||"http".equals(scheme)||"mailto".equals(scheme)){
      try {startActivity(new Intent(Intent.ACTION_VIEW,uri));}
      catch(Exception e) {Log.e(TAG,"External link failed",e);}
    }
    return true;
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(request==PICK_CODE && chooser!=null){
      chooser.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));
      chooser=null;
    }
  }
  class AppBridge {
    @JavascriptInterface public void copyText(String text) {
      ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Music Creator",text));
    }
    @JavascriptInterface public void fetchFeed(String rawUrl) {
      new Thread(()->{
        String response="";
        String error="";
        try {
          URL url=new URL(rawUrl);
          if(!"https".equalsIgnoreCase(url.getProtocol())) throw new Exception("Use HTTPS song catalog URL");
          HttpURLConnection c=(HttpURLConnection)url.openConnection();
          c.setConnectTimeout(10000);c.setReadTimeout(15000);
          c.setRequestProperty("Accept","application/json");
          if(c.getResponseCode()!=200)throw new Exception("HTTP "+c.getResponseCode());
          try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[4096];int n;
            while((n=in.read(b))!=-1){if(out.size()>1_500_000)throw new Exception("Catalog too large");out.write(b,0,n);}
            response=out.toString("UTF-8");
          }
          c.disconnect();
        }catch(Exception e){error=e.toString();}
        final String result=response,err=error;
        runOnUiThread(()->{
          if(web!=null)web.evaluateJavascript("window.feedResult("+JSONObject.quote(result)+","+JSONObject.quote(err)+")",null);
        });
      }).start();
    }
  }
  private void recovery(String error){
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER);root.setPadding(48,50,48,50);root.setBackgroundColor(Color.rgb(8,10,31));
    TextView t=new TextView(this);t.setText("AAII MUSIC CREATOR");t.setTextSize(25);t.setTextColor(Color.WHITE);t.setGravity(Gravity.CENTER);root.addView(t);
    TextView info=new TextView(this);info.setText(error);info.setTextColor(0xffd0d5ff);info.setTextSize(16);info.setGravity(Gravity.CENTER);root.addView(info);
    Button b=new Button(this);b.setText("Retry");b.setOnClickListener(v->launch());root.addView(b);setContentView(root);
  }
  @Override public void onBackPressed(){
    if(web!=null && web.canGoBack())web.goBack();else super.onBackPressed();
  }
  @Override public void onDestroy(){
    if(web!=null){web.destroy();web=null;}
    super.onDestroy();
  }
}
