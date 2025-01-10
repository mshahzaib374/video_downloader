package com.example.a4kvideodownloaderplayer.fragments.converter.model

import android.net.Uri

data class AudioFile(
    val id: Long,
    val contentUri: Uri,
    val fileName: String,
    val filePath: String,
    val dateModified: Long
)
