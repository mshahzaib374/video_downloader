package com.example.a4kvideodownloaderplayer.ads.native_ads

import android.app.Application
import android.view.ViewGroup
import com.example.a4kvideodownloaderplayer.ads.native_ads.ad_types.NativeAdType
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils

class NativeAdUtils {

    fun loadNativeAd(
        application: Application,
        adId: String,
        adRemote: Boolean,
        adContainer: ViewGroup?,
        model: NativeAdItemsModel?,
        callback: NativeAdCallback,
        adType: NativeAdType = NativeAdType.DEFAULT_AD
    ) {

        AdmobifyUtils.validateAdMobAdUnitId(adId)

        CustomNativeAd(
            application = application,
            adId = adId,
            remote = adRemote,
            adContainer = adContainer
        ).loadNativeAd(
            adListener = callback,
            adType = adType,
            model = model
        )

    }

}