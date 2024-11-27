package com.example.a4kvideodownloaderplayer.ads.advert

import android.app.Activity
import com.example.a4kvideodownloaderplayer.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigurations {

    fun fetchRemotes(
        activity: Activity,
        jsonKey: String,
        defaultXml: Int,
        fetchInterval:Long = 3600,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception?) -> Unit
    ) {

        FirebaseApp.initializeApp(activity)

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 60 else fetchInterval
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaultXml)

        remoteConfig.fetchAndActivate().addOnCompleteListener {

            try {
                if (it.isSuccessful) {
                    val response = remoteConfig[jsonKey].asString()
                    onSuccess(response)
                } else {
                    onFailure(it.exception)
                }
            } catch (e: Exception) {
                onFailure(e)
            }

        }

    }

}