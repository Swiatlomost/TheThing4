package com.example.thething4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thething4.core.cell.CellLifecycleCoordinator
import com.example.thething4.core.cell.CellSnapshot
import com.example.thething4.core.cell.CellStage
import com.example.thething4.observation.ObservationUiState
import com.example.thething4.observation.ObservationViewModel
import com.example.thething4.observation.ObservationViewModelFactory
import com.example.thething4.overlay.OverlayController
import com.example.thething4.ui.CellUiState
import com.example.thething4.ui.CellViewModel
import com.example.thething4.ui.CellViewModelFactory
import com.example.thething4.ui.CosLifecycleScreen
import com.example.thething4.ui.ObservationModeScreen
import com.example.thething4.ui.theme.CosTheme
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CosTheme {
                val lifecycleViewModel: CellViewModel = viewModel(factory = CellViewModelFactory.default())
                val observationViewModel: ObservationViewModel = viewModel(factory = ObservationViewModelFactory.default())

                val lifecycleUiState by lifecycleViewModel.uiState.collectAsStateWithLifecycle()
                val observationUiState by observationViewModel.uiState.collectAsStateWithLifecycle()
                val overlayRunning by OverlayController.state.collectAsStateWithLifecycle()

                var showObservation by rememberSaveable { mutableStateOf(false) }

                if (showObservation) {
                    ObservationModeScreen(
                        uiState = observationUiState,
                        gestures = observationViewModel.gestures,
                        onBack = { showObservation = false },
                        onBudding = observationViewModel::onBuddingRequested
                    )
                } else {
                    CosLifecycleApp(
                        uiState = lifecycleUiState,
                        overlayRunning = overlayRunning,
                        onToggleOverlay = {
                            if (overlayRunning) {
                                OverlayController.stop(this@MainActivity)
                            } else {
                                OverlayController.start(this@MainActivity)
                            }
                        },
                        onStopOverlay = { OverlayController.stop(this@MainActivity) },
                        onEnterObservation = { showObservation = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun CosLifecycleApp(
    uiState: CellUiState,
    overlayRunning: Boolean,
    onToggleOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onEnterObservation: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CosLifecycleScreen(
                uiState = uiState,
                modifier = Modifier.weight(1f, fill = true)
            )
            OverlayToggleSection(
                overlayRunning = overlayRunning,
                onToggle = onToggleOverlay,
                onStop = onStopOverlay
            )
            Button(onClick = onEnterObservation) {
                Text("Enter observation mode")
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101014)
@Composable
private fun CosLifecyclePreview() {
    CosTheme {
        val fakeCell = CellLifecycleCoordinator.lifecycle()
        val snapshot = CellSnapshot(
            lifecycle = fakeCell,
            stage = CellStage.Bud(progress = 0.5f)
        )
        CosLifecycleApp(
            uiState = CellUiState(
                cells = listOf(snapshot),
                now = 12.seconds
            ),
            overlayRunning = false,
            onToggleOverlay = {},
            onStopOverlay = {},
            onEnterObservation = {}
        )
    }
}

@Composable
private fun OverlayToggleSection(
    overlayRunning: Boolean,
    onToggle: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onToggle) {
            Text(if (overlayRunning) "Stop floating mode" else "Start floating mode")
        }
        if (overlayRunning) {
            OutlinedButton(onClick = onStop) {
                Text("Zatrzymaj teraz")
            }
        }
    }
}
