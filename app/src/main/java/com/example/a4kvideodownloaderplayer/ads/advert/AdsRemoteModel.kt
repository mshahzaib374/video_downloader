package com.example.a4kvideodownloaderplayer.ads.advert


var adsRemoteModel : AdsRemoteModel? = null
data class AdsRemoteModel(
    var native_language_l : Boolean? = true,
    var banner_language_l : Boolean? = true,
    var banner_splash_top : Boolean? = true,
    var banner_splash_med : Boolean? = true,
    var banner_splash_bottom : Boolean? = true,
    var banner_onboarding_top : Boolean? = true,
    var banner_onboarding_med : Boolean? = true,
    var banner_onboarding_bottom : Boolean? = true,
    var native_home_l : Boolean? = true,
    var fullscreen_home_l : Boolean? = true,
    var fullscreen_home_navigation_l : Boolean? = false,
    var fullscreen_disclaimer_l : Boolean? = true,
    var native_exit_l : Boolean? = true,
    var app_open_l : Boolean? = true,
    var fullscreen_video_l : Boolean? = true,
    var native_downloaded_video_l : Boolean? = true,
    var fullScreenCappingL : Long? = 5000
)

