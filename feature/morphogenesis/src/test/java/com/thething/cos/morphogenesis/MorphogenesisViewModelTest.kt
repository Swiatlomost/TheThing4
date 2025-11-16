package com.thething.cos.morphogenesis

import com.thething.cos.lifecycle.DefaultCosLifecycleEngine
import com.thething.cos.lifecycle.LifecycleAction
import com.thething.cos.lifecycle.LifecycleStageCommand
import com.thething.cos.lifecycle.morpho.ActiveMorphoForm
import com.thething.cos.lifecycle.morpho.MorphoFormChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MorphogenesisViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var engine: DefaultCosLifecycleEngine
    private lateinit var formRepository: InMemoryMorphoFormRepository
    private lateinit var eventRecorder: RecordingMorphoEventDispatcher
    private lateinit var morphoFormChannel: FakeMorphoFormChannel
    private lateinit var viewModel: MorphogenesisViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        engine = DefaultCosLifecycleEngine()
        formRepository = InMemoryMorphoFormRepository()
        eventRecorder = RecordingMorphoEventDispatcher()
        morphoFormChannel = FakeMorphoFormChannel()
        viewModel = MorphogenesisViewModel(engine, formRepository, eventRecorder, morphoFormChannel, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateExposesGuardRailDefaults() = runTest {
        val state = viewModel.state.value

        assertTrue(state.totalCells >= state.availableCells)
        assertEquals("FORM-0", state.activeFormId)
        assertEquals("Forma 0", state.activeFormName)
        assertTrue(state.forms.isNotEmpty())
        val base = state.forms.first { it.isBase }
        assertEquals(FormStatus.Active, base.status)
        assertNull(state.cellsAlert)
        assertTrue(state.editor.cells.isEmpty())
        assertNull(state.editor.formId)
        assertEquals("", state.editor.formName)
        assertFalse(state.editor.canAddCell)
        assertFalse(state.editor.hasDirtyChanges)
        assertFalse(state.editor.canSaveDraft)
        assertFalse(state.editor.canActivate)
        assertNotNull(morphoFormChannel.lastEmission)
        assertEquals(ActiveMorphoForm.BASE_FORM_ID, morphoFormChannel.lastEmission?.formId)
    }

    @Test
    fun saveDraftPersistsFormAndClearsDirtyFlag() = runTest {
        prepareMatureCell()
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
        val editorState = viewModel.state.value.editor
        assertFalse(editorState.canAddCell)
        assertFalse(editorState.hasDirtyChanges)
        assertFalse(editorState.canSaveDraft)
        assertEquals(saved.id, editorState.formId)
    }

    @Test
    fun activateDraftMarksFormActiveAndEmitsEvent() = runTest {
        prepareMatureCell()
        viewModel.addCell()
        viewModel.activateDraft()
        advanceUntilIdle()

        val forms = formRepository.savedForms.value
        assertEquals(1, forms.size)
        assertTrue(forms.first().isActive)
        assertTrue(eventRecorder.emitted)
        assertEquals(forms.first().id, eventRecorder.lastFormId)
        assertEquals(forms.first().id, morphoFormChannel.lastEmission?.formId)
    }

    @Test
    fun selectSavedFormLoadsDraft() = runTest {
        prepareMatureCell()
        viewModel.addCell()
        viewModel.saveDraft()
        advanceUntilIdle()

        val saved = formRepository.savedForms.value.first()
        viewModel.selectForm(saved.id)
        advanceUntilIdle()

        val editor = viewModel.state.value.editor
        assertEquals(saved.id, editor.formId)
        assertFalse(editor.hasDirtyChanges)
        assertTrue(editor.cells.isNotEmpty())
    }

    @Test
    fun selectBaseFormResetsDraft() = runTest {
        prepareMatureCell()
        viewModel.addCell()
        viewModel.saveDraft()
        advanceUntilIdle()

        viewModel.selectForm(ActiveMorphoForm.BASE_FORM_ID)
        advanceUntilIdle()

        val editor = viewModel.state.value.editor
        assertNull(editor.formId)
        assertFalse(editor.hasDirtyChanges)
    }

    @Test
    fun activateBaseFormEmitsBase() = runTest {
        prepareMatureCell()
        viewModel.selectForm(ActiveMorphoForm.BASE_FORM_ID)
        val before = morphoFormChannel.emissions.size
        viewModel.activateDraft()
        advanceUntilIdle()
        assertTrue(morphoFormChannel.emissions.size >= before)
        assertEquals(ActiveMorphoForm.BASE_FORM_ID, morphoFormChannel.lastEmission?.formId)
    }

    @Test
    fun removeSelectedCellClearsSelection() = runTest {
        prepareMatureCell()
        viewModel.addCell()
        advanceUntilIdle()

        viewModel.removeSelectedCell()
        advanceUntilIdle()

        val editorState = viewModel.state.value.editor
        assertNull(editorState.selectedCellId)
        assertTrue(editorState.cells.isEmpty())
    }

    private suspend fun TestScope.prepareMatureCell() {
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.MATURE))
        advanceUntilIdle()
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

    private class FakeMorphoFormChannel : MorphoFormChannel {
        private val _updates = MutableSharedFlow<ActiveMorphoForm>(replay = 1)
        private val recorded = mutableListOf<ActiveMorphoForm>()

        override val updates: SharedFlow<ActiveMorphoForm> = _updates.asSharedFlow()

        val lastEmission: ActiveMorphoForm? get() = recorded.lastOrNull()
        val emissions: List<ActiveMorphoForm> get() = recorded

        override suspend fun emit(form: ActiveMorphoForm) {
            recorded += form
            _updates.emit(form)
        }

        override fun tryEmit(form: ActiveMorphoForm): Boolean {
            val accepted = _updates.tryEmit(form)
            if (accepted) {
                recorded += form
            }
            return accepted
        }
    }
}




