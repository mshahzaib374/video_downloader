package com.example.a4kvideodownloaderplayer.ads.app_open_ad

object OpenAppAdState {

    private var openAppEnabled = true

    private var screenName = "UNDEFINED"

    fun enable(screenName: String = "UNDEFINED") {
        OpenAppAdState.screenName = screenName
        openAppEnabled = true
    }

    fun disable(screenName: String = "UNDEFINED") {
        OpenAppAdState.screenName = screenName
        openAppEnabled = false
    }

    fun isOpenAppAdEnabled(): Boolean = openAppEnabled

}