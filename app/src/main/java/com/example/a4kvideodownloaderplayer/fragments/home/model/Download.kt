package com.example.a4kvideodownloaderplayer.fragments.home.model


import com.google.gson.annotations.SerializedName

data class Download(
    @SerializedName("ext")
    val ext: String,
    @SerializedName("format_id")
    val formatId: String,
    @SerializedName("size")
    val size: String,
    @SerializedName("url")
    val url: String
)