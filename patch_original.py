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

# V1.6.5.3 — original More click handlers are unchanged; only the popup renderer changes.
from shutil import copyfile
ui=src/"UiKit.java"
ui_text=ui.read_text(encoding="utf-8")
start=ui_text.index('                else new AlertDialog.Builder(a).setTitle("ဗုဒ္ဓ၏ စိတ်ပညာ")')
end=ui_text.index('                   }).show();',start)+len('                   }).show();')
old=ui_text[start:end]
assert old.count('idx==') == 10, "Expected 11 original More actions; refuse to redesign"
handler=old.split('(d,idx)->{',1)[1].rsplit('}).show();',1)[0]
updated='                else MoreOptionsDialog.show(a, idx->{'+handler+'});'
ui.write_text(ui_text[:start]+updated+ui_text[end:],encoding="utf-8")
copyfile("more_patch/MoreOptionsDialog.java",src/"MoreOptionsDialog.java")
gradle=root/"app/build.gradle"
gr=gradle.read_text(encoding="utf-8")
assert "versionCode 16" in gr and "1.6.5.2-single-ad" in gr
gradle.write_text(gr.replace("versionCode 16","versionCode 17").replace("1.6.5.2-single-ad","1.6.5.3-more-admin"),encoding="utf-8")
assert "MoreOptionsDialog.show(a, idx->{" in ui.read_text(encoding="utf-8")
print("V1.6.5.3: original More actions preserved, dialog restyled, Home Ad single slot retained.")


# R4 license redemption under the existing Account License button; no new screen.
native_license=src/"PremiumLicense.java"
ps=native_license.read_text(encoding="utf-8")
needle='public static void prompt(Activity a){EditText code=codeField(a);'
assert needle in ps
ps=ps.replace(needle,'public static void prompt(Activity a){EditText code=codeField(a);',1)
old='(ok,msg)->new AlertDialog.Builder(a).setTitle(ok?"Activated":"Activation failed").setMessage(msg).setPositiveButton("OK",null).show()'
assert old in ps
new='(ok,msg)->{if(ok){new AlertDialog.Builder(a).setTitle("Activated").setMessage(msg).setPositiveButton("OK",null).show();}else if(AccountSession.isLoggedIn(a)){ManagedAppActivation.activate(a,code.getText().toString(),(yes,reason)->new AlertDialog.Builder(a).setTitle(yes?"App License Activated":"Activation failed").setMessage(yes?reason:reason+"\\nLegacy: "+msg).setPositiveButton("OK",null).show());}else{new AlertDialog.Builder(a).setTitle("Activation failed").setMessage(msg).setPositiveButton("OK",null).show();}}'
ps=ps.replace(old,new,1)
native_license.write_text(ps,encoding="utf-8")
copyfile("r4_native/ManagedAppActivation.java",src/"ManagedAppActivation.java")
gradle=root/"app/build.gradle"
gr=gradle.read_text(encoding="utf-8")
assert "versionCode 17" in gr
gradle.write_text(gr.replace("versionCode 17","versionCode 18").replace("1.6.5.3-more-admin","1.6.5.4-app-management"),encoding="utf-8")
print("R4 native client integrated, no navigation or screen modification.")


# R4.1: In-app server announcements, once on Home screen entry; preserve exact layout.
notices=src/"ServerAppNotices.java"
copyfile("r41_native/ServerAppNotices.java", notices)
home=src/"HomeActivity.java"
home_text=home.read_text(encoding="utf-8")
target='@Override public void onCreate(Bundle b){super.onCreate(b);render();}'
assert target in home_text
home.write_text(home_text.replace(target,'@Override public void onCreate(Bundle b){super.onCreate(b);render();ServerAppNotices.check(this);}',1),encoding="utf-8")
gradle=root/"app/build.gradle"
gr=gradle.read_text(encoding="utf-8")
assert "versionCode 18" in gr
gradle.write_text(gr.replace("versionCode 18","versionCode 19").replace("1.6.5.4-app-management","1.6.5.5-server-notices"),encoding="utf-8")
print("R4.1 notifications fetch on Home open with no new UI navigation or layout.")


# V1.6.6: visual-only More menu refinement; keep 11 original navigation actions.
copyfile("more_patch/MoreOptionsDialogV166.java",src/"MoreOptionsDialog.java")
more_text=(src/"MoreOptionsDialog.java").read_text(encoding="utf-8")
assert "V1.6.6" in more_text and "for(int i=6;i<10;i++)" in more_text
assert "MoreOptionsDialog.show(a, idx->{" in ui.read_text(encoding="utf-8")
gradle=root/"app/build.gradle"
gr=gradle.read_text(encoding="utf-8")
assert "versionCode 19" in gr and "1.6.5.5-server-notices" in gr
gradle.write_text(gr.replace("versionCode 19","versionCode 20").replace("1.6.5.5-server-notices","1.6.6"),encoding="utf-8")
print("Buddha Psychology V1.6.6 original UI + better More panel, 11 actions intact.")
