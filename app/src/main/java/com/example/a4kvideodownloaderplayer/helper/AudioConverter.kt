package com.example.a4kvideodownloaderplayer.helper

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.widget.Toast
import com.example.a4kvideodownloaderplayer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

class AudioConverter(private val context: Context) {

    fun extractAudio(
        inputFilePath: String,
        outputFilePath: String,
        noAudioFound: () -> Unit,
        above2Min: () -> Unit,
        onFailed: () -> Unit,
        onSuccess: () -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaExtractor = MediaExtractor()
                mediaExtractor.setDataSource(inputFilePath)

                // Check duration
                val durationUs = getDuration(mediaExtractor)
                val maxDurationUs = 20 * 60 * 1_000_000L // 5 minutes in microseconds
                if (durationUs > maxDurationUs) {
                    above2Min.invoke()
                    return@launch
                }

                val mediaMuxer =
                    MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val trackCount = mediaExtractor.trackCount
                var audioTrackIndex = -1
                var maxInputSize = 0

                for (i in 0 until trackCount) {
                    val trackFormat = mediaExtractor.getTrackFormat(i)
                    if (isAudioTrack(trackFormat)) {
                        val maxInputSizeFromThisTrack =
                            trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                        if (maxInputSizeFromThisTrack > maxInputSize) {
                            maxInputSize = maxInputSizeFromThisTrack
                        }
                        mediaExtractor.selectTrack(i)
                        audioTrackIndex = mediaMuxer.addTrack(trackFormat)
                    }
                }

                if (audioTrackIndex == -1) {
                    withContext(Dispatchers.Main) {
                        noAudioFound.invoke()
                        Toast.makeText(
                            context,
                            context.getString(R.string.audio_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val inputBuffer = ByteBuffer.allocate(maxInputSize)
                val bufferInfo = MediaCodec.BufferInfo()
                mediaMuxer.start()

                while (true) {
                    val isInputBufferEnd =
                        getInputBufferFromExtractor(mediaExtractor, inputBuffer, bufferInfo)
                    if (isInputBufferEnd) {
                        break
                    }
                    mediaMuxer.writeSampleData(audioTrackIndex, inputBuffer, bufferInfo)
                    mediaExtractor.advance()
                }

                mediaMuxer.stop()
                mediaMuxer.release()
                mediaExtractor.release()

                // Validate the conversion
                if (!isFileConverted(outputFilePath)) {
                    withContext(Dispatchers.Main) {
                        onFailed.invoke()
                        Toast.makeText(
                            context,
                            context.getString(R.string.audio_converted_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    return@launch
                } else {
                    context.scanFiles(File(outputFilePath))
                    withContext(Dispatchers.Main) {
                        onSuccess.invoke()
                        Toast.makeText(
                            context,
                            context.getString(R.string.audio_converted),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailed.invoke()
                    Toast.makeText(
                        context,
                        context.getString(R.string.audio_converted_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }
        }


    }

    private fun isAudioTrack(mediaFormat: MediaFormat): Boolean {
        val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
        return mime?.startsWith("audio/") ?: false
    }

    private fun getInputBufferFromExtractor(
        mediaExtractor: MediaExtractor,
        inputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ): Boolean {
        val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)
        if (sampleSize < 0) {
            return true
        }

        bufferInfo.size = sampleSize
        bufferInfo.presentationTimeUs = mediaExtractor.sampleTime
        bufferInfo.offset = 0
        bufferInfo.flags = mediaExtractor.sampleFlags

        return false
    }

    private fun getDuration(mediaExtractor: MediaExtractor): Long {
        for (i in 0 until mediaExtractor.trackCount) {
            val trackFormat = mediaExtractor.getTrackFormat(i)
            if (trackFormat.containsKey(MediaFormat.KEY_DURATION)) {
                return trackFormat.getLong(MediaFormat.KEY_DURATION)
            }
        }
        return 0L // Default if duration isn't available
    }

    private fun isFileConverted(outputFilePath: String): Boolean {
        val outputFile = File(outputFilePath)

        // Check if file exists
        if (!outputFile.exists()) {
            return false
        }

        // Check if file size is greater than 0
        if (outputFile.length() <= 0) {
            return false
        }

        // Validate format and duration
        val mediaExtractor = MediaExtractor()
        return try {
            mediaExtractor.setDataSource(outputFilePath)
            val duration = getDuration(mediaExtractor)
            duration > 0 // Ensure the file has valid duration
        } catch (e: Exception) {
            Log.e("Khan", "extractAudio: $e")

            false
        } finally {
            mediaExtractor.release()
        }
    }
}
