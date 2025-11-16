package com.thething.cos.sensorharness.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SensorHarnessModule {
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()
}
