package com.example.cos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cos.designsystem.theme.CosTheme
import com.example.cos.lifecycle.CosLifecycleScreen
import com.example.cos.lifecycle.CosLifecycleViewModel
import com.example.cos.morphogenesis.MorphogenesisScreen
import com.example.cos.morphogenesis.MorphogenesisViewModel
import com.example.cos.overlay.OverlayController
import com.example.cos.sensorharness.ui.SensorHarnessRoute
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
    var destination by rememberSaveable { mutableStateOf(AppDestination.Lifecycle) }
    CosTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Crossfade(targetState = destination, label = "cos-destination") { current ->
                when (current) {
                    AppDestination.Lifecycle -> {
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
                            onCreateChild = viewModel::createChild,
                            onOpenMorphogenesis = { destination = AppDestination.Morphogenesis },
                            onOpenSkinDemo = { destination = AppDestination.SkinDemo },
                            onOpenSensorHarness = { destination = AppDestination.SensorHarness }
                        )
                    }
                    AppDestination.Morphogenesis -> {
                        val morphoViewModel: MorphogenesisViewModel = hiltViewModel()
                        val morphoState by morphoViewModel.state.collectAsState()
                        MorphogenesisScreen(
                            state = morphoState,
                            onBack = { destination = AppDestination.Lifecycle },
                            onAddCell = morphoViewModel::addCell,
                            onSelectCell = morphoViewModel::selectCell,
                            onMoveCell = morphoViewModel::moveCell,
                            onRemoveSelectedCell = morphoViewModel::removeSelectedCell,
                            onRadiusChange = morphoViewModel::updateSelectedRadius,
                            onSaveDraft = morphoViewModel::saveDraft,
                            onActivate = morphoViewModel::activateDraft,
                            onSelectForm = morphoViewModel::selectForm
                        )
                    }
                    AppDestination.SkinDemo -> {
                        SkinDemoScreen(onBack = { destination = AppDestination.Lifecycle })
                    }
                    AppDestination.SensorHarness -> {
                        SensorHarnessRoute(onBack = { destination = AppDestination.Lifecycle })
                    }
                }
            }
        }
    }
}

private enum class AppDestination {
    Lifecycle,
    Morphogenesis,
    SkinDemo,
    SensorHarness
}
