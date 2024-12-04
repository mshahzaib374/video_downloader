package com.example.a4kvideodownloaderplayer.fragments.home.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VideoApiService {

    data class VideoRequest(
        val videoUrl: String
    )

    @POST("download-video-content")
    fun downloadVideo(
        @Body request: VideoRequest
    ): Call<ResponseBody>
}