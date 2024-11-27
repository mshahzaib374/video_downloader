package com.example.a4kvideodownloaderplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.databinding.DownloadsFragmentBinding
import com.example.a4kvideodownloaderplayer.viewPager.DownloadsFragmentPagerAdapter

class DownloadsFragment : Fragment() {

    private var _binding: DownloadsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DownloadsFragmentBinding.inflate(inflater, container, false)

        // Access the ViewPager2 from the binding
        viewPager2 = binding.viewPagerDownloadsFragment // Use the generated binding property


        // Set up the adapter
        viewPager2.adapter = DownloadsFragmentPagerAdapter(this)
        viewPager2.isUserInputEnabled = false

        // Set click listeners for navigation icons
        binding.downloadsFragmentInProgressRectangle.setOnClickListener {
            viewPager2.currentItem = 0
            updateNavigationIcons(0)
        }

        binding.downloadsFragmentDownloadedRectangle.setOnClickListener {
            viewPager2.currentItem = 1
            updateNavigationIcons(1)
        }

        return binding.root
    }

    private fun updateNavigationIcons(selectedPosition: Int) {
        // Set the default background for both
        binding.downloadsFragmentInProgressRectangle.setImageResource(R.drawable.downloads_fragment_in_progress_unselected_rectangle)
        binding.downloadsFragmentDownloadedRectangle.setImageResource(R.drawable.downloads_fragment_downloaded_rectangle)

        // Update the selected icon
        when (selectedPosition) {
            0 -> binding.downloadsFragmentInProgressRectangle.setImageResource(R.drawable.downloads_fragment_in_progress_rectangle)
            1 -> binding.downloadsFragmentDownloadedRectangle.setImageResource(R.drawable.downloads_fragment_downloaded_selected_rectangle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to avoid memory leaks
    }

}

