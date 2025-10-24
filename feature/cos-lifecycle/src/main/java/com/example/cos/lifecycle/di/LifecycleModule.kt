package com.example.cos.lifecycle.di

import com.example.cos.lifecycle.CosLifecycleEngine
import com.example.cos.lifecycle.DefaultCosLifecycleEngine
import com.example.cos.lifecycle.DefaultTimeProvider
import com.example.cos.lifecycle.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LifecycleModule {

    @Binds
    abstract fun bindEngine(impl: DefaultCosLifecycleEngine): CosLifecycleEngine

    @Binds
    abstract fun bindTimeProvider(impl: DefaultTimeProvider): TimeProvider
}
