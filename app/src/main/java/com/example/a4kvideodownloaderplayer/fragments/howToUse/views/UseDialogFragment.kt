package com.example.a4kvideodownloaderplayer.fragments.howToUse.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.databinding.HowUseFragmentBinding
import com.example.a4kvideodownloaderplayer.fragments.howToUse.adapter.HowToUseAdapter
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.showHowToUseImages
import com.google.android.material.tabs.TabLayoutMediator

class UseDialogFragment : DialogFragment() {

    private var binding: HowUseFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HowUseFragmentBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        context?.logFirebaseEvent("how_to_use_fragment", "screen_opened")
        loadSliderImages()
        clickEvent()
    }

    private fun clickEvent() {
        binding?.apply {
            closeIV.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun loadSliderImages() {
        val videoListAdapter = HowToUseAdapter(context ?: return, showHowToUseImages())
        binding?.apply {
            viewPager.adapter = videoListAdapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.setCustomView(R.layout.tab_dot_round)
            }.attach()

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    for (i in 0 until tabLayout.tabCount) {
                        val tab = tabLayout.getTabAt(i)
                        tab?.customView?.isSelected = i == position
                    }
                    updateTextViews(position)
                }
            })
        }


    }

    private fun updateTextViews(position: Int) {
        binding?.apply {
            when (position) {
                0 -> {
                    labelTv.text = getString(R.string.paste_your_copied_url)
                    countTv.text = "1"
                }

                1 -> {
                    labelTv.text = getString(R.string.click_download_button)
                    countTv.text = "2"
                }

                2 -> {
                    labelTv.text = getString(R.string.play_downloaded_videos)
                    countTv.text = "3"
                }

                3 -> {
                    labelTv.text = getString(R.string.built_in_player)
                    countTv.text = "4"
                }

                4 -> {
                    labelTv.text = getString(R.string.convert_videos_to_mp3)
                    countTv.text = "5"
                }

            }
        }
    }
}