# Cos v0.1 Architecture Notes (Draft)

## Goals
- Provide common lifecycle logic for both in-app and overlay modes.
- Ensure 2x10s phase transition (seed -> bud -> mature) and offspring generation.
- Share state via repository + ViewModel accessible from Activity and overlay service.

## Module Structure
- `app/`: root Compose application, hosts main UI + navigation.
- `feature/cos-lifecycle/`: domain, state machine, repository interfaces.
- `feature/cos-overlay/`: overlay controller, service, Compose UI.
- `core/designsystem/`: theming, shared components.

## State & Data Flow
```
CosLifecycleRepository --(StateFlow)-> CosLifecycleViewModel -- Compose UI (app)
                                      \-- OverlayLifecycleService -> Compose overlay
```
- Repository keeps list of `CellSnapshot` with phase, timers, contact graph.
- Time provider drives phase transitions (2x10s) with ability to pause (dev toggle).
- Offspring generation occurs when mature cell triggers action; new cell added with contact check.

## Overlay Integration
- Foreground service `LifecycleOverlayService` using `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
- `OverlayController` handles permission checks (`Settings.canDrawOverlays`), start/stop, DataStore position storage.
- Gestures: drag (window params), double tap (launch `MainActivity` via `FLAG_ACTIVITY_NEW_TASK`).

## Persistence & Tools
- DataStore (proto/json) for overlay position + dev settings (pause timer).
- Hilt modules: repository (SingletonComponent), ViewModel bindings (ActivityRetainedComponent).
- Testing hooks to inject fake time provider and repository.

## Next Steps
- Finalize ADR with module/DI specifics.
- Align with Lumen implementation tasks and Kai test plan.
