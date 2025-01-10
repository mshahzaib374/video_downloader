package com.example.a4kvideodownloaderplayer.fragments.main.viewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.a4kvideodownloaderplayer.fragments.converter.views.AudioFragment
import com.example.a4kvideodownloaderplayer.fragments.downloaded.views.DownloadedFragment
import com.example.a4kvideodownloaderplayer.fragments.home.views.HomeFragment
import com.example.a4kvideodownloaderplayer.fragments.settings.SettingsFragment

class ViewPagerViewModel : ViewModel() {
    val fragmentList = MutableLiveData<List<Fragment>>()

    init {
        // Initialize fragments
        fragmentList.value =
            listOf(HomeFragment(), AudioFragment(), DownloadedFragment(), SettingsFragment())
    }
}