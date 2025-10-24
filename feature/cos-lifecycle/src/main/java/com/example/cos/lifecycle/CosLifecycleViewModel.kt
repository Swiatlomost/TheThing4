package com.example.cos.lifecycle

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CosLifecycleViewModel @Inject constructor(
    private val repository: CosLifecycleRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _state = mutableStateOf(repository.initialState())
    val state: State<CosLifecycleState> get() = _state

    private var tickJob: Job? = null

    init {
        resumeCycle()
    }

    fun pauseCycle() {
        tickJob?.cancel()
    }

    fun resumeCycle() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(timeProvider.tickMillis())
                _state.value = repository.advanceState(_state.value)
            }
        }
    }
}
