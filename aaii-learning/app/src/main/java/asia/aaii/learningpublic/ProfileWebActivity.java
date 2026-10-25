package asia.aaii.learningpublic;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.*;
import android.webkit.*;
import android.widget.*;
public class ProfileWebActivity extends Activity {
  private static final String SITE="https://soemoe.aaii.asia/";
  @Override public void onCreate(Bundle state){
    super.onCreate(state);getWindow().setStatusBarColor(0xff082b53);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xfff3f8fe);setContentView(root);
    LinearLayout top=new LinearLayout(this);top.setBackgroundColor(0xff082b53);top.setPadding(14,12,14,12);top.setGravity(Gravity.CENTER_VERTICAL);
    root.addView(top);TextView back=new TextView(this);back.setText("‹  Back");
    back.setTextColor(Color.WHITE);back.setTextSize(16);back.setPadding(8,10,18,10);top.addView(back);back.setOnClickListener(v->finish());
    TextView title=new TextView(this);title.setText("Teacher Soe Moe • Official Site");title.setTextSize(15);title.setTextColor(0xfff5be42);top.addView(title);
    TextView msg=new TextView(this);msg.setText("LIVE PROFILE  •  soemoe.aaii.asia");
    msg.setTextSize(12);msg.setTextColor(0xff344966);msg.setPadding(18,12,8,12);root.addView(msg);
    ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);root.addView(bar,new LinearLayout.LayoutParams(-1,8));
    WebView view=new WebView(this);view.setBackgroundColor(Color.WHITE);
    view.getSettings().setJavaScriptEnabled(true);view.getSettings().setDomStorageEnabled(true);
    view.getSettings().setAllowFileAccess(false);view.getSettings().setAllowContentAccess(false);
    view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    view.setWebViewClient(new WebViewClient(){
      @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
        Uri uri=r.getUrl();if("https".equals(uri.getScheme())&&"soemoe.aaii.asia".equals(uri.getHost()))return false;
        if("https".equals(uri.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception ignored){}}
        return true;
      }
      @Override public void onReceivedError(WebView v,WebResourceRequest req,WebResourceError e){
        if(req.isForMainFrame())msg.setText("Website မဖွင့်နိုင်ပါ။ Internet / Website ကို စစ်ဆေးပါ။ Retry လုပ်နိုင်ပါသည်။");
      }
      @Override public void onReceivedSslError(WebView v,SslErrorHandler handler,SslError e){handler.cancel();}
    });
    view.setWebChromeClient(new WebChromeClient(){
      @Override public void onProgressChanged(WebView v,int n){bar.setProgress(n);bar.setVisibility(n>=100?View.GONE:View.VISIBLE);}
    });
    root.addView(view,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout actions=new LinearLayout(this);root.addView(actions);
    Button retry=new Button(this);retry.setText("Retry");actions.addView(retry,new LinearLayout.LayoutParams(0,-2,1));retry.setOnClickListener(v->view.loadUrl(SITE));
    Button external=new Button(this);external.setText("Open Browser");actions.addView(external,new LinearLayout.LayoutParams(0,-2,1));
    external.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(SITE)));}catch(Exception ex){}});
    view.loadUrl(SITE);
  }
}