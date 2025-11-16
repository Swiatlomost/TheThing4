package com.thething.cos.morphogenesis

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents a single cell within a morpho form.
 * The [center] coordinates use the editor space; scaling to other spaces is handled by the consumer.
 */
data class MorphoCell(
    val id: String,
    val center: Offset,
    val radius: Float,
    val traits: Map<String, String> = emptyMap()
)

/**
 * Definition of a saved morpho form.
 */
data class MorphoForm(
    val id: String,
    val name: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val cells: List<MorphoCell>,
    val metadata: Map<String, String> = emptyMap(),
    val isActive: Boolean = false
)

interface MorphoFormRepository {
    val savedForms: StateFlow<List<MorphoForm>>

    suspend fun upsert(form: MorphoForm)

    suspend fun markActive(formId: String)

    suspend fun delete(formId: String)

    suspend fun clear()
}

@Singleton
class InMemoryMorphoFormRepository @Inject constructor() : MorphoFormRepository {

    private val mutex = Mutex()
    private val forms = MutableStateFlow<List<MorphoForm>>(emptyList())

    override val savedForms: StateFlow<List<MorphoForm>> = forms.asStateFlow()

    override suspend fun upsert(form: MorphoForm) {
        mutex.withLock {
            forms.update { current ->
                val updated = current.toMutableList()
                val index = updated.indexOfFirst { it.id == form.id }
                if (index >= 0) {
                    updated[index] = form
                } else {
                    updated += form
                }
                updated.sortedByDescending { it.updatedAtMillis }
            }
        }
    }

    override suspend fun markActive(formId: String) {
        mutex.withLock {
            var found = false
            forms.update { current ->
                current.map { form ->
                    when {
                        form.id == formId -> {
                            found = true
                            if (form.isActive) form else form.copy(isActive = true, updatedAtMillis = now())
                        }
                        form.isActive -> form.copy(isActive = false)
                        else -> form
                    }
                }.sortedByDescending { it.updatedAtMillis }
            }
            if (!found) {
                // no-op when form not present
            }
        }
    }

    override suspend fun delete(formId: String) {
        mutex.withLock {
            forms.update { current ->
                current.filterNot { it.id == formId }
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            forms.value = emptyList()
        }
    }

    private fun now(): Long = System.currentTimeMillis()
}

