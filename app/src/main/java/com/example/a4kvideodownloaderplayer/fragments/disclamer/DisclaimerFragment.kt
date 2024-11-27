package com.example.a4kvideodownloaderplayer.fragments.disclamer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAdState
import com.example.a4kvideodownloaderplayer.databinding.DisclaimerScreenBinding
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.aiartgenerator.utils.AppPrefs

class DisclaimerFragment : Fragment() {

    private var binding: DisclaimerScreenBinding? = null

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
    }

    private fun attachClickListeners() {
        binding?.apply {
            disclaimerScreenOkButton.setOnClickListener {
                context?.logFirebaseEvent("disclaimer_fragment", "ok_button_clicked")

                AppPrefs(context?:return@setOnClickListener).putBoolean("isFirstTime", false)
                if (findNavController().currentDestination?.id == R.id.disclaimerFragment) {
                    findNavController().navigate(R.id.action_disclaimerFragment_to_mainFragment)
                }
            }
        }
    }
}