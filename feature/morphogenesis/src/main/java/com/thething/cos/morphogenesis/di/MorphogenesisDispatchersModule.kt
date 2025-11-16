package com.thething.cos.morphogenesis.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MorphogenesisIoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object MorphogenesisDispatchersModule {
    @Provides
    @MorphogenesisIoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
