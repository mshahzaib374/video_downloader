package com.example.a4kvideodownloaderplayer.helper

import android.app.Application
import com.google.firebase.FirebaseApp


class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }




}