package com.example.a4kvideodownloaderplayer.fragments.langugage.views

import LanguageViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAd
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAdState
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerAdType
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerAdUtils
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerCallback
import com.example.a4kvideodownloaderplayer.databinding.LanguageScreenBinding
import com.example.a4kvideodownloaderplayer.fragments.home.viewmodel.VideoViewModel
import com.example.a4kvideodownloaderplayer.fragments.langugage.model.Languages
import com.example.a4kvideodownloaderplayer.fragments.langugage.views.adapter.LanguagesAdapter
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.HomeViewModel
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.aiartgenerator.utils.AppPrefs
import com.google.android.gms.ads.AdView

class LanguageDialogFragment : DialogFragment() {

    private var binding: LanguageScreenBinding? = null
    private val languageViewModel: LanguageViewModel by activityViewModels()
    private val videoViewModel: VideoViewModel by activityViewModels()

    private var languagesAdapter: LanguagesAdapter? = null
    private var language: Languages? = null
    private var selectedPosition: Int? = null
    private var ad: AdView? = null
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LanguageScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLanguageAdapter()
        attachObserver()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        context?.logFirebaseEvent("language_fragment", "screen_opened")
        OpenAppAdState.enable("LanguageFragment")

        //loadBannerAds()
    }


    private fun attachObserver() {

        binding?.backIconLanguageScreen?.setOnClickListener {
            //homeViewModel.updatePageSelector(2)
            /* findNavController().navigateUp()*/
            dismiss()
        }

        languageViewModel.languagesList.observe(viewLifecycleOwner) {
            languagesAdapter?.setItems(it)
        }

        OpenAppAd.adEventListener = object : OpenAppAd.Companion.AdEventListener {
            override fun onAdShown() {
              //  if (AppPrefs(context ?: return).getBoolean("isFirstTime")) {
                    binding?.adsBannerPlaceHolder?.visibility = View.INVISIBLE
             //   }
            }

            override fun onAdDismissed() {
              //  if (AppPrefs(context ?: return).getBoolean("isFirstTime")) {
                    binding?.adsBannerPlaceHolder?.visibility = View.VISIBLE
             //   }
            }
        }

    }

    private fun setLanguageAdapter() {
        languageViewModel.selectedPosition.observe(viewLifecycleOwner) { position ->
            languagesAdapter = LanguagesAdapter(
                activity?.application ?: return@observe,
                context ?: return@observe,
                position
            ) { language, selectedPosition ->
                this.language = language
                this.selectedPosition = selectedPosition
            }
            binding?.languageRv?.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = languagesAdapter
            }

            binding?.textView?.setOnClickListener {
                if (this.language != null) {
                    this.language?.let { it1 ->
                        languageViewModel.selectLanguage(
                            it1, this.selectedPosition ?: return@setOnClickListener
                        )
                        context?.logFirebaseEvent(
                            "language_fragment",
                            "${this.language?.codeL}_language_selected"
                        )
                        val appLocale: LocaleListCompat =
                            LocaleListCompat.forLanguageTags(this.language?.codeL)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                        navigateScreen()
                    }
                } else {
                    if (AppPrefs(context ?: return@setOnClickListener).getBoolean("isFirstTime")) {
                        navigateScreen()
                    } else {
                        Toast.makeText(
                            context ?: return@setOnClickListener,
                            getString(R.string.already_applied), Toast.LENGTH_SHORT
                        ).show()
                    }

                }


            }
        }
    }

    private fun loadBannerAds() {
        binding?.adsBannerPlaceHolder?.visibility = View.VISIBLE
        binding?.shimmerLayout?.root?.visibility = View.VISIBLE

        BannerAdUtils(activity ?: return).loadBannerAd(
            adId = getString(R.string.languageBannerAd),
            remote = true,
            container = binding?.adsBannerPlaceHolder ?: return,
            adLoadingOrShimmer = binding?.shimmerLayout?.root,
            adType = BannerAdType.DEFAULT_BANNER,
            callback = object : BannerCallback() {
                override fun onAdValidate() {
                    super.onAdValidate()
                    binding?.adsBannerPlaceHolder?.visibility = View.GONE
                    binding?.shimmerLayout?.root?.visibility = View.GONE
                }

                override fun onAdLoaded(adView: AdView) {
                    super.onAdLoaded(adView)
                    ad = adView

                }
            },
        )
    }


    private fun navigateScreen() {
        videoViewModel.resetDownloadStatus()
        homeViewModel.updatePageSelector(3)
        homeViewModel.isLanguageSelected(true)
        dismiss()
    }


    override fun onResume() {
        super.onResume()
        ad?.resume()
    }

    override fun onPause() {
        super.onPause()
        ad?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        ad?.destroy()
    }

}