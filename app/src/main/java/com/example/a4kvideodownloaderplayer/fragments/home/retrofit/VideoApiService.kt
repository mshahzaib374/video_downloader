package com.example.a4kvideodownloaderplayer.fragments.home.retrofit

import com.example.a4kvideodownloaderplayer.fragments.home.model.ApiResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VideoApiService {

    data class VideoRequest(
        val videoUrl: String
    )

    @POST("download-video-content")
    fun downloadVideoNewApi(
        @Body request: VideoRequest
    ): Call<ResponseBody>


    @POST("download-video")
    fun downloadVideoOldApi(
        @Body requestBody: RequestBody
    ): Call<ApiResponse>
}