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
import androidx.lifecycle.lifecycleScope
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
import com.example.a4kvideodownloaderplayer.helper.disable
import com.example.a4kvideodownloaderplayer.helper.enable
import com.example.a4kvideodownloaderplayer.helper.invisible
import com.example.a4kvideodownloaderplayer.helper.showExitVideos
import com.example.a4kvideodownloaderplayer.helper.visible
import com.example.aiartgenerator.utils.AppPrefs
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch


class MainFragment : Fragment() {
    private var exitDialog: Dialog? = null
    private var exitBinding: ExitDialogLayoutBinding? = null
    private var binding: MainFragmentBinding? = null
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val viewPagerViewModel: ViewPagerViewModel by activityViewModels()

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
                        selectHomeTab()
                    }
                }
            }
        )
    }

    private fun registerViewPager() {
        binding?.viewPagerDashboardLight?.apply {
            adapter = ViewPagerAdapterDashboard(activity ?: return, viewPagerViewModel)
            isUserInputEnabled = false
            setOffscreenPageLimit(3)

            registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentTabPosition = position
                }
            })
        }


    }

    private fun selectHomeTab() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding?.apply {
                viewPagerDashboardLight.setCurrentItem(0, false)
                tabHome.disable()
                tabConverter.enable()
                tabDownload.enable()
                tabSettings.enable()

                polygon1.visible()
                polygon2.invisible()
                polygon3.invisible()
                polygon4.invisible()

                lastButtonClickID = tabHome.id

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

    private fun selectConverterTab() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding?.apply {
                viewPagerDashboardLight.setCurrentItem(1, false)
                tabHome.enable()
                tabConverter.disable()
                tabDownload.enable()
                tabSettings.enable()

                polygon1.invisible()
                polygon2.visible()
                polygon3.invisible()
                polygon4.invisible()

                lastButtonClickID = binding?.tabConverter?.id ?: -1

                iconHome.setImageResource(R.drawable.home_selected_icon)
                iconHome.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textHome.setTextColor(Color.parseColor("#BEB8B8"))

                iconConverter.setImageResource(R.drawable.mp3_converter)
                iconConverter.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                textConverter.setTextColor(Color.parseColor("#FFFFFF"))

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

    private fun selectDownloadTab() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding?.apply {
                viewPagerDashboardLight.setCurrentItem(2, false)
                tabHome.enable()
                tabConverter.enable()
                tabDownload.disable()
                tabSettings.enable()

                polygon1.invisible()
                polygon2.invisible()
                polygon3.visible()
                polygon4.invisible()

                lastButtonClickID = binding?.tabDownload?.id ?: -1

                iconHome.setImageResource(R.drawable.home_selected_icon)
                iconHome.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textHome.setTextColor(Color.parseColor("#BEB8B8"))

                iconConverter.setImageResource(R.drawable.mp3_converter)
                iconConverter.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textConverter.setTextColor(Color.parseColor("#BEB8B8"))

                iconDownload.setImageResource(R.drawable.downloads_icon)
                iconDownload.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                textDownload.setTextColor(Color.parseColor("#FFFFFF"))

                iconSettings.setImageResource(R.drawable.settings_icon)
                iconSettings.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textSettings.setTextColor(Color.parseColor("#BEB8B8"))
            }
        }

    }

    private fun selectSettingTab() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding?.apply {
                viewPagerDashboardLight.setCurrentItem(3, false)
                tabHome.enable()
                tabConverter.enable()
                tabDownload.enable()
                tabSettings.disable()

                polygon1.invisible()
                polygon2.invisible()
                polygon3.invisible()
                polygon4.visible()

                lastButtonClickID = binding?.tabSettings?.id ?: -1

                iconHome.setImageResource(R.drawable.home_selected_icon)
                iconHome.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#BEB8B8"))
                textHome.setTextColor(Color.parseColor("#BEB8B8"))

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
                    ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                textSettings.setTextColor(Color.parseColor("#FFFFFF"))
            }
        }

    }

    private fun resetAllTab() {
        binding?.apply {
            tabHome.enable()
            tabConverter.enable()
            tabDownload.enable()
            tabSettings.enable()
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
        }
    }

    private fun checkSelectedTab(selectedTab: String) {
        when (selectedTab) {
            "home" -> {
                selectHomeTab()
            }

            "convert" -> {
                selectConverterTab()
            }

            "download" -> {
                selectDownloadTab()
            }

            "settings" -> {
                selectSettingTab()
            }
        }
    }

    private fun updateTabSelection(buttonID: Int, selectedTab: String) {
        // Reset all tabs to unselected state
        binding?.apply {
            resetAllTab()
            val lastDismissTime = activity.let { AppPrefs(it).getLong("lastAdDismissTime") } ?: 0L
            val currentTimeMillis = SystemClock.elapsedRealtime()
            if (currentTimeMillis - lastDismissTime < fullScreenCappingL) { // 5 seconds
                Logger.log(
                    Level.DEBUG,
                    Category.OpenAd,
                    "Cannot show ad: Please wait 5 seconds before showing another ad."
                )
                checkSelectedTab(selectedTab)
                return
            }
            val adOptions = InterAdOptions().setAdId(getString(R.string.homeInterstitialAd))
                .setRemoteConfig(fullscreen_home_l).setLoadingDelayForDialog(2)
                .setFullScreenLoading(false)
                .build(activity ?: return)
            lastButtonClickID = buttonID
            adCount++
            if ((adCount % 3) == 0) {
                if (!AdmobifyUtils.isNetworkAvailable(context ?: return)) {
                    checkSelectedTab(selectedTab)
                } else {
                    InterstitialAdUtils(adOptions).loadAndShowInterAd(object :
                        InterAdLoadCallback() {
                        override fun adAlreadyLoaded() {}
                        override fun adLoaded() {}
                        override fun adFailed(error: LoadAdError?, msg: String?) {
                            checkSelectedTab(selectedTab)
                        }

                        override fun adValidate() {
                            checkSelectedTab(selectedTab)

                        }

                    },
                        object : InterAdShowCallback() {
                            override fun adNotAvailable() {}
                            override fun adShowFullScreen() {
                                checkSelectedTab(selectedTab)
                            }

                            override fun adDismiss() {
                            }

                            override fun adFailedToShow() {
                                checkSelectedTab(selectedTab)
                            }

                            override fun adImpression() {}

                            override fun adClicked() {}
                        })
                }
            } else {
                checkSelectedTab(selectedTab)
            }


        }
    }

    private fun attachObserver() {
        homeViewModel.pageSelector.observe(viewLifecycleOwner) {

            binding?.apply {
                viewPagerDashboardLight.currentItem = it
                resetAllTab()
                Log.e("TAG", "attachObserver: $it")
                when (it) {
                    0 -> {
                        selectHomeTab()

                    }


                    1 -> {
                        selectConverterTab()

                    }

                    2 -> {
                        selectDownloadTab()

                    }

                    3 -> {
                        selectSettingTab()
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