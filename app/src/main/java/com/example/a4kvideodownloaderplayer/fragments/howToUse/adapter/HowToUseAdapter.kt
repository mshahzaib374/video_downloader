package com.example.a4kvideodownloaderplayer.fragments.howToUse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.fragments.howToUse.model.ImageSlider

class HowToUseAdapter(
    private val context: Context,
    private val imageList: List<ImageSlider>) :
    RecyclerView.Adapter<HowToUseAdapter.VideoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_how_use_slider, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val img = imageList[position]

        Glide.with(context).load(img.image).placeholder(R.drawable.video_thumnail).into(holder.videoThumbnail)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoThumbnail: ImageView = itemView.findViewById(R.id.thumbnailImageView)
    }
}