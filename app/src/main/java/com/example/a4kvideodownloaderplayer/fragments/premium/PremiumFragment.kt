package com.example.a4kvideodownloaderplayer.fragments.premium

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.example.a4kvideodownloaderplayer.MainActivity
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.billing.BillingListener
import com.example.a4kvideodownloaderplayer.ads.billing.BillingUtils
import com.example.a4kvideodownloaderplayer.ads.utils.Admobify
import com.example.a4kvideodownloaderplayer.databinding.PremiumFragmentBinding
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.privacyPolicyUrl
import com.example.a4kvideodownloaderplayer.helper.termsUrl

class PremiumFragment : DialogFragment() {
    private var _binding: PremiumFragmentBinding? = null
    private var billingUtils: BillingUtils? = null
    private var SKUID = "one_month_package"


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
                    // Handle product and subscription metadata

                    subscriptions.forEach {
                        val productId = it.productId
                        Log.e("TAG", "productId: $productId")
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

                        val price =
                            it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice

                        Log.d("IAP", "productAndSubMetaData: $price")
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
                    // Handle combined list
                    Log.d("IAP", "purchasedAndSubscribedList: ${subList}")
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
               // findNavController().navigateUp()
                dismiss()
            }
            premiumBuy.setOnClickListener {
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


}