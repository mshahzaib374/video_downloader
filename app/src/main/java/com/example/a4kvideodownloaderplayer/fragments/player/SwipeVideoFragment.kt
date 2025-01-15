package com.example.a4kvideodownloaderplayer.fragments.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.a4kvideodownloaderplayer.databinding.SwipeVideoFragmentBinding
import com.example.a4kvideodownloaderplayer.fragments.home.model.PopularVideo
import com.example.a4kvideodownloaderplayer.helper.showTrendingVideos

class SwipeVideoFragment :  Fragment() {
    private var binding : SwipeVideoFragmentBinding ?= null
    private lateinit var videoAdapter: VideoPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SwipeVideoFragmentBinding.inflate(layoutInflater, container, false)
        return binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
    }

    private fun initViewPager() {


        videoAdapter = VideoPagerAdapter(this, showTrendingVideos())
        binding?.viewPager?.apply {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = videoAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding?.viewPager?.setCurrentItem(position, false)  // Smooth scroll to the target position
                    playVideoAtPosition(position)
                }
            })
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        //videoAdapter.releaseAllPlayers()
    }

    fun playVideoAtPosition(position: Int) {

        val fragmentTag = "f$position"  // Default tag format for ViewPager2 fragments
        val fragment = childFragmentManager.findFragmentByTag(fragmentTag) as? PopularVideoPlayerFragment

        fragment?.initPlayer()  // Initialize and play the video
    }



}



class VideoPagerAdapter(
    fragment: Fragment,
    private val videoUrls: List<PopularVideo>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = videoUrls.size

    override fun createFragment(position: Int): Fragment {
        return PopularVideoPlayerFragment().apply {
            arguments = Bundle().apply {
                putString("videoUrl", videoUrls[position].url)
            }
        }
    }
}