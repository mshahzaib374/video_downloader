package com.example.a4kvideodownloaderplayer.helper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.fragments.home.model.PopularVideo

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