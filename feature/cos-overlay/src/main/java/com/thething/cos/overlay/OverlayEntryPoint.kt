package com.thething.cos.overlay

import com.thething.cos.lifecycle.CosLifecycleEngine
import com.thething.cos.lifecycle.morpho.MorphoFormChannel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OverlayEntryPoint {
    fun engine(): CosLifecycleEngine
    fun morphoFormChannel(): MorphoFormChannel
}
