package asia.aaii.learningpublic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.pm.ActivityInfo;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * AAII Learning V5.5 Home is an exact rendered image of the user-approved
 * 941 x 1672 UI reference, with separately registered native Android
 * button, menu, category, profile, and content-card interactions.
 *
 * No WebView. Navigation opens the current functional native screens.
 * Reference art has static demonstration counts/progress and mockups.
 */
public final class ExactHomeActivity extends Activity {
    private static final float SOURCE_WIDTH = 941f;
    private static final float SOURCE_HEIGHT = 1672f;
    private static final int FOOTER_SOURCE_Y = 1520;
    private FrameLayout footerCanvas;
    private FrameLayout canvas;
    private SoundPool soundPool;
    private int clickId;
    private SharedPreferences prefs;

    private void immersive() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.rgb(1, 12, 31));
        getWindow().setNavigationBarColor(Color.rgb(1, 12, 31));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        immersive();
        prefs = getSharedPreferences("AAII_Learning_Study_V31", MODE_PRIVATE);
        soundPool = new SoundPool.Builder().setMaxStreams(3).build();
        clickId = soundPool.load(this, R.raw.aaii_click, 1);

        int deviceWidth=getResources().getDisplayMetrics().widthPixels;
        int bodyHeight=Math.round(deviceWidth*FOOTER_SOURCE_Y/SOURCE_WIDTH);
        int footerHeight=Math.round(deviceWidth*(SOURCE_HEIGHT-FOOTER_SOURCE_Y)/SOURCE_WIDTH);
        Bitmap original=BitmapFactory.decodeResource(getResources(),R.drawable.aaii_home_exact_v55);
        if(original==null)throw new IllegalStateException("Missing Home visual resource");
        Bitmap contentBitmap=Bitmap.createBitmap(original,0,0,original.getWidth(),
            Math.round(original.getHeight()*FOOTER_SOURCE_Y/SOURCE_HEIGHT));
        int cropY=Math.round(original.getHeight()*FOOTER_SOURCE_Y/SOURCE_HEIGHT);
        Bitmap footerBitmap=Bitmap.createBitmap(original,0,cropY,original.getWidth(),original.getHeight()-cropY);

        LinearLayout display=new LinearLayout(this);
        display.setOrientation(LinearLayout.VERTICAL);
        display.setBackgroundColor(0xff061a33);
        setContentView(display);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        display.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(0xff061a33);
        scroll.addView(content);
        canvas=new FrameLayout(this);
        canvas.setBackgroundColor(0xff061a33);
        content.addView(canvas,new LinearLayout.LayoutParams(-1,bodyHeight));
        ImageView art=new ImageView(this);
        art.setImageBitmap(contentBitmap);
        art.setScaleType(ImageView.ScaleType.FIT_XY);
        canvas.addView(art,new FrameLayout.LayoutParams(-1,-1));

        TextView more=new TextView(this);
        more.setText("🎓  COURSES  •  SOE MOE CERTIFICATE REFERENCES   ›");
        more.setTextColor(0xffffd573);
        more.setTextSize(13);
        more.setGravity(Gravity.CENTER);
        more.setPadding(8,12,8,12);
        more.setBackgroundColor(0xff0a2b50);
        content.addView(more,new LinearLayout.LayoutParams(-1,-2));
        more.setOnClickListener(v->toPage(1));

        footerCanvas=new FrameLayout(this);
        footerCanvas.setBackgroundColor(0xff061a33);
        display.addView(footerCanvas,new LinearLayout.LayoutParams(-1,footerHeight));
        ImageView footerImage=new ImageView(this);
        footerImage.setImageBitmap(footerBitmap);
        footerImage.setScaleType(ImageView.ScaleType.FIT_XY);
        footerCanvas.addView(footerImage,new FrameLayout.LayoutParams(-1,-1));
        canvas.post(this::installControls);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) immersive();
    }

    private void toPage(int p) { toPage(p, 0, "", -1); }
    private void toPage(int p, int group, String provider) {
        toPage(p, group, provider, -1);
    }
    private void toPage(int p, int group, String provider, int programIndex) {
        Intent next = new Intent(this, MainActivity.class);
        next.putExtra("aaii_page", p);
        next.putExtra("aaii_provider_group", group);
        next.putExtra("aaii_provider", provider);
        if (programIndex >= 0) {
            next.putExtra("aaii_page", 6);
            next.putExtra("aaii_detail_type", programIndex==3?2:3);
            next.putExtra("aaii_detail_index", programIndex);
        }
        startActivity(next);
    }

    private void toSearch(String query) {
        Intent next = new Intent(this, MainActivity.class);
        next.putExtra("aaii_page", 1);
        next.putExtra("aaii_search", query);
        startActivity(next);
    }

    private void searchPopup() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(16);
        input.setHint("Course / Provider / Topics...");
        int inset = (int)(20f*getResources().getDisplayMetrics().density);
        FrameLayout wrapper = new FrameLayout(this);
        wrapper.setPadding(inset, 0, inset, 0);
        wrapper.addView(input, new FrameLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this)
            .setTitle("AAII Learning • Search")
            .setView(wrapper)
            .setPositiveButton("Search Courses", (d, w) -> toSearch(input.getText().toString().trim()))
            .setNeutralButton("All Categories", (d, w) -> toPage(16))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void filtersPopup() {
        final String[] labels={"All Providers","Corporate Companies",
                "Universities","Learning Partners"};
        new AlertDialog.Builder(this)
            .setTitle("Category Filters")
            .setItems(labels,(d,w)->toPage(16,w,""))
            .setNegativeButton("Cancel",null).show();
    }

    private void profilePopup() {
        String[] options={"My Profile & Settings","Teacher Soe Moe Profile",
                "My Certificate Gallery","My Learning","Request History"};
        int[] destinations={11,9,17,12,13};
        new AlertDialog.Builder(this)
            .setTitle("AAII Learning • Profile")
            .setItems(options,(dialog,index)->toPage(destinations[index]))
            .setNegativeButton("Close",null).show();
    }

    private void notificationsPopup() {
        new AlertDialog.Builder(this).setTitle("AAII Learning • Notifications")
            .setMessage("New notifications are not connected to a server yet.\nVisit your request history or learning progress to review local entries.")
            .setPositiveButton("My Requests",(d,w)->toPage(13))
            .setNegativeButton("Close",null).show();
    }

    private void press(View v, Runnable onClick) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if(soundPool!=null && clickId!=0 &&
           prefs.getBoolean("click_enabled",true)) {
            soundPool.play(clickId,0.50f,0.50f,1,0,1f);
        }
        GradientDrawable gold = new GradientDrawable();
        gold.setColor(0x34fac74c);
        gold.setCornerRadius(20f*getResources().getDisplayMetrics().density);
        gold.setStroke(2,0xaaffd361);
        v.setBackground(gold);
        v.setAlpha(1f);
        v.animate().alpha(0f).setDuration(155).withEndAction(() -> {
            if(!isFinishing()&&!isDestroyed()) onClick.run();
        }).start();
    }

    // Each clickable area is mapped to the exact approved 941×1672 reference.
    // This overlay contains real Android views, independent from the background image.
    private void hotspot(int left, int top, int right, int bottom, Runnable action) {
        if(canvas==null || canvas.getWidth()<=0 || canvas.getHeight()<=0)return;
        int l=Math.round(left*canvas.getWidth()/SOURCE_WIDTH);
        int t=Math.round(top*canvas.getHeight()/FOOTER_SOURCE_Y);
        int r=Math.round(right*canvas.getWidth()/SOURCE_WIDTH);
        int b=Math.round(bottom*canvas.getHeight()/FOOTER_SOURCE_Y);
        if(r<=l||b<=t)return;
        View nativeButton=new View(this);
        nativeButton.setContentDescription("AAII Learning interactive control");
        nativeButton.setClickable(true);
        nativeButton.setFocusable(true);
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(r-l,b-t, Gravity.TOP|Gravity.LEFT);
        p.leftMargin=l; p.topMargin=t;
        canvas.addView(nativeButton,p);
        nativeButton.setOnClickListener(v->press(v,action));
    }

    // Fixed physical footer is cropped from the same user-approved image,
    // ensuring its artwork stays pinned at the bottom of any portrait phone.
    private void footerHotspot(int left,int right,Runnable action) {
        if(footerCanvas==null||footerCanvas.getWidth()<=0)return;
        int l=Math.round(left*footerCanvas.getWidth()/SOURCE_WIDTH);
        int r=Math.round(right*footerCanvas.getWidth()/SOURCE_WIDTH);
        View button=new View(this);
        button.setContentDescription("AAII Learning footer navigation");
        button.setClickable(true);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(r-l,-1,Gravity.TOP|Gravity.LEFT);
        lp.leftMargin=l; footerCanvas.addView(button,lp);
        button.setOnClickListener(v->press(v,action));
    }

    private void installControls() {
        // Fixed header and global native navigation.
        hotspot(770,53,836,134,this::notificationsPopup);
        hotspot(842,53,917,135,this::profilePopup);
        hotspot(20,325,813,400,this::searchPopup);
        hotspot(820,327,914,403,this::filtersPopup);

        // Category tabs.
        hotspot(22,401,162,462,()->toPage(16,0,""));
        hotspot(163,401,376,462,()->toPage(16,1,""));
        hotspot(375,401,626,462,()->toPage(16,2,""));
        hotspot(624,401,916,462,()->toPage(16,3,""));

        // Stats.
        hotspot(22,468,235,586,()->toPage(1));
        hotspot(235,468,465,586,()->toPage(2));
        hotspot(466,468,702,586,()->toPage(3));
        hotspot(706,468,917,586,()->toPage(15));

        // Provider header + individually clickable brand cards.
        hotspot(772,602,909,672,()->toPage(16));
        hotspot(26,679,174,890,()->toPage(1,1,"Google"));
        hotspot(177,679,318,890,()->toPage(1,1,"Meta"));
        hotspot(325,679,468,890,()->toPage(1,1,"Microsoft"));
        hotspot(470,679,613,890,()->toPage(1,1,"IBM"));
        hotspot(615,679,754,890,()->toPage(1,1,"Amazon Web Services"));
        hotspot(759,679,917,890,()->toPage(1,1,"Adobe"));

        // Six primary content/service cards in the screenshot.
        hotspot(19,906,318,1059,()->toPage(1));
        hotspot(326,906,616,1059,()->toPage(17));
        hotspot(624,906,919,1059,()->toPage(8));
        hotspot(19,1066,318,1182,()->toPage(16,2,""));
        hotspot(326,1066,616,1182,()->toPage(16,1,""));
        hotspot(624,1066,919,1182,()->toPage(16,3,""));

        // AI banner + recently viewed program cards.
        hotspot(626,1201,902,1294,()->toPage(15));
        hotspot(29,1201,624,1294,()->toPage(15));
        hotspot(788,1305,911,1363,()->toPage(12));
        hotspot(25,1370,237,1510,()->toPage(3,0,"",0));  // Google AI
        hotspot(244,1370,468,1510,()->toPage(3,0,"",1)); // Google Digital Marketing
        hotspot(473,1370,687,1510,()->toPage(3,0,"",2)); // Google Project Management
        hotspot(694,1370,912,1510,()->toPage(2,0,"",3)); // Google Cloud

        // True fixed home footer: Home, Categories, AI Tools, Services, Profile.
        footerHotspot(5,185,()->{}); // Already on home
        footerHotspot(190,374,()->toPage(16));
        footerHotspot(380,560,()->toPage(15));
        footerHotspot(566,748,()->toPage(14));
        footerHotspot(753,930,this::profilePopup);
    }

    @Override protected void onDestroy(){
        if(soundPool!=null){soundPool.release();soundPool=null;}
        super.onDestroy();
    }
}
