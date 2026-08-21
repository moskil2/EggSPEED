import java.util.Properties
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.bafspeed.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.spotrobotics.eggspeed"
        minSdk = 26
        targetSdk = 36
        versionCode = 50
        versionName = "0.3.48"

        val buildStamp = SimpleDateFormat("yyyyMMdd.HHmm").format(Date())
        buildConfigField("String", "BUILD_STAMP", "\"$buildStamp\"")
    }

    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties()
    if (keystorePropsFile.exists()) {
        keystoreProps.load(FileInputStream(keystorePropsFile))
    }

    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = rootProject.file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    // MediaSession/NotificationCompat.MediaStyle - wymagane w runtime przez app/libs/bafspeed-aod-release.aar
    // (plik .aar nie sciaga wlasnych zaleznosci automatycznie, trzeba je zadeklarowac tutaj tak samo)
    implementation("androidx.media:media:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // USB serial (UART przez OTG) — jitpack; potrzebne do zapakowania klas
    // wykorzystywanych wewnątrz app/libs/bafspeed-protocol-release.aar
    implementation("com.github.mik3y:usb-serial-for-android:3.8.1")

    // Prywatny moduł protokołu (BafSPEED-protocol), dostarczany jako skompilowany .aar
    implementation(files("libs/bafspeed-protocol-release.aar"))

    // Kokpit na ekranie blokady/AOD, dostarczany jako skompilowany .aar
    implementation(files("libs/bafspeed-aod-release.aar"))

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
