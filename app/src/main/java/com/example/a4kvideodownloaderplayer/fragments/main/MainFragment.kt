package com.example.a4kvideodownloaderplayer.fragments.main

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.fullScreenCappingL
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_home_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_home_l
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAdState
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdLoadCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdOptions
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdShowCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterstitialAdUtils
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdCallback
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdItemsModel
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdUtils
import com.example.a4kvideodownloaderplayer.ads.native_ads.ad_types.NativeAdType
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Category
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Level
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Logger
import com.example.a4kvideodownloaderplayer.databinding.ExitDialogLayoutBinding
import com.example.a4kvideodownloaderplayer.databinding.MainFragmentBinding
import com.example.a4kvideodownloaderplayer.databinding.NativeAdLayoutBinding
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.HomeViewModel
import com.example.a4kvideodownloaderplayer.helper.AppUtils.checkForInAppUpdate
import com.example.a4kvideodownloaderplayer.viewPager.ViewPagerAdapterDashboard
import com.example.aiartgenerator.utils.AppPrefs
import com.google.android.gms.ads.LoadAdError

class MainFragment : Fragment() {
    private var exitDialog: Dialog? = null
    private var exitBinding: ExitDialogLayoutBinding? = null
    private var binding: MainFragmentBinding? = null
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var lastButtonClickID: Int = -1

    private var adCount: Int = 0

    private var currentTabPosition: Int = 0

