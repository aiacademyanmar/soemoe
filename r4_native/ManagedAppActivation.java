package asia.aaii.buddhapsychology;
import android.app.Activity;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.BiConsumer;
/** R4 network-only managed-app license activation. Does not store raw codes. */
public final class ManagedAppActivation {
  private ManagedAppActivation(){}
  public static void activate(Activity activity,String code,BiConsumer<Boolean,String> done){
    if(!AccountSession.isLoggedIn(activity)){done.accept(false,"Account Register / Login အရင်ဝင်ပါ။");return;}
    final String raw=code.trim().toUpperCase(Locale.ROOT);
    if(!raw.matches("AAII-[0-9A-F]{24}")){done.accept(false,"Invalid managed app license code");return;}
    new Thread(()->{
      boolean allowed=false;String message="License verification failed";
      HttpURLConnection conn=null;
      try {
        URL u=new URL(AccountSession.API_BASE+"/api/managed_app_license.php");
        conn=(HttpURLConnection)u.openConnection();
        conn.setConnectTimeout(10000);conn.setReadTimeout(10000);conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json; charset=utf-8");
        conn.setRequestProperty("Accept","application/json");
        conn.setRequestProperty("Authorization","Bearer "+AccountSession.token(activity));
        conn.setDoOutput(true);
        JSONObject j=new JSONObject();j.put("action","activate");j.put("app_key","buddha_psychology");
        j.put("device_id",AccountSession.deviceId(activity));j.put("license_code",raw);
        try(OutputStream out=conn.getOutputStream()){out.write(j.toString().getBytes(StandardCharsets.UTF_8));}
        int status=conn.getResponseCode();
        InputStream is=status<400?conn.getInputStream():conn.getErrorStream();
        if(is==null)throw new IOException("HTTP "+status);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(InputStream in=is){byte[] b=new byte[2048];int n;while((n=in.read(b))!=-1){if(bytes.size()+n>8192)throw new IOException("Response too large");bytes.write(b,0,n);}}
        JSONObject response=new JSONObject(bytes.toString("UTF-8"));
        allowed=status==200 && response.optBoolean("allowed",false);
        message=allowed?"App License activated":response.optString("error","License rejected");
      }catch(Exception e){message="License API ကို ချိတ်ဆက်မရပါ။ Server နှင့် Internet စစ်ဆေးပါ။";}
      finally{if(conn!=null)conn.disconnect();}
      final boolean ok=allowed;final String msg=message;
      activity.runOnUiThread(()->done.accept(ok,msg));
    }).start();
  }
}
