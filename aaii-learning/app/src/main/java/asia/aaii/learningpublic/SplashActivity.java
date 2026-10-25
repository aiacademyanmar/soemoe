package asia.aaii.learningpublic;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
public class SplashActivity extends Activity {
  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    getWindow().setStatusBarColor(Color.rgb(1,9,30));getWindow().setNavigationBarColor(Color.rgb(1,9,30));
    FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.rgb(1,9,30));
    ImageView art=new ImageView(this);art.setImageResource(R.drawable.aaii_splash_v45);
    art.setScaleType(ImageView.ScaleType.FIT_CENTER);
    root.addView(art,new FrameLayout.LayoutParams(-1,-1));
    TextView bottom=new TextView(this);bottom.setText("AAII LEARNING  •  GLOBAL PROFESSIONAL EDUCATION");
    bottom.setTextColor(Color.rgb(255,211,120));bottom.setTextSize(10);bottom.setGravity(Gravity.CENTER);
    FrameLayout.LayoutParams label=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);
    label.bottomMargin=(int)(14*getResources().getDisplayMetrics().density);
    root.addView(bottom,label);setContentView(root);
    root.postDelayed(()->{
      if(!isFinishing()){startActivity(new Intent(this,MainActivity.class));finish();}
    },2300);
  }
}