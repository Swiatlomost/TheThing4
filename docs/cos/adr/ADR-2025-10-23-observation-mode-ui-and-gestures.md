# ADR-2025-10-23 - Observation Mode Layout and Group Drag

## Status
Proposed – 2025-10-23 (owner: Vireal)

## Context
- Observation mode extends the core lifecycle screen with a full-organism overview that must work on phones and tablets.
- The floating overlay already renders a single-cell canvas; observation mode must render multiple snapshots with shared context.
- Echo delivered research on rendering constraints and UX expectations (multi-cell drag, saturation timeline, readiness cues).
- Gating logic (availability of budding) is implemented and exposes readiness state via the domain layer; the UI must reflect that state but does not need to redefine the rules.

## Decision

### 1. UI Layout
We introduce a dedicated Compose entry point:
```kotlin
@Composable
fun ObservationModeScreen(
    uiState: ObservationUiState,
    gestures: ObservationGestures,
    modifier: Modifier = Modifier
)
```

`ObservationUiState` wraps the existing lifecycle snapshots and gating output:
```kotlin
data class ObservationUiState(
    val organism: OrganismSnapshot,
    val readiness: GatingReadiness,
    val timeline: SaturationTimeline,
    val hints: ObservationHints
)
```

Screen structure (phone layout):
```
 -------------------------------------------------
| TopBar: title, close, readiness pill            |
|-------------------------------------------------|
| Metrics strip: time, energy, mature cell count  |
|-------------------------------------------------|
| ObservationCanvas (flex)                        |
|  - renders cells, organism outline, pulsation   |
|  - accepts gestures provided by ObservationGestures |
|-------------------------------------------------|
| Timeline rail: saturation graph + markers       |
|-------------------------------------------------|
| Action row: Budding button, breadcrumbs, help   |
 -------------------------------------------------
```

Layout rules:
- Use `Scaffold` with permanent top bar and bottom action row.
- `ObservationCanvas` fills remaining height (phones) or sits next to timeline (tablets via `WindowSizeClass`).
- Metrics strip reuses `Material3` `AssistChip` components for quick visual parse; kept at 56dp height.
- Readiness pill shows `readiness.status` and subscribes to the gating output (already computed by domain).

### 2. Gesture Architecture
We introduce an explicit gesture contract to support whole-organism drag and future gestures:
```kotlin
data class ObservationGestures(
    val onOrganismDrag: (DragDelta) -> Unit,
    val onGestureEnd: () -> Unit,
    val onTapCell: (CellId) -> Unit = {},
    val onPinchScale: (Float) -> Unit = {}
)
```

`ObservationCanvas` hosts a single `pointerInput` chain:
1. `awaitEachGesture` to capture drag sequences.
2. On first pointer down, resolve synthetic anchor (organism centroid).
3. During drag, compute delta in canvas coordinates and call `onOrganismDrag`.
4. Pass deltas to `ObservationDragOrchestrator`, which propagates movement to all cell offsets and persists the aggregate position through `ObservationRepository`.

`ObservationDragOrchestrator` responsibilities:
- Maintain `MutableStateFlow<OrganismTransform>` with position, scale, rotation (rotation reserved for future).
- Clamp translation within virtual viewport defined by organism bounding box and screen insets.
- Debounce persistence writes (16 ms window) before writing via repository.
- Emit drag lifecycle events (`onDragStart`, `onDragEnd`) so gating UI can suppress accidental taps during movement.

Multi-pointer handling:
- If a second pointer enters before drag settles, transition to scale mode (`onPinchScale`), but still broadcast translation based on centroid movement.
- Gesture detection uses `TransformableState` internally but surfaces pure callbacks to avoid recomposition churn in caller.

Accessibility and TalkBack:
- Expose drag semantics via `semantics { customActions(...) }`.
- Provide alternative action buttons in action row for keyboard users: `Move Left/Right/Up/Down`.

### 3. Data Flow
- Extend `ObservationViewModel` (new) that composes existing `CellViewModel` snapshots with organism-level repositories:
  ```kotlin
  class ObservationViewModel(
      private val cellRepository: CellRepository,
      private val observationRepository: ObservationRepository,
      private val gatingService: GatingService,
      private val dispatcher: CoroutineDispatcher = Dispatchers.Default
  ) : ViewModel() { ... }
  ```
- `ObservationRepository` exposes:
  ```kotlin
  interface ObservationRepository {
      val organismTransform: Flow<OrganismTransform>
      suspend fun persistTransform(transform: OrganismTransform)
      val timeline: Flow<SaturationTimeline>
      val hints: Flow<ObservationHints>
  }
  ```
- ViewModel combines sources via `combine` and emits `ObservationUiState`:
  - Pull cell snapshots from `CellRepository.observeOrganism()` (already emits `List<CellSnapshot>`).
  - Map gating service output (`GatingState.Ready/NotReady/Cooldown`) into `GatingReadiness`.
  - Merge organism transform to keep canvas in sync with persisted position/scale.
- Drag orchestrator writes updates through `persistTransform`, debounced, so state remains consistent across restarts and between observation/overlay modes.

### 4. Integration Plan
- **Cell Repository**: Introduce `observeOrganism()` in addition to existing single-cell access; reuse domain `CellLifecycleCoordinator` to aggregate all lifecycles.
- **State Machine**: Observation view reads readiness via `CellLifecycleStateMachine.evaluate()` results already produced; gating service listens to same stream and publishes readiness events—no duplication.
- **Overlay Parity**: Share `LifecycleCanvas` primitives; extract draw logic into `OrganismCanvasLayer` so overlay and observation reuse visuals.
- **Navigation**: Add `ObservationRoute` to app graph. Overlay double-tap uses `NavigationController.navigate(ObservationRoute)` keeping continuity.
- **Persistence**: Extend DataStore schema with `OrganismPositionEntry` (x,y,scale). Migrate existing overlay position to new schema to keep drag parity.
- **Testing hooks**: Provide `ObservationRepositoryFake` for Compose previews and Kai's instrumentation tests.

## Rationale
- Separating `ObservationUiState` from the raw `CellUiState` lets us enrich the view with organism-level metadata (readiness, timeline) without leaking domain logic into the UI layer.
- A dedicated gesture orchestrator means Lumen can focus Canvas rendering while sharing a single source of truth for transforms across components (canvas, mini-map, HUD).
- Explicit contracts make it easy for tests (Kai) to simulate drag and verify repository writes without poking at Compose internals.

## Consequences
- Additional state holder (`ObservationDragOrchestrator`) increases code surface but enables reuse in other modes (edit, template).
- Requires `ObservationRepository` extension to persist organism transform separately from single-cell overlay.
- UI layout introduces tablet adaptation responsibilities; we rely on `WindowSizeClass` utility already present in the app shell.

## Follow-up
- Lumen: implement `ObservationModeScreen`, `ObservationCanvas`, and orchestrator; wire to repository and ViewModel.
- Echo: validate metrics strip content against research notes; provide copy guidelines for readiness pill.
- Kai: design Compose gesture tests using `performTouchInput` to assert drag translation and TalkBack actions.
- Nyx: capture new contracts in memory snapshot once implementation stabilises.
- Scribe: document UX walkthrough once the screen is feature-complete.
