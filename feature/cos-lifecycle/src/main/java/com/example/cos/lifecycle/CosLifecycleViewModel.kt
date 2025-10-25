package com.example.cos.lifecycle

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CosLifecycleViewModel @Inject constructor(
    private val engine: CosLifecycleEngine
) : ViewModel() {

    val state: StateFlow<CosLifecycleState> = engine.state

    fun resetOrganism() =
        engine.apply(LifecycleAction.Reset)

    fun setStage(command: LifecycleStageCommand) =
        engine.apply(LifecycleAction.SetStage(command))

    fun createChild() =
        engine.apply(LifecycleAction.CreateChild)
}
