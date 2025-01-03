package com.example.a4kvideodownloaderplayer.fragments.home.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.fragments.home.model.PopularVideo


class VideoSliderAdapter(
    private val context: Context,
    private val videoList: List<PopularVideo>,
    private val navigate: (PopularVideo) -> Unit
) :
    RecyclerView.Adapter<VideoSliderAdapter.VideoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_exit_slider_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]

        // Set the video thumbnail image and other data
        Glide.with(context).load(video.url).placeholder(R.drawable.video_thumnail).into(holder.videoThumbnail)

        holder.playButton.setOnClickListener { _: View? ->
            navigate.invoke(video)
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoThumbnail: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        var playButton: ImageView = itemView.findViewById(R.id.playIcon)
    }
}
