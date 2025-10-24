package com.example.cos.lifecycle.di

import com.example.cos.lifecycle.CosLifecycleRepository
import com.example.cos.lifecycle.DefaultCosLifecycleRepository
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
    abstract fun bindRepository(impl: DefaultCosLifecycleRepository): CosLifecycleRepository

    @Binds
    abstract fun bindTimeProvider(impl: DefaultTimeProvider): TimeProvider
}
