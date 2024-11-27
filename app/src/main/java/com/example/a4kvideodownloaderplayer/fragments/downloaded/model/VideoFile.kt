package com.example.a4kvideodownloaderplayer.fragments.downloaded.model

import android.graphics.Bitmap
import android.net.Uri

data class VideoFile(
    val id: Long,
    val contentUri: Uri,
    val fileName: String,
    val filePath: String,
    val thumbnail: Bitmap?
)
