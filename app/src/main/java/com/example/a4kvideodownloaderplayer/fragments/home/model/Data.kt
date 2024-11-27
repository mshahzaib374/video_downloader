package com.example.a4kvideodownloaderplayer.fragments.home.model


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("description")
    val description: Any,
    @SerializedName("downloads")
    val downloads: List<Download>,
    @SerializedName("source")
    val source: String,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String
)