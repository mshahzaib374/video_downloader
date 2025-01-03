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

class PopularVideosAdapter (
    private val context: Context,
    private val videoList: MutableList<PopularVideo>,
    private val navigate: (PopularVideo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {




    // Video ViewHolder
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        private val playIcon: ImageView = itemView.findViewById(R.id.playIcon)

        fun bind(videoFile: PopularVideo) {
            Glide.with(context).load(videoFile.url).placeholder(R.drawable.video_thumnail).into(thumbnailImageView)

            playIcon.setOnClickListener {
                navigate.invoke(videoFile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VideoViewHolder) {
            val video = videoList[position]
            holder.bind(video)
        }
    }

    override fun getItemCount(): Int = videoList.size


}