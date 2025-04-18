package com.example.a4kvideodownloaderplayer.fragments.downloaded.views.adapter

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdCallback
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdItemsModel
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdUtils
import com.example.a4kvideodownloaderplayer.ads.native_ads.ad_types.NativeAdType
import com.example.a4kvideodownloaderplayer.ads.utils.Admobify
import com.example.a4kvideodownloaderplayer.ads.utils.AdmobifyUtils
import com.example.a4kvideodownloaderplayer.databinding.NativeAdLayoutBinding
import com.example.a4kvideodownloaderplayer.databinding.RecyclerviewNativeAdBinding
import com.example.a4kvideodownloaderplayer.fragments.downloaded.model.VideoFile
import com.example.a4kvideodownloaderplayer.helper.gone
import com.example.a4kvideodownloaderplayer.helper.shareFile
import com.google.android.gms.ads.LoadAdError
import java.io.File


class VideoAdapter(
    private val application: Application,
    private val context: Context,
    private val videoList: MutableList<VideoFile>,
    private val navigate: (VideoFile) -> Unit,
    private val videoDeleted: () -> Unit,
    private val mp3Converter: (VideoFile) -> Unit,
    private val videoDeletedRecovery: (RecoverableSecurityException, Int, Uri) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_VIDEO = 0
    private val VIEW_TYPE_AD = 1
    private var showAds: Boolean = true


    // Adjusted list with ad placeholders
    private val combinedList = mutableListOf<Any>()

    init {
        prepareList()
    }

    fun toggleAds(show: Boolean) {
        showAds = show
        prepareList()
        notifyDataSetChanged()
    }

    private fun prepareList() {
        combinedList.clear()
        for ((index, video) in videoList.withIndex()) {
            combinedList.add(video)
            if (showAds && index == 3 && !Admobify.isPremiumUser() && AdmobifyUtils.isNetworkAvailable(context)) {
                combinedList.add("AD_PLACEHOLDER")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (combinedList[position] is VideoFile) VIEW_TYPE_VIDEO else VIEW_TYPE_AD
    }

    // Video ViewHolder
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        private val playIcon: ImageView = itemView.findViewById(R.id.playIcon)
        private val shareImageView: ImageView = itemView.findViewById(R.id.shareIv)
        private val deleteImageView: ImageView = itemView.findViewById(R.id.deleteIv)
        private val mp3Iv: ImageView = itemView.findViewById(R.id.mp3Iv)

        fun bind(videoFile: VideoFile) {
            fileNameTextView.text = videoFile.fileName
            thumbnailImageView.setImageBitmap(videoFile.thumbnail)

            mp3Iv.setOnClickListener {
                mp3Converter.invoke(videoFile)
            }

            shareImageView.setOnClickListener {
                context.shareFile(videoFile.contentUri, "video/mp4")
            }

            deleteImageView.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.delete_video))
                    .setMessage(context.getString(R.string.are_you_sure_you_want_to_delete_this_video))
                    .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                        deleteImage(videoFile.contentUri, adapterPosition)
                    }
                    .setNegativeButton(context.getString(R.string.no), null)
                    .show()
            }

            playIcon.setOnClickListener {
                navigate.invoke(videoFile)
            }
        }
    }



    // Ad ViewHolder
    inner class AdViewHolder(
        private val binding: RecyclerviewNativeAdBinding,
        private val nativeBind: NativeAdLayoutBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            val nativeAdModel = NativeAdItemsModel(
                nativeBind.root,
                adHeadline = nativeBind.tvHeadline,
                adBody = nativeBind.tvBody,
                adIcon = nativeBind.appIcon,
                mediaView = nativeBind.mediaView,
                adCTA = nativeBind.adCTA
            )
            NativeAdUtils().loadNativeAd(
                application,
                context.getString(R.string.languageNativeAd),
                adRemote = true,
                binding.root,
                nativeAdModel,
                object : NativeAdCallback() {
                    override fun adFailed(error: LoadAdError?) {
                        binding.root.gone()
                        binding.nativeContainer.gone()
                        binding.shimmerHomeLayout.root.gone()
                    }

                    override fun adLoaded() {
                        binding.root.visibility
                    }

                    override fun adValidate() {
                        Log.e("TAG", "adValidate" )
                        binding.root.gone()
                        binding.nativeContainer.gone()
                        binding.shimmerHomeLayout.root.gone()
                    }
                },
                NativeAdType.DEFAULT_AD
            )



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_VIDEO) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_video, parent, false)
            VideoViewHolder(view)
        } else {
            Log.d("TAG", "onCreateViewHolder: AD VIEW")
            val binding = RecyclerviewNativeAdBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            val adBind = NativeAdLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AdViewHolder(binding, adBind)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VideoViewHolder) {
            val video = combinedList[position] as VideoFile
            holder.bind(video)
        } else if (holder is AdViewHolder) {
            holder.bind()
        }
    }

    override fun getItemCount(): Int = combinedList.size

    private fun deleteImage(contentUri: Uri, position: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val rowsDeleted = context.contentResolver?.delete(contentUri, null, null) ?: 0
                if (rowsDeleted > 0) {
                    combinedList.removeAt(position)
                    videoList.removeAt(position / 5 * 4) // Adjust index for original list
                    notifyDataSetChanged()
                    videoDeleted.invoke()
                    Toast.makeText(
                        context,
                        context.getString(R.string.video_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            catch (e : RecoverableSecurityException){
                videoDeletedRecovery.invoke(e, position, contentUri)

            }
        } else {
            val isFileDeleted = File(contentUri.path ?: "").delete()
            if (isFileDeleted) {
                videoDeleted.invoke()
            }
        }
    }

    fun updateAdapter(position: Int){
        combinedList.removeAt(position)
        videoList.removeAt(position / 5 * 4)
        notifyDataSetChanged()
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        (recyclerView.layoutManager as? GridLayoutManager)?.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == VIEW_TYPE_AD) {
                        // Make ad span 2 columns
                        2
                    } else {
                        // Make regular items span 1 column
                        1
                    }
                }
            }
    }

}



/*
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

*/
