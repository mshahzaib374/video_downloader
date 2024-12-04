package com.example.a4kvideodownloaderplayer.fragments.home.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitInstance {
    private const val BASE_URL = "http://51.20.108.109:8080/api/"
    val api: VideoApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 30 seconds connect timeout
            .readTimeout(30, TimeUnit.SECONDS)    // 30 seconds read timeout
            .writeTimeout(30, TimeUnit.SECONDS)   // 30 seconds write timeout
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(VideoApiService::class.java)
    }
}

