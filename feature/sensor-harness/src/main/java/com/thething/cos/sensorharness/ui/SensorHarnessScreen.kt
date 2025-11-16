package com.thething.cos.sensorharness.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thething.cos.sensorharness.R
import com.thething.cos.sensorharness.model.SensorCaptureState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorHarnessRoute(
    onBack: () -> Unit,
    viewModel: SensorHarnessViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val stateFlow = viewModel.state
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.sensor_harness_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        val state by stateFlow.collectAsState()
        SensorHarnessScreen(
            state = state,
            onStart = viewModel::startCapture,
            onStop = viewModel::stopCapture,
            onFingerprint = viewModel::snapshotFingerprint,
            modifier = Modifier.padding(paddingValues)
        )

        LaunchedEffect(state.fingerprintBase64) {
            state.fingerprintBase64?.let {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.sensor_harness_copy)
                )
            }
        }
    }
}

@Composable
private fun SensorHarnessScreen(
    state: SensorCaptureState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onFingerprint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(id = R.string.sensor_harness_samples, state.sampleCount))
        Text(text = stringResource(id = R.string.sensor_harness_entropy, state.entropyEstimate))
        Text(text = stringResource(id = R.string.sensor_harness_trust, state.trustEstimate))
        Text(text = stringResource(id = R.string.sensor_harness_last_motion, state.motionMagnitude))
        Text(text = stringResource(id = R.string.sensor_harness_last_gyro, state.gyroMagnitude))

        if (state.isCapturing) {
            Button(onClick = onStop) {
                Text(text = stringResource(id = R.string.sensor_harness_stop))
            }
        } else {
            Button(onClick = onStart) {
                Text(text = stringResource(id = R.string.sensor_harness_start))
            }
        }

        Button(onClick = onFingerprint, enabled = state.sampleCount > 0) {
            Text(text = stringResource(id = R.string.sensor_harness_fingerprint))
        }

        state.fingerprintBase64?.let {
            Text(text = it)
        }
    }
}
