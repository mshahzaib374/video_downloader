package com.example.a4kvideodownloaderplayer.fragments.home.viewmodel


import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4kvideodownloaderplayer.fragments.home.model.ApiResponse
import com.example.a4kvideodownloaderplayer.fragments.home.retrofit.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoViewModel : ViewModel() {

    private val _downloadStatus = MutableLiveData<String>()
    val downloadStatus: LiveData<String> get() = _downloadStatus

    private val _downloadProgress = MutableLiveData<Int>()
    val downloadProgress: LiveData<Int> get() = _downloadProgress

    fun downloadVideo(url: String) {
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
                                downloadFileFromUrl(body.message)
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
    }


    private fun downloadFileFromUrl(videoUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create URL object for the video download link
                // Start fake progress immediately with a fast increment

                val url = URL(videoUrl)
                val connection = url.openConnection()
                val fileSize = connection.contentLength
                val inputStream: InputStream = connection.getInputStream()

                // Directory to save the video
                val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDirectory = File(downloadsDirectory, "4kVideoDownloader")
                if (!targetDirectory.exists()) {
                    targetDirectory.mkdirs()
                }
                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName =
                    "video_$timestamp.mp4" // Replace with `uniqueName.mp4` for UUID option
                val file = File(targetDirectory, fileName)

                // Write the file
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                var totalBytesRead = 0

                var actualProgress = 15
                // Start downloading and updating the real progress
                inputStream.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            val progress = (totalBytesRead * 85 / fileSize)

                            // Update the actual download progress
                            //fakeProgressJob.cancel()
                            _downloadProgress.postValue(progress+actualProgress)
                        }
                    }
                }

                // Cancel the fake progress job as download is complete

                // SUCCESS
                _downloadStatus.postValue("SUCCESS")
            } catch (e: Exception) {
                // ERROR
                _downloadStatus.postValue("ERROR")
            }
        }
    }


}
