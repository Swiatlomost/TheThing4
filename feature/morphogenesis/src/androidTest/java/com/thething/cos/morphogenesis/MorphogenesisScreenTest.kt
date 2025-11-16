package com.thething.cos.morphogenesis

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class MorphogenesisScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun headerDisplaysAlertWhenCellsLow() {
        val alertMessage = "Niski zapas komorek: 2"

        composeRule.setContent {
            MorphogenesisScreen(
                state = MorphogenesisUiState(
                    levelTag = "Lv. 2",
                    levelDescription = "Pierscien 2 - limit 19 komorek",
                    availableCells = 2,
                    totalCells = 19,
                    cellsAlert = CellsAlert(
                        message = alertMessage,
                        severity = AlertSeverity.Warning
                    ),
                    activeFormId = "FORM-0",
                    activeFormName = "Forma 0",
                    forms = listOf(
                        MorphogenesisFormSummary(
                            id = "FORM-0",
                            name = "Forma 0 (baza)",
                            cellsCount = 17,
                            status = FormStatus.Active
                        )
                    ),
                    editor = MorphogenesisEditorState(
                        cells = listOf(
                            EditorCellState(
                                id = "FORM-0-CELL-0",
                                center = Offset.Zero,
                                radius = 0.5f,
                                stageLabel = "Seed",
                                isSelected = true
                            )
                        ),
                        selectedCellId = "FORM-0-CELL-0",
                        radiusSliderValue = 0.5f,
                        radiusSliderRange = 0.1f..1f,
                        canAddCell = true,
                        canSaveDraft = true,
                        canActivate = true,
                        hasDirtyChanges = true,
                        validationMessage = "Kolizja testowa"
                    ),
                    notes = "Test alertu"
                ),
                onBack = {}
            )
        }

        composeRule.onNodeWithText(alertMessage).assertExists()
        composeRule.onNodeWithText("Komorki 2/19").assertExists()
        composeRule.onNodeWithText("Lv. 2").assertExists()
        composeRule.onNodeWithText("Edytor komorek").assertExists()
        composeRule.onNodeWithText("Zapisz szkic").assertExists()
        composeRule.onNodeWithText("Aktywuj").assertExists()
        composeRule.onNodeWithText("Kolizja testowa").assertExists()
    }
}

