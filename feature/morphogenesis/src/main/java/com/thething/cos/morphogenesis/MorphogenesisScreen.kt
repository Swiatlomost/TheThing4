package com.thething.cos.morphogenesis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import androidx.compose.ui.graphics.Color
import com.thething.cos.morphogenesis.AlertSeverity
import com.thething.cos.lifecycle.morpho.ActiveMorphoForm
import com.thething.cos.designsystem.components.NeonButton
import com.thething.cos.morphogenesis.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphogenesisScreen(
    state: MorphogenesisUiState,
    onBack: () -> Unit,
    onAddCell: () -> Unit = {},
    onSelectCell: (String) -> Unit = {},
    onMoveCell: (String, Offset) -> Unit = { _, _ -> },
    onRemoveSelectedCell: () -> Unit = {},
    onRadiusChange: (Float) -> Unit = {},
    onSaveDraft: () -> Unit = {},
    onActivate: () -> Unit = {},
    onSelectForm: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.morphogenesis_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(R.string.morphogenesis_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                HeaderStatusBar(state = state, onSelectForm = onSelectForm)
            }
            item {
                MorphogenesisEditor(
                    editorState = state.editor,
                    onAddCell = onAddCell,
                    onSelectCell = onSelectCell,
                    onMoveCell = onMoveCell,
                    onRemoveSelectedCell = onRemoveSelectedCell,
                    onRadiusChange = onRadiusChange,
                    onSaveDraft = onSaveDraft,
                    onActivate = onActivate
                )
            }
            if (state.forms.isNotEmpty()) {
                item {
                    Text(
                        text = "Ostatnio zapisane formy",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                val currentFormId = state.editor.formId ?: ActiveMorphoForm.BASE_FORM_ID
                items(state.forms) { form ->
                    val selected = if (form.isBase) {
                        currentFormId == ActiveMorphoForm.BASE_FORM_ID
                    } else {
                        form.id == state.editor.formId
                    }
                    FormRowCard(
                        summary = form,
                        selected = selected,
                        onSelect = { onSelectForm(form.id) }
                    )
                }
            }
            item {
                PlaceholderCard(message = state.notes)
            }
        }
    }
}

