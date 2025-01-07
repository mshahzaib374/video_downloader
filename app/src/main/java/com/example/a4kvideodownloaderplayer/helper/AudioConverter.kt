package com.example.a4kvideodownloaderplayer.helper

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

class AudioConverter {

    fun extractAudio(
        inputFilePath: String,
        outputFilePath: String,
        noAudioFound: () -> Unit,
        above2Min: () -> Unit,
        onFailed: () -> Unit,
        onSuccess: () -> Unit,
    ) {
        try {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(inputFilePath)

            // Check duration
            val durationUs = getDuration(mediaExtractor)
            val maxDurationUs = 2 * 60 * 1_000_000L // 2 minutes in microseconds
            if (durationUs > maxDurationUs) {
                above2Min.invoke()
                return
            }

            val mediaMuxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val trackCount = mediaExtractor.trackCount
            var audioTrackIndex = -1
            var maxInputSize = 0

            for (i in 0 until trackCount) {
                val trackFormat = mediaExtractor.getTrackFormat(i)
                if (isAudioTrack(trackFormat)) {
                    val maxInputSizeFromThisTrack = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    if (maxInputSizeFromThisTrack > maxInputSize) {
                        maxInputSize = maxInputSizeFromThisTrack
                    }
                    mediaExtractor.selectTrack(i)
                    audioTrackIndex = mediaMuxer.addTrack(trackFormat)
                }
            }

            if (audioTrackIndex == -1) {
                noAudioFound.invoke()
                return
            }

            val inputBuffer = ByteBuffer.allocate(maxInputSize)
            val bufferInfo = MediaCodec.BufferInfo()
            mediaMuxer.start()

            while (true) {
                val isInputBufferEnd = getInputBufferFromExtractor(mediaExtractor, inputBuffer, bufferInfo)
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
                onFailed.invoke()
                return
            }else{
                onSuccess.invoke()
                return
            }
        }catch (e: Exception) {
            onFailed.invoke()
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
            false
        } finally {
            mediaExtractor.release()
        }
    }
}
