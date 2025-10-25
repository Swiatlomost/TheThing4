package com.example.cos.morphogenesis

import androidx.compose.runtime.Immutable

@Immutable
data class MorphogenesisUiState(
    val levelTag: String = "Lv. 1",
    val levelDescription: String = "Poziom 1",
    val availableCells: Int = 0,
    val totalCells: Int = 0,
    val activeFormId: String? = null,
    val activeFormName: String? = null,
    val forms: List<MorphogenesisFormSummary> = emptyList(),
    val notes: String = "Interfejs edytora morfogenezy jest w trakcie budowy."
)

@Immutable
data class MorphogenesisFormSummary(
    val id: String,
    val name: String,
    val cellsCount: Int,
    val status: FormStatus
) {
    val infoLine: String
        get() = when (status) {
            FormStatus.Active -> "Forma aktywna"
            FormStatus.Draft -> "Szkic — aktywacja po edycji"
        }
}

enum class FormStatus { Active, Draft }
