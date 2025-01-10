package com.example.a4kvideodownloaderplayer.fragments.converter.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.fragments.converter.model.AudioFile


class AudioAdapter(
    private val videoList: List<AudioFile>,
    private val navigate: (AudioFile) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    // Adjusted list with ad placeholders
    private val combinedList = mutableListOf<Any>()

    init {
        prepareList()
    }


    private fun prepareList() {
        combinedList.clear()
        for (video in videoList) {
            combinedList.add(video)
        }
    }

    inner class Mp3ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)

        fun bind(videoFile: AudioFile) {
            fileNameTextView.text = videoFile.fileName

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mp3, parent, false)
        return Mp3ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Mp3ViewHolder) {
            val video = combinedList[position] as AudioFile
            holder.bind(video)
        }
    }

    override fun getItemCount(): Int = combinedList.size




}