package com.example.thething4.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.thething4.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object OverlayController {
    private val _state = MutableStateFlow(false)
    val state: StateFlow<Boolean> = _state

    private const val PERMISSION_ACTION = Settings.ACTION_MANAGE_OVERLAY_PERMISSION

    fun start(context: Context) {
        if (_state.value) return
        if (!Settings.canDrawOverlays(context)) {
            requestPermission(context)
            return
        }
        val intent = Intent(context, LifecycleOverlayService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        if (!_state.value) return
        val intent = Intent(context, LifecycleOverlayService::class.java)
        context.stopService(intent)
    }

    fun launchApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)
    }

    internal fun notifyRunning(running: Boolean) {
        _state.value = running
    }

    private fun requestPermission(context: Context) {
        val intent = Intent(context, OverlayPermissionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)
    }

    internal fun openSystemPermission(context: Context) {
        val intent = Intent(PERMISSION_ACTION, Uri.parse("package:${context.packageName}"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)
    }
}
