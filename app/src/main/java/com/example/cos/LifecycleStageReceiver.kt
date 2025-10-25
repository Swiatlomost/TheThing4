package com.example.cos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.cos.lifecycle.CosLifecycleEngine
import com.example.cos.lifecycle.LifecycleAction
import com.example.cos.lifecycle.LifecycleStageCommand
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class LifecycleStageReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = resolveAction(intent) ?: return
        val applicationContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ReceiverEntryPoint::class.java
        )
        entryPoint.engine().apply(action)
        Log.i(TAG, "Applied $action via intent=${intent.action}")
    }

    private fun resolveAction(intent: Intent): LifecycleAction? {
        when (intent.action) {
            ACTION_SEED -> return LifecycleAction.Reset
            ACTION_BUD -> return LifecycleAction.SetStage(LifecycleStageCommand.BUD)
            ACTION_MATURE -> return LifecycleAction.SetStage(LifecycleStageCommand.MATURE)
        }
        return when (intent.action) {
            ACTION_SET_STAGE -> {
                val stageName = intent.getStringExtra(EXTRA_STAGE)?.uppercase()
                when (stageName) {
                    "SEED" -> LifecycleAction.Reset
                    "BUD", "NARODZINY" -> LifecycleAction.SetStage(LifecycleStageCommand.BUD)
                    "MATURE", "DOJRZEWANIE" -> LifecycleAction.SetStage(LifecycleStageCommand.MATURE)
                    else -> null
                }
            }
            ACTION_NEW_CELL -> LifecycleAction.CreateChild
            else -> null
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun engine(): CosLifecycleEngine
    }

    companion object {
        private const val TAG = "LifecycleStageReceiver"
        const val ACTION_SET_STAGE = "com.example.cos.action.SET_STAGE"
        const val ACTION_SEED = "com.example.cos.action.SEED"
        const val ACTION_BUD = "com.example.cos.action.NARODZINY"
        const val ACTION_MATURE = "com.example.cos.action.DOJRZEWANIE"
        const val ACTION_NEW_CELL = "com.example.cos.action.NOWA_KOMORKA"
        const val EXTRA_STAGE = "com.example.cos.extra.STAGE"
    }
}