    companion object {
        interface DialogEventListener {
            fun onDialogShown()
            fun onDialogDismissed()
        }

        var dialogEventListener: DialogEventListener? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenAppAdState.enable("MainFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObserver()
        registerViewPager()
        context?.checkForInAppUpdate(activity?:return)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (currentTabPosition == 0) {
                        exitDialog()
                    } else {
                        binding?.apply {
                            viewPagerDashboardLight.currentItem = 0
                            tabHome.isEnabled = false
                            tabDownload.isEnabled = true
                            tabSettings.isEnabled = true
                            lastButtonClickID = binding?.tabHome?.id ?: -1
                            iconHome.setImageResource(R.drawable.home_selected_icon)
                            iconHome.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                            textHome.setTextColor(Color.parseColor("#FFFFFF"))

                            iconDownload.setImageResource(R.drawable.downloads_icon)
                            iconDownload.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                            textDownload.setTextColor(Color.parseColor("#BEB8B8"))

                            iconSettings.setImageResource(R.drawable.settings_icon)
                            iconSettings.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                            textSettings.setTextColor(Color.parseColor("#BEB8B8"))
                        }
                    }
                }
            }
        )
    }


    private fun registerViewPager() {
        binding?.viewPagerDashboardLight?.apply {
            binding?.viewPagerDashboardLight?.adapter =
                ViewPagerAdapterDashboard(activity ?: return)
            binding?.viewPagerDashboardLight?.isUserInputEnabled = false

            registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentTabPosition = position
                }
            })
        }


    }


    private fun updateTabSelection(buttonID: Int, selectedTab: String) {
        // Reset all tabs to unselected state
        binding?.apply {

            iconHome.setImageResource(R.drawable.home_icon)
            iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
            textHome.setTextColor(Color.parseColor("#BEB8B8"))

            iconDownload.setImageResource(R.drawable.downloads_icon)
            iconDownload.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
            textDownload.setTextColor(Color.parseColor("#BEB8B8"))

            iconSettings.setImageResource(R.drawable.settings_icon)
            iconSettings.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
            textSettings.setTextColor(Color.parseColor("#BEB8B8"))


            val lastDismissTime = activity.let { AppPrefs(it).getLong("lastAdDismissTime") } ?: 0L
            val currentTimeMillis = SystemClock.elapsedRealtime()
            if (currentTimeMillis - lastDismissTime < fullScreenCappingL) { // 5 seconds
                Logger.log(
                    Level.DEBUG,
                    Category.OpenAd,
                    "Cannot show ad: Please wait 5 seconds before showing another ad."
                )
                when (selectedTab) {
                    "home" -> {
                        iconHome.setImageResource(R.drawable.home_selected_icon)
                        iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textHome.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            0,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "download" -> {
                        iconDownload.setImageResource(R.drawable.download_icon)
                        iconDownload.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            1,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "settings" -> {
                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                        iconSettings.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            2,
                            true
                        ) // Navigate to page 0 (Home)

                    }
                }
                return
            }

            //if (lastButtonClickID != buttonID) {
            val adOptions = InterAdOptions().setAdId(getString(R.string.homeInterstitialAd))
                .setRemoteConfig(fullscreen_home_l).setLoadingDelayForDialog(2)
                .setFullScreenLoading(false)
                .build(activity ?: return)
            lastButtonClickID = buttonID
            adCount++
            if ((adCount % 3) == 0) {
                if (!AdmobifyUtils.isNetworkAvailable(context ?: return) ) {
                    when (selectedTab) {
                        "home" -> {
                            iconHome.setImageResource(R.drawable.home_selected_icon)
                            iconHome.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#ffffff"))
                            textHome.setTextColor(Color.parseColor("#ffffff"))
                            binding?.viewPagerDashboardLight?.setCurrentItem(
                                0,
                                true
                            )

                        }

                        "download" -> {
                            iconDownload.setImageResource(R.drawable.download_icon)
                            iconDownload.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#ffffff"))
                            textDownload.setTextColor(Color.parseColor("#ffffff"))
                            binding?.viewPagerDashboardLight?.setCurrentItem(
                                1,
                                true
                            ) // Navigate to page 0 (Home)

                        }

                        "settings" -> {
                            iconSettings.setImageResource(R.drawable.settings_selected_icon)
                            iconSettings.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#ffffff"))
                            textSettings.setTextColor(Color.parseColor("#ffffff"))
                            binding?.viewPagerDashboardLight?.setCurrentItem(
                                2,
                                true
                            ) // Navigate to page 0 (Home)

                        }
                    }
                } else {
                    InterstitialAdUtils(adOptions).loadAndShowInterAd(object :
                        InterAdLoadCallback() {
                        override fun adAlreadyLoaded() {}
                        override fun adLoaded() {}
                        override fun adFailed(error: LoadAdError?, msg: String?) {
                            when (selectedTab) {
                                "home" -> {
                                    iconHome.setImageResource(R.drawable.home_selected_icon)
                                    iconHome.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textHome.setTextColor(Color.parseColor("#ffffff"))
                                    //binding?.viewPagerDashboardLight?.currentItem = 0
                                    binding?.viewPagerDashboardLight?.setCurrentItem(
                                        0,
                                        true
                                    ) // Navigate to page 0 (Home)


                                }

                                "download" -> {
                                    iconDownload.setImageResource(R.drawable.download_icon)
                                    iconDownload.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textDownload.setTextColor(Color.parseColor("#ffffff"))
                                    binding?.viewPagerDashboardLight?.setCurrentItem(
                                        1,
                                        true
                                    ) // Navigate to page 0 (Home)

                                }

                                "settings" -> {
                                    iconSettings.setImageResource(R.drawable.settings_selected_icon)
                                    iconSettings.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textSettings.setTextColor(Color.parseColor("#ffffff"))
                                    binding?.viewPagerDashboardLight?.setCurrentItem(
                                        2,
                                        true
                                    )

                                }
                            }
                        }

                        override fun adValidate() {
                            when (selectedTab) {
                                "home" -> {
                                    iconHome.setImageResource(R.drawable.home_selected_icon)
                                    iconHome.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textHome.setTextColor(Color.parseColor("#ffffff"))
                                    //binding?.viewPagerDashboardLight?.currentItem = 0
                                    binding?.viewPagerDashboardLight?.setCurrentItem(
                                        0,
                                        true
                                    ) // Navigate to page 0 (Home)


                                }

                                "download" -> {
                                    iconDownload.setImageResource(R.drawable.download_icon)
                                    iconDownload.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textDownload.setTextColor(Color.parseColor("#ffffff"))
                                    binding?.viewPagerDashboardLight?.setCurrentItem(
                                        1,
                                        true
                                    ) // Navigate to page 0 (Home)

                                }

                                "settings" -> {
                                    iconSettings.setImageResource(R.drawable.settings_selected_icon)
                                    iconSettings.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textSettings.setTextColor(Color.parseColor("#ffffff"))
                                    binding?.viewPagerDashboardLight?.setCurrentItem(
                                        2,
                                        true
                                    )

                                }
                            }


                        }

                    },
                        object : InterAdShowCallback() {
                            override fun adNotAvailable() {}
                            override fun adShowFullScreen() {
                                when (selectedTab) {
                                    "home" -> {
                                        iconHome.setImageResource(R.drawable.home_selected_icon)
                                        iconHome.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textHome.setTextColor(Color.parseColor("#ffffff"))
                                        //binding?.viewPagerDashboardLight?.currentItem = 0
                                        binding?.viewPagerDashboardLight?.setCurrentItem(
                                            0,
                                            true
                                        ) // Navigate to page 0 (Home)


                                    }

                                    "download" -> {
                                        iconDownload.setImageResource(R.drawable.download_icon)
                                        iconDownload.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                                        binding?.viewPagerDashboardLight?.setCurrentItem(
                                            1,
                                            true
                                        ) // Navigate to page 0 (Home)

                                    }

                                    "settings" -> {
                                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                                        iconSettings.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                                        binding?.viewPagerDashboardLight?.setCurrentItem(
                                            2,
                                            true
                                        ) // Navigate to page 0 (Home)

                                    }
                                }
                            }

                            override fun adDismiss() {
                            }

                            override fun adFailedToShow() {
                                when (selectedTab) {
                                    "home" -> {
                                        iconHome.setImageResource(R.drawable.home_selected_icon)
                                        iconHome.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textHome.setTextColor(Color.parseColor("#ffffff"))
                                        //binding?.viewPagerDashboardLight?.currentItem = 0
                                        binding?.viewPagerDashboardLight?.setCurrentItem(
                                            0,
                                            true
                                        ) // Navigate to page 0 (Home)


                                    }

                                    "download" -> {
                                        iconDownload.setImageResource(R.drawable.download_icon)
                                        iconDownload.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                                        binding?.viewPagerDashboardLight?.setCurrentItem(
                                            1,
                                            true
                                        ) // Navigate to page 0 (Home)

                                    }

                                    "settings" -> {
                                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                                        iconSettings.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                                        binding?.viewPagerDashboardLight?.setCurrentItem(
                                            2,
                                            true
                                        )

                                    }
                                }
                            }

                            override fun adImpression() {}

                            override fun adClicked() {}
                        })
                }
            } else {
                when (selectedTab) {
                    "home" -> {
                        iconHome.setImageResource(R.drawable.home_selected_icon)
                        iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textHome.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            0,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "download" -> {
                        iconDownload.setImageResource(R.drawable.download_icon)
                        iconDownload.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            1,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "settings" -> {
                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                        iconSettings.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            2,
                            true
                        ) // Navigate to page 0 (Home)

                    }
                }
            }
            //}


        }
    }

    private fun attachObserver() {
        homeViewModel.pageSelector.observe(viewLifecycleOwner) {
            binding?.viewPagerDashboardLight?.currentItem = it

            binding?.apply {
                tabHome.isEnabled = true
                tabDownload.isEnabled = true
                tabSettings.isEnabled = true
                iconHome.setImageResource(R.drawable.home_icon)
                iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textHome.setTextColor(Color.parseColor("#BEB8B8"))

                iconDownload.setImageResource(R.drawable.downloads_icon)
                iconDownload.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textDownload.setTextColor(Color.parseColor("#BEB8B8"))

                iconSettings.setImageResource(R.drawable.settings_icon)
                iconSettings.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textSettings.setTextColor(Color.parseColor("#BEB8B8"))
                when (it) {
                    0 -> {
                        iconHome.setImageResource(R.drawable.home_selected_icon)
                        iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textHome.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            0,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    1 -> {
                        iconDownload.setImageResource(R.drawable.download_icon)
                        iconDownload.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            1,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    2 -> {
                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                        iconSettings.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            2,
                            true
                        ) // Navigate to page 0 (Home)

                    }
                }
            }
        }

        binding?.apply {
            tabHome.setOnClickListener {
                if (tabHome.isEnabled) {
                    tabHome.isEnabled = false
                    tabDownload.isEnabled = true
                    tabSettings.isEnabled = true
                    updateTabSelection(it.id, "home")
                }
            }

            tabDownload.setOnClickListener {
                if (tabDownload.isEnabled) {
                    tabHome.isEnabled = true
                    tabDownload.isEnabled = false
                    tabSettings.isEnabled = true
                    updateTabSelection(it.id, "download")
                }
                // Handle navigation to "Download" fragment
            }

            tabSettings.setOnClickListener {

                if (tabSettings.isEnabled) {
                    tabHome.isEnabled = true
                    tabDownload.isEnabled = true
                    tabSettings.isEnabled = false
                    updateTabSelection(it.id, "settings")
                }
                // Handle navigation to "Settings" fragment
            }
        }
    }

    private fun exitDialog() {
        exitBinding = ExitDialogLayoutBinding.inflate(LayoutInflater.from(context))
        exitDialog = Dialog(context ?: return)
        exitDialog?.apply {
            setContentView(exitBinding?.root ?: return)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.5f)
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // 90% of screen width
                WindowManager.LayoutParams.WRAP_CONTENT                    // Adjust height as needed
            )
            show()
            dialogEventListener?.onDialogShown()
        }

        exitBinding?.apply {
            cancelTv.setOnClickListener {
                exitDialog?.dismiss()
            }


            exitETv.setOnClickListener {
                activity?.finish()
            }
        }



        exitDialog?.setOnShowListener {
            //homeViewModel?.showExitDialog(true)
        }
        exitDialog?.setOnDismissListener {
            dialogEventListener?.onDialogDismissed()
            //homeViewModel?.hideExitDialog(false)
        }

        val nativeBinding = NativeAdLayoutBinding.inflate(layoutInflater)
        nativeBinding.apply {
            val nativeAdModel = NativeAdItemsModel(
                root,
                adHeadline = tvHeadline,
                adBody = tvBody,
                adIcon = appIcon,
                mediaView = mediaView,
                adCTA = adCTA
            )
            NativeAdUtils().loadNativeAd(
                activity?.application ?: return,
                getString(R.string.homeNativeAd),
                adRemote = native_home_l,
                exitBinding?.nativeContainer ?: return,
                nativeAdModel, object : NativeAdCallback() {

                    override fun adFailed(error: LoadAdError?) {
                        super.adFailed(error)
                        exitBinding?.nativeContainer?.visibility = View.INVISIBLE
                        exitBinding?.shimmerHomeLayout?.root?.visibility = View.INVISIBLE
                    }


                    override fun adLoaded() {
                        super.adLoaded()
                        exitBinding?.nativeContainer?.visibility = View.VISIBLE
                        exitBinding?.shimmerHomeLayout?.root?.visibility = View.GONE
                    }

                    override fun adValidate() {
                        exitBinding?.nativeContainer?.visibility = View.GONE
                        exitBinding?.shimmerHomeLayout?.root?.visibility = View.GONE
                    }


                },
                NativeAdType.EXIT_SCREEN_AD
            )
        }

    }

    override fun onPause() {
        if (exitDialog?.isShowing == true) {
            exitDialog?.dismiss()
        }
        super.onPause()
    }

    override fun onStop() {
        if (exitDialog?.isShowing == true) {
            exitDialog?.dismiss()
        }
        super.onStop()
    }

    override fun onDestroyView() {
        if (exitDialog?.isShowing == true) {
            exitDialog?.dismiss()
            exitBinding = null
        }
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onResume: ")
    }


}