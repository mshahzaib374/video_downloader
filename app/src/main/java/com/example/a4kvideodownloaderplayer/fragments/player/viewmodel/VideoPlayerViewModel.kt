package com.example.a4kvideodownloaderplayer.fragments.player.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.ExoPlayer

class VideoPlayerViewModel : ViewModel() {
    var player: ExoPlayer? = null
    var playbackPosition: Long = 0
    var playWhenReady: Boolean = true
    var currentWindowIndex: Int = 0

    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }
}
