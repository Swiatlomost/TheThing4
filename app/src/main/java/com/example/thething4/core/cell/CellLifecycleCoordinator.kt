package com.example.thething4.core.cell

import com.example.thething4.core.time.MonotonicTimeProvider
import com.example.thething4.core.time.TimeProvider
import kotlin.time.Duration

object CellLifecycleCoordinator {
    private val _timeProvider: TimeProvider = MonotonicTimeProvider()
    private val creationTimestamp: Duration = _timeProvider.now()

    val timeProvider: TimeProvider get() = _timeProvider

    fun lifecycle(): CellLifecycle = CellLifecycle.default(creationTimestamp)

    fun stateMachine(): CellLifecycleStateMachine = CellLifecycleStateMachine(_timeProvider)
}
