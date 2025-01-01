package com.example.a4kvideodownloaderplayer.fragments.home.views

import android.app.ProgressDialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.example.a4kvideodownloaderplayer.databinding.HomeFragmentBinding
import com.example.a4kvideodownloaderplayer.databinding.MediumNativeAdsBinding
import com.example.a4kvideodownloaderplayer.databinding.NativeAdLayoutBinding
import com.example.a4kvideodownloaderplayer.databinding.NativeSmallAdBinding
import com.example.a4kvideodownloaderplayer.fragments.home.viewmodel.VideoViewModel
import com.example.a4kvideodownloaderplayer.fragments.main.MainFragment
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.HomeViewModel
import com.example.a4kvideodownloaderplayer.fragments.premium.PremiumFragment
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.DownloadDialogHelper
import com.google.android.gms.ads.LoadAdError

class HomeFragment : Fragment() {
    private var binding: HomeFragmentBinding? = null
    private val videoViewModel: VideoViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var progressDialog: ProgressDialog? = null
    private var downloadDialog: DownloadDialogHelper? = null
    private var isLinkedien = false

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
            premiumIcon.setOnClickListener {
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


            cardViewDashboardHomeUpper.setBackgroundResource(R.drawable.rounded_corners_home_fragment_card)
            downloadTv.setOnClickListener {
                context?.logFirebaseEvent("home_fragment", "download_button_clicked")
                context?.logFirebaseEvent("home_fragment", "Entered Download Link ${input.text.toString().trim()}")
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
                        downloadDialog = DownloadDialogHelper {
                            //videoViewModel.cancelDownload()
                        }
                        // downloadDialog?.initListeners(context ?: return@checkForYoutubeLink)
                        downloadDialog?.showDownloadDialog(context ?: return@checkForYoutubeLink)
                        videoViewModel.newDownloadVideoApi(
                            downloadUrl,
                            context ?: return@checkForYoutubeLink
                        )
                        /*checkForLinkedinLink(downloadUrl) {
                            if (it) {
                                Log.d("SHAH", "oldDownloadVideoApi: ")
                                videoViewModel.oldDownloadVideoApi(
                                    downloadUrl,
                                    context ?: return@checkForLinkedinLink
                                )
                            } else {
                                Log.d("SHAH", "newDownloadVideoApi: ")
                                videoViewModel.newDownloadVideoApi(
                                    downloadUrl,
                                    context ?: return@checkForLinkedinLink
                                )
                            }
                        }*/

                    }
                }

            }

            pasteLinkTv.setOnClickListener {
                context?.logFirebaseEvent("home_fragment", "paste_button_clicked")
                pasteClipboardText()
            }
        }

        setUpProgressDialog()
        loadDefaultNativeAd()

        videoViewModel.downloadStatus.observe(viewLifecycleOwner) { status ->
            Log.e("TAG", "onViewCreated: $status")
            var isLangSelected=false
            homeViewModel.isLanguageSelected.observe(viewLifecycleOwner){isLanguageSelected->
                    isLangSelected=isLanguageSelected
            }

            if (isLangSelected){
                homeViewModel.isLanguageSelected(false)
                return@observe
            }

            binding?.input?.text?.clear()
            when (status) {
                "SUCCESS" -> {
                    if (Admobify.isPremiumUser()) {
                        homeViewModel.updatePageSelector(1)
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
                    videoViewModel.resetDownloadStatus()
                }

                "ERROR" -> {
                    Toast.makeText(
                        context ?: return@observe,
                        getString(R.string.downloading_failed), Toast.LENGTH_SHORT
                    ).show()
                    videoViewModel.resetDownloadStatus()


                }

                else -> {
                    if (!TextUtils.isEmpty(status)) {
                        Toast.makeText(
                            context ?: return@observe,
                            getString(R.string.downloading_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }


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
                Log.e("TAG", "onAdShown: ")
                binding?.nativeContainer?.visibility = View.INVISIBLE
            }

            override fun onAdDismissed() {
                Log.e("TAG", "onAdDismissed: ")
                binding?.nativeContainer?.visibility = View.VISIBLE
            }
        }


        MainFragment.dialogEventListener = object : MainFragment.Companion.DialogEventListener {
            override fun onDialogShown() {
                binding?.nativeContainer?.visibility = View.INVISIBLE

            }

            override fun onDialogDismissed() {
                binding?.nativeContainer?.visibility = View.VISIBLE
            }

        }

    }

    private fun checkSocialMediaLink(link: String, socialMediaPatterns: Map<String, Regex>) {

        for ((platform, pattern) in socialMediaPatterns) {
            if (pattern.containsMatchIn(link)) {
                binding?.apply {
                    when (platform) {
                        "Facebook" -> {
                            fIV.setImageResource(R.drawable.f_enable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }

                        "TikTok" -> {
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_enable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }

                        "Pinterest" -> {
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_enable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }

                        "Snapchat" -> {
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_enable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_disable)
                        }

                        "Linkedin" -> {
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_enable)
                            fIV6.setImageResource(R.drawable.insta_disable)

                        }

                        "Instagram" -> {
                            fIV.setImageResource(R.drawable.f_disable)
                            fIV2.setImageResource(R.drawable.t_disable)
                            fIV3.setImageResource(R.drawable.pin_disable)
                            fIV4.setImageResource(R.drawable.snp_disable)
                            fIV5.setImageResource(R.drawable.lin_disable)
                            fIV6.setImageResource(R.drawable.insta_enable)

                        }

                        else -> {
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

    private fun checkForLinkedinLink(downloadUrl: String, isLinkedinURL: (Boolean) -> Unit = {}) {
        val linkedinRegex = Regex("https?://(www\\.)?linkedin\\.com/.*", RegexOption.IGNORE_CASE)
        if (linkedinRegex.matches(downloadUrl)) {
            isLinkedinURL.invoke(true)
        } else {
            isLinkedinURL.invoke(false)
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
                        binding?.nativeContainer?.visibility = View.INVISIBLE
                        binding?.shimmerHomeLayout?.root?.visibility = View.INVISIBLE
                    }


                    override fun adLoaded() {
                        super.adLoaded()
                        binding?.nativeContainer?.visibility = View.VISIBLE
                        binding?.shimmerHomeLayout?.root?.visibility = View.GONE
                    }

                    override fun adValidate() {
                        binding?.nativeContainer?.visibility = View.INVISIBLE
                        binding?.shimmerHomeLayout?.root?.visibility = View.INVISIBLE
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
                // todo
                homeViewModel.updatePageSelector(1)
                Toast.makeText(
                    context ?: return,
                    getString(R.string.video_downloaded_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                videoViewModel.resetDownloadStatus()

            }

            override fun adValidate() {
                // todo
                Toast.makeText(
                    context ?: return,
                    getString(R.string.video_downloaded_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                videoViewModel.resetDownloadStatus()
                homeViewModel.updatePageSelector(1)

            }
        },
            object : InterAdShowCallback() {
                override fun adNotAvailable() {}
                override fun adShowFullScreen() {
                    //todo
                    videoViewModel.resetDownloadStatus()
                    homeViewModel.updatePageSelector(1)

                }

                override fun adDismiss() {
                    Toast.makeText(
                        context ?: return,
                        getString(R.string.video_downloaded_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun adFailedToShow() {
                    //todo
                    homeViewModel.updatePageSelector(1)
                }

                override fun adImpression() {}

                override fun adClicked() {}
            })
    }

}







