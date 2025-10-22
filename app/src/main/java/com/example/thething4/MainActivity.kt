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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thething4.core.cell.CellLifecycleCoordinator
import com.example.thething4.core.cell.CellSnapshot
import com.example.thething4.core.cell.CellStage
import com.example.thething4.overlay.OverlayController
import com.example.thething4.ui.CellUiState
import com.example.thething4.ui.CellViewModel
import com.example.thething4.ui.CellViewModelFactory
import com.example.thething4.ui.CosLifecycleScreen
import com.example.thething4.ui.theme.CosTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CosTheme {
                val viewModel: CellViewModel = viewModel(factory = CellViewModelFactory.default())
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                CosLifecycleApp(uiState = uiState)
            }
        }
    }
}

@Composable
private fun CosLifecycleApp(uiState: CellUiState) {
    val context = LocalContext.current
    val overlayRunning by OverlayController.state.collectAsStateWithLifecycle()

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
                onToggle = {
                    if (overlayRunning) {
                        OverlayController.stop(context)
                    } else {
                        OverlayController.start(context)
                    }
                },
                onStop = { OverlayController.stop(context) }
            )
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
            )
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

