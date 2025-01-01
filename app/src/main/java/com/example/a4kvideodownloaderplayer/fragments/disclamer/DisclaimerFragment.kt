package com.example.a4kvideodownloaderplayer.fragments.disclamer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_disclaimer_l
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAdState
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdBuilder
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdLoadCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdOptions
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdShowCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterstitialAdUtils
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils
import com.example.a4kvideodownloaderplayer.databinding.DisclaimerScreenBinding
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.aiartgenerator.utils.AppPrefs
import com.google.android.gms.ads.LoadAdError

class DisclaimerFragment : Fragment() {

    private var binding: DisclaimerScreenBinding? = null
    private var mAdOptions: InterAdBuilder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenAppAdState.disable("DisclaimerFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DisclaimerScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.logFirebaseEvent("disclaimer_fragment", "screen_opened")

        attachClickListeners()

        mAdOptions = InterAdOptions().setAdId(getString(R.string.disclaimerInterstitialAd))
            .setRemoteConfig(fullscreen_disclaimer_l).setLoadingDelayForDialog(2)
            .setFullScreenLoading(false)
            .build(activity ?: return)
    }

    private fun attachClickListeners() {
        binding?.apply {
            disclaimerScreenOkButton.setOnClickListener {
                showInterAd {
                    context?.logFirebaseEvent("disclaimer_fragment", "ok_button_clicked")

                    AppPrefs(context ?: return@showInterAd).putBoolean("isFirstTime", false)
                    if (findNavController().currentDestination?.id == R.id.disclaimerFragment) {
                        findNavController().navigate(R.id.action_disclaimerFragment_to_mainFragment)
                    }
                }
            }

            crossIc.setOnClickListener {
                showInterAd {
                    context?.logFirebaseEvent("disclaimer_fragment", "cross_button_clicked")
                    AppPrefs(context ?: return@showInterAd).putBoolean("isFirstTime", false)
                    if (findNavController().currentDestination?.id == R.id.disclaimerFragment) {
                        findNavController().navigate(R.id.action_disclaimerFragment_to_mainFragment)
                    }
                }
            }
        }
    }


    private fun showInterAd(callback: () -> Unit) {
        if (!AdmobifyUtils.isNetworkAvailable(context ?: return)) {
            callback.invoke()
            return
        }

        InterstitialAdUtils(mAdOptions!!).loadAndShowInterAd(object :
            InterAdLoadCallback() {
            override fun adAlreadyLoaded() {}
            override fun adLoaded() {}
            override fun adFailed(error: LoadAdError?, msg: String?) {
                callback.invoke()
            }

            override fun adValidate() {
                callback.invoke()
            }

        },
            object : InterAdShowCallback() {
                override fun adNotAvailable() {}
                override fun adShowFullScreen() {
                    callback.invoke()
                }

                override fun adDismiss() {
                }

                override fun adFailedToShow() {
                    callback.invoke()
                }

                override fun adImpression() {}

                override fun adClicked() {}
            })
    }
}