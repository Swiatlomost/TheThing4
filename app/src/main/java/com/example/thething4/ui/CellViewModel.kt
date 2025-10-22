package com.example.thething4.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thething4.core.cell.CellLifecycle
import com.example.thething4.core.cell.CellLifecycleStateMachine
import com.example.thething4.core.cell.CellSnapshot
import com.example.thething4.core.time.TimeProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CellUiState(
    val cells: List<CellSnapshot> = emptyList(),
    val now: Duration = ZERO
)

class CellViewModel(
    private val timeProvider: TimeProvider,
    private val stateMachine: CellLifecycleStateMachine,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val tickDelayMillis: Long = 32L
) : ViewModel() {

    private val lifecycles: List<CellLifecycle> =
        listOf(CellLifecycle.default(timeProvider.now()))

    private val _uiState = MutableStateFlow(CellUiState())
    val uiState: StateFlow<CellUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            while (isActive) {
                val now = timeProvider.now()
                val snapshots = lifecycles.map { lifecycle ->
                    CellSnapshot(
                        lifecycle = lifecycle,
                        stage = stateMachine.evaluate(lifecycle, now)
                    )
                }

                _uiState.value = CellUiState(
                    cells = snapshots,
                    now = now
                )
                delay(tickDelayMillis)
            }
        }
    }
}

class CellViewModelFactory(
    private val timeProvider: TimeProvider,
    private val stateMachine: CellLifecycleStateMachine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CellViewModel::class.java)) {
            return CellViewModel(
                timeProvider = timeProvider,
                stateMachine = stateMachine
            ) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
