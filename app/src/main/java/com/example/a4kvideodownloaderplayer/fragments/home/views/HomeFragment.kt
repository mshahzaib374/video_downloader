package com.example.a4kvideodownloaderplayer.fragments.home.views

import android.app.ProgressDialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_home_l
import com.example.a4kvideodownloaderplayer.ads.advert.native_home_l
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAd
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdLoadCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdOptions
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdShowCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterstitialAdUtils
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdCallback
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdItemsModel
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdUtils
import com.example.a4kvideodownloaderplayer.ads.native_ads.ad_types.NativeAdType
import com.example.a4kvideodownloaderplayer.ads.utils.Admobify
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils
import com.example.a4kvideodownloaderplayer.databinding.HomeFragmentBinding
import com.example.a4kvideodownloaderplayer.databinding.MediumNativeAdsBinding
import com.example.a4kvideodownloaderplayer.fragments.home.viewmodel.VideoViewModel
import com.example.a4kvideodownloaderplayer.fragments.home.views.adapter.PopularVideosAdapter
import com.example.a4kvideodownloaderplayer.fragments.howToUse.views.UseDialogFragment
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.HomeViewModel
import com.example.a4kvideodownloaderplayer.fragments.premium.PremiumFragment
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.DownloadDialogHelper
import com.example.a4kvideodownloaderplayer.helper.showTrendingVideos
import com.google.android.gms.ads.LoadAdError

class HomeFragment : Fragment() {
    private var binding: HomeFragmentBinding? = null
    private val videoViewModel: VideoViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var progressDialog: ProgressDialog? = null
    private var downloadDialog: DownloadDialogHelper? = null
    private var isAppOpenAdsShown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.logFirebaseEvent("home_fragment", "screen_opened")
        binding?.apply {
            howUseIcon.setOnClickListener {
                context?.logFirebaseEvent("setting_fragment", "how_use_button_clicked")
                UseDialogFragment().show(parentFragmentManager, "HowToUseFragment")
            }
            premiumIcon.setOnClickListener {
                context?.logFirebaseEvent("home_fragment", "premium_button_clicked")
                PremiumFragment().show(parentFragmentManager, "HomeFragment")
            }

            val socialMediaPatterns = mapOf(
                "TikTok" to Regex("tiktok\\.com", RegexOption.IGNORE_CASE),
                "Facebook" to Regex("facebook\\.com", RegexOption.IGNORE_CASE),
                "Pinterest" to Regex(
                    "(pinterest\\.com|pin\\.it)",
                    RegexOption.IGNORE_CASE
                ), // Includes shortened URL
                "Snapchat" to Regex("snapchat\\.com", RegexOption.IGNORE_CASE),
                "Linkedin" to Regex("linkedin\\.com", RegexOption.IGNORE_CASE),
                "Instagram" to Regex("instagram\\.com", RegexOption.IGNORE_CASE)
            )

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // No action needed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // No action needed
                    val link = s.toString().trim()
                    checkSocialMediaLink(link, socialMediaPatterns)
                }

