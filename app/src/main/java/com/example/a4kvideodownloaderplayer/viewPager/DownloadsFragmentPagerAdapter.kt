package com.example.a4kvideodownloaderplayer.viewPager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.a4kvideodownloaderplayer.fragments.InProgressFragment
import com.example.a4kvideodownloaderplayer.fragments.downloaded.views.DownloadedFragment

class DownloadsFragmentPagerAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2 // Number of fragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InProgressFragment()
            1 -> DownloadedFragment()
            else -> InProgressFragment() // Default fragment
        }
    }
}