package com.example.a4kvideodownloaderplayer.fragments.settings

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.databinding.DialogRateUsBinding
import com.example.a4kvideodownloaderplayer.databinding.SettingsFragmentBinding
import com.example.a4kvideodownloaderplayer.fragments.langugage.views.LanguageDialogFragment
import com.example.a4kvideodownloaderplayer.fragments.premium.PremiumFragment
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.feedback
import com.example.a4kvideodownloaderplayer.helper.privacyPolicyUrl
import com.example.a4kvideodownloaderplayer.helper.shareApp

class SettingsFragment : Fragment() {

    private var ratingDialog: Dialog? = null
    private var ratingBinding: DialogRateUsBinding? = null
    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.logFirebaseEvent("settings_fragment", "screen_opened")

        binding.apply {

            upgradeToPremiumCard.setOnClickListener {
                PremiumFragment().show(parentFragmentManager, "PremiumFragment")
            }

            languageTv.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "language_button_clicked")
                LanguageDialogFragment().show(parentFragmentManager, "SettingsFragment")
               /* if (findNavController().currentDestination?.id == R.id.mainFragment) {
                    findNavController().navigate(R.id.action_mainFragment_to_languageFragment)
                }*/
            }
            languageIcon.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "language_button_clicked")
                if (findNavController().currentDestination?.id == R.id.mainFragment) {
                    findNavController().navigate(R.id.action_mainFragment_to_languageFragment)
                }
            }

            privacyPolicyTv.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "privacy_button_clicked")
                activity?.privacyPolicyUrl()
            }

            privacyPolicyIcon.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "privacy_button_clicked")
                activity?.privacyPolicyUrl()
            }

            shareWithFriendsIcon.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "share_button_clicked")
                activity?.shareApp()
            }

            shareWithFriendsTv.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "share_button_clicked")
                activity?.shareApp()
            }

            feedbackIcon.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "feedback_button_clicked")
                activity?.feedback()
            }

            feedbackTv.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "feedback_button_clicked")
                activity?.feedback()
            }

            rateUsTv.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "rate_dialog_opened")
                showRateUsDialog()

            }
            rateUsIcon.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "rate_dialog_opened")
                showRateUsDialog()
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showRateUsDialog() {
        ratingBinding = DialogRateUsBinding.inflate(LayoutInflater.from(context))
        ratingDialog = Dialog(context ?: return)
        ratingDialog?.apply {
            setContentView(ratingBinding?.root ?: return)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.5f)
            show()
        }

        ratingBinding?.apply {
            cancelTv.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "rating_cancel")

                ratingDialog?.dismiss()
            }

            exitETv.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "rating_proceed")

                openAppInPlayStore()
                ratingDialog?.dismiss()
            }
        }
    }

    private fun openAppInPlayStore() {
        val appPackageName = requireContext().packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }
}

