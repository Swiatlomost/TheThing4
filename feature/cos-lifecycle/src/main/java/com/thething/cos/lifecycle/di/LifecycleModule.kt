package com.thething.cos.lifecycle.di

import com.thething.cos.lifecycle.CosLifecycleEngine
import com.thething.cos.lifecycle.DefaultCosLifecycleEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LifecycleModule {

    @Binds
    abstract fun bindEngine(impl: DefaultCosLifecycleEngine): CosLifecycleEngine
}
