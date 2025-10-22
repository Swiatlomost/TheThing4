package com.example.thething4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thething4.core.cell.CellLifecycle
import com.example.thething4.core.cell.CellLifecycleStateMachine
import com.example.thething4.core.cell.CellSnapshot
import com.example.thething4.core.cell.CellStage
import com.example.thething4.core.time.MonotonicTimeProvider
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
        val timeProvider = MonotonicTimeProvider()
        val stateMachine = CellLifecycleStateMachine(timeProvider)
        val factory = CellViewModelFactory(
            timeProvider = timeProvider,
            stateMachine = stateMachine
        )

        setContent {
            CosTheme {
                val viewModel: CellViewModel = viewModel(factory = factory)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                CosLifecycleApp(uiState = uiState)
            }
        }
    }
}

@Composable
private fun CosLifecycleApp(uiState: CellUiState) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        CosLifecycleScreen(uiState = uiState)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101014)
@Composable
private fun CosLifecyclePreview() {
    CosTheme {
        val fakeCell = CellLifecycle.default(Duration.ZERO)
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
