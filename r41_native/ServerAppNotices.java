package asia.aaii.buddhapsychology;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** R4.1: server-published in-app announcement; existing Home UI remains unchanged. Not FCM push. */
public final class ServerAppNotices {
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private ServerAppNotices(){}

    public static void check(Activity a) {
        IO.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String path = AccountSession.API_BASE + "/api/managed_app_public.php?resource=notifications&app_key=buddha_psychology";
                conn = (HttpURLConnection)new URL(path).openConnection();
                conn.setConnectTimeout(6000);conn.setReadTimeout(6000);
                conn.setInstanceFollowRedirects(false);
                if (conn.getResponseCode()!=200 || conn.getContentLengthLong()>131072)return;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try(InputStream in=conn.getInputStream()) {
                    byte[] b = new byte[2048];int n;
                    while((n=in.read(b))!=-1){if(out.size()+n>131072)return;out.write(b,0,n);}
                }
                JSONObject response = new JSONObject(out.toString("UTF-8"));
                if(!response.optBoolean("ok",false))return;
                JSONArray arr=response.optJSONArray("notifications");
                if(arr==null || arr.length()==0)return;
                JSONObject latest=arr.optJSONObject(0);
                if(latest==null)return;
                int id=latest.optInt("id",0);
                if(id<=0 || id<=a.getSharedPreferences("bp_notifications",Context.MODE_PRIVATE).getInt("seen_id",0))return;
                String title=latest.optString("title","").trim();
                String body=latest.optString("body","").trim();
                String link=latest.optString("target_url","").trim();
                if(title.isEmpty()||title.length()>160||body.isEmpty()||body.length()>2000)return;
                MAIN.post(()->{
                    if(a.isFinishing()||a.isDestroyed())return;
                    if(id<=a.getSharedPreferences("bp_notifications",Context.MODE_PRIVATE).getInt("seen_id",0))return;
                    AlertDialog.Builder box=new AlertDialog.Builder(a)
                      .setTitle("Announcement · "+title)
                      .setMessage(body)
                      .setPositiveButton("OK",(d,w)->{});
                    if(link.startsWith("https://")){
                        box.setNeutralButton("Open Link",(d,w)->{
                            try {a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(link)).addCategory(Intent.CATEGORY_BROWSABLE));}catch(Exception ignored){}
                        });
                    }
                    AlertDialog dialog=box.create();
                    dialog.setOnDismissListener(d->a.getSharedPreferences("bp_notifications",Context.MODE_PRIVATE).edit().putInt("seen_id",id).apply());
                    dialog.show();
                });
            }catch(Exception ignored){
                // Offline is normal. Existing native book and audio continue functioning.
            }finally{if(conn!=null)conn.disconnect();}
        });
    }
}
