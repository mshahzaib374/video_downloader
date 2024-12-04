package com.example.a4kvideodownloaderplayer.fragments.home.viewmodel


import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.fragments.home.retrofit.RetrofitInstance
import com.example.a4kvideodownloaderplayer.fragments.home.retrofit.VideoApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoViewModel : ViewModel() {

    private var downloadJob: Job? = null
    private var downloadManager: DownloadManager? = null
    private val _downloadStatus = MutableLiveData<String>()
    val downloadStatus: LiveData<String> get() = _downloadStatus

    private val _downloadProgress = MutableLiveData<Int>()
    val downloadProgress: LiveData<Int> get() = _downloadProgress

    private var downloadId: Long = 0

    fun resetDownloadStatus() {
        _downloadStatus.value = ""
    }

    fun downloadVideo(url: String, context: Context) {
        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            /* try {*/
            val videoRequest = VideoApiService.VideoRequest(videoUrl = url)
            RetrofitInstance.api.downloadVideo(videoRequest)
                .enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: retrofit2.Response<ResponseBody>
                    ) {
                        Log.e("TAG", "onResponse: $response")
                        if (response.isSuccessful) {
                                response.body()?.let { body ->
                                    saveFileFromResponse(body, context)
                                } ?: run {
                                    _downloadStatus.postValue("ERROR")
                                }

                        } else if(response.code() == 400) {
                            _downloadStatus.postValue("ERROR_SIZE")
                        }

                        else {
                            _downloadStatus.postValue("ERROR")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        _downloadStatus.postValue("ERROR")
                    }
                })
        }
    }

    private fun saveFileFromResponse(body: ResponseBody, context: Context) {
        try {
            // Get the Downloads directory
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Create a subfolder named "4kVideoDownloader"
            val subFolder = File(downloadsFolder, "4kVideoDownloader")
            if (!subFolder.exists()) {
                subFolder.mkdirs()
            }

            val timestamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName =
                "video_$timestamp.mp4" // Replace with `uniqueName.mp4` for UUID option
            // Create the file in the subfolder
            val file =
                File(subFolder, fileName) // You can modify the filename dynamically if needed

            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            _downloadStatus.postValue("SUCCESS")
        } catch (e: Exception) {
            // Handle exception during file saving
            _downloadStatus.postValue("ERROR")
        }
    }


    /*fun downloadVideo(url: String, context: Context) {
        viewModelScope.launch {
            try {
                val json = """{"videoURL": "$url"}"""
                val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
                val response: Call<ApiResponse> = RetrofitInstance.api.downloadVideo(
                    requestBody
                )
                response.enqueue(object : retrofit2.Callback<ApiResponse> {
                    override fun onResponse(
                        call: Call<ApiResponse>,
                        response: Response<ApiResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                downloadFileFromUrl(body.message, context)
                            } ?: run {
                                //ERROR
                                _downloadStatus.postValue("ERROR")
                            }
                        } else {
                            //ERROR
                            _downloadStatus.postValue("ERROR")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        //ERROR
                        _downloadStatus.postValue("ERROR")

                    }

                })

            } catch (e: Exception) {
                //ERROR
                _downloadStatus.postValue("ERROR")
            }
        }
    }*/


    fun downloadFileFromUrl(videoUrl: String, context: Context) {
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // Define the subfolder path
        val subfolderName = "4kVideoDownloader"
        val downloadFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            subfolderName
        )

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "video_$timestamp.mp4"

        // Create the subfolder if it doesn't exist
        if (!downloadFolder.exists()) {
            if (downloadFolder.mkdirs()) {
                Log.e("TAG", "download folder created")
            } else {
                Log.e("TAG", "download folder failed to create")
                return
            }
        }

        val request = DownloadManager.Request(Uri.parse(videoUrl)).apply {
            setTitle("${context.getString(R.string.downloading)} $fileName")
            setDescription(context.getString(R.string.wait_video_is_downloading))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$subfolderName/$fileName"
            )
        }
        downloadManager?.let {
            downloadId = it.enqueue(request)
            trackDownloadProgress(context, it, downloadId)
        }

    }

    fun cancelDownload() {
        //downloadManager?.remove(downloadId)
        downloadJob?.cancel()
    }


    private fun trackDownloadProgress(
        context: Context,
        downloadManager: DownloadManager,
        downloadId: Long
    ) {
        val handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val statusIndex =
                        cursor.getInt(
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS) ?: -1
                        )
                    val bytesDownloadedIndex = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            ?: -1
                    )
                    val totalBytesIndex = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES) ?: -1
                    )

                    if (statusIndex >= 0 && bytesDownloadedIndex >= 0 && totalBytesIndex >= 0) {
                        if (statusIndex == DownloadManager.STATUS_RUNNING && totalBytesIndex > 0) {
                            val progress =
                                (bytesDownloadedIndex * 100L / totalBytesIndex).toInt()
                            _downloadProgress.postValue(progress)
                        } else if (statusIndex == DownloadManager.STATUS_SUCCESSFUL) {
                            _downloadStatus.postValue("SUCCESS")
                            cursor.close()
                            return
                        } else if (statusIndex == DownloadManager.STATUS_FAILED) {
                            _downloadStatus.postValue("ERROR")
                            cursor.close()
                            return
                        }
                    }

                }
                cursor.close()
                handler.postDelayed(this, 1000)
            }
        })
    }


}
