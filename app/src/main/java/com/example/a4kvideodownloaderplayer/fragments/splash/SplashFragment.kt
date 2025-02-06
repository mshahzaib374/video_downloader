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
import com.example.a4kvideodownloaderplayer.ads.advert.banner_onboarding_bottom
import com.example.a4kvideodownloaderplayer.ads.advert.banner_onboarding_med
import com.example.a4kvideodownloaderplayer.ads.advert.banner_onboarding_top
import com.example.a4kvideodownloaderplayer.ads.advert.banner_splash_bottom
import com.example.a4kvideodownloaderplayer.ads.advert.banner_splash_med
import com.example.a4kvideodownloaderplayer.ads.advert.banner_splash_top
import com.example.a4kvideodownloaderplayer.ads.advert.fullScreenCappingL
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_disclaimer_l
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_home_l
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_home_navigation_l
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_video_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_downloaded_video_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_exit_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_home_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_language_l
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAd
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAdState
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.SplashOpenAppAd
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerAdType
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerAdUtils
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerCallback
import com.example.a4kvideodownloaderplayer.ads.billing.BillingListener
import com.example.a4kvideodownloaderplayer.ads.billing.BillingUtils
import com.example.a4kvideodownloaderplayer.ads.consent_form.AdmobConsentForm
import com.example.a4kvideodownloaderplayer.ads.utils.Admobify
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils.hide
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils.invisible
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils.show
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Logger
import com.example.a4kvideodownloaderplayer.databinding.FragmentSplashBinding
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.visible
import com.example.aiartgenerator.utils.AppPrefs
import com.google.android.gms.ads.AdView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {
    private var binding: FragmentSplashBinding? = null
    private var defaultTime = 15000L
    private var countDownTimer: CountDownTimer? = null
    private var shouldStart = false
    private var isPause = false
    private var topAd: AdView? = null
    private var medAd: AdView? = null
    private var bottomAd: AdView? = null
    private var navigateOrNot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenAppAdState.disable("SplashFragment")
        checkOldSubscription()
    }

    private fun checkOldSubscription() {
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
                    } else {
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
        //startSplashAnimations()
        binding?.apply {
            lifecycleScope.launch(Dispatchers.Main) {
                lottieAnimationView2.setAnimation(R.raw.slider)
                lottieAnimationView2.playAnimation()
                viewF.visible()
            }

        }
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



        binding?.letsStartTv?.setOnClickListener {
            // navigateScreen()
            if (Admobify.isPremiumUser()) {
                navigateScreen()
            } else {
                showSplashAd()
            }
        }
        initAppOpenListener()
    }

    private fun adsInitialize() {
        adsRemoteConfig()
        Admobify.initialize(
            context = context ?: return,
            testDevicesList = arrayListOf(
                /* "40FCFB5F8EE015FEC661521E08D51DB6",
                 "9BBA43A5DE227FB7AD3E9A1D6CDE3B9B",
                 "7C12541A1F5DAD8864C7DDBC33D2E96B",
                 "608D73A6D4228BA4F1C4638DC40AE6F1"*/
            ),
            premiumUser = Admobify.isPremiumUser()
        )
        Logger.enableLogging()
        loadSplashAppOpen()

    }

    private fun adsRemoteConfig() {
        RemoteConfigurations.fetchRemotes(
            activity ?: return,
            jsonKey = "ads_json_07",
            defaultXml = R.xml.ads_remote_config,
            onSuccess = {
                try {
                    val data = Gson().fromJson(it, AdsRemoteModel::class.java)
                    adsRemoteModel = data
                    native_language_l = adsRemoteModel?.native_language_l == true
                    banner_splash_top = adsRemoteModel?.banner_splash_top ?: false
                    banner_splash_med = adsRemoteModel?.banner_splash_med ?: false
                    banner_splash_bottom = adsRemoteModel?.banner_splash_bottom ?: true
                    banner_onboarding_top = adsRemoteModel?.banner_onboarding_top ?: false
                    banner_onboarding_med = adsRemoteModel?.banner_onboarding_med ?: false
                    banner_onboarding_bottom = adsRemoteModel?.banner_onboarding_bottom == true
                    native_home_l = adsRemoteModel?.native_home_l == true
                    fullscreen_home_l = adsRemoteModel?.fullscreen_home_l == true
                    fullscreen_home_navigation_l =
                        adsRemoteModel?.fullscreen_home_navigation_l ?: false
                    fullscreen_disclaimer_l = adsRemoteModel?.fullscreen_disclaimer_l == true
                    native_exit_l = adsRemoteModel?.native_exit_l == true
                    app_open_l = adsRemoteModel?.app_open_l == true
                    fullScreenCappingL = adsRemoteModel?.fullScreenCappingL ?: 2000
                    fullscreen_video_l = adsRemoteModel?.fullscreen_video_l == true
                    native_downloaded_video_l = adsRemoteModel?.native_downloaded_video_l == true


                    Log.d("REMOTE_CONFIG", "native_language_l: $native_language_l")
                    Log.d("REMOTE_CONFIG", "banner_onboarding_top: $banner_onboarding_top")
                    Log.d("REMOTE_CONFIG", "banner_onboarding_med: $banner_onboarding_med")
                    Log.d("REMOTE_CONFIG", "banner_onboarding_bottom: $banner_onboarding_bottom")
                    Log.d("REMOTE_CONFIG", "splash_banner_top: $banner_splash_top")
                    Log.d("REMOTE_CONFIG", "splash_banner_med: $banner_splash_med")
                    Log.d("REMOTE_CONFIG", "splash_banner_bottom: $banner_splash_bottom")
                    Log.d("REMOTE_CONFIG", "native_home_l: $native_home_l")
                    Log.d("REMOTE_CONFIG", "fullscreen_home_l: $fullscreen_home_l")
                    Log.d(
                        "REMOTE_CONFIG",
                        "fullscreen_home_navigation_l: $fullscreen_home_navigation_l"
                    )
                    Log.d("REMOTE_CONFIG", "native_exit_l: $native_exit_l")
                    Log.d("REMOTE_CONFIG", "app_open_l: $app_open_l")
                    Log.d("REMOTE_CONFIG", "fullScreenCapping: $fullScreenCappingL")
                    Log.d("REMOTE_CONFIG", "fullscreen_video_l: $fullscreen_video_l")
                    Log.d("REMOTE_CONFIG", "native_downloaded_video_l: $native_downloaded_video_l")


                    //if (AppPrefs(context ?: return@fetchRemotes).getBoolean("isFirstTime")) {
                    loadBannerAdTop()
                    loadBannerAdMed()
                    loadBannerAdBottom()
                    // }

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
                    //  navigateScreen()
                    //binding?.lottieAnimationView?.invisible()
                    binding?.letsStartTv?.show()
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
                //   showSplashAd()
                binding?.lottieAnimationView?.invisible()
                binding?.letsStartTv?.show()
            },
            adFailed = {
                Log.e("Ads_", "adFailed")
                if (!isPause && !navigateOrNot) {
                    countDownTimer?.cancel()
                    // navigateScreen()
                    binding?.lottieAnimationView?.invisible()
                    binding?.letsStartTv?.show()
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
                        binding?.lottieAnimationView?.invisible()
                        binding?.letsStartTv?.show()
                        // navigateScreen()
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
                navigateScreen()
            },
            adDismiss = {
                navigateScreen()
                binding?.lottieAnimationView?.invisible()
                binding?.letsStartTv?.show()
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

    private fun loadBannerAdTop() {
        binding?.adsBannerPlaceHolderTop?.visibility = View.VISIBLE
        binding?.shimmerLayoutTop?.root?.visibility = View.VISIBLE
        BannerAdUtils(activity ?: return).loadBannerAd(
            adId = getString(R.string.splashBannerAd),
            remote = banner_splash_top,
            container = binding?.adsBannerPlaceHolderTop ?: return,
            adLoadingOrShimmer = binding?.shimmerLayoutTop?.root,
            adType = BannerAdType.DEFAULT_BANNER,
            callback = object : BannerCallback() {
                override fun onAdValidate() {
                    super.onAdValidate()
                    binding?.adsBannerPlaceHolderTop?.visibility = View.GONE
                    binding?.shimmerLayoutTop?.root?.visibility = View.GONE
                }

                override fun onAdLoaded(adView: AdView) {
                    super.onAdLoaded(adView)
                    topAd = adView
                }
            },
        )
    }

    private fun loadBannerAdMed() {
        binding?.adsBannerPlaceHolderMed?.visibility = View.VISIBLE
        binding?.shimmerLayoutMed?.root?.visibility = View.VISIBLE
        BannerAdUtils(activity ?: return).loadBannerAd(
            adId = getString(R.string.splashBannerAd),
            remote = banner_splash_med,
            container = binding?.adsBannerPlaceHolderMed ?: return,
            adLoadingOrShimmer = binding?.shimmerLayoutMed?.root,
            adType = BannerAdType.DEFAULT_BANNER,
            callback = object : BannerCallback() {
                override fun onAdValidate() {
                    super.onAdValidate()
                    binding?.adsBannerPlaceHolderMed?.visibility = View.GONE
                    binding?.shimmerLayoutMed?.root?.visibility = View.GONE

                }

                override fun onAdLoaded(adView: AdView) {
                    super.onAdLoaded(adView)
                    medAd = adView
                }
            },
        )
    }

    private fun loadBannerAdBottom() {
        binding?.adsBannerPlaceHolderBottom?.visibility = View.VISIBLE
        binding?.shimmerLayoutBottom?.root?.visibility = View.VISIBLE
        BannerAdUtils(activity ?: return).loadBannerAd(
            adId = getString(R.string.splashBannerAd),
            remote = banner_splash_bottom,
            container = binding?.adsBannerPlaceHolderBottom ?: return,
            adLoadingOrShimmer = binding?.shimmerLayoutBottom?.root,
            adType = BannerAdType.DEFAULT_BANNER,
            callback = object : BannerCallback() {
                override fun onAdValidate() {
                    super.onAdValidate()
                    binding?.adsBannerPlaceHolderBottom?.visibility = View.GONE
                    binding?.shimmerLayoutBottom?.root?.visibility = View.GONE

                }

                override fun onAdLoaded(adView: AdView) {
                    super.onAdLoaded(adView)
                    bottomAd = adView
                }
            },
        )
    }

    private fun initAppOpenListener() {
        SplashOpenAppAd.adEventListener = object : SplashOpenAppAd.AdEventListener {
            override fun onAdShown() {
                hideBannerAd()
            }

            override fun onAdDismissed() {
                hideBannerAd()
            }
        }
    }

    private fun hideBannerAd() {
        binding?.adsBannerPlaceHolderTop?.hide()
        binding?.adsBannerPlaceHolderMed?.hide()
        binding?.adsBannerPlaceHolderBottom?.hide()
        binding?.shimmerLayoutTop?.root?.hide()
        binding?.shimmerLayoutMed?.root?.hide()
        binding?.shimmerLayoutBottom?.root?.hide()
    }

    override fun onResume() {
        super.onResume()
        isPause = false
        if (shouldStart) {
            startTimer()
        }
        topAd?.resume()
        medAd?.resume()
        bottomAd?.resume()
    }

    override fun onPause() {
        isPause = true
        countDownTimer?.cancel()
        super.onPause()
        topAd?.pause()
        medAd?.pause()
        bottomAd?.pause()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        isPause = true
        super.onDestroy()
        topAd?.destroy()
        medAd?.destroy()
        bottomAd?.destroy()
    }


}