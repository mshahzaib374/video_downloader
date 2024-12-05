import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.a4kvideodownloaderplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "vid.hd.videodownloader.videoplayer.download.all.videosapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 6
        versionName = "1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val formattedDate = SimpleDateFormat("yyyy_MM_dd_HH_mm")
        setProperty("archivesBaseName", "video_downloader_v$versionName($versionCode)_" + formattedDate.format(
            Date()
        ))
    }

    buildTypes {
        release {
            resValue("string", "appAdId", "ca-app-pub-4199820502555116~1802004629")
            resValue("string", "bannerOnBoardingAd", "ca-app-pub-4199820502555116/7000590482")
            resValue("string", "languageNativeAd", "ca-app-pub-4199820502555116/5903131232")
            resValue("string", "languageBannerAd", "ca-app-pub-4199820502555116/8064160646")
            resValue("string", "homeNativeAd", "ca-app-pub-4199820502555116/4124915630")
            resValue("string", "homeInterstitialAd", "ca-app-pub-4199820502555116/9650804557")
            resValue("string", "appOpenAdSplash", "ca-app-pub-4199820502555116/1610432933")
            resValue("string", "appOpenAdResume", "ca-app-pub-4199820502555116/1556692113")
            resValue("string", "exitNativeAd", "ca-app-pub-4199820502555116/1059105889")
            resValue("string", "playerInterstitialAd", "ca-app-pub-4199820502555116/2835770729")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            debug {
                resValue("string", "appAdId", "ca-app-pub-3940256099942544~3347511713")
                resValue("string", "bannerOnBoardingAd", "ca-app-pub-3940256099942544/9214589741")
                resValue("string", "languageNativeAd", "ca-app-pub-3940256099942544/2247696110")
                resValue("string", "languageBannerAd", "ca-app-pub-3940256099942544/9214589741")
                resValue("string", "homeNativeAd", "ca-app-pub-3940256099942544/2247696110")
                resValue("string", "homeInterstitialAd", "ca-app-pub-3940256099942544/1033173712")
                resValue("string", "appOpenAdSplash", "ca-app-pub-3940256099942544/9257395921")
                resValue("string", "appOpenAdResume", "ca-app-pub-3940256099942544/9257395921")
                resValue("string", "exitNativeAd", "ca-app-pub-3940256099942544/2247696110")
                resValue("string", "playerInterstitialAd", "ca-app-pub-3940256099942544/1033173712")

            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    bundle {
        language {
            enableSplit = false
        }
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.sdp.android)
    implementation (libs.ssp.android)
    implementation (libs.lottie)
    implementation (libs.androidx.viewpager2)
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation (libs.dexter)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.gson)

    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.glide)
    implementation (libs.okhttp)
    implementation(libs.androidx.lifecycle.process)


    implementation ("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.19.1")

    //firebase
    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation ("com.facebook.shimmer:shimmer:0.5.0")


    //admob
    implementation(libs.play.services.ads)
    implementation(libs.billing)

    //in app update
    implementation("com.google.android.play:app-update-ktx:2.1.0")




}