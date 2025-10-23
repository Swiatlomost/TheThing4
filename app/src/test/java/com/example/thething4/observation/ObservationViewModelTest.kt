package com.example.thething4.observation

import androidx.compose.ui.geometry.Offset
import com.example.thething4.core.cell.CellId
import com.example.thething4.core.cell.CellLifecycle
import com.example.thething4.core.cell.CellLifecycleStateMachine
import com.example.thething4.core.time.TimeProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObservationViewModelTest {

    @Test
    fun readinessReflectsMatureCells() = runObservationTest {
        val time = MutableTimeProvider()
        val lifecycle = CellLifecycle(
            id = CellId("root"),
            createdAt = Duration.ZERO,
            warmupDuration = 5.seconds,
            saturationDuration = 5.seconds
        )

        val viewModel = createViewModel(timeProvider = time, lifecycles = listOf(lifecycle))
        try {
            runCurrent()
            assertFalse(viewModel.uiState.value.readiness.isReady)

            time.advanceBy(11.seconds)
            advanceTimeBy(1)
            runCurrent()
            assertTrue(viewModel.uiState.value.readiness.isReady)
        } finally {
            viewModel.dispose()
            runCurrent()
        }
    }

    @Test
    fun buddingTriggersCooldown() = runObservationTest {
        val time = MutableTimeProvider()
        val lifecycle = CellLifecycle(
            id = CellId("root"),
            createdAt = Duration.ZERO,
            warmupDuration = 5.seconds,
            saturationDuration = 5.seconds
        )

        val viewModel = createViewModel(timeProvider = time, lifecycles = listOf(lifecycle))
        try {
            time.advanceBy(12.seconds)
            advanceTimeBy(1)
            runCurrent()
            assertEquals(GatingStatus.Ready, viewModel.uiState.value.readiness.status)

            viewModel.onBuddingRequested()
            advanceTimeBy(1)
            runCurrent()
            assertEquals(GatingStatus.Cooldown, viewModel.uiState.value.readiness.status)

            time.advanceBy(6.seconds)
            advanceTimeBy(1)
            runCurrent()
            assertEquals(GatingStatus.Ready, viewModel.uiState.value.readiness.status)
        } finally {
            viewModel.dispose()
            runCurrent()
        }
    }

    @Test
    fun dragPersistsTransform() = runObservationTest {
        val time = MutableTimeProvider()
        val lifecycle = CellLifecycle(
            id = CellId("root"),
            createdAt = Duration.ZERO
        )

        val repository = InMemoryObservationRepository()
        val viewModel = ObservationViewModel(
            timeProvider = time,
            stateMachine = CellLifecycleStateMachine(time),
            repository = repository,
            dispatcher = StandardTestDispatcher(testScheduler),
            tickDelayMillis = 1L,
            createOrganism = { listOf(lifecycle) }
        )
        try {
            advanceTimeBy(1)
            runCurrent()
            viewModel.gestures.onOrganismDrag(Offset(10f, 5f))
            advanceTimeBy(1)
            runCurrent()

            assertEquals(
                10f,
                viewModel.uiState.value.organism.transform.position.x
            )
            assertEquals(
                5f,
                viewModel.uiState.value.organism.transform.position.y
            )
        } finally {
            viewModel.dispose()
            runCurrent()
        }
    }

    private fun TestScope.createViewModel(
        timeProvider: MutableTimeProvider,
        lifecycles: List<CellLifecycle>
    ): ObservationViewModel {
        return ObservationViewModel(
            timeProvider = timeProvider,
            stateMachine = CellLifecycleStateMachine(timeProvider),
            repository = InMemoryObservationRepository(),
            dispatcher = StandardTestDispatcher(testScheduler),
            tickDelayMillis = 1L,
            createOrganism = { lifecycles }
        )
    }

    private fun runObservationTest(block: suspend TestScope.() -> Unit) {
        runTest {
            val mainDispatcher = UnconfinedTestDispatcher(testScheduler)
            Dispatchers.setMain(mainDispatcher)
            try {
                withTimeout(5_000) {
                    block()
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
}

private class MutableTimeProvider : TimeProvider {
    private var now: Duration = Duration.ZERO

    override fun now(): Duration = now

    fun advanceBy(delta: Duration) {
        now += delta
    }
}
