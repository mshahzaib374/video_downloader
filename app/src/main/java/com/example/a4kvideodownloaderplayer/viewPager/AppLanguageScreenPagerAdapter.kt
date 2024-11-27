package com.example.a4kvideodownloaderplayer.viewPager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.a4kvideodownloaderplayer.fragments.DownloadsFragment
import com.example.a4kvideodownloaderplayer.fragments.home.views.HomeFragment
import com.example.a4kvideodownloaderplayer.fragments.settings.SettingsFragment

class AppLanguageScreenPagerAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 3 // Number of fragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> DownloadsFragment()
            2 -> SettingsFragment()
            else -> HomeFragment() // Default fragment
        }
    }
}