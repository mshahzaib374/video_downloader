package com.example.a4kvideodownloaderplayer.fragments.home.model


import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("data")
    val `data`: Data
)