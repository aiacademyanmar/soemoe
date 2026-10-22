plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}
android {
  namespace = "asia.aaii.kidcreator"
  compileSdk = 35
  defaultConfig {
    applicationId = "asia.aaii.kidcreator.buildtest"
    minSdk = 26
    targetSdk = 35
    versionCode = 2402
    versionName = "2.4.2-scene-only"
  }
  buildTypes { release { isMinifyEnabled = false } }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
  buildFeatures { compose = true }
}
dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
  implementation(platform("androidx.compose:compose-bom:2024.12.01"))
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")
  testImplementation("junit:junit:4.13.2")
}
