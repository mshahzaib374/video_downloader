package com.example.a4kvideodownloaderplayer.fragments.player

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
import com.example.a4kvideodownloaderplayer.databinding.PopularVideoplayerFragmentBinding
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.HomeViewModel
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


class PopularVideoPlayerFragment : Fragment(), Player.Listener {
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var binding: PopularVideoplayerFragmentBinding? = null
    private var mPlayer: ExoPlayer? = null
    private var videoUrl: String? = null
    private var mIsAppOpenShown=false
    private var isVideoEnded = false
    private var isControlsViewed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         activity?.onBackPressedDispatcher?.addCallback(this) {
            showAds()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PopularVideoplayerFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.logFirebaseEvent("video_player_fragment", "screen_opened")
        homeViewModel.updatePageSelector(0)

        arguments?.let {
            videoUrl = it.getString("videoUrl")
        }

        initClicksEvents()
        initAppOpenListener()
    }


    private fun initClicksEvents() {
        binding?.playerView?.setOnClickListener {
            toggleControllerVisibility()
        }

       /* binding?.shareIcon?.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "share_button_clicked")
            if (mPlayer!!.isPlaying) {
                mPlayer!!.pause()
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
*/

        /*binding?.titleTv!!.text = videoName ?: ""
        binding?.backIcon!!.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "back_button_clicked")
            mPlayer?.stop()
            showAds()

        }*/

        binding?.playOrPause!!.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "play_pause_clicked")

            if (mPlayer!!.isPlaying) {
                mPlayer!!.pause()
                binding!!.playOrPause.setImageResource(R.drawable.play_ic)
            } else {
                if (isVideoEnded) {
                    mPlayer?.seekTo(0) // Seek to the start of the video
                    isVideoEnded = false // Reset the flag
                }
                mPlayer!!.play()
                binding!!.playOrPause.setImageResource(R.drawable.pause_ic)
            }
        }

        binding?.rewindBtn!!.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "rewind_clicked")

            mPlayer!!.seekTo(mPlayer!!.currentPosition - 10000)
        }

        binding?.forwardBtn!!.setOnClickListener {
            context?.logFirebaseEvent("video_player_fragment", "forward_clicked")

            mPlayer!!.seekTo(mPlayer!!.currentPosition + 10000)
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
                Log.d("TAG", "ads Failed")
                homeViewModel.updatePageSelector(0)
                findNavController().navigateUp()
            }

            override fun adValidate() {
                homeViewModel.updatePageSelector(0)
                findNavController().navigateUp()
            }

        },
            object : InterAdShowCallback() {
                override fun adNotAvailable() {
                    homeViewModel.updatePageSelector(0)
                    findNavController().navigateUp()
                }
                override fun adShowFullScreen() {
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(800)
                        homeViewModel.updatePageSelector(0)
                        findNavController().navigateUp()
                    }

                }

                override fun adDismiss() {

                }

                override fun adFailedToShow() {
                    homeViewModel.updatePageSelector(0)
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
                //backIcon.visibility = View.VISIBLE
               // titleTv.visibility = View.VISIBLE
                playOrPause.visibility = View.VISIBLE
                rewindBtn.visibility = View.VISIBLE
                forwardBtn.visibility = View.VISIBLE
                controls.visibility = View.VISIBLE
                //shareIcon.visibility = View.VISIBLE
            } else {
                isControlsViewed = false
                //backIcon.visibility = View.GONE
                //titleTv.visibility = View.GONE
                playOrPause.visibility = View.GONE
                rewindBtn.visibility = View.GONE
                forwardBtn.visibility = View.GONE
                controls.visibility = View.GONE
               // shareIcon.visibility = View.GONE
            }
        }
    }


    private fun initPlayer() {
        mPlayer = ExoPlayer.Builder(context ?: return).build().also {
            binding!!.playerView.player = it
            binding!!.controls.player = it
            binding?.playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            val mediaItem = MediaItem.fromUri(videoUrl ?: return)
            it.addMediaItem(mediaItem)
            //it.setMediaSource(buildMediaSource())
            /*videoList?.let { items ->
                items.forEach { data ->
                    if (data.isApi30) {
                        Log.e("TAG", "initPlayer: ${data.documentFile.uri}")
                        val item: MediaItem = MediaItem.fromUri(data.documentFile.uri)
                        it.addMediaItem(item)
                    } else {
                        Log.e("TAG", "initPlayer: ${data.file.path}")
                        val item: MediaItem = MediaItem.fromUri(Uri.parse(data.file.path))
                        it.addMediaItem(item)
                    }

                }
            }*/
            // Prepare the player.
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
        if (!mIsAppOpenShown) {
            if (Util.SDK_INT < 24 || mPlayer == null) {
                initPlayer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
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
        if (mPlayer == null) {
            return
        }
        //release player when done
        mPlayer!!.release()
        mPlayer = null
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
                    mPlayer?.pause()
                mIsAppOpenShown=true
            }

            override fun onAdDismissed() {
                mIsAppOpenShown=false
                if (isVideoEnded) {
                    mPlayer?.seekTo(0) // Seek to the start of the video
                    isVideoEnded = false // Reset the flag
                }
                mPlayer!!.play()
            }
        }
    }

    /*override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        pos = newPosition.mediaItemIndex
        Log.e("TAG", "onPositionDiscontinuity: $pos")
        if (pos!! <= videoList!!.size) {
            val videoItem = videoList!![pos!!]
            isApi30 = videoItem.isApi30
            if (isApi30!!) {
                videoUri = videoItem.documentFile.uri.toString()
            } else {
                videoUri = Uri.fromFile(videoItem.file).toString()
            }
        }
    }*/




}