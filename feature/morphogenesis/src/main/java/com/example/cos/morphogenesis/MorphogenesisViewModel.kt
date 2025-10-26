package com.example.cos.morphogenesis

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cos.lifecycle.CellStage
import com.example.cos.lifecycle.CellSnapshot
import com.example.cos.lifecycle.CosLifecycleEngine
import com.example.cos.lifecycle.CosLifecycleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import javax.inject.Inject

@HiltViewModel
class MorphogenesisViewModel @Inject constructor(
    private val engine: CosLifecycleEngine,
    private val formRepository: MorphoFormRepository,
    private val eventDispatcher: MorphoEventDispatcher
) : ViewModel() {

    private val editorDraft = MutableStateFlow(createInitialDraft(engine.state.value))
    private val history = EditorHistory(HISTORY_CAPACITY)

    init {
        history.clear()
    }

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
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT),
            initialValue = mapToUiState(
                engine.state.value,
                editorDraft.value,
                formRepository.savedForms.value
            )
        )

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
                hasDirtyChanges = true,
                statusMessage = null
            )
        }
    }

    fun selectCell(cellId: String) {
        mutateDraft(recordHistory = false) { draft, _ ->
            if (draft.cells.none { it.id == cellId }) return@mutateDraft draft
            val updatedCells = draft.cells.map { cell ->
                cell.copy(isSelected = cell.id == cellId)
            }
            val selectedRadius = updatedCells.firstOrNull { it.id == cellId }?.radius
                ?: draft.radiusSliderValue
            draft.copy(
                cells = updatedCells,
                selectedCellId = cellId,
                radiusSliderValue = selectedRadius,
                statusMessage = null
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
                hasDirtyChanges = true,
                statusMessage = null
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
                hasDirtyChanges = true,
                statusMessage = null
            )
        }
    }

    fun updateSelectedRadius(value: Float) {
        mutateDraft { draft, _ ->
            val clampedValue = value.coerceIn(draft.radiusSliderRange)
            val selected = draft.selectedCellId ?: return@mutateDraft draft.copy(
                radiusSliderValue = clampedValue,
                statusMessage = null
            )
            val updatedCells = draft.cells.map { cell ->
                if (cell.id == selected) cell.copy(radius = clampedValue) else cell
            }
            draft.copy(
                cells = updatedCells,
                radiusSliderValue = clampedValue,
                hasDirtyChanges = true,
                statusMessage = null
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
        viewModelScope.launch {
            val persisted = persistDraft(draft, lifecycleState, activate = false)
            editorDraft.update { current ->
                val ensured = current.ensureInitialized(lifecycleState)
                ensured.copy(
                    formId = persisted.id,
                    formName = persisted.name,
                    formCreatedAtMillis = persisted.createdAtMillis,
                    hasDirtyChanges = false,
                    statusMessage = "Szkic zapisany"
                )
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
        viewModelScope.launch {
            val persisted = persistDraft(draft, lifecycleState, activate = true)
            eventDispatcher.emitActivation(persisted.id, draft.cells)
            editorDraft.update { current ->
                val ensured = current.ensureInitialized(lifecycleState)
                ensured.copy(
                    formId = persisted.id,
                    formName = persisted.name,
                    formCreatedAtMillis = persisted.createdAtMillis,
                    hasDirtyChanges = false,
                    statusMessage = "Forma aktywowana"
                )
            }
        }
    }

    fun undo() {
        val lifecycleState = engine.state.value
        val current = editorDraft.value.ensureInitialized(lifecycleState)
        val previous = history.undo(current.deepCopy()) ?: return
        editorDraft.value = previous.copy(statusMessage = "Cofnieto zmiany", initialized = true)
    }

    fun redo() {
        val lifecycleState = engine.state.value
        val current = editorDraft.value.ensureInitialized(lifecycleState)
        val next = history.redo(current.deepCopy()) ?: return
        editorDraft.value = next.copy(statusMessage = "Przywrocono zmiany", initialized = true)
    }

    fun autosort() {
        mutateDraft { draft, lifecycleState ->
            if (draft.cells.size <= 1) return@mutateDraft draft.copy(statusMessage = "Brak kolizji do uporzadkowania")
            val layout = generateAutosortLayout(draft.cells.size, lifecycleState.cellRadius)
            val updatedCells = draft.cells
                .sortedBy { it.id }
                .mapIndexed { index, cell ->
                    cell.copy(center = layout.getOrElse(index) { cell.center })
                }
            draft.copy(
                cells = updatedCells.mapIndexed { index, cell ->
                    val source = draft.cells.firstOrNull { it.id == cell.id }
                    cell.copy(isSelected = source?.isSelected == true)
                },
                hasDirtyChanges = true,
                statusMessage = "Autosort zastosowany"
            )
        }
    }

    private fun mutateDraft(
        recordHistory: Boolean = true,
        mutator: (EditorDraft, CosLifecycleState) -> EditorDraft
    ) {
        val lifecycleState = engine.state.value
        editorDraft.update { current ->
            val ready = current.ensureInitialized(lifecycleState)
            val mutated = mutator(ready.deepCopy(), lifecycleState)
            if (recordHistory && mutated != ready) {
                history.push(ready.deepCopy())
            }
            mutated
        }
    }

    private suspend fun persistDraft(
        draft: EditorDraft,
        lifecycleState: CosLifecycleState,
        activate: Boolean
    ): MorphoForm {
        val now = System.currentTimeMillis()
        val forms = formRepository.savedForms.value
        val formId = draft.formId ?: "FORM-$now"
        val formName = draft.formName.ifBlank { defaultFormName(forms) }
        val createdAt = draft.formCreatedAtMillis ?: now
        val persisted = MorphoForm(
            id = formId,
            name = formName,
            createdAtMillis = createdAt,
            updatedAtMillis = now,
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
        history.clear()
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
            status = FormStatus.Active
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
            "Zapisanych form: ${savedSummaries.size}. Cofnij/Przywroc aby eksplorowac zmiany, autosort porzadkuje kolizje."
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
        return MorphogenesisEditorState(
            cells = draft.cells,
            selectedCellId = draft.selectedCellId,
            radiusSliderValue = if (draft.radiusSliderValue == 0f) lifecycleState.cellRadius else draft.radiusSliderValue,
            radiusSliderRange = minRadius..maxRadius,
            boundaryRadius = boundaryRadius,
            canAddCell = available > 0 && draft.cells.size < totalCells,
            canSaveDraft = draft.cells.isNotEmpty() && draft.hasDirtyChanges && validation.isValid,
            canActivate = draft.cells.isNotEmpty() && validation.isValid,
            hasDirtyChanges = draft.hasDirtyChanges,
            canUndo = history.canUndo(),
            canRedo = history.canRedo(),
            validationMessage = validation.message,
            infoMessage = draft.statusMessage
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
        val initial = createInitialDraft(lifecycleState)
        history.clear()
        return initial
    }

    private fun createInitialDraft(state: CosLifecycleState): EditorDraft {
        val minRadius = max(MIN_RADIUS_FLOOR, state.cellRadius * 0.5f)
        val maxRadius = state.cellRadius * 1.5f
        val cells = state.maturedCells().mapIndexed { index, snapshot ->
            EditorCellState(
                id = snapshot.id,
                center = snapshot.center,
                radius = state.cellRadius,
                stageLabel = snapshot.stage.label,
                isSelected = index == 0
            )
        }
        val selectedId = cells.firstOrNull()?.id
        return EditorDraft(
            formId = null,
            formName = "",
            formCreatedAtMillis = null,
            cells = cells,
            selectedCellId = selectedId,
            radiusSliderValue = state.cellRadius,
            radiusSliderRange = minRadius..maxRadius,
            hasDirtyChanges = false,
            statusMessage = null,
            initialized = true
        )
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

    private fun generateAutosortLayout(cellCount: Int, baseRadius: Float): List<Offset> {
        if (cellCount <= 0) return emptyList()
        val result = ArrayList<Offset>(cellCount)
        result += Offset.Zero
        if (cellCount == 1) return result
        var remaining = cellCount - 1
        var ring = 1
        while (remaining > 0) {
            val ringPositions = createRingPositions(ring, baseRadius)
            val take = min(remaining, ringPositions.size)
            for (i in 0 until take) {
                result += ringPositions[i]
            }
            remaining -= take
            ring++
        }
        return result
    }

    private fun createRingPositions(ringIndex: Int, radius: Float): List<Offset> {
        if (ringIndex <= 0) return listOf(Offset.Zero)
        val coords = ArrayList<HexCoord>(ringIndex * 6)
        var q = ringIndex
        var r = 0
        for (direction in HEX_DIRECTIONS) {
            repeat(ringIndex) {
                coords += HexCoord(q, r)
                q += direction.q
                r += direction.r
            }
        }
        return coords.map { coord ->
            val x = (2f * radius * coord.q) + (radius * coord.r)
            val y = SQRT_THREE * radius * coord.r
            Offset(x, y)
        }
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
        val statusMessage: String?,
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

    private data class HexCoord(val q: Int, val r: Int)

    private class EditorHistory(private val capacity: Int) {
        private val undoStack = ArrayDeque<EditorDraft>()
        private val redoStack = ArrayDeque<EditorDraft>()

        fun push(state: EditorDraft) {
            if (undoStack.size == capacity) {
                undoStack.removeFirst()
            }
            undoStack.addLast(state)
            redoStack.clear()
        }

        fun undo(current: EditorDraft): EditorDraft? {
            val previous = undoStack.removeLastOrNull() ?: return null
            redoStack.addLast(current)
            return previous
        }

        fun redo(current: EditorDraft): EditorDraft? {
            val next = redoStack.removeLastOrNull() ?: return null
            undoStack.addLast(current)
            return next
        }

        fun canUndo(): Boolean = undoStack.isNotEmpty()
        fun canRedo(): Boolean = redoStack.isNotEmpty()

        fun clear() {
            undoStack.clear()
            redoStack.clear()
        }
    }

    private fun ArrayDeque<EditorDraft>.removeLastOrNull(): EditorDraft? =
        if (isEmpty()) null else removeLast()

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
        private const val BASE_FORM_ID = "FORM-0"
        private const val LOW_CAPACITY_THRESHOLD = 0.1f
        private const val BOUNDARY_MARGIN = 1.05f
        private const val MIN_CLAMP_FRACTION = 0.25f
        private const val MIN_RADIUS_FLOOR = 0.1f
        private const val STATE_STOP_TIMEOUT = 5_000L
        private const val HISTORY_CAPACITY = 10
        private val HEX_DIRECTIONS = arrayOf(
            HexCoord(-1, 1),
            HexCoord(-1, 0),
            HexCoord(0, -1),
            HexCoord(1, -1),
            HexCoord(1, 0),
            HexCoord(0, 1)
        )
        private const val SQRT_THREE = 1.7320508f
    }
}

