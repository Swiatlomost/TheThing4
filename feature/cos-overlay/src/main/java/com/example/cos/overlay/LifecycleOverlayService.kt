package com.example.cos.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cos.designsystem.theme.CosTheme
import com.example.cos.lifecycle.CosLifecycleCanvas
import com.example.cos.lifecycle.CosLifecycleEngine
import com.example.cos.lifecycle.CosLifecycleState
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class LifecycleOverlayService : android.app.Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: FrameLayout? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val engine: CosLifecycleEngine by lazy {
        EntryPointAccessors.fromApplication(applicationContext, OverlayEntryPoint::class.java).engine()
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createOverlay()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private fun createOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 120
            y = 120
        }
        layoutParams = params

        overlayView = FrameLayout(this).apply {
            val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
                setContent {
                    val lifecycleState by engine.state.collectAsState()
                    CosTheme {
                        OverlayContent(
                            state = lifecycleState,
                            onDrag = { dx, dy -> updatePosition(dx, dy) },
                            onDismiss = { launchApp(); stopSelf() }
                        )
                    }
                }
            }
            addView(composeView)
        }
        windowManager.addView(overlayView, params)
    }

    private fun updatePosition(dx: Float, dy: Float) {
        val params = layoutParams ?: return
        params.x += dx.toInt()
        params.y += dy.toInt()
        windowManager.updateViewLayout(overlayView, params)
    }

    private fun launchApp() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }

    @Composable
    private fun OverlayContent(
        state: CosLifecycleState,
        onDrag: (Float, Float) -> Unit,
        onDismiss: () -> Unit
    ) {
        CosLifecycleCanvas(
            state = state,
            modifier = Modifier
                .size(160.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consumeAllChanges()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { onDismiss() })
                }
        )
    }

    private fun buildNotification(): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Cos overlay active")
        .setSmallIcon(android.R.drawable.ic_menu_info_details)
        .setOngoing(true)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Cos Overlay", NotificationManager.IMPORTANCE_LOW)
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "cos_overlay"
        private const val NOTIFICATION_ID = 42
    }
}
