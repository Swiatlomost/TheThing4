package com.example.cos.morphogenesis

import com.example.cos.lifecycle.DefaultCosLifecycleEngine
import com.example.cos.lifecycle.LifecycleAction
import com.example.cos.lifecycle.LifecycleStageCommand
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MorphogenesisViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var engine: DefaultCosLifecycleEngine
    private lateinit var formRepository: InMemoryMorphoFormRepository
    private lateinit var eventRecorder: RecordingMorphoEventDispatcher
    private lateinit var viewModel: MorphogenesisViewModel

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        engine = DefaultCosLifecycleEngine()
        formRepository = InMemoryMorphoFormRepository()
        eventRecorder = RecordingMorphoEventDispatcher()
        viewModel = MorphogenesisViewModel(engine, formRepository, eventRecorder)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun initialStateExposesGuardRailDefaults() = runTest {
        val state = viewModel.state.first()

        assertTrue(state.totalCells >= state.availableCells)
        assertEquals("FORM-0", state.activeFormId)
        assertEquals("Forma 0", state.activeFormName)
        assertTrue(state.forms.isNotEmpty())
        val active = state.forms.first { it.id == state.activeFormId }
        assertEquals(FormStatus.Active, active.status)
        val alert = state.cellsAlert
        assertNull(alert)
        assertTrue(state.editor.cells.isEmpty())
        assertTrue(state.editor.radiusSliderRange.contains(state.editor.radiusSliderValue))
        assertFalse(state.editor.canAddCell)
        assertFalse(state.editor.hasDirtyChanges)
        assertFalse(state.editor.canSaveDraft)
        assertFalse(state.editor.canActivate)
    }

    @Test
    fun saveDraftPersistsFormAndClearsDirtyFlag() = runTest {
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.MATURE))
        advanceUntilIdle()
        viewModel.addCell()
        advanceUntilIdle()

        val initialRadius = engine.state.value.cellRadius
        viewModel.updateSelectedRadius(initialRadius * 0.8f)

        viewModel.saveDraft()
        advanceUntilIdle()

        val forms = formRepository.savedForms.value
        assertEquals(1, forms.size)
        val saved = forms.first()
        assertTrue(saved.cells.isNotEmpty())
        val editorState = viewModel.state.first().editor
        assertFalse(editorState.canAddCell)
        assertFalse(editorState.hasDirtyChanges)
        assertFalse(editorState.canSaveDraft)
    }

    @Test
    fun activateDraftMarksFormActiveAndEmitsEvent() = runTest {
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.MATURE))
        advanceUntilIdle()
        viewModel.addCell()
        viewModel.activateDraft()
        advanceUntilIdle()

        val forms = formRepository.savedForms.value
        assertEquals(1, forms.size)
        assertTrue(forms.first().isActive)
        assertTrue(eventRecorder.emitted)
        assertEquals(forms.first().id, eventRecorder.lastFormId)
    }

    private class RecordingMorphoEventDispatcher : MorphoEventDispatcher {
        var emitted: Boolean = false
            private set
        var lastFormId: String? = null
            private set

        override fun emitActivation(formId: String, cells: List<EditorCellState>) {
            emitted = true
            lastFormId = formId
        }
    }
}
