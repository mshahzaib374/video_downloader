package com.example.a4kvideodownloaderplayer.fragments.splash

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.example.a4kvideodownloaderplayer.BuildConfig
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.AdsRemoteModel
import com.example.a4kvideodownloaderplayer.ads.advert.RemoteConfigurations
import com.example.a4kvideodownloaderplayer.ads.advert.adsRemoteModel
import com.example.a4kvideodownloaderplayer.ads.advert.app_open_l
import com.example.a4kvideodownloaderplayer.ads.advert.banner_language_l
import com.example.a4kvideodownloaderplayer.ads.advert.banner_onboarding_l
import com.example.a4kvideodownloaderplayer.ads.advert.fullScreenCappingL
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_home_l
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_video_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_exit_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_home_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_language_l
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAd
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAdState
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.SplashOpenAppAd
import com.example.a4kvideodownloaderplayer.ads.billing.BillingListener
import com.example.a4kvideodownloaderplayer.ads.billing.BillingUtils
import com.example.a4kvideodownloaderplayer.ads.consent_form.AdmobConsentForm
import com.example.a4kvideodownloaderplayer.ads.utils.Admobify
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils
import com.example.a4kvideodownloaderplayer.databinding.FragmentSplashBinding
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.aiartgenerator.utils.AppPrefs
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var defaultTime = 15000L
    private var countDownTimer: CountDownTimer? = null
    private var shouldStart = false
    private var isPause = false
    private var navigateOrNot = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenAppAdState.disable("SplashFragment")
        checkOldSubScription()
    }

    private fun checkOldSubScription() {
        BillingUtils.getInstance()
            .setBillingListener(object : BillingListener() {

                override fun productAndSubMetaData(
                    products: List<ProductDetails>,
                    subscriptions: List<ProductDetails>
                ) {
                    // Handle product and subscription metadata
                }

                override fun purchasedORSubDone(productsList: List<Purchase?>) {
                    Admobify.setPremiumUser(true)
                }

                override fun purchasedAndSubscribedList(
                    purchaseList: List<Purchase>,
                    subList: List<Purchase>
                ) {
                    // Handle combined list
                    if (subList.isNotEmpty()) {
                        binding?.containAdsTv?.visibility = View.GONE
                        Admobify.setPremiumUser(true)
                    }else{
                        binding?.containAdsTv?.visibility = View.VISIBLE
                        Admobify.setPremiumUser(false)
                    }
                }
            })
            .setSubscriptionIds("one_month_package", "six_month_package")
            .build(activity ?: return)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSplashAnimations()
        context?.logFirebaseEvent("splash_fragment", "screen_opened")
        if (AdmobifyUtils.isNetworkAvailable(context ?: return) && !Admobify.isPremiumUser()) {
            AdmobConsentForm
                .getInstance()
                .setDebug(BuildConfig.DEBUG)
                .build(activity ?: return)
                .loadAndShowConsentForm {
                    adsInitialize()
                    startTimer()
                }

        } else {
            defaultTime = 3000
            startTimer()
        }




    }

    private fun adsInitialize() {
        adsRemoteConfig()
        Admobify.initialize(
            context = context ?: return,
            testDevicesList = arrayListOf(
                "40FCFB5F8EE015FEC661521E08D51DB6",
                "7C12541A1F5DAD8864C7DDBC33D2E96B"
            ),
            premiumUser = Admobify.isPremiumUser()
        )
        //Logger.enableLogging()

        loadSplashAppOpen()

    }

    private fun adsRemoteConfig() {
        RemoteConfigurations.fetchRemotes(
            activity ?: return,
            jsonKey = "ads_json_05",
            defaultXml = R.xml.ads_remote_config,
            onSuccess = {
                try {
                    val data = Gson().fromJson(it, AdsRemoteModel::class.java)
                    adsRemoteModel = data
                    native_language_l = adsRemoteModel?.native_language_l == true
                    banner_language_l = adsRemoteModel?.banner_language_l == true
                    banner_onboarding_l = adsRemoteModel?.banner_onboarding_l == true
                    native_home_l = adsRemoteModel?.native_home_l == true
                    fullscreen_home_l = adsRemoteModel?.fullscreen_home_l == true
                    native_exit_l = adsRemoteModel?.native_exit_l == true
                    app_open_l = adsRemoteModel?.app_open_l == true
                    fullScreenCappingL = adsRemoteModel?.fullScreenCappingL ?: 2000
                    fullscreen_video_l = adsRemoteModel?.fullscreen_video_l == true


                    Log.d("REMOTE_CONFIG", "native_language_l: $native_language_l")
                    Log.d("REMOTE_CONFIG", "banner_language_l: $banner_language_l")
                    Log.d("REMOTE_CONFIG", "banner_onboarding_l: $banner_onboarding_l")
                    Log.d("REMOTE_CONFIG", "native_home_l: $native_home_l")
                    Log.d("REMOTE_CONFIG", "native_exit_l: $native_exit_l")
                    Log.d("REMOTE_CONFIG", "app_open_l: $app_open_l")
                    Log.d("REMOTE_CONFIG", "fullScreenCapping: $fullScreenCappingL")
                    Log.d("REMOTE_CONFIG", "fullscreen_video_l: $fullscreen_video_l")




                    OpenAppAd().init(
                        application = activity?.application ?: return@fetchRemotes,
                        adId = getString(R.string.appOpenAdResume),
                        remote = app_open_l,
                        preloadAd = false,
                        loadOnPause = true,
                        reloadOnDismiss = false
                    )

                } catch (e: Exception) {

                }
            },
            onFailure = {

            }
        )
    }


    private fun startTimer() {
        shouldStart = true
        val targetProgress = 100
        val progressBarUpdateInterval = (defaultTime / targetProgress).coerceAtLeast(1)
        countDownTimer = object : CountDownTimer(defaultTime, progressBarUpdateInterval) {
            override fun onTick(millisUntilFinished: Long) {
                defaultTime = millisUntilFinished
            }

            override fun onFinish() {
                Log.e("TAG", "onFinish")
                navigateOrNot = true
                if (!isPause) {
                    navigateScreen()
                }
            }

        }
        countDownTimer?.start()
    }


    private fun loadSplashAppOpen() {
        SplashOpenAppAd.loadOpenAppAd(
            context ?: return,
            getString(R.string.appOpenAdSplash),
            true,
            adLoaded = {
                showSplashAd()
            },
            adFailed = {
                Log.e("Ads_", "adFailed")
                if (!isPause && !navigateOrNot) {
                    countDownTimer?.cancel()
                    navigateScreen()
                } else {
                    defaultTime = 500L
                }
            },
            adValidate = {
                Log.e("Ads_", "adValidate")
                lifecycleScope.launch {
                    delay(3000L)
                    if (!isPause && !navigateOrNot) {
                        countDownTimer?.cancel()
                        navigateScreen()
                    } else {
                        defaultTime = 500L
                    }
                }
            }
        )
    }

    private fun showSplashAd() {
        SplashOpenAppAd.showOpenAppAd(
            activity ?: return,
            adShowFullScreen = {

            },
            adFailedToShow = {

            },
            adDismiss = {
                navigateScreen()
            }
        )
    }

    private fun startSplashAnimations() {
        binding?.apply {
            splashIconIv.alpha = 0f
            splashIconIv.scaleX = 0.2f // Start even smaller
            splashIconIv.scaleY = 0.2f // Start even smaller


            // Splash Description: Start invisible and from much further below
            splashDescription.alpha = 0f
            splashDescription.translationY = 800f // Start much further below the screen

            // Splash Ads Text: Start invisible and from much further left
           // containAdsTv.alpha = 0f
            //containAdsTv.translationX = -800f // Start much further from the left

            // Animate all three views at the same time
            splashIconIv.animate()
                .alpha(1f) // Fade in
                .scaleX(1.3f) // Scale up for a stronger popping effect
                .scaleY(1.3f) // Scale up for a stronger popping effect
                .setDuration(600) // Duration of 0.6 seconds
                .withEndAction {
                    // Return to original size after popping
                    splashIconIv.animate()
                        .scaleX(1f) // Return to original size
                        .scaleY(1f) // Return to original size
                        .setDuration(300) // Duration for scaling back
                }

            splashDescription.animate()
                .alpha(1f) // Fade in
                .translationY(0f) // Move to its position
                .setDuration(900) // Longer duration for a smoother appearance
                .setInterpolator(AccelerateDecelerateInterpolator()) // Smooth acceleration and deceleration

           /* containAdsTv.animate()
                .alpha(1f) // Fade in
                .translationX(0f) // Move to its position
                .setDuration(900) // Longer duration for a smoother appearance
                .setInterpolator(AccelerateDecelerateInterpolator()) // Smooth acceleration and deceleration*/

        }
    }

    private fun navigateScreen() {
        if (AppPrefs(context ?: return).getBoolean("isFirstTime")) {
            if (findNavController().currentDestination?.id == R.id.splashFragment) {
                findNavController().navigate(R.id.action_splashFragment_to_languageFragment)
            }
        } else {
            if (findNavController().currentDestination?.id == R.id.splashFragment) {
                findNavController().navigate(R.id.action_splashFragment_to_onHomeFragment)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        isPause = false
        if (shouldStart) {
            startTimer()
        }
    }

    override fun onPause() {
        isPause = true
        countDownTimer?.cancel()
        super.onPause()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        isPause = true
        super.onDestroy()
    }


}