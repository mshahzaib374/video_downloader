package com.example.a4kvideodownloaderplayer.viewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.a4kvideodownloaderplayer.fragments.home.views.HomeFragment
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.ViewPagerViewModel

class ViewPagerAdapterDashboard(
    fragmentActivity: FragmentActivity,
    private val viewPagerViewModel: ViewPagerViewModel) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return viewPagerViewModel.fragmentList.value?.size ?: 0
    }

    override fun createFragment(position: Int): Fragment {
        return viewPagerViewModel.fragmentList.value?.get(position) ?: HomeFragment()
    }
}

