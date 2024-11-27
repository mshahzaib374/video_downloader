package com.example.a4kvideodownloaderplayer.ads.advert

import android.os.Bundle
import androidx.annotation.Size
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Category
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Level
import com.example.a4kvideodownloaderplayer.ads.utils.logger.Logger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun firebaseAnalytics(@Size(min = 1, max = 40) eventId: String, eventName: String) {
    CoroutineScope(Dispatchers.IO).launch {

        try {
            val analytics = Firebase.analytics

            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, eventName)
            }

            analytics.logEvent(eventId, bundle)

        } catch (e: Exception) {
            Logger.log(Level.ERROR, Category.General, "error logging firebase event:${e.message}")
        }

    }
}