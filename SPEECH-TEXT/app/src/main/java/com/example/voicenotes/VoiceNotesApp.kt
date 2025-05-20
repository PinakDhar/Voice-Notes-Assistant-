package com.example.voicenotes

import android.app.Application
import com.example.voicenotes.util.NetworkUtils
import com.example.voicenotes.util.PreferenceManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VoiceNotesApp : Application() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var networkUtils: NetworkUtils

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Hilt will handle injection of dependencies automatically
        // No need to manually initialize preferenceManager or networkUtils
    }
}