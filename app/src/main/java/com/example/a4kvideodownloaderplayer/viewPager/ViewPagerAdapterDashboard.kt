package com.example.a4kvideodownloaderplayer.viewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.a4kvideodownloaderplayer.fragments.downloaded.views.DownloadedFragment
import com.example.a4kvideodownloaderplayer.fragments.home.views.HomeFragment
import com.example.a4kvideodownloaderplayer.fragments.settings.SettingsFragment

class ViewPagerAdapterDashboard(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val childFragments: Array<Fragment> = arrayOf(
        HomeFragment(), // 0
        DownloadedFragment(), // 1
        SettingsFragment()  // 2
    )

    override fun getItemCount(): Int {
        return childFragments.size // 3 items
    }

    override fun createFragment(position: Int): Fragment {
        return childFragments[position]
    }
}

