package com.example.thething4.observation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thething4.core.cell.CellLifecycleCoordinator

class ObservationViewModelFactory(
    private val repository: ObservationRepository = InMemoryObservationRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ObservationViewModel::class.java)) {
            val stateMachine = CellLifecycleCoordinator.stateMachine()
            val timeProvider = CellLifecycleCoordinator.timeProvider
            return ObservationViewModel(
                timeProvider = timeProvider,
                stateMachine = stateMachine,
                repository = repository
            ) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }

    companion object {
        fun default(): ObservationViewModelFactory = ObservationViewModelFactory()
    }
}
