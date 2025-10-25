package com.example.cos.morphogenesis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphogenesisScreen(
    state: MorphogenesisUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Morfogeneza") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Wróć")
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
                HeaderStatusBar(state = state)
            }
            if (state.forms.isNotEmpty()) {
                item {
                    Text(
                        text = "Ostatnio zapisane formy",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(state.forms) { form ->
                    FormRowCard(summary = form)
                }
            }
            item {
                PlaceholderCard(message = state.notes)
            }
        }
    }
}

@Composable
private fun HeaderStatusBar(state: MorphogenesisUiState) {
    var formsExpanded by remember { mutableStateOf(false) }
    val activeFormName = state.activeFormName
        ?: state.forms.firstOrNull { it.status == FormStatus.Active }?.name
        ?: "Brak aktywnej formy"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = state.levelTag,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Cells ${state.availableCells}/${state.totalCells}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeFormName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { formsExpanded = !formsExpanded }) {
                        Text(
                            text = if (formsExpanded) "▲" else "▼",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                DropdownMenu(
                    expanded = formsExpanded,
                    onDismissRequest = { formsExpanded = false }
                ) {
                    if (state.forms.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(text = "Brak zapisanych form") },
                            onClick = { formsExpanded = false },
                            enabled = false
                        )
                    } else {
                        state.forms.forEach { form ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = form.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = "Komórki: ${form.cellsCount} • ${form.infoLine}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = { formsExpanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormRowCard(summary: MorphogenesisFormSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                text = "Komórki: ${summary.cellsCount}",
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
