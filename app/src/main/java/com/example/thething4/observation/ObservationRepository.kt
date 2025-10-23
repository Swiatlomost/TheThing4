package com.example.thething4.observation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ObservationRepository {
    val organismTransform: Flow<OrganismTransform>
    val timeline: Flow<SaturationTimeline>
    val hints: Flow<ObservationHints>
    suspend fun persistTransform(transform: OrganismTransform)
}

class InMemoryObservationRepository : ObservationRepository {

    private val transformFlow = MutableStateFlow(OrganismTransform.Identity)
    private val timelineFlow = MutableStateFlow(SaturationTimeline.Empty)
    private val hintsFlow = MutableStateFlow(ObservationHints())

    override val organismTransform: Flow<OrganismTransform> = transformFlow.asStateFlow()
    override val timeline: Flow<SaturationTimeline> = timelineFlow.asStateFlow()
    override val hints: Flow<ObservationHints> = hintsFlow.asStateFlow()

    override suspend fun persistTransform(transform: OrganismTransform) {
        transformFlow.value = transform
    }

    fun updateTimeline(timeline: SaturationTimeline) {
        timelineFlow.value = timeline
    }

    fun updateHints(hints: ObservationHints) {
        hintsFlow.value = hints
    }
}
