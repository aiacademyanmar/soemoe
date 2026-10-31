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
print("Patched original V1.6.5 HomeActivity with one optional ad slot and supplied app icon")