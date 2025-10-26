package com.example.cos.lifecycle.di

import com.example.cos.lifecycle.morpho.MorphoFormChannel
import com.example.cos.lifecycle.morpho.SharedMorphoFormChannel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MorphoFormChannelModule {

    @Binds
    @Singleton
    abstract fun bindMorphoFormChannel(
        impl: SharedMorphoFormChannel
    ): MorphoFormChannel
}
