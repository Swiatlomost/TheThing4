package com.thething.cos.morphogenesis

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thething.cos.lifecycle.CellStage
import com.thething.cos.lifecycle.CellSnapshot
import com.thething.cos.lifecycle.CosLifecycleEngine
import com.thething.cos.lifecycle.CosLifecycleState
import com.thething.cos.lifecycle.morpho.ActiveMorphoCell
import com.thething.cos.lifecycle.morpho.ActiveMorphoForm
import com.thething.cos.lifecycle.morpho.ActiveMorphoForm.Companion.BASE_FORM_ID
import com.thething.cos.lifecycle.morpho.MorphoFormChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineDispatcher
import com.thething.cos.morphogenesis.di.MorphogenesisIoDispatcher
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import javax.inject.Inject

@HiltViewModel
class MorphogenesisViewModel @Inject constructor(
    private val engine: CosLifecycleEngine,
    private val formRepository: MorphoFormRepository,
    private val eventDispatcher: MorphoEventDispatcher,
    private val morphoFormChannel: MorphoFormChannel,
    @MorphogenesisIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val editorDraft = MutableStateFlow(createInitialDraft(engine.state.value))

    val state: StateFlow<MorphogenesisUiState> =
        combine(
            engine.state,
            formRepository.savedForms,
            editorDraft
        ) { lifecycleState, forms, draft ->
            val readyDraft = draft.ensureInitialized(lifecycleState)
            mapToUiState(lifecycleState, readyDraft, forms)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = mapToUiState(
                engine.state.value,
                editorDraft.value,
                formRepository.savedForms.value
            )
        )

    init {
        emitBaseForm(engine.state.value)
    }

    fun addCell() {
        mutateDraft { draft, lifecycleState ->
            val availablePool = lifecycleState.maturedCells().filterNot { candidate ->
                draft.cells.any { it.id == candidate.id }
            }
            val candidate = availablePool.firstOrNull() ?: return@mutateDraft draft
            val radius = lifecycleState.cellRadius
            val newCells = draft.cells
                .map { it.copy(isSelected = false) }
                .plus(
                    EditorCellState(
                        id = candidate.id,
                        center = candidate.center,
                        radius = radius,
                        stageLabel = candidate.stage.label,
                        isSelected = true
                    )
                )
            draft.copy(
                cells = newCells,
                selectedCellId = candidate.id,
                radiusSliderValue = radius,
                hasDirtyChanges = true
            )
        }
    }

    fun selectForm(formId: String) {
        val lifecycleState = engine.state.value
        if (formId == BASE_FORM_ID) {
            editorDraft.value = createInitialDraft(lifecycleState)
            return
        }
        val form = formRepository.savedForms.value.firstOrNull { it.id == formId } ?: return
        editorDraft.value = createDraftFromForm(form, lifecycleState)
    }

    fun selectCell(cellId: String) {
        mutateDraft { draft, _ ->
            if (draft.cells.none { it.id == cellId }) return@mutateDraft draft
            val updatedCells = draft.cells.map { cell ->
                cell.copy(isSelected = cell.id == cellId)
            }
            val selectedRadius = updatedCells.firstOrNull { it.id == cellId }?.radius
                ?: draft.radiusSliderValue
            draft.copy(
                cells = updatedCells,
                selectedCellId = cellId,
                radiusSliderValue = selectedRadius
            )
        }
    }

    fun removeSelectedCell() {
        mutateDraft { draft, lifecycleState ->
            val selected = draft.selectedCellId ?: return@mutateDraft draft
            val filtered = draft.cells.filterNot { it.id == selected }
            val newSelectedId = filtered.lastOrNull()?.id
            draft.copy(
                cells = filtered.map { cell ->
                    cell.copy(isSelected = cell.id == newSelectedId)
                },
                selectedCellId = newSelectedId,
                radiusSliderValue = newSelectedId?.let { id ->
                    filtered.firstOrNull { it.id == id }?.radius ?: lifecycleState.cellRadius
                } ?: lifecycleState.cellRadius,
                hasDirtyChanges = true
            )
        }
    }

    fun moveCell(cellId: String, newCenter: Offset) {
        mutateDraft { draft, lifecycleState ->
            val index = draft.cells.indexOfFirst { it.id == cellId }
            if (index < 0) return@mutateDraft draft
            val updatedCells = draft.cells.toMutableList()
            val current = updatedCells[index]
            val limitRadius = computeBoundaryLimit(draft, lifecycleState)
            val maxDistance = (limitRadius - current.radius).coerceAtLeast(0f)
            val clampedCenter = clampToCircle(newCenter, maxDistance, current.center)
            updatedCells[index] = current.copy(center = clampedCenter, isSelected = true)
            draft.copy(
                cells = updatedCells,
                selectedCellId = cellId,
                radiusSliderValue = updatedCells[index].radius,
                hasDirtyChanges = true
            )
        }
    }

    fun updateSelectedRadius(value: Float) {
        mutateDraft { draft, _ ->
            val clampedValue = value.coerceIn(draft.radiusSliderRange)
            val selected = draft.selectedCellId ?: return@mutateDraft draft.copy(
                radiusSliderValue = clampedValue
            )
            val updatedCells = draft.cells.map { cell ->
                if (cell.id == selected) cell.copy(radius = clampedValue) else cell
            }
            draft.copy(
                cells = updatedCells,
                radiusSliderValue = clampedValue,
                hasDirtyChanges = true
            )
        }
    }

    fun saveDraft() {
        val lifecycleState = engine.state.value
        val draft = editorDraft.value.ensureInitialized(lifecycleState)
        val validation = validateDraft(draft, lifecycleState)
        if (!validation.isValid || draft.cells.isEmpty()) {
            return
        }
        if (draft.formId == null && !draft.hasDirtyChanges) {
            emitBaseForm(lifecycleState)
            return
        }
        val timestamp = System.currentTimeMillis()
        val existingForms = formRepository.savedForms.value
        val preparedDraft = draft.prepareForPersistence(existingForms, timestamp)
        editorDraft.value = preparedDraft
        viewModelScope.launch {
            val persisted = withContext(ioDispatcher) {
                persistDraft(
                    draft = preparedDraft,
                    lifecycleState = lifecycleState,
                    activate = false,
                    timestamp = timestamp,
                    existingForms = existingForms
                )
            }
            if (
                persisted.name != preparedDraft.formName ||
                persisted.createdAtMillis != preparedDraft.formCreatedAtMillis
            ) {
                editorDraft.update { current ->
                    current.copy(
                        formName = persisted.name,
                        formCreatedAtMillis = persisted.createdAtMillis
                    )
                }
            }
        }
    }

    fun activateDraft() {
        val lifecycleState = engine.state.value
        val draft = editorDraft.value.ensureInitialized(lifecycleState)
        val validation = validateDraft(draft, lifecycleState)
        if (!validation.isValid || draft.cells.isEmpty()) {
            return
        }
        if (draft.formId == null && !draft.hasDirtyChanges) {
            emitBaseForm(lifecycleState)
            return
        }
        val timestamp = System.currentTimeMillis()
        val existingForms = formRepository.savedForms.value
        val preparedDraft = draft.prepareForPersistence(existingForms, timestamp)
        editorDraft.value = preparedDraft
        viewModelScope.launch {
            val persisted = withContext(ioDispatcher) {
                persistDraft(
                    draft = preparedDraft,
                    lifecycleState = lifecycleState,
                    activate = true,
                    timestamp = timestamp,
                    existingForms = existingForms
                )
            }
            eventDispatcher.emitActivation(persisted.id, preparedDraft.cells)
            emitActiveForm(
                formId = persisted.id,
                formName = persisted.name,
                lifecycleState = lifecycleState,
                cells = preparedDraft.cells
            )
            if (
                persisted.name != preparedDraft.formName ||
                persisted.createdAtMillis != preparedDraft.formCreatedAtMillis
            ) {
                editorDraft.update { current ->
                    current.copy(
                        formName = persisted.name,
                        formCreatedAtMillis = persisted.createdAtMillis
                    )
                }
            }
        }
    }

    private fun mutateDraft(
        mutator: (EditorDraft, CosLifecycleState) -> EditorDraft
    ) {
        val lifecycleState = engine.state.value
        editorDraft.update { current ->
            val ready = current.ensureInitialized(lifecycleState)
            mutator(ready.deepCopy(), lifecycleState)
        }
    }

    private fun EditorDraft.prepareForPersistence(
        existingForms: List<MorphoForm>,
        timestamp: Long
    ): EditorDraft {
        val nextFormId = formId ?: "FORM-$timestamp"
        val nextFormName = formName.ifBlank { defaultFormName(existingForms) }
        val nextCreatedAt = formCreatedAtMillis ?: timestamp
        return copy(
            formId = nextFormId,
            formName = nextFormName,
            formCreatedAtMillis = nextCreatedAt,
            hasDirtyChanges = false
        )
    }

    private suspend fun persistDraft(
        draft: EditorDraft,
        lifecycleState: CosLifecycleState,
        activate: Boolean,
        timestamp: Long,
        existingForms: List<MorphoForm>
    ): MorphoForm {
        val formId = draft.formId ?: "FORM-$timestamp"
        val formName = draft.formName.ifBlank { defaultFormName(existingForms) }
        val createdAt = draft.formCreatedAtMillis ?: timestamp
        val persisted = MorphoForm(
            id = formId,
            name = formName,
            createdAtMillis = createdAt,
            updatedAtMillis = timestamp,
            cells = draft.cells.map { it.toMorphoCell() },
            metadata = mapOf(
                "level_tag" to levelTagFor(draft.cells.size),
                "cell_count" to draft.cells.size.toString(),
                "radius" to lifecycleState.cellRadius.toString()
            ),
            isActive = activate
        )
        formRepository.upsert(persisted)
        if (activate) {
            formRepository.markActive(formId)
        }
        return persisted
    }

    private fun mapToUiState(
        lifecycleState: CosLifecycleState,
        draft: EditorDraft,
        forms: List<MorphoForm>
    ): MorphogenesisUiState {
        val maturedCells = lifecycleState.maturedCells()
        val totalCells = maturedCells.size
        val cellCount = draft.cells.size
        val available = (totalCells - cellCount).coerceAtLeast(0)
        val ring = determineRing(totalCells.coerceAtLeast(1))
        val nextCapacity = capacityForRing(ring + 1)
        val alert = buildCellsAlert(available, totalCells)
        val baseFormSummary = MorphogenesisFormSummary(
            id = BASE_FORM_ID,
            name = "Forma 0 (baza)",
            cellsCount = lifecycleState.cells.size,
            status = FormStatus.Active,
            isBase = true
        )
        val savedSummaries = forms.map { form ->
            MorphogenesisFormSummary(
                id = form.id,
                name = form.name,
                cellsCount = form.cells.size,
                status = if (form.isActive) FormStatus.Active else FormStatus.Draft
            )
        }
        val activeSaved = savedSummaries.firstOrNull { it.status == FormStatus.Active }
        val formsForUi = buildList {
            add(
                baseFormSummary.copy(
                    status = if (activeSaved == null) FormStatus.Active else FormStatus.Draft
                )
            )
            addAll(savedSummaries)
        }
        val notes = if (savedSummaries.isNotEmpty()) {
            "Zapisanych form: ${savedSummaries.size}. Wybierz, aby aktywować lub kontynuować edycję szkicu."
        } else {
            "Zasoby organizmu: $totalCells dojrzalych komorek. Nastepny pierscien pomiesci do $nextCapacity."
        }
        val editorState = buildEditorState(lifecycleState, draft, totalCells, available)
        val activeFormId = activeSaved?.id ?: BASE_FORM_ID
        val activeFormName = activeSaved?.name ?: "Forma 0"
        return MorphogenesisUiState(
            levelTag = "Lv. ${ring + 1}",
            levelDescription = "Pierscien ${ring + 1} - dojrzale komorki $totalCells",
            availableCells = available,
            totalCells = totalCells,
            cellsAlert = alert,
            activeFormId = activeFormId,
            activeFormName = activeFormName,
            forms = formsForUi,
            editor = editorState,
            notes = notes
        )
    }

    private fun buildEditorState(
        lifecycleState: CosLifecycleState,
        draft: EditorDraft,
        totalCells: Int,
        available: Int
    ): MorphogenesisEditorState {
        val minRadius = max(MIN_RADIUS_FLOOR, lifecycleState.cellRadius * 0.5f)
        val maxRadius = lifecycleState.cellRadius * 1.5f
        val validation = validateDraft(draft, lifecycleState)
        val boundaryRadius = computeBoundaryLimit(draft, lifecycleState)
        val selectedCellRadius = draft.selectedCellId?.let { id ->
            draft.cells.firstOrNull { it.id == id }?.radius
        }
        val sliderValue = if (draft.radiusSliderValue == 0f) {
            selectedCellRadius ?: lifecycleState.cellRadius
        } else {
            draft.radiusSliderValue
        }
        return MorphogenesisEditorState(
            formId = draft.formId,
            formName = draft.formName,
            cells = draft.cells,
            selectedCellId = draft.selectedCellId,
            radiusSliderValue = sliderValue,
            radiusSliderRange = minRadius..maxRadius,
            boundaryRadius = boundaryRadius,
            canAddCell = available > 0 && draft.cells.size < totalCells,
            canSaveDraft = draft.cells.isNotEmpty() && draft.hasDirtyChanges && validation.isValid,
            canActivate = draft.cells.isNotEmpty() && validation.isValid,
            hasDirtyChanges = draft.hasDirtyChanges,
            validationMessage = validation.message
        )
    }

    private fun validateDraft(
        draft: EditorDraft,
        lifecycleState: CosLifecycleState
    ): DraftValidation {
        val cells = draft.cells
        if (cells.isEmpty()) {
            return DraftValidation(isValid = false, message = "Dodaj komorke, aby zapisac lub aktywowac forme.")
        }
        val limitRadius = computeBoundaryLimit(draft, lifecycleState)
        for (i in 0 until cells.lastIndex) {
            val first = cells[i]
            for (j in i + 1 until cells.size) {
                val second = cells[j]
                val distance = (first.center - second.center).getDistance()
                if (distance < first.radius + second.radius) {
                    return DraftValidation(
                        isValid = false,
                        message = "Kolizja komorek: ${first.id} oraz ${second.id}."
                    )
                }
            }
        }
        cells.forEach { cell ->
            val distanceFromOrigin = cell.center.getDistance()
            if (distanceFromOrigin + cell.radius > limitRadius) {
                return DraftValidation(
                    isValid = false,
                    message = "Komorka ${cell.id} wychodzi poza granice organizmu."
                )
            }
        }
        return DraftValidation.Valid
    }

    private fun EditorDraft.ensureInitialized(
        lifecycleState: CosLifecycleState
    ): EditorDraft {
        if (initialized) return this
        return createInitialDraft(lifecycleState)
    }

    private fun createInitialDraft(state: CosLifecycleState): EditorDraft {
        val minRadius = max(MIN_RADIUS_FLOOR, state.cellRadius * 0.5f)
        val maxRadius = state.cellRadius * 1.5f
        val cells = state.maturedCells().mapIndexed { index, snapshot ->
            val snapshotRadius = snapshot.radius.takeIf { it > 0f } ?: state.cellRadius
            EditorCellState(
                id = snapshot.id,
                center = snapshot.center,
                radius = snapshotRadius,
                stageLabel = snapshot.stage.label,
                isSelected = index == 0
            )
        }
        val selectedId = cells.firstOrNull()?.id
        val initialRadius = cells.firstOrNull()?.radius ?: state.cellRadius
        return EditorDraft(
            formId = null,
            formName = "",
            formCreatedAtMillis = null,
            cells = cells,
            selectedCellId = selectedId,
            radiusSliderValue = initialRadius,
            radiusSliderRange = minRadius..maxRadius,
            hasDirtyChanges = false,
            initialized = true
        )
    }

    private fun createDraftFromForm(
        form: MorphoForm,
        lifecycleState: CosLifecycleState
    ): EditorDraft {
        val minRadius = max(MIN_RADIUS_FLOOR, lifecycleState.cellRadius * 0.5f)
        val maxRadius = lifecycleState.cellRadius * 1.5f
        val cells = form.cells.mapIndexed { index, cell ->
            EditorCellState(
                id = cell.id,
                center = cell.center,
                radius = cell.radius,
                stageLabel = cell.traits["stage"]
                    ?: lifecycleState.cells.firstOrNull { it.id == cell.id }?.stage?.label
                    ?: CellStage.Mature.label,
                isSelected = index == 0
            )
        }
        val selectedId = cells.firstOrNull()?.id
        val radiusValue = cells.firstOrNull()?.radius ?: lifecycleState.cellRadius
        return EditorDraft(
            formId = form.id,
            formName = form.name,
            formCreatedAtMillis = form.createdAtMillis,
            cells = cells,
            selectedCellId = selectedId,
            radiusSliderValue = radiusValue,
            radiusSliderRange = minRadius..maxRadius,
            hasDirtyChanges = false,
            initialized = true
        )
    }

    private fun emitActiveForm(
        formId: String,
        formName: String,
        lifecycleState: CosLifecycleState,
        cells: List<EditorCellState>
    ) {
        val activeForm = ActiveMorphoForm(
            formId = formId,
            formName = formName,
            cellRadius = lifecycleState.cellRadius,
            cells = cells.map { cell ->
                ActiveMorphoCell(
                    id = cell.id,
                    center = cell.center,
                    radius = cell.radius,
                    stageLabel = cell.stageLabel
                )
            }
        )
        morphoFormChannel.tryEmit(activeForm)
    }

    private fun emitBaseForm(state: CosLifecycleState) {
        val baseForm = ActiveMorphoForm(
            formId = BASE_FORM_ID,
            formName = "Forma 0",
            cellRadius = state.cellRadius,
            cells = state.maturedCells().map { snapshot ->
                val snapshotRadius = snapshot.radius.takeIf { it > 0f } ?: state.cellRadius
                ActiveMorphoCell(
                    id = snapshot.id,
                    center = snapshot.center,
                    radius = snapshotRadius,
                    stageLabel = snapshot.stage.label
                )
            }
        )
        morphoFormChannel.tryEmit(baseForm)
    }

    private fun computeBoundaryLimit(
        draft: EditorDraft,
        lifecycleState: CosLifecycleState
    ): Float {
        val organismRing = determineRing(lifecycleState.cells.size.coerceAtLeast(1))
        val organismRadius = lifecycleState.cellRadius * (2 * organismRing + 1)
        val paddedRadius = organismRadius * BOUNDARY_MARGIN
        val maxCellRadius = draft.cells.maxOfOrNull { it.radius } ?: lifecycleState.cellRadius
        val relaxedRadius = maxCellRadius * (1f + MIN_CLAMP_FRACTION)
        return max(paddedRadius, relaxedRadius)
    }

    private fun clampToCircle(position: Offset, maxDistance: Float, fallback: Offset): Offset {
        if (maxDistance <= 0f) {
            return fallback
        }
        val distance = position.getDistance()
        if (distance == 0f || distance <= maxDistance) {
            return position
        }
        val scale = maxDistance / distance
        return Offset(position.x * scale, position.y * scale)
    }

    private fun buildCellsAlert(available: Int, capacity: Int): CellsAlert? {
        if (capacity <= 0) return null
        val warningThreshold = ceil(capacity * LOW_CAPACITY_THRESHOLD).toInt().coerceAtLeast(1)
        return when {
            available <= 0 -> CellsAlert(
                message = "Zapas komorek wyczerpany",
                severity = AlertSeverity.Critical
            )
            available <= warningThreshold -> CellsAlert(
                message = "Niski zapas komorek: $available",
                severity = AlertSeverity.Warning
            )
            else -> null
        }
    }

    private fun EditorCellState.toMorphoCell(): MorphoCell =
        MorphoCell(
            id = id,
            center = center,
            radius = radius,
            traits = mapOf("stage" to stageLabel)
        )

    private fun defaultFormName(existing: List<MorphoForm>): String {
        val nextIndex = existing.size + 1
        return "Forma $nextIndex"
    }

    private fun determineRing(count: Int): Int {
        if (count <= 1) return 0
        var ring = 0
        while (capacityForRing(ring) < count) {
            ring++
        }
        return ring
    }

    private fun capacityForRing(ring: Int): Int {
        if (ring <= 0) return 1
        return 1 + 3 * ring * (ring + 1)
    }

    private fun levelTagFor(cellCount: Int): String = "Lv.${determineRing(cellCount) + 1}"

    private data class EditorDraft(
        val formId: String?,
        val formName: String,
        val formCreatedAtMillis: Long?,
        val cells: List<EditorCellState>,
        val selectedCellId: String?,
        val radiusSliderValue: Float,
        val radiusSliderRange: ClosedFloatingPointRange<Float>,
        val hasDirtyChanges: Boolean,
        val initialized: Boolean
    )

    private data class DraftValidation(
        val isValid: Boolean,
        val message: String?
    ) {
        companion object {
            val Valid = DraftValidation(isValid = true, message = null)
        }
    }

    private fun EditorDraft.deepCopy(): EditorDraft = copy(
        cells = cells.map { it.copy() }
    )

    private fun CosLifecycleState.maturedCells(): List<CellSnapshot> =
        cells.filter { snapshot ->
            when (snapshot.stage) {
                CellStage.Mature,
                CellStage.Spawned -> true
                else -> false
            }
        }

    private operator fun Offset.minus(other: Offset): Offset = Offset(x - other.x, y - other.y)

    companion object {
        private const val LOW_CAPACITY_THRESHOLD = 0.1f
        private const val BOUNDARY_MARGIN = 1.05f
        private const val MIN_CLAMP_FRACTION = 0.25f
        private const val MIN_RADIUS_FLOOR = 0.1f
    }
}


