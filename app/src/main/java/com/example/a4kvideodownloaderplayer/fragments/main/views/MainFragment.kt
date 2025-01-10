package com.example.a4kvideodownloaderplayer.fragments.main.views

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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
import com.example.a4kvideodownloaderplayer.databinding.MediumNativeAdsBinding
import com.example.a4kvideodownloaderplayer.fragments.home.views.adapter.VideoSliderAdapter
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.HomeViewModel
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.ViewPagerViewModel
import com.example.a4kvideodownloaderplayer.helper.AppUtils.checkForInAppUpdate
import com.example.a4kvideodownloaderplayer.helper.showExitVideos
import com.example.a4kvideodownloaderplayer.viewPager.ViewPagerAdapterDashboard
import com.example.aiartgenerator.utils.AppPrefs
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.tabs.TabLayoutMediator


class MainFragment : Fragment() {
    private var exitDialog: Dialog? = null
    private var exitBinding: ExitDialogLayoutBinding? = null
    private var binding: MainFragmentBinding? = null
    private val homeViewModel: HomeViewModel by activityViewModels()
    val viewPagerViewModel: ViewPagerViewModel by activityViewModels()

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
        context?.checkForInAppUpdate(activity ?: return)
        activity?.onBackPressedDispatcher?.addCallback(
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
                            polygon1.visibility = View.VISIBLE
                            polygon2.visibility = View.INVISIBLE
                            polygon3.visibility = View.INVISIBLE
                            polygon4.visibility = View.INVISIBLE
                            lastButtonClickID = binding?.tabHome?.id ?: -1
                            iconHome.setImageResource(R.drawable.home_selected_icon)
                            iconHome.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                            textHome.setTextColor(Color.parseColor("#FFFFFF"))

                            iconConverter.setImageResource(R.drawable.mp3_converter)
                            iconConverter.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                            textConverter.setTextColor(Color.parseColor("#BEB8B8"))

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
            activity?.let {
                binding?.viewPagerDashboardLight?.adapter =
                    ViewPagerAdapterDashboard(it, viewPagerViewModel)
            }
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

            polygon1.visibility = View.INVISIBLE
            polygon2.visibility = View.INVISIBLE
            polygon3.visibility = View.INVISIBLE
            polygon4.visibility = View.INVISIBLE

            iconHome.setImageResource(R.drawable.home_icon)
            iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
            textHome.setTextColor(Color.parseColor("#BEB8B8"))

            iconConverter.setImageResource(R.drawable.mp3_converter)
            iconConverter.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
            textConverter.setTextColor(Color.parseColor("#BEB8B8"))


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
                        polygon1.visibility = View.VISIBLE
                        iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textHome.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            0,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "convert" -> {
                        iconConverter.setImageResource(R.drawable.mp3_converter)
                        polygon2.visibility = View.VISIBLE
                        iconConverter.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textConverter.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            1,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "download" -> {
                        iconDownload.setImageResource(R.drawable.download_icon)
                        polygon3.visibility = View.VISIBLE
                        iconDownload.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            2,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "settings" -> {
                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                        polygon4.visibility = View.VISIBLE
                        iconSettings.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                        binding?.viewPagerDashboardLight?.setCurrentItem(
                            3,
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
                if (!AdmobifyUtils.isNetworkAvailable(context ?: return)) {
                    when (selectedTab) {
                        "home" -> {
                            polygon1.visibility = View.VISIBLE
                            iconHome.setImageResource(R.drawable.home_selected_icon)
                            iconHome.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#ffffff"))
                            textHome.setTextColor(Color.parseColor("#ffffff"))
                            viewPagerDashboardLight.setCurrentItem(
                                0,
                                true
                            )

                        }

                        "convert" -> {
                            polygon2.visibility = View.VISIBLE
                            iconConverter.setImageResource(R.drawable.download_icon)
                            iconConverter.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#ffffff"))
                            textConverter.setTextColor(Color.parseColor("#ffffff"))
                            viewPagerDashboardLight.setCurrentItem(
                                1,
                                true
                            ) // Navigate to page 0 (Home)

                        }

                        "download" -> {
                            polygon3.visibility = View.VISIBLE
                            iconDownload.setImageResource(R.drawable.download_icon)
                            iconDownload.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#ffffff"))
                            textDownload.setTextColor(Color.parseColor("#ffffff"))
                            viewPagerDashboardLight.setCurrentItem(
                                2,
                                true
                            ) // Navigate to page 0 (Home)

                        }

                        "settings" -> {
                            polygon4.visibility = View.VISIBLE
                            iconSettings.setImageResource(R.drawable.settings_selected_icon)
                            iconSettings.imageTintList =
                                ColorStateList.valueOf(Color.parseColor("#ffffff"))
                            textSettings.setTextColor(Color.parseColor("#ffffff"))
                            viewPagerDashboardLight.setCurrentItem(
                                3,
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
                                    polygon1.visibility = View.VISIBLE

                                    iconHome.setImageResource(R.drawable.home_selected_icon)
                                    iconHome.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textHome.setTextColor(Color.parseColor("#ffffff"))
                                    //binding?.viewPagerDashboardLight?.currentItem = 0
                                    viewPagerDashboardLight?.setCurrentItem(
                                        0,
                                        true
                                    ) // Navigate to page 0 (Home)


                                }

                                "convert" -> {
                                    polygon2.visibility = View.VISIBLE

                                    iconConverter.setImageResource(R.drawable.download_icon)
                                    iconConverter.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textConverter.setTextColor(Color.parseColor("#ffffff"))
                                    viewPagerDashboardLight.setCurrentItem(
                                        1,
                                        true
                                    ) // Navigate to page 0 (Home)

                                }

                                "download" -> {
                                    polygon3.visibility = View.VISIBLE

                                    iconDownload.setImageResource(R.drawable.download_icon)
                                    iconDownload.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textDownload.setTextColor(Color.parseColor("#ffffff"))
                                    viewPagerDashboardLight?.setCurrentItem(
                                        2,
                                        true
                                    ) // Navigate to page 0 (Home)

                                }

                                "settings" -> {
                                    polygon4.visibility = View.VISIBLE

                                    iconSettings.setImageResource(R.drawable.settings_selected_icon)
                                    iconSettings.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textSettings.setTextColor(Color.parseColor("#ffffff"))
                                    viewPagerDashboardLight.setCurrentItem(
                                        3,
                                        true
                                    )

                                }
                            }
                        }

                        override fun adValidate() {
                            when (selectedTab) {
                                "home" -> {
                                    polygon1.visibility = View.VISIBLE

                                    iconHome.setImageResource(R.drawable.home_selected_icon)
                                    iconHome.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textHome.setTextColor(Color.parseColor("#ffffff"))
                                    //binding?.viewPagerDashboardLight?.currentItem = 0
                                    viewPagerDashboardLight?.setCurrentItem(
                                        0,
                                        true
                                    ) // Navigate to page 0 (Home)


                                }

                                "convert" -> {
                                    polygon2.visibility = View.VISIBLE

                                    iconConverter.setImageResource(R.drawable.download_icon)
                                    iconConverter.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textConverter.setTextColor(Color.parseColor("#ffffff"))
                                    viewPagerDashboardLight.setCurrentItem(
                                        1,
                                        true
                                    ) // Navigate to page 0 (Home)

                                }

                                "download" -> {
                                    polygon3.visibility = View.VISIBLE

                                    iconDownload.setImageResource(R.drawable.download_icon)
                                    iconDownload.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textDownload.setTextColor(Color.parseColor("#ffffff"))
                                    viewPagerDashboardLight?.setCurrentItem(
                                        2,
                                        true
                                    ) // Navigate to page 0 (Home)

                                }

                                "settings" -> {
                                    polygon4.visibility = View.VISIBLE

                                    iconSettings.setImageResource(R.drawable.settings_selected_icon)
                                    iconSettings.imageTintList =
                                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                    textSettings.setTextColor(Color.parseColor("#ffffff"))
                                    viewPagerDashboardLight.setCurrentItem(
                                        3,
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
                                        polygon1.visibility = View.VISIBLE
                                        iconHome.setImageResource(R.drawable.home_selected_icon)
                                        iconHome.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textHome.setTextColor(Color.parseColor("#ffffff"))
                                        //binding?.viewPagerDashboardLight?.currentItem = 0
                                        viewPagerDashboardLight.setCurrentItem(
                                            0,
                                            true
                                        ) // Navigate to page 0 (Home)


                                    }

                                    "convert" -> {
                                        polygon2.visibility = View.VISIBLE
                                        iconConverter.setImageResource(R.drawable.download_icon)
                                        iconConverter.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textConverter.setTextColor(Color.parseColor("#ffffff"))
                                        viewPagerDashboardLight.setCurrentItem(
                                            1,
                                            true
                                        ) // Navigate to page 0 (Home)

                                    }

                                    "download" -> {
                                        polygon3.visibility = View.VISIBLE
                                        iconDownload.setImageResource(R.drawable.download_icon)
                                        iconDownload.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                                        viewPagerDashboardLight.setCurrentItem(
                                            2,
                                            true
                                        ) // Navigate to page 0 (Home)

                                    }

                                    "settings" -> {
                                        polygon4.visibility = View.VISIBLE
                                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                                        iconSettings.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                                        viewPagerDashboardLight.setCurrentItem(
                                            3,
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
                                        polygon1.visibility = View.VISIBLE
                                        iconHome.setImageResource(R.drawable.home_selected_icon)
                                        iconHome.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textHome.setTextColor(Color.parseColor("#ffffff"))
                                        //binding?.viewPagerDashboardLight?.currentItem = 0
                                        viewPagerDashboardLight.setCurrentItem(
                                            0,
                                            true
                                        ) // Navigate to page 0 (Home)


                                    }

                                    "convert" -> {
                                        polygon2.visibility = View.VISIBLE
                                        iconConverter.setImageResource(R.drawable.download_icon)
                                        iconConverter.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                                        viewPagerDashboardLight.setCurrentItem(
                                            1,
                                            true
                                        ) // Navigate to page 0 (Home)

                                    }

                                    "download" -> {
                                        polygon3.visibility = View.VISIBLE
                                        iconDownload.setImageResource(R.drawable.download_icon)
                                        iconDownload.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                                        viewPagerDashboardLight.setCurrentItem(
                                            1,
                                            true
                                        ) // Navigate to page 0 (Home)

                                    }

                                    "settings" -> {
                                        polygon4.visibility = View.VISIBLE
                                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                                        iconSettings.imageTintList =
                                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                                        viewPagerDashboardLight.setCurrentItem(
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
                        polygon1.visibility = View.VISIBLE
                        iconHome.setImageResource(R.drawable.home_selected_icon)
                        iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textHome.setTextColor(Color.parseColor("#ffffff"))
                        viewPagerDashboardLight.setCurrentItem(
                            0,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "convert" -> {
                        polygon2.visibility = View.VISIBLE
                        iconHome.setImageResource(R.drawable.home_selected_icon)
                        iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textHome.setTextColor(Color.parseColor("#ffffff"))
                        viewPagerDashboardLight.setCurrentItem(
                            1,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "download" -> {
                        polygon3.visibility = View.VISIBLE
                        iconDownload.setImageResource(R.drawable.download_icon)
                        iconDownload.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                        viewPagerDashboardLight.setCurrentItem(
                            2,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    "settings" -> {
                        polygon4.visibility = View.VISIBLE
                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                        iconSettings.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                        viewPagerDashboardLight.setCurrentItem(
                            3,
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

            binding?.apply {
                viewPagerDashboardLight.currentItem = it
                tabHome.isEnabled = true
                tabDownload.isEnabled = true
                tabSettings.isEnabled = true
                polygon1.visibility = View.INVISIBLE
                polygon2.visibility = View.INVISIBLE
                polygon3.visibility = View.INVISIBLE
                polygon4.visibility = View.INVISIBLE

                iconHome.setImageResource(R.drawable.home_icon)
                iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textHome.setTextColor(Color.parseColor("#BEB8B8"))


                iconConverter.setImageResource(R.drawable.mp3_converter)
                iconConverter.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textConverter.setTextColor(Color.parseColor("#BEB8B8"))

                iconDownload.setImageResource(R.drawable.downloads_icon)
                iconDownload.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textDownload.setTextColor(Color.parseColor("#BEB8B8"))

                iconSettings.setImageResource(R.drawable.settings_icon)
                iconSettings.imageTintList = ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textSettings.setTextColor(Color.parseColor("#BEB8B8"))
                Log.e("TAG", "attachObserver: $it" )
                when (it) {
                    0 -> {
                        polygon1.visibility = View.VISIBLE
                        iconHome.setImageResource(R.drawable.home_selected_icon)
                        iconHome.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textHome.setTextColor(Color.parseColor("#ffffff"))
                        viewPagerDashboardLight.setCurrentItem(
                            0,
                            true
                        ) // Navigate to page 0 (Home)

                    }


                    1 -> {
                        polygon2.visibility = View.VISIBLE
                        iconConverter.setImageResource(R.drawable.mp3_converter)
                        iconConverter.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textConverter.setTextColor(Color.parseColor("#ffffff"))
                        viewPagerDashboardLight.setCurrentItem(
                            1,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    2 -> {
                        polygon3.visibility = View.VISIBLE
                        iconDownload.setImageResource(R.drawable.download_icon)
                        iconDownload.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textDownload.setTextColor(Color.parseColor("#ffffff"))
                        viewPagerDashboardLight.setCurrentItem(
                            2,
                            true
                        ) // Navigate to page 0 (Home)

                    }

                    3 -> {
                        polygon4.visibility = View.VISIBLE
                        iconSettings.setImageResource(R.drawable.settings_selected_icon)
                        iconSettings.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#ffffff"))
                        textSettings.setTextColor(Color.parseColor("#ffffff"))
                        viewPagerDashboardLight.setCurrentItem(
                            3,
                            true
                        )

                    }
                }
            }
        }

        binding?.apply {
            tabHome.setOnClickListener {
                if (tabHome.isEnabled) {
                    tabHome.isEnabled = false
                    tabConverter.isEnabled = true
                    tabDownload.isEnabled = true
                    tabSettings.isEnabled = true
                    updateTabSelection(it.id, "home")
                }
            }

            tabConverter.setOnClickListener {
                if (tabConverter.isEnabled) {
                    tabHome.isEnabled = true
                    tabConverter.isEnabled = false
                    tabDownload.isEnabled = true
                    tabSettings.isEnabled = true
                    updateTabSelection(it.id, "convert")
                }
            }

            tabDownload.setOnClickListener {
                if (tabDownload.isEnabled) {
                    tabHome.isEnabled = true
                    tabConverter.isEnabled = true
                    tabDownload.isEnabled = false
                    tabSettings.isEnabled = true
                    updateTabSelection(it.id, "download")
                }
            }

            tabSettings.setOnClickListener {

                if (tabSettings.isEnabled) {
                    tabHome.isEnabled = true
                    tabConverter.isEnabled = true
                    tabDownload.isEnabled = true
                    tabSettings.isEnabled = false
                    updateTabSelection(it.id, "settings")
                }
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
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            show()
            dialogEventListener?.onDialogShown()
        }

        exitBinding?.apply {
            val videoListAdapter = VideoSliderAdapter(context ?: return, showExitVideos()) {
                if (findNavController().currentDestination?.id == R.id.mainFragment) {
                    if (AdmobifyUtils.isNetworkAvailable(context)) {
                        Bundle().apply {
                            putString("videoUrl", it.url)
                            findNavController().navigate(
                                R.id.action_mainFragment_to_PopularVideoPlayerFragment,
                                this
                            )
                        }
                    } else {
                        Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show()
                    }

                }
            }
            viewPager.adapter = videoListAdapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                // Set custom view for each tab
                tab.setCustomView(R.layout.tab_dot)
            }.attach()

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    for (i in 0 until tabLayout.tabCount) {
                        val tab = tabLayout.getTabAt(i)
                        tab?.customView?.isSelected = i == position
                    }
                }
            })
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

        val nativeBinding = MediumNativeAdsBinding.inflate(layoutInflater)
        nativeBinding.apply {
            val nativeAdModel = NativeAdItemsModel(
                root,
                adHeadline = adHeadline,
                adBody = adBody,
                adIcon = null,
                mediaView = AdMedia,
                adCTA = adCallToAction
            )

            NativeAdUtils().loadNativeAd(
                activity?.application ?: return,
                getString(R.string.exitNativeAd),
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


}