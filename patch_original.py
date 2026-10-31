from pathlib import Path
from PIL import Image
import os
root=Path("project/BuddhaPsychologyNative_V1_6_5")
src=root/"app/src/main/java/asia/aaii/buddhapsychology"
home=src/"HomeActivity.java"
s=home.read_text()
needle='root.addView(navigation(this,"Home"));refreshProgress();'
assert needle in s
home.write_text(s.replace(needle,'SingleHomeAd.install(this,body);\n        '+needle,1))
manifest=root/"app/src/main/AndroidManifest.xml"
m=manifest.read_text()
assert 'android:label="ဗုဒ္ဓ၏ စိတ်ပညာ"' in m
manifest.write_text(m.replace('android:label="ဗုဒ္ဓ၏ စိတ်ပညာ"','android:label="ဗုဒ္ဓ၏ စိတ်ပညာ" android:icon="@drawable/buddha_app_logo"',1))
logo=Image.open("approved_logo.png").convert("RGBA")
logo.thumbnail((512,512),Image.Resampling.LANCZOS)
logo.save(root/"app/src/main/res/drawable-nodpi/buddha_app_logo.png")
g=root/"app/build.gradle"
t=g.read_text()
assert "versionCode 15" in t
g.write_text(t.replace("versionCode 15","versionCode 16").replace("versionName '1.6.5'","versionName '1.6.5.2-single-ad'"))
url=os.environ.get("AAII_CENTRAL_ADMIN_URL","").strip()
if url and not url.startswith("https://"):raise ValueError("Admin API requires HTTPS")
(src/"AdminConnectionConfig.java").write_text('package asia.aaii.buddhapsychology; public final class AdminConnectionConfig { public static final String URL="'+url.replace('"','')+'"; }')
(src/"SingleHomeAd.java").write_text("package asia.aaii.buddhapsychology;\nimport android.app.Activity;\nimport android.content.Intent;\nimport android.net.Uri;\nimport android.os.Handler;\nimport android.os.Looper;\nimport android.view.View;\nimport android.widget.LinearLayout;\nimport android.widget.TextView;\nimport android.graphics.Color;\nimport org.json.JSONObject;\nimport java.net.HttpURLConnection;\nimport java.net.URL;\nimport java.io.ByteArrayOutputStream;\nimport java.io.InputStream;\nimport java.util.concurrent.ExecutorService;\nimport java.util.concurrent.Executors;\npublic final class SingleHomeAd {\n  private static final ExecutorService IO=Executors.newSingleThreadExecutor();\n  private static final Handler MAIN=new Handler(Looper.getMainLooper());\n  public static void install(Activity activity,LinearLayout homeBody) {\n    String api=AdminConnectionConfig.URL;\n    if(api==null||!api.startsWith(\"https://\"))return;\n    LinearLayout slot=new LinearLayout(activity);\n    slot.setVisibility(View.GONE);\n    homeBody.addView(slot,new LinearLayout.LayoutParams(-1,-2));\n    IO.execute(()->{\n      HttpURLConnection c=null;\n      try {\n        String sep=api.contains(\"?\")?\"&\":\"?\";\n        URL u=new URL(api+sep+\"page=api_home_ad&app_key=buddha_psychology\");\n        c=(HttpURLConnection)u.openConnection();\n        c.setInstanceFollowRedirects(false);c.setConnectTimeout(5000);c.setReadTimeout(5000);\n        if(c.getResponseCode()!=200||c.getContentLengthLong()>100000)return;\n        ByteArrayOutputStream o=new ByteArrayOutputStream();\n        try(InputStream in=c.getInputStream()){\n          byte[] b=new byte[4096];int n;\n          while((n=in.read(b))!=-1){if(o.size()+n>100000)return;o.write(b,0,n);}\n        }\n        JSONObject ad=new JSONObject(o.toString(\"UTF-8\")).optJSONObject(\"ad\");\n        if(ad==null)return;\n        String title=ad.optString(\"title\",\"\").trim();\n        String sub=ad.optString(\"subtitle\",\"\").trim();\n        String link=ad.optString(\"destination_url\",\"\").trim();\n        if(title.length()==0||title.length()>180||sub.length()>400||!link.startsWith(\"https://\"))return;\n        MAIN.post(()->{\n          if(activity.isFinishing()||activity.isDestroyed())return;\n          slot.removeAllViews();slot.setOrientation(LinearLayout.VERTICAL);\n          int pad=(int)(12*activity.getResources().getDisplayMetrics().density);\n          slot.setPadding(pad,pad,pad,pad);slot.setBackgroundColor(0xff30271a);\n          TextView name=new TextView(activity);name.setText(\"Software Advertisement · \"+title);\n          name.setTextColor(0xfff4d49a);name.setTextSize(15);slot.addView(name);\n          if(!sub.isEmpty()){\n            TextView description=new TextView(activity);description.setText(sub);\n            description.setTextColor(Color.WHITE);description.setTextSize(12);slot.addView(description);\n          }\n          slot.setOnClickListener(v->{\n            try{activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(link)).addCategory(Intent.CATEGORY_BROWSABLE));}catch(Exception ignored){}\n          });\n          slot.setVisibility(View.VISIBLE);\n        });\n      }catch(Exception ignored){}finally{if(c!=null)c.disconnect();}\n    });\n  }\n}")
print("Patched original V1.6.5 HomeActivity with one optional ad slot and supplied app icon")