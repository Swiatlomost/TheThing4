# ADR-2025-10-22 — Cell Lifecycle State Machine

## Status
Proposed — 2025-10-22 (owner: Vireal)

## Context
- Coś składa się z komórek, które przechodzą cykl życia opisany w `notes/cosv1.0.txt`.
- Repozytorium nie posiada jeszcze modelu danych, zegara monotonicznego ani warstwy prezentacji zarządzającej transformacjami.
- Tryb A (pływający) i tryb B (aplikacja) muszą dzielić wspólny stan oraz zachowywać ciągłość czasu i saturacji.
- Wytyczne architektoniczne (docs/android-guidelines/architecture.md) preferują Compose, unidirectional data flow i wydzielenie warstwy domenowej.
- Analiza Echo (`docs/cos/lifecycle-analysis.md`) dostarcza rekomendacji dot. zegarów, animacji i testów.

## Decision
1. **Model domenowy**
   ```kotlin
   data class CellLifecycle(
       val id: CellId,
       val creationEpochMs: Long,
       val warmupDuration: Duration = 10.seconds,
       val saturationDuration: Duration = 15.seconds,
       val overrides: CellLifecycleOverrides = CellLifecycleOverrides()
   )
   ```
   - `CellId` = value class (UUID/String).
   - `CellLifecycleOverrides` pozwala w przyszłości konfigurować parametry (energia, puls).

2. **State Machine (Domain)**
   ```kotlin
   sealed interface CellStage {
       val progress: Float

       data class Seed(override val progress: Float) : CellStage
       data class Bud(override val progress: Float) : CellStage
       data class Mature(override val progress: Float) : CellStage
   }
   ```
   - `progress` w [0f,1f] reprezentuje nasycenie lub postęp w bieżącym stanie.
   - Wyliczany przez `CellLifecycleStateMachine`.

   ```kotlin
   interface TimeProvider {
       fun now(): Duration
   }

   class CellLifecycleStateMachine(
       private val timeProvider: TimeProvider
   ) {
       fun evaluate(cell: CellLifecycle, reference: Duration = timeProvider.now()): CellStage
   }
   ```
   - Monotoniczne źródło czasu (`SystemClock.elapsedRealtime()` lub coroutine `TimeSource.Monotonic`).
   - Wartość `reference` domyślnie bieżący czas, ale można ją wstrzykiwać w testach.

3. **Repository**
   - `CellRepository` przechowuje listę komórek oraz ich `CellLifecycle`.
   - Źródłem prawdy jest `DataStore` (proto) + cache w pamięci.
   - Repo udostępnia `StateFlow<List<CellSnapshot>>`, gdzie `CellSnapshot` zawiera `CellLifecycle` + obliczony `CellStage`.

4. **Warstwa prezentacji**
   - `CellViewModel` subskrybuje repozytorium i mapuje do `UiState`.
   - W Compose używamy `Ticker` (coroutine) do odświeżania stanu (50 Hz maks.).
   - Tryb pływający używa tego samego repozytorium przez `Hilt` + `SingletonComponent`.

5. **Animacje**
   - UI odczytuje `CellStage` i używa Compose `Canvas`.
   - Puls implementowany przez `rememberInfiniteTransition`.

6. **Testy**
   - Unit: `FakeTimeProvider` i asercje na `CellStage`.
   - Instrumented: Compose `mainClock.advanceTimeBy`.
   - Manual: Pixel_5 scenariusz opisany przez Kai.

7. **API publiczne**
   - Pakiet `com.example.thething4.core.time` — `MonotonicTimeProvider`.
   - Pakiet `com.example.thething4.core.cell` — modele i state machine.
   - Feature modularyzacja (przyszłościowo): na razie w module `app`, ale kod w folderach `core/`.

## Consequences
- Umożliwia spójne obliczanie stanu w obu trybach i testowanie bez zależności od UI.
- Wymaga dodania zależności Compose i coroutines (już wymagane).
- Repozytorium komórek musi zostać zintegrowane z trybem pływającym (Nodus).
- Zwiększona złożoność modułu `app`; docelowo warto wydzielić moduł `core`.

## Alternatives Considered
- **Liczenie progresu w UI** — odrzucono: utrudnia testy i współdzielenie z trybem pływającym.
- **Użycie `ValueAnimator`** — odrzucono: Compose i multiplatformowy `TimeSource` dają większą kontrolę, a ValueAnimator komplikuje tryb pływający.
- **Brak DataStore** — odrzucono: pamięć musi być trwała między sesjami, a DataStore dobrze współgra z Hilt.

## Follow-up Tasks
- Lumen: wdrożyć model i state machine (`app/src/main/java/.../core/`), zmigrować główny ekran na Compose.
- Kai: zbudować plan testowy na bazie tego ADR.
- Nyx: dodać heurystyki do pamięci dot. monotonicznego czasu i stanów.
- Scribe: odnotować decyzję w kronice oraz logu.
