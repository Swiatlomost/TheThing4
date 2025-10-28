package com.example.cos.morphogenesis

import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface MorphoEventDispatcher {
    fun emitActivation(formId: String, cells: List<EditorCellState>)
}

@Singleton
class AndroidMorphoEventDispatcher @Inject constructor(
    @ApplicationContext private val context: Context
) : MorphoEventDispatcher {
    override fun emitActivation(formId: String, cells: List<EditorCellState>) {
        val timestamp = System.currentTimeMillis()
        val cellsHash = cells.joinToString(separator = "|") {
            "${it.id}:${it.center.x}:${it.center.y}:${it.radius}"
        }.hashCode().toUInt().toString(16)
        Log.i(
            TAG,
            "forma_aktywna formId=$formId cellsHash=$cellsHash timestamp=$timestamp"
        )
        val intent = Intent(ACTION_FORMA_AKTYWNA).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_FORM_ID, formId)
            putExtra(EXTRA_CELLS_HASH, cellsHash)
            putExtra(EXTRA_TIMESTAMP, timestamp)
        }
        context.sendBroadcast(intent, CONTROL_PERMISSION)
    }

    companion object {
        private const val TAG = "MorfoEvent"
        private const val CONTROL_PERMISSION = "com.example.cos.permission.CONTROL_LIFECYCLE"
        const val ACTION_FORMA_AKTYWNA = "com.example.cos.FORMA_AKTYWNA"
        const val EXTRA_FORM_ID = "form_id"
        const val EXTRA_CELLS_HASH = "cells_hash"
        const val EXTRA_TIMESTAMP = "timestamp"
    }
}
