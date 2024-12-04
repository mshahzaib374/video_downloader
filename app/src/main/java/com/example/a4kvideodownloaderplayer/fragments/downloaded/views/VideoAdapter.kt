package com.example.a4kvideodownloaderplayer.fragments.downloaded.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.fragments.downloaded.model.VideoFile
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import java.io.File

class VideoAdapter(
    private val videoList: MutableList<VideoFile>,
    private val navigate: (VideoFile) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        val playIcon: ImageView = itemView.findViewById(R.id.playIcon)
        val shareImageView: ImageView = itemView.findViewById(R.id.shareIv)
        val deleteImageView: ImageView = itemView.findViewById(R.id.deleteIv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoFile = videoList[position]
        holder.fileNameTextView.text = videoFile.fileName
        holder.thumbnailImageView.setImageBitmap(videoFile.thumbnail)
        holder.shareImageView.setOnClickListener {
            holder.itemView.context?.logFirebaseEvent("downloaded_fragment", "share_video_clicked")

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, videoFile.contentUri)
            }
            // Grant permission to the receiving app to access the URI
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            holder.itemView.context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
        }

        holder.deleteImageView.setOnClickListener {
            holder.itemView.context?.logFirebaseEvent("downloaded_fragment", "delete_video_clicked")
            AlertDialog.Builder(holder.itemView.context)
                .setTitle(holder.itemView.context.getString(R.string.delete_video))
                .setMessage(holder.itemView.context.getString(R.string.are_you_sure_you_want_to_delete_this_video))
                .setPositiveButton(holder.itemView.context.getString(R.string.yes)) { _, _ ->
                    deleteImage(videoFile.contentUri, holder.itemView.context, position)
                }
                .setNegativeButton(holder.itemView.context.getString(R.string.no), null)
                .show()
        }

        holder.playIcon.setOnClickListener {
            navigate.invoke(videoFile)
        }


    }

    override fun getItemCount(): Int = videoList.size

    private fun deleteImage(contentUri: Uri, context: Context, position: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val rowsDeleted = context.contentResolver?.delete(contentUri, null, null) ?: 0
                if (rowsDeleted > 0) {
                    videoList.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(
                        context,
                        context.getString(R.string.video_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_delete_video),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val isFileDeleted = File(contentUri.path ?: "").delete()
            if (isFileDeleted) {
                videoList.removeAt(position)
                notifyDataSetChanged()
            }
        }
    }


}

