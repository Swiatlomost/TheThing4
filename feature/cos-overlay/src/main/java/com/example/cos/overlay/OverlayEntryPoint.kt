package com.example.cos.overlay

import com.example.cos.lifecycle.CosLifecycleEngine
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OverlayEntryPoint {
    fun engine(): CosLifecycleEngine
}
