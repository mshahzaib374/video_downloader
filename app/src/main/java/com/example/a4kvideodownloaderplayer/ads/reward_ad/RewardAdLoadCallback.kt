package com.example.a4kvideodownloaderplayer.ads.reward_ad

import com.google.android.gms.ads.LoadAdError

abstract class RewardAdLoadCallback {

    open fun adAlreadyLoaded() {

    }

    open fun adLoaded() {

    }

    open fun adFailed(error: LoadAdError?,msg:String?) {

    }

}