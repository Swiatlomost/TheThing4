package com.example.cos.morphogenesis

import com.example.cos.lifecycle.DefaultCosLifecycleEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MorphogenesisViewModelTest {

    private val engine = DefaultCosLifecycleEngine()
    private val viewModel = MorphogenesisViewModel(engine)

    @Test
    fun `initial state exposes guard rail defaults`() = runBlocking {
        val state = viewModel.state.first()

        assertTrue(state.totalCells >= state.availableCells)
        assertEquals("FORM-0", state.activeFormId)
        assertEquals("Forma 0", state.activeFormName)
        assertTrue(state.forms.isNotEmpty())
        val active = state.forms.first { it.id == state.activeFormId }
        assertEquals(FormStatus.Active, active.status)
    }
}
