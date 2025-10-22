package com.example.thething4.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.thething4.core.cell.CellLifecycle
import com.example.thething4.core.cell.CellLifecycleCoordinator
import com.example.thething4.core.cell.CellLifecycleStateMachine
import com.example.thething4.core.cell.CellStage
import com.example.thething4.ui.OverlayCosLifecycleScreen
import com.example.thething4.ui.theme.CosTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class LifecycleOverlayService : LifecycleService(), SavedStateRegistryOwner {

    private val savedStateRegistryController: SavedStateRegistryController by lazy {
        SavedStateRegistryController.create(this)
    }





    private val serviceJob = SupervisorJob()
    private val overlayScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private val stageFlow = MutableStateFlow<CellStage?>(null)

    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var overlayView: FrameLayout

    private val timeProvider = CellLifecycleCoordinator.timeProvider
    private val stateMachine: CellLifecycleStateMachine = CellLifecycleCoordinator.stateMachine()
    private val cellLifecycle: CellLifecycle = CellLifecycleCoordinator.lifecycle()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        OverlayController.notifyRunning(true)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        layoutParams = createLayoutParams()
        overlayView = FrameLayout(this).apply {
            setBackgroundColor(0x00000000)
            setLifecycleTags(this)
        }

        val composeView = ComposeView(this).apply {
            setLifecycleTags(this)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                val lifecycleOwner = this@LifecycleOverlayService
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.lifecycle.compose.LocalLifecycleOwner provides lifecycleOwner
                ) {
                    CosTheme {
                        OverlayCosLifecycleScreen(
                            stageFlow = stageFlow,
                            onDrag = { dx, dy -> updatePosition(dx, dy) },
                            onDoubleTap = { OverlayController.launchApp(this@LifecycleOverlayService) }
                        )
                    }
                }
            }
        }
        overlayView.addView(composeView)
        windowManager.addView(overlayView, layoutParams)

        startForeground(NOTIFICATION_ID, buildNotification())
        startTicker()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        OverlayController.notifyRunning(false)
    }

    private fun startTicker() {
        overlayScope.launch {
            while (isActive) {
                val stage = stateMachine.evaluate(cellLifecycle, timeProvider.now())
                stageFlow.value = stage
                delay(32)
            }
        }
    }

    private fun updatePosition(dx: Float, dy: Float) {
        layoutParams.x += dx.roundToInt()
        layoutParams.y += dy.roundToInt()
        windowManager.updateViewLayout(overlayView, layoutParams)
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 120
        }
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cos overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cos zyje poza aplikacja")
            .setContentText("Tryb plywajacy jest aktywny")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private fun setLifecycleTags(view: View) {
        view.setTag(androidx.lifecycle.runtime.R.id.view_tree_lifecycle_owner, this)
        view.setTag(androidx.lifecycle.viewmodel.R.id.view_tree_view_model_store_owner, this)
        view.setTag(androidx.savedstate.R.id.view_tree_saved_state_registry_owner, this)
    }

companion object {
        private const val CHANNEL_ID = "cos_overlay_channel"
        private const val NOTIFICATION_ID = 2001
    }
}




























