plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "asia.aaii.soemoeacademy"
 compileSdk = 35
 defaultConfig { applicationId = "asia.aaii.soemoeacademy"; minSdk = 24; targetSdk = 35; versionCode = 57; versionName = "5.7" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