                override fun afterTextChanged(s: Editable?) {
                    // Move the cursor to the end of the text
                    input.setSelection(input.text?.length ?: 0)
                }
            })


            downloadTv.setOnClickListener {
                context?.logFirebaseEvent("home_fragment", "download_button_clicked")
                val downloadUrl = input.text.toString().trim()
                if (!URLUtil.isValidUrl(downloadUrl)) {
                    Toast.makeText(
                        context ?: return@setOnClickListener,
                        getString(R.string.please_enter_a_valid_url), Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                checkForYoutubeLink(downloadUrl) {
                    if (downloadUrl.isNotEmpty()) {
                        downloadDialog = DownloadDialogHelper {}
                        downloadDialog?.showDownloadDialog(context ?: return@checkForYoutubeLink)
                        videoViewModel.newDownloadVideoApi(
                            downloadUrl,
                            context ?: return@checkForYoutubeLink
                        )
                    }
                }

            }

            pasteLinkTv.setOnClickListener {
                context?.logFirebaseEvent("home_fragment", "paste_button_clicked")
                pasteClipboardText()
            }
        }

        setUpProgressDialog()
        loadPopularVideos()

        videoViewModel.downloadStatus.observe(viewLifecycleOwner) { status ->
            /*var isLangSelected = false
            homeViewModel.isLanguageSelected.observe(viewLifecycleOwner) { isLanguageSelected ->
                isLangSelected = isLanguageSelected
            }

            if (isLangSelected) {
                homeViewModel.isLanguageSelected(false)
                return@observe
            }*/
            //if (videoViewModel.isDownloadHandled) return@observe
            binding?.input?.text?.clear()
            Log.e("SHAH", "onResponse: $status")
            when (status) {
                "SUCCESS" -> {
                    videoViewModel.isDownloadHandled = true
                    if (Admobify.isPremiumUser()) {
                        homeViewModel.updatePageSelector(2)
                        Toast.makeText(
                            context ?: return@observe,
                            getString(R.string.video_downloaded_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showInterAd()
                    }
                }

                "ERROR_SIZE" -> {
                    Toast.makeText(
                        context ?: return@observe,
                        getString(R.string.video_size_is_too_large),
                        Toast.LENGTH_SHORT
                    ).show()
                   // videoViewModel.resetDownloadStatus()
                }

                "ERROR" -> {
                    Toast.makeText(
                        context ?: return@observe,
                        getString(R.string.downloading_failed), Toast.LENGTH_SHORT
                    ).show()
                   // videoViewModel.resetDownloadStatus()


                }
            }
            downloadDialog?.dismissDialog()
        }

        videoViewModel.downloadProgress.observe(viewLifecycleOwner) { progress ->
            downloadDialog?.updateText(getString(R.string.your_video_is_being_download_nplease_wait))
            downloadDialog?.updateProgress(progress)

        }

        OpenAppAd.adEventListener = object : OpenAppAd.Companion.AdEventListener {
            override fun onAdShown() {
                Log.e("SHAH", "onAdShown: ")
                isAppOpenAdsShown = true
                //binding?.nativeContainer?.visibility = View.GONE
            }

            override fun onAdDismissed() {
                Log.e("SHAH", "onAdDismissed: ")
                isAppOpenAdsShown = false
                //binding?.nativeContainer?.visibility = View.VISIBLE
            }
        }

    }

    private fun loadPopularVideos() {
        val adapter = PopularVideosAdapter(context ?: return, showTrendingVideos()) {
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
        binding?.popularRecyclerview?.adapter = adapter
    }

    private fun checkSocialMediaLink(link: String, socialMediaPatterns: Map<String, Regex>) {

        for ((platform, pattern) in socialMediaPatterns) {
            if (pattern.containsMatchIn(link)) {
                binding?.apply {
                    when (platform) {
                        "Facebook" -> {
                            context?.logFirebaseEvent("home_fragment", "facebook_link")
                            fIV.setImageResource(R.drawable.f_enable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }

                        "TikTok" -> {
                            context?.logFirebaseEvent("home_fragment", "tiktok_link")
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_enable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }

                        "Pinterest" -> {
                            context?.logFirebaseEvent("home_fragment", "pinterest_link")
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_enable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }

                        "Snapchat" -> {
                            context?.logFirebaseEvent("home_fragment", "snapchat_link")
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_enable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }

                        "Linkedin" -> {
                            context?.logFirebaseEvent("home_fragment", "linkedin_link")
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_enable)
                            fIV6.setImageResource(R.drawable.insta_disable)

                        }

                        "Instagram" -> {
                            context?.logFirebaseEvent("home_fragment", "instagram_link")
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_enable)

                        }

                        else -> {
                            context?.logFirebaseEvent("home_fragment", "other_link")
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }
                    }
                }

                break
            } else {
                binding?.apply {
                    fIV.setImageResource(R.drawable.f_disable)
                    fIV2.setImageResource(R.drawable.t_disable)
                    fIV3.setImageResource(R.drawable.pin_disable)
                    fIV4.setImageResource(R.drawable.snp_disable)
                    fIV5.setImageResource(R.drawable.lin_disable)
                    fIV6.setImageResource(R.drawable.insta_disable)
                }
            }
        }

    }

    private fun checkForYoutubeLink(downloadUrl: String, downloadStart: () -> Unit = {}) {
        val youtubePattern = "^(https?://)?(www\\.)?(youtube|youtu|youtube-nocookie)\\.(com|be)/.*$"
        val isNotYouTubeLink = !Regex(youtubePattern).matches(downloadUrl)
        if (isNotYouTubeLink) {
            downloadStart.invoke()
        } else {
            Toast.makeText(
                context ?: return,
                getString(R.string.youtube_is_legally_restricted), Toast.LENGTH_SHORT
            )
                .show()
        }

    }

    private fun pasteClipboardText() {
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val clip = clipboard.primaryClip
            val item = clip?.getItemAt(0)
            val copiedText = item?.text.toString()
            binding?.input?.setText(copiedText)
        }
    }

    private fun loadDefaultNativeAd() {
        Log.e("SHAH", "loadDefaultNativeAd")
        val bind = MediumNativeAdsBinding.inflate(layoutInflater)
        bind.apply {
            val nativeAdModel = NativeAdItemsModel(
                root,
                adHeadline = adHeadline,
                adBody = adBody,
                adIcon = null,
                mediaView = AdMedia,
                adCTA = adCallToAction
            )

            NativeAdUtils().loadNativeAd(
                activity?.application ?: return@apply,
                getString(R.string.homeNativeAd),
                adRemote = native_home_l,
                binding?.nativeContainer ?: return,
                nativeAdModel, object : NativeAdCallback() {
                    override fun adFailed(error: LoadAdError?) {
                        super.adFailed(error)
                        binding?.nativeContainer?.visibility = View.GONE
                        binding?.shimmerHomeLayout?.root?.visibility = View.GONE
                    }


                    override fun adLoaded() {
                        super.adLoaded()
                        if (isAppOpenAdsShown) {
                            binding?.nativeContainer?.visibility = View.GONE
                            binding?.shimmerHomeLayout?.root?.visibility = View.GONE
                        } else {
                            binding?.nativeContainer?.visibility = View.VISIBLE
                            binding?.shimmerHomeLayout?.root?.visibility = View.GONE
                        }

                    }

                    override fun adValidate() {
                        Log.e("SHAH", "adValidate")

                        binding?.nativeContainer?.visibility = View.GONE
                        binding?.shimmerHomeLayout?.root?.visibility = View.GONE
                    }


                }, NativeAdType.DEFAULT_AD
            )
        }

    }

    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(context ?: return)
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.setMessage(getString(R.string.downloading))
    }

    override fun onDestroyView() {
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
        downloadDialog?.dismissDialog()
        super.onDestroyView()
    }

    private fun showInterAd() {
        val adOptions = InterAdOptions().setAdId(getString(R.string.homeInterstitialAd))
            .setRemoteConfig(fullscreen_home_l).setLoadingDelayForDialog(2)
            .setFullScreenLoading(false)
            .build(activity ?: return)
        InterstitialAdUtils(adOptions).loadAndShowInterAd(object :
            InterAdLoadCallback() {
            override fun adAlreadyLoaded() {}
            override fun adLoaded() {}
            override fun adFailed(error: LoadAdError?, msg: String?) {
                homeViewModel.updatePageSelector(2)
                Toast.makeText(
                    context ?: return,
                    getString(R.string.video_downloaded_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                videoViewModel.resetDownloadStatus()

            }

            override fun adValidate() {
                Toast.makeText(
                    context ?: return,
                    getString(R.string.video_downloaded_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                videoViewModel.resetDownloadStatus()
                homeViewModel.updatePageSelector(2)

            }
        },
            object : InterAdShowCallback() {
                override fun adNotAvailable() {}
                override fun adShowFullScreen() {
                    videoViewModel.resetDownloadStatus()
                    homeViewModel.updatePageSelector(2)

                }

                override fun adDismiss() {
                    Toast.makeText(
                        context ?: return,
                        getString(R.string.video_downloaded_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun adFailedToShow() {
                    homeViewModel.updatePageSelector(2)
                }

                override fun adImpression() {}

                override fun adClicked() {}
            })
    }

    override fun onResume() {
        super.onResume()
        if (!isAppOpenAdsShown) {
            loadDefaultNativeAd()
        }
    }

}







