package asia.aaii.dama;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.webkit.WebView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * V22 global, lightweight touch feedback used by all native Activity screens.
 * Gold-themed controls respond on touch DOWN; on a true tap they spring/pop and
 * give an optional very soft click. Scrolling/cancel/multitouch never emits sound.
 * No popup window is created per tap, so playback and navigation stay responsive.
 */
public class DamaActivity extends Activity {
    private View pressed;
    private float downX,downY;
    private int touchSlop;
    private long lastStartAt=0;
    private String lastStart="";
    private static final long ROUTE_DEBOUNCE_MS=350;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);
        touchSlop=ViewConfiguration.get(this).getScaledTouchSlop();
        DamaTouchFeedback.warmup(this);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event){
        final int action=event.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){
            restorePressed(false);
            downX=event.getRawX();downY=event.getRawY();
            View root=getWindow()==null?null:getWindow().getDecorView();
            pressed=findClickable(root,downX,downY);
            if(pressed!=null){
                pressed.animate().cancel();
                pressed.animate().scaleX(.965f).scaleY(.965f).alpha(.88f)
                    .setDuration(65).start();
            }
        } else if(action==MotionEvent.ACTION_MOVE){
            if(pressed!=null&&(Math.abs(event.getRawX()-downX)>touchSlop ||
                  Math.abs(event.getRawY()-downY)>touchSlop))restorePressed(false);
        } else if(action==MotionEvent.ACTION_UP){
            if(pressed!=null){
                View hit=findClickable(getWindow().getDecorView(),event.getRawX(),event.getRawY());
                final View clicked=pressed;
                if(hit==clicked && clicked.isEnabled() && clicked.isShown()){
                    pressed=null;
                    clicked.animate().cancel();
                    clicked.animate().scaleX(1.045f).scaleY(1.045f).alpha(1f).setDuration(75)
                        .withEndAction(()->clicked.animate().scaleX(1f).scaleY(1f)
                            .setDuration(115).start()).start();
                    DamaTouchFeedback.play(clicked);
                }else restorePressed(false);
            }
        } else if(action==MotionEvent.ACTION_CANCEL || action==MotionEvent.ACTION_POINTER_DOWN){
            restorePressed(false);
        }
        // Never swallow Android's touch event: click, long click, zoom and scroll keep working.
        return super.dispatchTouchEvent(event);
    }

    private void restorePressed(boolean animated){
        if(pressed==null)return;
        View v=pressed;pressed=null;v.animate().cancel();
        if(animated)v.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(90).start();
        else {v.setScaleX(1f);v.setScaleY(1f);v.setAlpha(1f);}
    }

    private static boolean hit(View view,float screenX,float screenY){
        if(!view.isShown()||view.getWidth()<=0||view.getHeight()<=0)return false;
        int[] coords=new int[2];view.getLocationOnScreen(coords);
        return screenX>=coords[0]&&screenX<coords[0]+view.getWidth()
            &&screenY>=coords[1]&&screenY<coords[1]+view.getHeight();
    }
    private static View findClickable(View view,float x,float y){
        if(view==null||!hit(view,x,y))return null;
        if(view instanceof WebView || view instanceof EditText || view instanceof SeekBar)
            return null;
        // ListView rows are clicked via adapter listener; only animate the touched row.
        if(view instanceof AbsListView){
            AbsListView list=(AbsListView)view;
            for(int i=list.getChildCount()-1;i>=0;i--){
                View child=list.getChildAt(i);
                if(hit(child,x,y)&&child.isEnabled())return child;
            }
            return null;
        }
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=group.getChildCount()-1;i>=0;i--){
                View candidate=findClickable(group.getChildAt(i),x,y);
                if(candidate!=null)return candidate;
            }
        }
        if(view instanceof ScrollView || view instanceof HorizontalScrollView)return null;
        return view.isClickable()&&view.isEnabled()?view:null;
    }

    /** Reuse already-created in-app activities, avoiding repeated huge catalog parsing. */
    @Override public void startActivity(Intent intent){
        if(intent!=null && intent.getComponent()!=null
            && getPackageName().equals(intent.getComponent().getPackageName())){
            String route=intent.getComponent().flattenToShortString();
            long now=SystemClock.uptimeMillis();
            if(route.equals(lastStart) && now-lastStartAt<ROUTE_DEBOUNCE_MS)return;
            lastStart=route;lastStartAt=now;
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        super.startActivity(intent);
    }
}

/** Shared accessibility-friendly soft sound with per-device opt-out. */
final class DamaTouchFeedback {
    static final String PREFS="dama_touch_feedback";
    static final String SOUND_ENABLED="sound_enabled";
    private static final Object LOCK=new Object();
    private static final ExecutorService WORK=Executors.newSingleThreadExecutor();
    private static volatile ToneGenerator tone;
    private static volatile boolean prepared=false;
    private static volatile long lastSound=0;

    private DamaTouchFeedback(){}
    static boolean enabled(Context c){
        return c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
                .getBoolean(SOUND_ENABLED,true);
    }
    static void setEnabled(Context c,boolean enable){
        c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .putBoolean(SOUND_ENABLED,enable).apply();
        if(enable)warmup(c);
    }
    static void warmup(Context c){
        if(prepared || !enabled(c))return;
        synchronized(LOCK){
            if(prepared)return;
            prepared=true;
        }
        // Build ToneGenerator once on background worker to avoid slowing first tap.
        WORK.execute(()->{
            try{tone=new ToneGenerator(AudioManager.STREAM_MUSIC,17);}
            catch(Exception e){android.util.Log.w("DAMA_TOUCH","Tone unavailable",e);}
        });
    }
    static void play(View clicked){
        Context ctx=clicked.getContext();
        if(!enabled(ctx))return;
        long at=SystemClock.uptimeMillis();
        if(at-lastSound<110)return; // avoid duplicate sound from inherited Popup callbacks
        lastSound=at;
        try{
            AudioManager am=(AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
            if(am!=null&&am.getStreamVolume(AudioManager.STREAM_MUSIC)==0)return;
            ToneGenerator gen=tone;
            if(gen!=null){gen.startTone(ToneGenerator.TONE_PROP_ACK,38);}
            else clicked.playSoundEffect(android.view.SoundEffectConstants.CLICK);
        }catch(Exception e){
            try{clicked.playSoundEffect(android.view.SoundEffectConstants.CLICK);}
            catch(Exception ignored){}
        }
    }

    /** Explicit dialog-view binding: dialogs own a separate Window, not Activity dispatch. */
    static void bind(View control,Runnable action){
        control.setClickable(true);
        control.setOnTouchListener((v,e)->{
            int a=e.getActionMasked();
            if(a==MotionEvent.ACTION_DOWN){
                v.animate().cancel();v.animate().scaleX(.965f).scaleY(.965f)
                   .alpha(.88f).setDuration(65).start();
            }else if(a==MotionEvent.ACTION_UP){
                v.animate().cancel();
                v.animate().scaleX(1.045f).scaleY(1.045f).alpha(1f).setDuration(75)
                  .withEndAction(()->v.animate().scaleX(1f).scaleY(1f).setDuration(115).start()).start();
            }else if(a==MotionEvent.ACTION_CANCEL){
                v.animate().cancel();v.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(75).start();
            }
            return false;
        });
        control.setOnClickListener(v->{play(v);action.run();});
    }
}
