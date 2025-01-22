package com.example.a4kvideodownloaderplayer.fragments.downloaded.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.RecoverableSecurityException
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
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAd
import com.example.a4kvideodownloaderplayer.databinding.DownloadedFragmentBinding
import com.example.a4kvideodownloaderplayer.dialogs.ConverterDialog
import com.example.a4kvideodownloaderplayer.fragments.converter.viewmodel.AudioViewModel
import com.example.a4kvideodownloaderplayer.fragments.downloaded.model.VideoFile
import com.example.a4kvideodownloaderplayer.fragments.downloaded.views.adapter.VideoAdapter
import com.example.a4kvideodownloaderplayer.fragments.howToUse.views.UseDialogFragment
import com.example.a4kvideodownloaderplayer.fragments.main.viewmodel.HomeViewModel
import com.example.a4kvideodownloaderplayer.fragments.premium.PremiumFragment
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.AudioConverter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DownloadedFragment : Fragment() {

    private var videoAdapter: VideoAdapter? = null
    private var binding: DownloadedFragmentBinding? = null
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val audioViewModel: AudioViewModel by activityViewModels()

    private var converterDialog: ConverterDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        converterDialog = ConverterDialog(activity ?: return)

        recoverableIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Refresh the adapter after permission is granted
                    urii?.let {
                        val rowsDeleted = context?.contentResolver?.delete(it, null, null) ?: 0
                        if (rowsDeleted > 0) {
                            videoAdapter?.updateAdapter(pendingDeletePosition)
                            Toast.makeText(
                                context ?: return@let,
                                context?.getString(R.string.video_deleted),
                                Toast.LENGTH_SHORT
                            ).show()
                            getVideoFiles()
                        }

                    }

                }
            }
    }


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

            howUseIcon.setOnClickListener {
                context?.logFirebaseEvent("downloads_fragment", "how_use_button_clicked")
                UseDialogFragment().show(parentFragmentManager, "HowToUseFragment")
            }

            premiumIcon.setOnClickListener {
                PremiumFragment().show(parentFragmentManager, "DownloadedFragment")

            }
        }

        getVideoFiles()

        homeViewModel.pageSelector.observe(viewLifecycleOwner) {
            if (it == 2) {
                getVideoFiles()
            }
        }


        OpenAppAd.adEventListener = object : OpenAppAd.Companion.AdEventListener {
            override fun onAdShown() {
                videoAdapter?.toggleAds(false)

            }

            override fun onAdDismissed() {
                videoAdapter?.toggleAds(true)
            }

        }


    }

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
                MediaStore.Video.Media.DATE_ADDED
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
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)


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
            videoAdapter = VideoAdapter(
                activity?.application ?: return,
                context ?: return,
                videosList,
                videoDeleted = {
                    getVideoFiles()
                },
                navigate = {
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
                },
                videoDeletedRecovery = { e, i, u -> handleRecoverableException(e, i, u) },
                mp3Converter = {
                    viewLifecycleOwner.lifecycleScope.launch {
                        converterDialog?.show()
                        delay(2000)
                        convertToAudio(it.contentUri)
                    }
                }
            )
            binding?.rv?.adapter = videoAdapter
            binding?.progressBar?.visibility = View.GONE
            binding?.noVideoTv?.visibility = View.GONE
        } else {
            binding?.progressBar?.visibility = View.GONE
            binding?.noVideoTv?.visibility = View.VISIBLE
        }
    }

    private var pendingDeletePosition: Int = -1
    private var urii: Uri? = null

    @SuppressLint("NewApi")
    private fun handleRecoverableException(
        e: RecoverableSecurityException,
        position: Int,
        uri: Uri
    ) {
        pendingDeletePosition = position
        urii = uri
        val intentSender = e.userAction.actionIntent.intentSender
        val request = IntentSenderRequest.Builder(intentSender).build()
        recoverableIntentLauncher.launch(request)
    }

    private lateinit var recoverableIntentLauncher: ActivityResultLauncher<IntentSenderRequest>


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


    private fun convertToAudio(videoUri: Uri) {
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).absolutePath

        // Create a subfolder named "4kVideoDownloader"
        val subFolder = File(downloadsFolder, "4kVideoDownloader")
        if (!subFolder.exists()) {
            subFolder.mkdirs()
        }

        val timestamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName =
            "audio_$timestamp.mp3" // Replace with `uniqueName.mp4` for UUID option
        // Create the file in the subfolder
        val file = File(
            subFolder,
            fileName
        )

        AudioConverter(context?:return)
            .extractAudio(
                getRealPathFromURI(videoUri, context?:return) ?: "",
                file.path,
                onSuccess = {
                    converterDialog?.dismiss()
                    audioViewModel.loadAudioFiles(context?:return@extractAudio)
                    homeViewModel.updatePageSelector(1)

                },
                onFailed = {
                    if (file.exists()) {
                        file.delete()
                    }
                    converterDialog?.dismiss()
                },
                above2Min = {
                    if (file.exists()) {
                        file.delete()
                    }
                    converterDialog?.dismiss()
                },
                noAudioFound = {
                    if (file.exists()) {
                        file.delete()
                    }
                    converterDialog?.dismiss()
                }
            )

    }

    fun getRealPathFromURI(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex =  returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val file = File(context.cacheDir, name)
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream?.available() ?: 0
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream?.close()
            outputStream.close()

        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        return file.path
    }
}

