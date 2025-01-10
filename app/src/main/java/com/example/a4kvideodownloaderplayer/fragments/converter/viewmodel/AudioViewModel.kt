package com.example.a4kvideodownloaderplayer.fragments.converter.viewmodel

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4kvideodownloaderplayer.fragments.converter.model.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AudioViewModel() : ViewModel() {

    private val _audioFiles = MutableLiveData<List<AudioFile>>()
    val audioFiles: LiveData<List<AudioFile>> get() = _audioFiles

    fun loadAudioFiles(context: Context) {
        viewModelScope.launch {
            _audioFiles.postValue(getAudioFiles(context))
        }
    }


    private suspend fun getAudioFiles(context: Context): List<AudioFile> =
        withContext(Dispatchers.IO) {
            val audioFiles = mutableListOf<AudioFile>()
            val folderName = "4kVideoDownloader"

            try {
                val downloadsPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                val targetFolderPath = "$downloadsPath/$folderName"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val projection = arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.RELATIVE_PATH,
                        MediaStore.Audio.Media.DATE_ADDED,
                        MediaStore.Audio.Media.DURATION
                    )
                    val selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
                    val selectionArgs = arrayOf("%$folderName%")

                    context.contentResolver?.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val nameColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                        val pathColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
                        val dateColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                        val durationColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)


                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val fileName = cursor.getString(nameColumn)
                            val filePath =
                                "$downloadsPath/${cursor.getString(pathColumn)}/$fileName"
                            val durationInMillis = cursor.getLong(durationColumn)

                            // Fix for duplicate "Download" in path
                            val filePath2 =
                                if (cursor.getString(pathColumn).startsWith("Download/")) {
                                    "$downloadsPath/${
                                        cursor.getString(pathColumn).substringAfter("Download/")
                                    }/$fileName"
                                } else {
                                    "$downloadsPath/$cursor.getString(pathColumn)/$fileName"
                                }

                            val contentUri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                id
                            )


                            audioFiles.add(
                                AudioFile(
                                    id,
                                    contentUri,
                                    fileName,
                                    filePath,
                                    cursor.getLong(dateColumn)
                                )
                            )
                        }
                    }
                } else {
                    val folder = File(targetFolderPath)
                    if (folder.exists()) {
                        folder.listFiles { file -> file.extension == "mp3" }?.forEach { file ->
                            audioFiles.add(
                                AudioFile(
                                    0L,
                                    Uri.fromFile(file),
                                    file.name,
                                    file.absolutePath,
                                    file.lastModified()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioRepository", "Error fetching audio files", e)
            }

            audioFiles.sortedByDescending { it.dateModified }
        }



}

