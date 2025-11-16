package com.thething.cos.morphogenesis

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

@Immutable
data class MorphogenesisUiState(
    val levelTag: String = "Lv. 1",
    val levelDescription: String = "Poziom 1",
    val availableCells: Int = 0,
    val totalCells: Int = 0,
    val cellsAlert: CellsAlert? = null,
    val activeFormId: String? = null,
    val activeFormName: String? = null,
    val forms: List<MorphogenesisFormSummary> = emptyList(),
    val editor: MorphogenesisEditorState = MorphogenesisEditorState(),
    val notes: String = "Interfejs edytora Morfogenezy jest w trakcie budowy."
)

@Immutable
data class MorphogenesisFormSummary(
    val id: String,
    val name: String,
    val cellsCount: Int,
    val status: FormStatus,
    val isBase: Boolean = false
) {
    val infoLine: String
        get() = when (status) {
            FormStatus.Active -> "Forma aktywna"
            FormStatus.Draft -> "Szkic - aktywacja po edycji"
        }
}

enum class FormStatus { Active, Draft }

@Immutable
data class CellsAlert(
    val message: String,
    val severity: AlertSeverity
)

enum class AlertSeverity {
    Warning,
    Critical
}

@Immutable
data class MorphogenesisEditorState(
    val formId: String? = null,
    val formName: String = "",
    val cells: List<EditorCellState> = emptyList(),
    val selectedCellId: String? = null,
    val radiusSliderValue: Float = 0f,
    val radiusSliderRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val boundaryRadius: Float = 0f,
    val canAddCell: Boolean = false,
    val canSaveDraft: Boolean = false,
    val canActivate: Boolean = false,
    val hasDirtyChanges: Boolean = false,
    val validationMessage: String? = null
)

@Immutable
data class EditorCellState(
    val id: String,
    val center: Offset,
    val radius: Float,
    val stageLabel: String,
    val isSelected: Boolean
)
