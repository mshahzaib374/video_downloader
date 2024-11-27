package com.example.a4kvideodownloaderplayer.fragments.home.retrofit

import com.example.a4kvideodownloaderplayer.fragments.home.model.ApiResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VideoApiService {
    @POST("api/download-video")
    fun downloadVideo(
        @Body requestBody: RequestBody
    ): Call<ApiResponse>
}