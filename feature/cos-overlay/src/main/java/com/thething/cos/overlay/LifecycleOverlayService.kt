package com.thething.cos.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.lifecycle.runtime.R as LifecycleRuntimeR
import androidx.lifecycle.viewmodel.R as LifecycleViewModelR
import androidx.savedstate.R as SavedStateR
import com.thething.cos.designsystem.theme.CosTheme
import com.thething.cos.lifecycle.CosLifecycleCanvas
import com.thething.cos.lifecycle.CellSnapshot
import com.thething.cos.lifecycle.CellStage
import com.thething.cos.lifecycle.CosLifecycleEngine
import com.thething.cos.lifecycle.CosLifecycleState
import com.thething.cos.lifecycle.morpho.ActiveMorphoForm
import com.thething.cos.lifecycle.morpho.MorphoFormChannel

import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class LifecycleOverlayService : android.app.Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: FrameLayout? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private lateinit var foregroundStarter: ForegroundStarter
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    private var overlayViewModelStoreOwner: OverlayViewModelStoreOwner? = null

    private val engine: CosLifecycleEngine by lazy {
        EntryPointAccessors.fromApplication(applicationContext, OverlayEntryPoint::class.java).engine()
    }

    private val morphoFormChannel: MorphoFormChannel by lazy {
        EntryPointAccessors.fromApplication(applicationContext, OverlayEntryPoint::class.java).morphoFormChannel()
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        foregroundStarter = overrideForegroundStarter ?: DefaultForegroundStarter
        createOverlay()
        createNotificationChannel()
        val notification = buildNotification()
        val useSpecialType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        foregroundStarter.start(this, NOTIFICATION_ID, notification, useSpecialType)
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

        if (disableOverlayForTests) {
            return
        }

        overlayLifecycleOwner = OverlayLifecycleOwner().also { it.onCreate() }
        overlayViewModelStoreOwner = OverlayViewModelStoreOwner()

        overlayView = FrameLayout(this).apply {
            overlayLifecycleOwner?.let { owner ->
                setTag(LifecycleRuntimeR.id.view_tree_lifecycle_owner, owner)
                setTag(SavedStateR.id.view_tree_saved_state_registry_owner, owner)
            }
            overlayViewModelStoreOwner?.let { vmOwner ->
                setTag(LifecycleViewModelR.id.view_tree_view_model_store_owner, vmOwner)
            }

            val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
                overlayLifecycleOwner?.let { owner ->
                    setTag(LifecycleRuntimeR.id.view_tree_lifecycle_owner, owner)
                    setTag(SavedStateR.id.view_tree_saved_state_registry_owner, owner)
                }
                overlayViewModelStoreOwner?.let { vmOwner ->
                    setTag(LifecycleViewModelR.id.view_tree_view_model_store_owner, vmOwner)
                }
                setContent {
                    val lifecycleState by engine.state.collectAsState()
                    val activeForm by morphoFormChannel.updates.collectAsState(initial = null)
                    val renderState = remember(lifecycleState, activeForm) {
                        val form = activeForm
                        if (form == null || form.isBaseForm) {
                            lifecycleState
                        } else {
                            form.toLifecycleState(lifecycleState)
                        }
                    }
                    CosTheme {
                        OverlayContent(
                            state = renderState,
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
                        change.consume()
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
        overlayLifecycleOwner?.onDestroy()
        overlayLifecycleOwner = null
        overlayViewModelStoreOwner?.clear()
        overlayViewModelStoreOwner = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        @JvmStatic
        @VisibleForTesting
        internal var overrideForegroundStarter: ForegroundStarter? = null

        @JvmStatic
        @VisibleForTesting
        internal var disableOverlayForTests: Boolean = false

        private const val CHANNEL_ID = "cos_overlay"
        private const val NOTIFICATION_ID = 42
    }

    internal interface ForegroundStarter {
        fun start(
            service: LifecycleOverlayService,
            id: Int,
            notification: Notification,
            useSpecialType: Boolean
        )
    }

    private object DefaultForegroundStarter : ForegroundStarter {
        override fun start(
            service: LifecycleOverlayService,
            id: Int,
            notification: Notification,
            useSpecialType: Boolean
        ) {
            if (useSpecialType) {
                service.startForeground(
                    id,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                service.startForeground(id, notification)
            }
        }
    }
}

private class OverlayLifecycleOwner : LifecycleOwner, androidx.savedstate.SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = registry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    fun onCreate() {
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        savedStateController.performSave(Bundle())
    }
}

private class OverlayViewModelStoreOwner : ViewModelStoreOwner {
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = store
    fun clear() = store.clear()
}


private fun ActiveMorphoForm.toLifecycleState(fallback: CosLifecycleState): CosLifecycleState {
    if (cells.isEmpty()) return fallback
    val snapshots = cells.map { cell ->
        CellSnapshot(
            id = cell.id,
            stage = cell.stageLabel.toCellStage(),
            center = cell.center,
            radius = if (cell.radius > 0f) cell.radius else fallback.cellRadius
        )
    }
    val radius = if (cellRadius > 0f) cellRadius else fallback.cellRadius
    return CosLifecycleState(
        cells = snapshots,
        cellRadius = radius
    )
}

private fun String.toCellStage(): CellStage = when (this) {
    CellStage.Seed.label -> CellStage.Seed
    CellStage.Bud.label -> CellStage.Bud
    CellStage.Mature.label -> CellStage.Mature
    CellStage.Spawned.label -> CellStage.Spawned
    else -> CellStage.Mature
}

