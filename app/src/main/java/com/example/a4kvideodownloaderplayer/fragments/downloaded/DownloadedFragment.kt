package com.example.a4kvideodownloaderplayer.fragments.downloaded

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.utils.Admobify
import com.example.a4kvideodownloaderplayer.databinding.DownloadedFragmentBinding
import com.example.a4kvideodownloaderplayer.fragments.downloaded.model.VideoFile
import com.example.a4kvideodownloaderplayer.fragments.downloaded.views.VideoAdapter
import com.example.a4kvideodownloaderplayer.fragments.premium.PremiumFragment
import java.io.File

class DownloadedFragment : Fragment() {

    private lateinit var videoAdapter: VideoAdapter
    private var binding: DownloadedFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DownloadedFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            rv.layoutManager = GridLayoutManager(context, 2)
            if (Admobify.isPremiumUser()) {
                premiumIcon.visibility = View.GONE
            } else {
                premiumIcon.visibility = View.VISIBLE
            }

            premiumIcon.setOnClickListener {
                PremiumFragment().show(parentFragmentManager, "DownloadedFragment")

            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        getVideoFiles()
    }


    /*private fun getVideoFiles() {
        binding?.progressBar?.visibility = View.VISIBLE
        val videoList = mutableListOf<VideoFile>()
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDirectory = File(downloadsDirectory, "4kVideoDownloader")
        if (targetDirectory.exists()) {
            val files = targetDirectory.listFiles { file -> file.extension == "mp4" }
            files?.forEach { file ->
                val thumbnail = ThumbnailUtils.createVideoThumbnail(
                    file.path,
                    MediaStore.Images.Thumbnails.MINI_KIND
                )
                videoList.add(VideoFile(file.name, file.path, thumbnail))
            }
        }
        if (videoList.isNotEmpty()) {
            videoAdapter = VideoAdapter(videoList)
            binding?.rv?.adapter = videoAdapter
            binding?.progressBar?.visibility = View.GONE
            binding?.noVideoTv?.visibility = View.GONE
        } else {
            binding?.progressBar?.visibility = View.GONE
            binding?.noVideoTv?.visibility = View.VISIBLE
        }


    }*/

    private fun getVideoFiles() {
        binding?.progressBar?.visibility = View.VISIBLE
        val videosList = mutableListOf<VideoFile>()
        val folderName = "4kVideoDownloader"
        val downloadsPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val targetFolderPath = "$downloadsPath/$folderName"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android Q and above
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.DATE_MODIFIED
            )
            val selection = "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("%$folderName%")

            val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            context?.contentResolver?.query(
                queryUri, projection, selection, selectionArgs, null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)


                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val fileName = cursor.getString(nameColumn)
                    val relativePath = cursor.getString(pathColumn)
                    val filePath = "$downloadsPath/$relativePath/$fileName"
                    val contentUri = ContentUris.withAppendedId(queryUri, id) // Add to video list
                    videosList.add(
                        VideoFile(
                            id,
                            contentUri,
                            fileName,
                            filePath,
                            generateVideoThumbnail(context ?: return, contentUri),
                            cursor.getLong(dateColumn)
                        )
                    )
                }

            }
        } else {
            // Below Android Q
            val folder = File(targetFolderPath)
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles { file ->
                    file.isFile && file.extension == "mp4" // Filter only video files
                }?.forEach { file ->
                    val fileName = file.name
                    val filePath = file.absolutePath

                    val thumbnail = try {
                        // Attempt to manually generate a thumbnail using ThumbnailUtils
                        ThumbnailUtils.createVideoThumbnail(
                            file.absolutePath,
                            MediaStore.Video.Thumbnails.MINI_KIND // Specify thumbnail kind
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "ThumbnailError",
                            "Failed to generate thumbnail for ${file.absolutePath}",
                            e
                        )
                        null
                    }
                    // Add to video list
                    videosList.add(
                        VideoFile(
                            0L,
                            Uri.fromFile(file),
                            fileName,
                            filePath,
                            thumbnail,
                            file.lastModified()
                        )
                    )
                }
            }
        }

        videosList.sortByDescending { it.dateModified }


        // Update the UI with the video list
        if (videosList.isNotEmpty()) {
            videoAdapter = VideoAdapter(videosList) {
                if (findNavController().currentDestination?.id == R.id.mainFragment) {
                    Bundle().apply {
                        putString("videoUri", it.contentUri.toString())
                        putString("videoName", it.fileName)
                        putString("videoPath", it.filePath)
                        findNavController().navigate(
                            R.id.action_mainFragment_to_VideoPlayerFragment,
                            this
                        )
                    }
                }
            }
            binding?.rv?.adapter = videoAdapter
            binding?.progressBar?.visibility = View.GONE
            binding?.noVideoTv?.visibility = View.GONE
        } else {
            binding?.progressBar?.visibility = View.GONE
            binding?.noVideoTv?.visibility = View.VISIBLE
        }
    }


    private fun generateVideoThumbnail(context: Context, contentUri: Uri): Bitmap? {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val cursor: Cursor? = contentResolver.query(
                contentUri,
                arrayOf(MediaStore.Video.Media._ID),
                null, null, null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val videoId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    val thumbnail: Bitmap? = MediaStore.Video.Thumbnails.getThumbnail(
                        contentResolver,
                        videoId,
                        MediaStore.Video.Thumbnails.MINI_KIND, // You can use FULL_KIND for a larger thumbnail
                        null
                    )
                    return thumbnail
                }
            }

            null

        } catch (e: Exception) {
            null
        }
    }
}

