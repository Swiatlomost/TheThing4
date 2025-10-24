package com.example.cos.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CosLifecycleViewModel @Inject constructor(
    private val engine: CosLifecycleEngine
) : ViewModel() {

    val state: StateFlow<CosLifecycleState> =
        engine.state.stateIn(viewModelScope, SharingStarted.Eagerly, engine.state.value)

    fun pauseCycle() = engine.pause()
    fun resumeCycle() = engine.resume()
}
