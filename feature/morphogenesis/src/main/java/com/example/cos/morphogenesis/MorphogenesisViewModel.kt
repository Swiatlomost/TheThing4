package com.example.cos.morphogenesis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cos.lifecycle.CosLifecycleEngine
import com.example.cos.lifecycle.CosLifecycleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MorphogenesisViewModel @Inject constructor(
    private val engine: CosLifecycleEngine
) : ViewModel() {

    val state: StateFlow<MorphogenesisUiState> =
        engine.state
            .map(::mapToUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = mapToUiState(engine.state.value)
            )

    private fun mapToUiState(state: CosLifecycleState): MorphogenesisUiState {
        val cellCount = state.cells.size
        val ring = determineRing(cellCount)
        val capacity = capacityForRing(ring)
        val nextCapacity = capacityForRing(ring + 1)
        val available = (capacity - cellCount).coerceAtLeast(0)

        return MorphogenesisUiState(
            levelTag = "Lv. ${ring + 1}",
            levelDescription = "Okręg ${ring + 1} — maks. $capacity komórek",
            availableCells = available,
            totalCells = capacity,
            activeFormId = "FORM-0",
            activeFormName = "Forma 0",
            forms = buildList {
                add(
                    MorphogenesisFormSummary(
                        id = "FORM-0",
                        name = "Forma 0 (baza)",
                        cellsCount = cellCount,
                        status = FormStatus.Active
                    )
                )
                // przyszłe szkice pojawią się, gdy repozytorium form będzie dostępne
            },
            notes = "Następny poziom pomieści do $nextCapacity komórek. Przygotuj zmiany, które chcesz dodać."
        )
    }

    private fun determineRing(count: Int): Int {
        if (count <= 1) return 0
        var ring = 0
        while (capacityForRing(ring) < count) {
            ring++
        }
        return ring
    }

    private fun capacityForRing(ring: Int): Int {
        if (ring <= 0) return 1
        return 1 + 3 * ring * (ring + 1)
    }
}
