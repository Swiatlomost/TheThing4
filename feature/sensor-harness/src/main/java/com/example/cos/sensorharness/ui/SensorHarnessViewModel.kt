package com.example.cos.sensorharness.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cos.sensorharness.data.SensorCaptureController
import com.example.cos.sensorharness.model.SensorCaptureState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorHarnessViewModel @Inject constructor(
    private val controller: SensorCaptureController
) : ViewModel() {

    val state: StateFlow<SensorCaptureState> = controller.state

    fun startCapture() {
        controller.startCapture()
    }

    fun stopCapture() {
        controller.stopCapture()
    }

    fun snapshotFingerprint() {
        viewModelScope.launch {
            controller.generateFingerprint()
        }
    }

    override fun onCleared() {
        controller.stopCapture()
        super.onCleared()
    }
}
