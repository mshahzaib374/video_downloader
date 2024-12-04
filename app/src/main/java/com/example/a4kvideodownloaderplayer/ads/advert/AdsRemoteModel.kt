package com.example.a4kvideodownloaderplayer.ads.advert


var adsRemoteModel : AdsRemoteModel? = null
data class AdsRemoteModel(
    var native_language_l : Boolean? = true,
    var banner_language_l : Boolean? = true,
    var banner_onboarding_l : Boolean? = true,
    var native_home_l : Boolean? = true,
    var fullscreen_home_l : Boolean? = true,
    var native_exit_l : Boolean? = true,
    var app_open_l : Boolean? = true,
    var fullscreen_video_l : Boolean? = true,
    var fullScreenCappingL : Long? = 5000
)

