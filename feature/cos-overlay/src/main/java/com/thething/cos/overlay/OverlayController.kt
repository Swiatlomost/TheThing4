package com.thething.cos.overlay

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OverlayController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasPermission(): Boolean = Settings.canDrawOverlays(context)

    fun requestPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, intent, null)
    }

    fun startOverlayService() {
        val intent = Intent(context, LifecycleOverlayService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopOverlayService() {
        val intent = Intent(context, LifecycleOverlayService::class.java)
        context.stopService(intent)
    }
}
