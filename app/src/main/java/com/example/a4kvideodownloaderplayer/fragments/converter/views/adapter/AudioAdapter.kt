package com.example.a4kvideodownloaderplayer.fragments.converter.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.databinding.ItemMp3Binding
import com.example.a4kvideodownloaderplayer.fragments.converter.model.AudioFile


class AudioAdapter(
    private val context: Context,
    private val videoList: List<AudioFile>,
    private val navigate: (AudioFile) -> Unit,
    private val shareAudio: (AudioFile) -> Unit,
    private val deleteAudio: (AudioFile) -> Unit,
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

    inner class Mp3ViewHolder(private val binding: ItemMp3Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(audioFile: AudioFile) {
            binding.apply {
                fileNameTextView.text = audioFile.fileName
                //durationTV.text = audioFile.duration
                itemView.setOnClickListener {
                    navigate.invoke(audioFile)
                }
                menuIv.setOnClickListener {
                    val popupMenu = PopupMenu(context, menuIv)
                    popupMenu.inflate(R.menu.item_menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_share -> {
                                shareAudio.invoke(audioFile)
                                return@setOnMenuItemClickListener true
                            }

                            R.id.menu_delete -> {
                                deleteAudio.invoke(audioFile)
                                return@setOnMenuItemClickListener true
                            }

                            else -> return@setOnMenuItemClickListener false
                        }
                    }
                    popupMenu.show()
                }
            }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mp3, parent, false)
        val bind = ItemMp3Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Mp3ViewHolder(bind)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Mp3ViewHolder) {
            val video = combinedList[position] as AudioFile
            holder.bind(video)
        }
    }

    override fun getItemCount(): Int = combinedList.size


}