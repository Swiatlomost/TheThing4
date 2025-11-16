package com.thething.cos.lifecycle.morpho

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveMorphoCell(
    val id: String,
    val center: Offset,
    val radius: Float,
    val stageLabel: String
)

data class ActiveMorphoForm(
    val formId: String,
    val formName: String,
    val cellRadius: Float,
    val cells: List<ActiveMorphoCell>
) {
    val isBaseForm: Boolean get() = formId == BASE_FORM_ID

    companion object {
        const val BASE_FORM_ID: String = "FORM-0"
    }
}

interface MorphoFormChannel {
    val updates: SharedFlow<ActiveMorphoForm>
    suspend fun emit(form: ActiveMorphoForm)
    fun tryEmit(form: ActiveMorphoForm): Boolean
}

@Singleton
class SharedMorphoFormChannel @Inject constructor() : MorphoFormChannel {
    private val _updates = MutableSharedFlow<ActiveMorphoForm>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val updates: SharedFlow<ActiveMorphoForm> = _updates.asSharedFlow()

    override suspend fun emit(form: ActiveMorphoForm) {
        _updates.emit(form)
    }

    override fun tryEmit(form: ActiveMorphoForm): Boolean = _updates.tryEmit(form)
}
