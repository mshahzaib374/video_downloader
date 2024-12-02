package com.example.a4kvideodownloaderplayer.helper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.fragments.langugage.model.Languages
import com.google.android.exoplayer2.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.ktx.analytics


object AppUtils {

    var isComingFromSettingLanguage = false

    fun getDefaultLanguages(): List<Languages> {
        return listOf(
            Languages(R.drawable.america_logo, "English", "en"),
            Languages(R.drawable.spanish_logo, "Spanish", "es"),
            Languages(R.drawable.chinese_logo, "Chinese", "zh"),
            Languages(R.drawable.french_logo, "French", "fr"),
            Languages(R.drawable.arabic_logo, "Arabic", "ar"),
            Languages(R.drawable.korean_logo, "Korean", "ko"),
            Languages(R.drawable.german_logo, "German", "de"),
            Languages(R.drawable.turkish_logo, "Turkish", "tr"),
            Languages(R.drawable.indonesian_logo, "Indonesian", "in"),
            Languages(R.drawable.malay_logo, "Malay", "ms"),
            Languages(R.drawable.russian_logo, "Russian", "ru"),
            Languages(R.drawable.india, "Hindi", "hi"),
            Languages(R.drawable.vietname, "Vietnamese", "vi"),
            Languages(R.drawable.italian, "Italian", "in"),
            Languages(R.drawable.thai, "Thai", "th"),
            Languages(R.drawable.portagal, "Portuguese", "pt"),
        )
    }



    fun Context.logFirebaseEvent(screenName: String, action: String) {
        val analytics = Firebase.analytics
        val bundle = Bundle()
        bundle.putString("screen_name", screenName)
        bundle.putString("action", action)
        analytics.logEvent("user_action_event", bundle)
        Log.d("events_firebase", "Event logged: user_action_event, screen_name: $screenName, action: $action")
    }


    fun Context.checkForInAppUpdate(activity : Activity) {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        // Check if an update is available
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Start the update process
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    1000
                )
            }
        }
    }

}