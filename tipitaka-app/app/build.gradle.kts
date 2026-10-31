plugins { id("com.android.application") }
android {
 namespace = "asia.aaii.tipitaka"
 compileSdk = 35
 defaultConfig {
  applicationId = "asia.aaii.tipitaka"
  minSdk = 26
  targetSdk = 35
  versionCode = 8
  versionName = "1.7.0"
 }
 compileOptions {
   sourceCompatibility = JavaVersion.VERSION_17
   targetCompatibility = JavaVersion.VERSION_17
 }
}
