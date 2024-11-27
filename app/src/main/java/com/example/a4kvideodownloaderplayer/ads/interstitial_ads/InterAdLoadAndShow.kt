package com.example.a4kvideodownloaderplayer.ads.interstitial_ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterstitialAdUtils.Companion.loadingInterstitialAd
import com.example.a4kvideodownloaderplayer.ads.utils.Admobify
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils
import com.example.a4kvideodownloaderplayer.ads.utils.LoadingDialog
import com.example.a4kvideodownloaderplayer.ads.utils.isShowingInterAd
import com.example.a4kvideodownloaderplayer.ads.utils.isShowingOpenAd
import com.example.a4kvideodownloaderplayer.ads.utils.isShowingRewardAd
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Category
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Level
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Logger
import com.google.android.gms.ads.LoadAdError

internal class InterAdLoadAndShow(
    val activity: Activity,
    private val interstitialAdUtils: InterstitialAdUtils
) {

    private var loadingDialog: LoadingDialog? = null

    private var appPausedOnce = false

    private var isPaused = false

    private var adOptions = interstitialAdUtils.adOptions

    init {
        registerActivityLifeCycle()
    }

    fun loadAndShowInter(
        interAdLoadCallback: InterAdLoadCallback?,
        showCallback: InterAdShowCallback?, readyToNavigate: (() -> Unit)? = null
    ) {


        appPausedOnce = false

        /** An Ad is not already loaded , remote is enabled & not a premium user */

        if (InterstitialAdUtils.isAdAvailable() && adOptions.isRemoteEnabled() && !Admobify.isPremiumUser()) {


            /** An Inter , Rewarded OR Open Ad is not already showing to
             * prevent overlaying of ad on other ad */

            if (!isShowingInterAd() && !isShowingRewardAd() && !isShowingOpenAd()) {

                val delay = adOptions.getDialogFakeDelay

                if (delay > 0){
                    showLoadingDialog()
                }

                Handler(Looper.getMainLooper()).postDelayed({

                    if (!isPaused && !appPausedOnce){
                        readyToNavigate?.invoke()
                        InterstitialAdUtils.fromLoadAndShow = true
                        showInterAd(showCallback)
                    }

                }, delay)

            }

        } else {
            /** Loading function have built in mechanism to handle all scenarios */
            loadInterAd(interAdLoadCallback, showCallback, readyToNavigate)
        }

    }

    private fun loadInterAd(
        loadCallback: InterAdLoadCallback?,
        showCallback: InterAdShowCallback?,
        readyToNavigate: (() -> Unit)? = null
    ) {

        /**
         * An Ad request is already being processed
         */

        if (loadingInterstitialAd) {
            val msg = "Already processing ad request"
            Logger.log(Level.DEBUG, Category.Interstitial, msg)
            loadCallback?.adFailed(null, msg)
            return
        }

        /** Check required params before showing loading dialog */

        if (interstitialAdUtils.adOptions.isRemoteEnabled() &&
            !Admobify.isPremiumUser() &&
            AdmobifyUtils.isNetworkAvailable(activity)
        ) {

            showLoadingDialog()

            interstitialAdUtils.loadInterstitialAd(object : InterAdLoadCallback() {

                override fun adAlreadyLoaded() {
                    loadCallback?.adAlreadyLoaded()

                    if (!isPaused && !appPausedOnce) {
                        readyToNavigate?.invoke()
                        InterstitialAdUtils.fromLoadAndShow = true
                        showInterAd(showCallback)
                    }
                }

                override fun adLoaded() {
                    loadCallback?.adLoaded()

                    if (!isPaused && !appPausedOnce) {
                        readyToNavigate?.invoke()
                        InterstitialAdUtils.fromLoadAndShow = true
                        showInterAd(showCallback)
                    }
                }

                override fun adFailed(error: LoadAdError?, msg: String?) {
                    readyToNavigate?.invoke()
                    loadCallback?.adFailed(error, msg)
                }

                override fun adValidate() {
                    readyToNavigate?.invoke()
                    loadCallback?.adValidate()
                }

            })
        } else {
            loadCallback?.adValidate()
        }
    }

    private fun canShowInterAd(): Boolean {
        return InterstitialAdUtils.isAdAvailable() &&
                adOptions.isRemoteEnabled() && !Admobify.isPremiumUser()
    }

    private fun showInterAd(interAdShowCallback: InterAdShowCallback?) {

        dismissLoadingDialog()

        interstitialAdUtils.showInterstitialAd(object : InterAdShowCallback() {

            override fun adNotAvailable() {
                dismissLoadingDialog()
                interAdShowCallback?.adNotAvailable()
            }

            override fun adShowFullScreen() {
                interAdShowCallback?.adShowFullScreen()
            }

            override fun adDismiss() {
                interAdShowCallback?.adDismiss()
            }

            override fun adFailedToShow() {
                interAdShowCallback?.adFailedToShow()
            }

            override fun adImpression() {
                interAdShowCallback?.adImpression()
            }

            override fun adClicked() {
                interAdShowCallback?.adClicked()
            }

        })
    }


    private fun registerActivityLifeCycle() {
        activity.application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(currActivity: Activity, p1: Bundle?) {

            }

            override fun onActivityStarted(p0: Activity) {
            }

            override fun onActivityResumed(currActivity: Activity) {
                if (currActivity == activity){
                    isPaused = false
                }
            }

            override fun onActivityPaused(currActivity: Activity) {
                if (currActivity == activity){
                    appPausedOnce = true
                    isPaused = true
                    dismissLoadingDialog()
                }
            }

            override fun onActivityStopped(currActivity: Activity) {

            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityDestroyed(currActivity: Activity) {

            }
        })
    }

    private fun showLoadingDialog() {

        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        loadingDialog = LoadingDialog(activity,adOptions.getCustomLoadingLayout(),adOptions.isFullScreenLoading())
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }

}