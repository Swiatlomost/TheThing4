package com.example.cos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cos.designsystem.theme.CosTheme
import com.example.cos.lifecycle.CosLifecycleScreen
import com.example.cos.lifecycle.CosLifecycleViewModel
import com.example.cos.overlay.OverlayController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var overlayController: OverlayController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CosApp(overlayController = overlayController)
        }
    }
}

@Composable
fun CosApp(
    overlayController: OverlayController,
    viewModel: CosLifecycleViewModel = hiltViewModel()
) {
    val lifecycleState by viewModel.state.collectAsState()
    CosTheme {
        CosLifecycleScreen(
            state = lifecycleState,
            onToggleOverlay = {
                if (overlayController.hasPermission()) {
                    overlayController.startOverlayService()
                } else {
                    overlayController.requestPermission()
                }
            },
            onReset = viewModel::resetOrganism,
            onSetStage = viewModel::setStage,
            onCreateChild = viewModel::createChild
        )
    }
}
