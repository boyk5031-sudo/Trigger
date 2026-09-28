package com.trigger.overlay

import android.app.Application
import rikka.shizuku.ShizukuProvider

class TriggerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Shizuku provider init via manifest, no extra setup needed
    }
}
