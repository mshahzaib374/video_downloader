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
        add(PopularVideo("https://videos.pond5.com/sexy-girl-dancing-sexy-man-footage-108406067_main_xxl.mp4"))
        add(PopularVideo("https://www.pond5.com/stock-footage/item/294913315-all-parts-sexy-woman-dancing-seductive-dance-twerking-dancer.mp4"))
    }
}

fun showExitVideos(): MutableList<PopularVideo> {
    return mutableListOf<PopularVideo>().apply {
        add(PopularVideo("https://videos.pond5.com/sexy-girl-dancing-sexy-man-footage-108406067_main_xxl.mp4"))
        add(PopularVideo("https://www.pond5.com/stock-footage/item/294913315-all-parts-sexy-woman-dancing-seductive-dance-twerking-dancer.mp4"))
    }
}