@Composable
private fun HeaderStatusBar(
    state: MorphogenesisUiState,
    onSelectForm: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val activeFormName = state.activeFormName
        ?: state.forms.firstOrNull { it.status == FormStatus.Active }?.name
        ?: "Brak aktywnej formy"
    val currentFormId = state.editor.formId ?: ActiveMorphoForm.BASE_FORM_ID

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = state.levelTag,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = state.levelDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Komorki ${state.availableCells}/${state.totalCells}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            painter = painterResource(
                                if (expanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            state.cellsAlert?.let { alert ->
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (alert.severity) {
                        AlertSeverity.Critical -> MaterialTheme.colorScheme.error
                        AlertSeverity.Warning -> MaterialTheme.colorScheme.tertiary
                    }
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (state.forms.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(text = "Brak zapisanych form") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    state.forms.forEach { form ->
                        val isCurrent = if (form.isBase) {
                            currentFormId == ActiveMorphoForm.BASE_FORM_ID
                        } else {
                            currentFormId == form.id
                        }
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = form.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Komorki: ${form.cellsCount} - ${form.infoLine}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isCurrent) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        }
                                    )
                                }
                            },
                            onClick = {
                                expanded = false
                                onSelectForm(form.id)
                            }
                        )
                    }
                }
            }
            Text(
                text = activeFormName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MorphogenesisEditor(
    editorState: MorphogenesisEditorState,
    onAddCell: () -> Unit,
    onSelectCell: (String) -> Unit,
    onMoveCell: (String, Offset) -> Unit,
    onRemoveSelectedCell: () -> Unit,
    onRadiusChange: (Float) -> Unit,
    onSaveDraft: () -> Unit,
    onActivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Edytor komorek",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                MorphogenesisEditorCanvas(
                    editorState = editorState,
                    onSelectCell = onSelectCell,
                    onMoveCell = onMoveCell,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Liczba komorek: ${editorState.cells.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                val selected = editorState.cells.firstOrNull { it.isSelected }
                if (selected != null) {
                    Text(
                        text = "Wybrano: ${selected.id} · ${selected.stageLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Dotknij komorke na plotnie, aby ja zaznaczyc",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonButton(text = "Dodaj", onClick = onAddCell, enabled = editorState.canAddCell)
                NeonButton(text = "Usuń", onClick = onRemoveSelectedCell, enabled = editorState.selectedCellId != null)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Rozmiar komorki",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Slider(
                    value = editorState.radiusSliderValue,
                    onValueChange = onRadiusChange,
                    valueRange = editorState.radiusSliderRange
                )
            }
            editorState.validationMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeonButton(text = "Zapisz szkic", onClick = onSaveDraft, enabled = editorState.canSaveDraft)
                    NeonButton(text = "Aktywuj", onClick = onActivate, enabled = editorState.canActivate)
                }
                val statusText: String
                val statusColor = if (!editorState.hasDirtyChanges) {
                    statusText = "Brak niezapisanych zmian"
                    MaterialTheme.colorScheme.outline
                } else {
                    statusText = "Zmiany oczekuja na zapis"
                    MaterialTheme.colorScheme.primary
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun MorphogenesisEditorCanvas(
    editorState: MorphogenesisEditorState,
    onSelectCell: (String) -> Unit,
    onMoveCell: (String, Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val cells = editorState.cells
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val paddingPx = with(density) { CanvasPadding.toPx() }
    val boundary = editorState.boundaryRadius.takeIf { it > 0f }
    val updatedCells = rememberUpdatedState(cells)
    val updatedBoundary = rememberUpdatedState(boundary)
    val layout = remember(cells, canvasSize, paddingPx, boundary) {
        if (canvasSize.width == 0 || canvasSize.height == 0) {
            null
        } else {
            calculateCanvasLayout(canvasSize, cells, paddingPx, boundary)
        }
    }
    val updatedLayout = rememberUpdatedState(layout)
    val baseModifier = modifier.onSizeChanged { canvasSize = it }
    val pointerModifier = if (cells.isEmpty()) {
        baseModifier
    } else {
        val tapModifier = Modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val currentLayout = updatedLayout.value
                    ?: calculateCanvasLayout(size, updatedCells.value, paddingPx, updatedBoundary.value)
                currentLayout?.findCellAtPosition(updatedCells.value, offset)?.let { touched ->
                    onSelectCell(touched.id)
                }
            }
        }
        val dragModifier = Modifier.pointerInput(Unit) {
            var activeCellId: String? = null
            var activeLayout: CanvasLayout? = null
            var pointerOffset = Offset.Zero
            detectDragGestures(
                onDragStart = { offset ->
                    val currentLayout = updatedLayout.value
                        ?: calculateCanvasLayout(size, updatedCells.value, paddingPx, updatedBoundary.value)
                    val touched = currentLayout?.findCellAtPosition(updatedCells.value, offset)
                    if (currentLayout != null && touched != null) {
                        activeCellId = touched.id
                        activeLayout = currentLayout
                        val pointerDomain = currentLayout.positionToDomain(offset)
                        pointerOffset = pointerDomain - touched.center
                        onSelectCell(touched.id)
                    } else {
                        activeCellId = null
                        activeLayout = null
                        pointerOffset = Offset.Zero
                    }
                },
                onDrag = { change, _ ->
                    val cellId = activeCellId
                    val currentLayout = activeLayout
                    if (cellId != null && currentLayout != null && currentLayout.scale > 0f) {
                        change.consume()
                        val pointerDomain = currentLayout.positionToDomain(change.position)
                        val newCenter = pointerDomain - pointerOffset
                        onMoveCell(cellId, newCenter)
                    }
                },
                onDragEnd = {
                    activeCellId = null
                    activeLayout = null
                    pointerOffset = Offset.Zero
                },
                onDragCancel = {
                    activeCellId = null
                    activeLayout = null
                    pointerOffset = Offset.Zero
                }
            )
        }
        baseModifier.then(tapModifier).then(dragModifier)
    }

    Box(
        modifier = pointerModifier,
        contentAlignment = Alignment.Center
    ) {
        if (cells.isEmpty()) {
            Text(
                text = "Brak komorek do wyswietlenia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            val fillColor = MaterialTheme.colorScheme.primary
            val outlineColor = MaterialTheme.colorScheme.outline
            val selectionColor = MaterialTheme.colorScheme.secondary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val currentLayout = updatedLayout.value
                    ?: calculateCanvasLayout(size, updatedCells.value, paddingPx, updatedBoundary.value)
                if (currentLayout != null) {
                    boundary?.let { limit ->
                        if (currentLayout.scale > 0f) {
                            val boundaryRadiusPx = limit * currentLayout.scale
                            drawCircle(
                                color = outlineColor.copy(alpha = 0.2f),
                                radius = boundaryRadiusPx,
                                center = currentLayout.origin,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                    updatedCells.value.forEach { cell ->
                        val centerPx = currentLayout.domainToPosition(cell.center)
                        val radiusPx = cell.radius * currentLayout.scale
                        drawCircle(
                            color = if (cell.isSelected) fillColor.copy(alpha = 0.85f) else fillColor.copy(alpha = 0.6f),
                            radius = radiusPx,
                            center = centerPx
                        )
                        val strokeWidth = (radiusPx * 0.18f).coerceAtLeast(2f)
                        drawCircle(
                            color = outlineColor.copy(alpha = 0.6f),
                            radius = radiusPx,
                            center = centerPx,
                            style = Stroke(width = strokeWidth)
                        )
                        if (cell.isSelected) {
                            drawCircle(
                                color = selectionColor.copy(alpha = 0.8f),
                                radius = radiusPx * 1.1f,
                                center = centerPx,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class CanvasLayout(
    val origin: Offset,
    val scale: Float
) {
    fun domainToPosition(domain: Offset): Offset {
        val scaled = Offset(domain.x * scale, domain.y * scale)
        return origin + scaled
    }

    fun positionToDomain(position: Offset): Offset {
        if (scale <= 0f) return Offset.Zero
        val delta = position - origin
        return Offset(delta.x / scale, delta.y / scale)
    }

    fun findCellAtPosition(
        cells: List<EditorCellState>,
        position: Offset
    ): EditorCellState? {
        if (scale <= 0f) return null
        return cells
            .map { cell ->
                val centerPx = domainToPosition(cell.center)
                val radiusPx = cell.radius * scale
                val distance = (position - centerPx).getDistance()
                Triple(cell, radiusPx, radiusPx - distance)
            }
            .filter { it.third >= 0f }
            .maxByOrNull { it.third }
            ?.first
    }
}

private val CanvasPadding = 12.dp

private fun calculateCanvasLayout(
    size: IntSize,
    cells: List<EditorCellState>,
    padding: Float,
    boundaryRadius: Float?
): CanvasLayout? = calculateCanvasLayout(
    Size(size.width.toFloat(), size.height.toFloat()),
    cells,
    padding,
    boundaryRadius
)

private fun calculateCanvasLayout(
    size: Size,
    cells: List<EditorCellState>,
    padding: Float,
    boundaryRadius: Float?
): CanvasLayout? {
    val effectiveBoundary = boundaryRadius?.takeIf { it > 0f }
    if (cells.isEmpty() && effectiveBoundary == null) return null
    val contentRadius = when {
        effectiveBoundary != null -> max(1f, effectiveBoundary)
        cells.isNotEmpty() -> max(1f, cells.maxOf { it.center.getDistance() + it.radius })
        else -> 1f
    }
    val minDimension = min(size.width, size.height)
    val availableRadius = (minDimension / 2f) - padding
    val scale = if (availableRadius <= 0f) 0f else availableRadius / contentRadius
    val origin = Offset(size.width / 2f, size.height / 2f)
    return CanvasLayout(origin = origin, scale = scale)
}

@Composable
private fun PlaceholderCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun FormRowCard(
    summary: MorphogenesisFormSummary,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            ),
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = summary.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (summary.status == FormStatus.Active) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = "Komorki: ${summary.cellsCount}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = summary.infoLine,
                style = MaterialTheme.typography.bodySmall,
                color = if (summary.status == FormStatus.Active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

private fun Offset.getDistance(): Float = sqrt(x * x + y * y)

private operator fun Offset.minus(other: Offset): Offset = Offset(x - other.x, y - other.y)
