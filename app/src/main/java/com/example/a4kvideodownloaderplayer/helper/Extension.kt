package com.example.a4kvideodownloaderplayer.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.fragments.home.model.PopularVideo
import com.example.a4kvideodownloaderplayer.fragments.howToUse.model.ImageSlider
import java.io.File

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun Context.shareFile(contentUri: Uri, fileType : String = "video/mp4") {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = fileType
        putExtra(Intent.EXTRA_STREAM, Uri.parse(contentUri.toString()))
    }
    // Grant permission to the receiving app to access the URI
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(Intent.createChooser(shareIntent, "Share Video"))
}


fun Activity.moreApps() {
    try {
        this.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.more_app_link)))
        )
    } catch (ex: Exception) {
        ex.printStackTrace()


    }
}

fun Activity.privacyPolicyUrl() {
    try {
        this.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.privacy_policy_link)))
        )
    } catch (ex: Exception) {
        ex.printStackTrace()

    }
}

fun Activity.termsUrl() {
    try {
        this.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.terms_of_use_link)))
        )
    } catch (ex: Exception) {
        ex.printStackTrace()

    }
}

fun Activity.shareApp() {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_SUBJECT, this.getString(R.string.app_name)
        )
        var shareMessage = "\nLet me recommend you this application\n\n"
        shareMessage = """
                ${shareMessage}https://play.google.com/store/apps/details?id=${this.packageName}
                """.trimIndent()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        this.startActivity(Intent.createChooser(shareIntent, "choose one"))
    } catch (e: Exception) {
        e.printStackTrace()
    }


}

fun Activity.feedback() {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.feedback_email)))
            putExtra(Intent.EXTRA_SUBJECT, "App Feedback")
        }
        startActivity(intent)
    } catch (_: Exception) {
    }

}

fun showTrendingVideos(): MutableList<PopularVideo> {
    return mutableListOf<PopularVideo>().apply {
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid1.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid2.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid3.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid4.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid5.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid6.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid7.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid8.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid9.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid10.mp4"))
    }
}

fun showExitVideos(): MutableList<PopularVideo> {
    return mutableListOf<PopularVideo>().apply {

        add(PopularVideo("http://51.20.108.109:8080/downloads/vid11.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid12.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid13.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid14.mp4"))
        add(PopularVideo("http://51.20.108.109:8080/downloads/vid15.mp4"))
    }
}

fun showHowToUseImages(): MutableList<ImageSlider> {
    return mutableListOf<ImageSlider>().apply {
        add(ImageSlider(R.drawable.use1))
        add(ImageSlider(R.drawable.use2))
        add(ImageSlider(R.drawable.use3))
        add(ImageSlider(R.drawable.use4))
        add(ImageSlider(R.drawable.use5))
        add(ImageSlider(R.drawable.use6))
        add(ImageSlider(R.drawable.use7))
    }
}

fun downloadDirectory(): File {
    return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "4kVideoDownloader")
}


 fun Context.scanFiles(file : File) {
    if (file.exists()) {
        MediaScannerConnection.scanFile(
            this,
            arrayOf(file.absolutePath),
            null
        ) { path, uri ->
            Log.d("MediaScan", "Scanned $path -> URI = $uri")
        }
    }
}