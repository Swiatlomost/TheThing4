package com.thething.cos.morphogenesis

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MorphogenesisModule {
    @Binds
    fun bindMorphoEventDispatcher(
        impl: AndroidMorphoEventDispatcher
    ): MorphoEventDispatcher

    @Binds
    fun bindMorphoFormRepository(
        impl: InMemoryMorphoFormRepository
    ): MorphoFormRepository
}
