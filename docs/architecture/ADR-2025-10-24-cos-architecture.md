# ADR-2025-10-24: Cos v0.1 Architecture

## Status
Accepted – 2025-10-24

## Context
- Building Cos v0.1 from scratch requires shared lifecycle logic for in-app and overlay modes.
- Overlay must comply with Android 14 restrictions (`SYSTEM_ALERT_WINDOW`, foreground service type `specialUse`).
- We want modular structure to support future growth and testing.

## Decision
1. **Module Layout**
   - `app/`: hosts Compose application, navigation, DI entry point.
   - `feature/cos-lifecycle/`: domain models, state machine, repository implementation.
   - `feature/cos-overlay/`: overlay controller, foreground service, Compose overlay UI.
   - `core/designsystem/`: Material 3 theming and shared components.

2. **State Management**
   - `CosLifecycleRepository` exposes `StateFlow<CosLifecycleState>` with list of `CellSnapshot` (phase, timer, contact graph).
   - `CosLifecycleViewModel` (Activity-scoped) provides state to in-app UI via Compose, and to overlay via `LifecycleOverlayService`.
   - `TimeProvider` drives phase transitions (seed -> bud -> mature). Supports dev toggle to pause cycle for testing.
   - Offspring generation triggered when mature cell emits event; repository ensures new cell has contact with parent.

3. **Overlay Architecture**
   - `OverlayController` (Hilt singleton) checks permission, starts/stops service, persists position using DataStore.
   - `LifecycleOverlayService` runs as foreground service with `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
   - Compose overlay UI uses same ViewModel flow; gestures: drag (update window params), double tap (launch `MainActivity`).

4. **Dependency Injection**
   - Hilt modules:
     - `@Module` + `@InstallIn(SingletonComponent::class)` for repository/data module.
     - `@HiltViewModel` for `CosLifecycleViewModel` with injected repository and `TimeProvider`.
     - Overlay service obtains dependencies via `EntryPointAccessors`.

5. **Persistence & Tooling**
   - DataStore for overlay position and dev settings (pause cycle).
   - Tester hooks to inject fake repository/time provider.
   - Compose UI previews located in `core/designsystem` and respective feature modules.

## Consequences
- Clear separation allows independent testing of lifecycle and overlay features.
- Additional complexity in repository/time provider, but enables synchronous behaviour across modes.
- Need to ensure permission flow is user-friendly.

## Diagram
```
app/MainActivity          feature/cos-overlay
     |                            |
CosLifecycleViewModel --- StateFlow<CosLifecycleState> --- LifecycleOverlayService
     |                            |
   Compose UI             Compose overlay (drag/double tap)

CosLifecycleRepository <-> CosDataStore (position, settings)
TimeProvider (2x10s) -> phase transitions
```

## Follow-up
- Lumen uses ADR to scaffold new modules and implementation.
- Kai prepares tests referencing repository/time provider hooks.
- Nyx documents module layout in memory.
