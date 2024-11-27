package com.example.a4kvideodownloaderplayer.ads.reward_ad

import android.app.Activity
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils
import com.google.android.gms.ads.rewarded.RewardedAd

class RewardedAdUtils {

    companion object {

        private var mRewardAd: RewardedAd? = null

        fun isAdAvailable(): Boolean = mRewardAd != null

        internal fun getRewardAd(): RewardedAd? {
            return mRewardAd
        }

        internal fun setRewardAd(ad: RewardedAd?) {
            mRewardAd = ad
        }

    }


    fun loadRewardedAd(
        context: Activity,
        remote: Boolean,
        adUnit: String,
        callback: RewardAdLoadCallback
    ) {

        AdmobifyUtils.validateAdMobAdUnitId(adUnit)

            RewardAdLoader.loadRewardAd(
                context = context,
                remote = remote,
                adUnit = adUnit,
                callback = callback
            )

    }

    fun showRewardedAd(
        context: Activity,
        remote: Boolean,
        callback: RewardAdShowCallback
    ) {

        RewardAdLoader.showRewardAd(
            context = context,
            remote = remote,
            callback = callback
        )

    }


}