package com.example.a4kvideodownloaderplayer.helper


import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


object RemoteConfig {
    private var _remoteConfig: FirebaseRemoteConfig? = null

    const val OnBoardBannerAd=  "OnBoardBannerAd"
    const val OnBoardBannerAdId= "OnBoardBannerAdId"

    const val LanguageNativeAd= "LanguageNativeAd"
    const val LanguageNativeAdId= "nativeAdIdLanguage"

    const val HomeNativeAd= "HomeNativeAd"
    const val HomeNativeAdId= "nativeAdIdHome"

    const val LanguageBannerAd= "LanguageBannerAd"
    const val LanguageBannerAdId= "bannerAdIdLanguage"

    const val ExitNativeAd= "exitnativead"
    const val ExitNativeAdId= "nativeAdIdExit"


    const val InterstitialAd= "interstitialAd"
    const val InterstitialAdId= "interstitialAdId"


    const val AppOpenAdId= "appOpenonAdIdresume"
    const val AppOpenSplashAdId= "appOpenAdIdSplash"


    init {
        initRemoteConfig()
    }


    private fun initRemoteConfig() {
        _remoteConfig = FirebaseRemoteConfig.getInstance()

        val defaultValues = mapOf(
            OnBoardBannerAd to true,
            HomeNativeAd to true,
            LanguageBannerAd to true,
            LanguageNativeAd to true,
            InterstitialAd to true,
            ExitNativeAd to true,
            HomeNativeAdId to "",
            ExitNativeAdId to "",
            OnBoardBannerAdId to "",
            LanguageNativeAdId to "",
            InterstitialAdId to "",
            LanguageBannerAdId to "",
            AppOpenAdId to "",
            AppOpenSplashAdId to ""
        )

        _remoteConfig?.setDefaultsAsync(defaultValues)

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(5)
            .build()

        _remoteConfig?.setConfigSettingsAsync(configSettings)

        fetchRemoteConfig()
    }

    private fun fetchRemoteConfig() {
        _remoteConfig?.fetchAndActivate()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FirebaseManager", "Fetch and activate succeeded")
            } else {
                Log.e("FirebaseManager", "Fetch and activate failed", task.exception)
            }
        }
    }

    fun getRemoteConfigValue(key: String): String {
        return _remoteConfig?.getString(key) ?: "default_ad_id_value"
    }

    fun getRemoteConfigBoolean(key: String): Boolean {
        return true
    }
}



