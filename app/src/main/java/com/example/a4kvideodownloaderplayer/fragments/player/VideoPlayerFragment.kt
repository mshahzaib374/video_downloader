package com.example.a4kvideodownloaderplayer.fragments.player

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.fullscreen_video_l
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAd
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdLoadCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdOptions
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterAdShowCallback
import com.example.a4kvideodownloaderplayer.ads.interstitial_ads.InterstitialAdUtils
import com.example.a4kvideodownloaderplayer.databinding.VideoplayerFragmentBinding
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.HomeViewModel
import com.example.a4kvideodownloaderplayer.fragments.player.viewmodel.VideoPlayerViewModel
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class VideoPlayerFragment : Fragment(), Player.Listener {
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val videoPlayerViewModel: VideoPlayerViewModel by activityViewModels()
    private var binding: VideoplayerFragmentBinding? = null
   /// private var mPlayer: ExoPlayer? = null
    private var videoUri: String? = null
    private var videoName: String? = null
    private var videoPath: String? = null
    private var mIsAppOpenShown=false
    private var isVideoEnded = false
    private var isControlsViewed = true
    private var isPotrait = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this) {
            videoPlayerViewModel.player?.stop()
            videoPlayerViewModel.player?.release()
            videoPlayerViewModel.player = null
            showAds()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VideoplayerFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.logFirebaseEvent("video_player_fragment", "screen_opened")
        homeViewModel.updatePageSelector(1)

        arguments?.let {
            videoUri = it.getString("videoUri")
            videoName = it.getString("videoName")
            videoPath = it.getString("videoPath")
        }

        initClicksEvents()
        initAppOpenListener()

        /*val orientationEventListener: OrientationEventListener = object : OrientationEventListener(
            context
        ) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                if (orientation in 46..134) {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                } else if (orientation in 136..224) {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                } else if (orientation in 226..314) {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }*/

    }






    private fun initClicksEvents() {

        binding?.rotateIV?.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "rotate_button_clicked")
            if (isPotrait) {
                isPotrait = false
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                isPotrait = true
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        binding?.playerView?.setOnClickListener {
            toggleControllerVisibility()
        }

        binding?.shareIcon?.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "share_button_clicked")
            if ( videoPlayerViewModel.player!!.isPlaying) {
                 videoPlayerViewModel.player!!.pause()
                binding!!.playOrPause.setImageResource(R.drawable.play_ic)
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(videoUri))
            }
            // Grant permission to the receiving app to access the URI
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context?.startActivity(Intent.createChooser(shareIntent, "Share Video"))
        }


        binding?.titleTv!!.text = videoName ?: ""
        binding?.backIcon!!.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "back_button_clicked")
             videoPlayerViewModel.player?.stop()
            showAds()

        }

        binding?.playOrPause!!.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "play_pause_clicked")

            if ( videoPlayerViewModel.player!!.isPlaying) {
                 videoPlayerViewModel.player!!.pause()
                binding!!.playOrPause.setImageResource(R.drawable.play_ic)
            } else {
                if (isVideoEnded) {
                     videoPlayerViewModel.player?.seekTo(0) // Seek to the start of the video
                    isVideoEnded = false // Reset the flag
                }
                 videoPlayerViewModel.player!!.play()
                binding!!.playOrPause.setImageResource(R.drawable.pause_ic)
            }
        }

        binding?.rewindBtn!!.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "rewind_clicked")
             videoPlayerViewModel.player!!.seekTo( videoPlayerViewModel.player!!.currentPosition - 10000)
        }

        binding?.forwardBtn!!.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "forward_clicked")
             videoPlayerViewModel.player!!.seekTo( videoPlayerViewModel.player!!.currentPosition + 10000)
        }

        binding?.prevBtn!!.setOnClickListener {
            if (binding!!.playerView.player!!.hasPreviousMediaItem()) {
                binding!!.playerView.player!!.seekToPreviousMediaItem()
            } else {
                Toast.makeText(
                    context ?: return@setOnClickListener,
                    "No previous Video found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding?.nextBtn!!.setOnClickListener {
            if (binding!!.playerView.player!!.hasNextMediaItem()) {
                binding!!.playerView.player!!.seekToNextMediaItem()
            } else {
                Toast.makeText(
                    context ?: return@setOnClickListener,
                    "No next Video found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun showAds() {
        val adOptions = InterAdOptions().setAdId(getString(R.string.playerInterstitialAd))
            .setRemoteConfig(fullscreen_video_l).setLoadingDelayForDialog(2)
            .setFullScreenLoading(false)
            .build(activity ?: return)

        InterstitialAdUtils(adOptions).loadAndShowInterAd(object :
            InterAdLoadCallback() {
            override fun adAlreadyLoaded() {}
            override fun adLoaded() {}
            override fun adFailed(error: LoadAdError?, msg: String?) {
                homeViewModel.updatePageSelector(1)
                findNavController().navigateUp()
            }

            override fun adValidate() {
                homeViewModel.updatePageSelector(1)
                findNavController().navigateUp()
            }

        },
            object : InterAdShowCallback() {
                override fun adNotAvailable() {
                    homeViewModel.updatePageSelector(1)
                    findNavController().navigateUp()
                }
                override fun adShowFullScreen() {
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(800)
                        homeViewModel.updatePageSelector(1)
                        findNavController().navigateUp()
                    }

                }

                override fun adDismiss() {

                }

                override fun adFailedToShow() {
                    homeViewModel.updatePageSelector(1)
                    findNavController().navigateUp()
                }

                override fun adImpression() {}

                override fun adClicked() {}
            }) {

        }
    }

    private fun toggleControllerVisibility() {
        binding?.apply {
            if (!isControlsViewed) {
                isControlsViewed = true
                backIcon.visibility = View.VISIBLE
                titleTv.visibility = View.VISIBLE
                playOrPause.visibility = View.VISIBLE
                rewindBtn.visibility = View.VISIBLE
                forwardBtn.visibility = View.VISIBLE
                controls.show()
                shareIcon.visibility = View.VISIBLE
            } else {
                isControlsViewed = false
                backIcon.visibility = View.INVISIBLE
                titleTv.visibility = View.INVISIBLE
                playOrPause.visibility = View.INVISIBLE
                rewindBtn.visibility = View.INVISIBLE
                forwardBtn.visibility = View.INVISIBLE
                controls.hide()
                shareIcon.visibility = View.INVISIBLE
            }
        }
    }


    private fun initPlayer() {
         videoPlayerViewModel.player = ExoPlayer.Builder(context ?: return).build().also {
            binding!!.playerView.player = it
            binding!!.controls.player = it
            binding?.playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUri) ?: return)
            it.addMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = true
            it.addListener(this)
            it.seekTo(0, C.TIME_UNSET)

        }

    }

    override fun onStart() {
        super.onStart()
        if (!mIsAppOpenShown) {
            if (Util.SDK_INT >= 24) {
                initPlayer()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (videoPlayerViewModel.player != null) {
            videoPlayerViewModel.player?.playWhenReady = videoPlayerViewModel.playWhenReady
            videoPlayerViewModel.player?.seekTo(videoPlayerViewModel.playbackPosition)
        }
        if (!mIsAppOpenShown) {
            if (Util.SDK_INT < 24 ||  videoPlayerViewModel.player == null) {
                initPlayer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoPlayerViewModel.player != null) {
            videoPlayerViewModel.playbackPosition = videoPlayerViewModel.player?.currentPosition ?:0
            videoPlayerViewModel.playWhenReady = videoPlayerViewModel.player?.playWhenReady ?:true
            videoPlayerViewModel.player?.playWhenReady = false
        }
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if ( videoPlayerViewModel.player == null) {
            return
        }
        //release player when done
         videoPlayerViewModel.player!!.release()
         videoPlayerViewModel.player = null
    }


    @Deprecated("Deprecated in Java")
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Log.e("TAG", "onPlayerStateChanged: $playbackState")
        when (playbackState) {
            Player.STATE_READY -> {
                binding!!.playOrPause.setImageResource(R.drawable.pause_ic)
                binding!!.progressBarVideoPlay.visibility = View.INVISIBLE
            }

            Player.STATE_BUFFERING -> {
                binding!!.progressBarVideoPlay.visibility = View.VISIBLE
            }

            Player.STATE_ENDED -> {
                binding!!.progressBarVideoPlay.visibility = View.INVISIBLE
                binding!!.playOrPause.setImageResource(R.drawable.play_ic)
                isVideoEnded = true // Mark that the video has ended
                isControlsViewed = false
                toggleControllerVisibility()
            }

            Player.STATE_IDLE -> {
                binding!!.progressBarVideoPlay.visibility = View.INVISIBLE
            }
        }
    }

    private fun initAppOpenListener(){
        OpenAppAd.adEventListener = object : OpenAppAd.Companion.AdEventListener {
            override fun onAdShown() {
                     videoPlayerViewModel.player?.pause()
                mIsAppOpenShown=true
            }

            override fun onAdDismissed() {
                mIsAppOpenShown=false
                if (isVideoEnded) {
                     videoPlayerViewModel.player?.seekTo(0) // Seek to the start of the video
                    isVideoEnded = false // Reset the flag
                }
                 videoPlayerViewModel.player!!.play()
            }
        }
    }



}