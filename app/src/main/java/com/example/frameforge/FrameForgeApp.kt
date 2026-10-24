package com.example.frameforge

import android.app.Application
import com.example.frameforge.log.AppLogger

class FrameForgeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        AppLogger.i("LIFECYCLE", "FrameForgeApp application launched successfully")
    }
}
