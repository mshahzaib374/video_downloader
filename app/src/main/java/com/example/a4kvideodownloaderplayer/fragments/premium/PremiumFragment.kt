package com.example.a4kvideodownloaderplayer.fragments.premium

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.example.a4kvideodownloaderplayer.MainActivity
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_video_l
import com.example.a4kvideodownloaderplayer.ads.billing.BillingListener
import com.example.a4kvideodownloaderplayer.ads.billing.BillingUtils
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdLoadCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdOptions
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdShowCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterstitialAdUtils
import com.example.a4kvideodownloaderplayer.ads.utils.Admobify
import com.example.a4kvideodownloaderplayer.databinding.PremiumFragmentBinding
import com.example.a4kvideodownloaderplayer.databinding.RestrictPremiumDialogLayoutBinding
import com.example.a4kvideodownloaderplayer.fragments.main.MainFragment.Companion.dialogEventListener
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.privacyPolicyUrl
import com.example.a4kvideodownloaderplayer.helper.termsUrl
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PremiumFragment : DialogFragment() {
    private var _binding: PremiumFragmentBinding? = null
    private var billingUtils: BillingUtils? = null
    private var SKUID = "one_month_package"
    private var subscribedList = emptyList<Purchase>()
    private var mAdCount=2
    private var exitBinding: RestrictPremiumDialogLayoutBinding? = null
    private var exitDialog: Dialog? = null

    companion object{
        var onDismissAdCapping=2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PremiumFragmentBinding.inflate(inflater, container, false)
        return _binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.logFirebaseEvent("premium_fragment", "screen_opened")
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        billingUtils = BillingUtils.getInstance()
            .setBillingListener(object : BillingListener() {

                override fun productAndSubMetaData(
                    products: List<ProductDetails>,
                    subscriptions: List<ProductDetails>
                ) {
                    subscriptions.forEach {
                        val productId = it.productId
                        when (productId) {
                            "one_month_package" -> {
                                _binding?.monthlyPriceTv?.text =
                                    it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                            }

                            "six_month_package" -> {
                                _binding?.sixMonthlyPriceTv?.text =
                                    it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                            }
                        }
                    }
                }

                override fun purchasedORSubDone(productsList: List<Purchase?>) {
                    context?.logFirebaseEvent("premium_fragment", "buy_$SKUID")

                    Admobify.setPremiumUser(true)
                    activity?.finishAffinity()
                    startActivity(Intent(activity ?: return, MainActivity::class.java))
                }

                override fun purchasedAndSubscribedList(
                    purchaseList: List<Purchase>,
                    subList: List<Purchase>
                ) {
                    subscribedList = subList
                }
            })
            .setSubscriptionIds("one_month_package", "six_month_package")
            .build(activity ?: return)

        clickEvent()

    }

    private fun clickEvent() {


        _binding?.apply {
            termsTv.setOnClickListener {
                context?.logFirebaseEvent("premium_fragment", "privacy_button_clicked")
                activity?.termsUrl()
            }

            privacyTv.setOnClickListener {
                context?.logFirebaseEvent("premium_fragment", "privacy_button_clicked")
                activity?.privacyPolicyUrl()
            }

            premiumCloseIV.setOnClickListener {
                if (onDismissAdCapping!=0){
                    onDismissAdCapping--
                    dismiss()
                }else {
                    showInterAd()
                }
            }

            premiumContinue.setOnClickListener {
                    showInterAd()

            }

            premiumBuy.setOnClickListener {
                /*subscribedList.forEach {
                    val constained = it.products.contains(SKUID)
                    if (constained) {
                        restrictDialog()
                        return@setOnClickListener
                    }
                }*/
             /*   if (Admobify.isPremiumUser()) {
                    restrictDialog()
                    return@setOnClickListener
                }*/
                billingUtils?.subscribe(SKUID)
            }

            monthlyView.setOnClickListener {
                monthlyView.setBackgroundResource(R.drawable.premium_package_selector_bg)
                sixMonthlyView.setBackgroundResource(R.drawable.premium_package_un_selector_bg)
                thirtyPercentTv.visibility = View.GONE

                monthlySelectorIV.setBackgroundResource(R.drawable.premiun_selector_radio)
                sixMonthlySelectorIV.setBackgroundResource(R.drawable.premium_un_selector_radio)

                SKUID = "one_month_package"
            }

            sixMonthlyView.setOnClickListener {
                monthlyView.setBackgroundResource(R.drawable.premium_package_un_selector_bg)
                sixMonthlyView.setBackgroundResource(R.drawable.premium_package_selector_bg)
                thirtyPercentTv.visibility = View.VISIBLE

                monthlySelectorIV.setBackgroundResource(R.drawable.premium_un_selector_radio)
                sixMonthlySelectorIV.setBackgroundResource(R.drawable.premiun_selector_radio)

                SKUID = "six_month_package"
            }
        }

    }


    private fun restrictDialog() {
        exitBinding = RestrictPremiumDialogLayoutBinding.inflate(layoutInflater)
        exitDialog = Dialog(context ?: return)
        exitDialog?.apply {
            setContentView(exitBinding?.root ?: return)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.5f)
            show()
            dialogEventListener?.onDialogShown()
        }

        exitBinding?.apply {
            exitETv.setOnClickListener {
                exitDialog?.dismiss()
            }
        }



        exitDialog?.setOnShowListener {
            //homeViewModel?.showExitDialog(true)
        }
        exitDialog?.setOnDismissListener {
            dialogEventListener?.onDialogDismissed()
            //homeViewModel?.hideExitDialog(false)
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

    private fun showInterAd() {
        val adOptions = InterAdOptions().setAdId(getString(R.string.playerInterstitialAd))
            .setRemoteConfig(fullscreen_video_l).setLoadingDelayForDialog(2)
            .setFullScreenLoading(false)
            .build(activity ?: return)
        InterstitialAdUtils(adOptions).loadAndShowInterAd(object :
            InterAdLoadCallback() {
            override fun adAlreadyLoaded() {}
            override fun adLoaded() {}
            override fun adFailed(error: LoadAdError?, msg: String?) {
                dismiss()
            }

            override fun adValidate() {
                dismiss()

            }
        },
            object : InterAdShowCallback() {
                override fun adNotAvailable() {}
                override fun adShowFullScreen() {
                   /* if(mAdCount==1){
                        _binding?.adRemainingTv?.text=context?.getString(R.string.one_ad_left)
                        return
                    }*/
                    if (onDismissAdCapping==0){
                        onDismissAdCapping=2
                    }
                    lifecycleScope.launch {
                        delay(800)
                        onDismissAdCapping=2
                        dismiss()
                    }

                }

                override fun adDismiss() {

                }

                override fun adFailedToShow() {
                    dismiss()
                }

                override fun adImpression() {}

                override fun adClicked() {}
            })
    }

}