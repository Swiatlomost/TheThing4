package com.example.thething4.observation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thething4.core.cell.CellId
import com.example.thething4.core.cell.CellLifecycle
import com.example.thething4.core.cell.CellLifecycleCoordinator
import com.example.thething4.core.cell.CellLifecycleStateMachine
import com.example.thething4.core.cell.CellSnapshot
import com.example.thething4.core.cell.CellStage
import com.example.thething4.core.time.TimeProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ObservationViewModel(
    private val timeProvider: TimeProvider,
    private val stateMachine: CellLifecycleStateMachine,
    private val repository: ObservationRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val tickDelayMillis: Long = 32L,
    createOrganism: (TimeProvider) -> List<CellLifecycle> = { defaultOrganism(it) }
) : ViewModel() {

    private val lifecycles: List<CellLifecycle> = createOrganism(timeProvider)
    private val cooldownUntil = MutableStateFlow<Duration?>(null)
    private val layoutPositions = radialLayout(count = lifecycles.size)

    private val transformState = repository.organismTransform
        .stateIn(viewModelScope, started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = OrganismTransform.Identity)
    private val timelineState = repository.timeline
        .stateIn(viewModelScope, started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = SaturationTimeline.Empty)
    private val hintsState = repository.hints
        .stateIn(viewModelScope, started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = ObservationHints())

    private val _uiState = MutableStateFlow(ObservationUiState())
    val uiState: StateFlow<ObservationUiState> = _uiState.asStateFlow()

    private var pendingPersist: Job? = null
    private val running = MutableStateFlow(true)
    private val updateJob: Job

    val gestures: ObservationGestures
        get() = ObservationGestures(
            onOrganismDrag = ::onOrganismDragged,
            onGestureEnd = ::onGestureEnd
        )

    init {
        updateJob = viewModelScope.launch(dispatcher) {
            while (isActive) {
                if (!running.value) break
                val now = timeProvider.now()
                val snapshots = lifecycles.map { lifecycle ->
                    CellSnapshot(
                        lifecycle = lifecycle,
                        stage = stateMachine.evaluate(lifecycle, now)
                    )
                }
                val readiness = computeReadiness(now, snapshots)
                val timeline = if (timelineState.value.entries.isEmpty()) {
                    timelineForSnapshot(now, snapshots)
                } else {
                    timelineState.value
                }
                val hints = hintsState.value.takeIf { it.showDragHint || it.showBuddingHint }
                    ?: computeHints(readiness)
                val organismCells = snapshots.mapIndexed { index, snapshot ->
                    val layoutIndex = layoutPositions.getOrNull(index) ?: Offset.Zero
                    OrganismCellSnapshot(
                        snapshot = snapshot,
                        layoutPosition = layoutIndex
                    )
                }

                _uiState.value = ObservationUiState(
                    organism = OrganismSnapshot(
                        cells = organismCells,
                        transform = transformState.value
                    ),
                    readiness = readiness,
                    timeline = timeline,
                    hints = hints
                )
                delay(tickDelayMillis)
            }
        }
    }

    fun onBuddingRequested() {
        val readiness = _uiState.value.readiness
        if (!readiness.isReady) return

        val cooldownExpiry = timeProvider.now() + COOLDOWN_DURATION
        cooldownUntil.value = cooldownExpiry
    }

    private fun onOrganismDragged(delta: DragDelta) {
        val updated = transformState.value.translate(delta)
        pendingPersist?.cancel()
        pendingPersist = viewModelScope.launch(dispatcher) {
            repository.persistTransform(updated)
        }
    }

    private fun onGestureEnd() {
        pendingPersist = null
    }

    fun dispose() {
        running.value = false
        pendingPersist?.cancel()
        pendingPersist = null
        updateJob.cancel()
    }

    override fun onCleared() {
        dispose()
        super.onCleared()
    }

    private fun computeReadiness(now: Duration, snapshots: List<CellSnapshot>): GatingReadiness {
        val matureCount = snapshots.count { it.stage is CellStage.Mature }
        val totalCount = snapshots.size

        val cooldownExpiry = cooldownUntil.value
        if (cooldownExpiry != null && now >= cooldownExpiry) {
            cooldownUntil.value = null
        }

        val status = when {
            cooldownUntil.value != null -> GatingStatus.Cooldown
            matureCount > 0 -> GatingStatus.Ready
            else -> GatingStatus.NotReady
        }

        val message = when (status) {
            GatingStatus.Ready -> if (matureCount == totalCount) "Organism ready to bud" else "Partial readiness"
            GatingStatus.NotReady -> "Awaiting maturity"
            GatingStatus.Cooldown -> "Regenerating energy"
        }

        return GatingReadiness(
            status = status,
            matureCount = matureCount,
            totalCount = totalCount,
            message = message
        )
    }

    companion object {
        private val COOLDOWN_DURATION = 5.seconds

        private fun defaultOrganism(timeProvider: TimeProvider): List<CellLifecycle> {
            val now = timeProvider.now()
            val offsets = listOf(0.seconds, 4.seconds, 8.seconds, 12.seconds, 16.seconds, 20.seconds)
            return offsets.mapIndexed { index, offset ->
                CellLifecycle(
                    id = CellId("observation-${index + 1}"),
                    createdAt = now - offset,
                    warmupDuration = (6 + (index % 3) * 2).seconds,
                    saturationDuration = (12 + (index % 2) * 4).seconds
                )
            }
        }
    }
}
