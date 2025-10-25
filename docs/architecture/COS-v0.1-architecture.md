# Cos v0.1 Architecture Notes (Draft)

## Goals
- Provide common lifecycle logic for both in-app and overlay modes.
- Umożliwić ręczne sterowanie etapami (Seed → Bud → Mature → Spawned → kolejny Seed) bez łamania wymogu stałego kontenera.
- Share state via lifecycle engine + ViewModel accessible from Activity and overlay service.

## Module Structure
- pp/: root Compose application, hosts main UI + navigation.
- eature/cos-lifecycle/: domain, state machine, lifecycle engine API.
- eature/cos-overlay/: overlay controller, service, Compose UI.
- core/designsystem/: theming, shared components.

## State & Data Flow
`
CosLifecycleEngine --(StateFlow)-> CosLifecycleViewModel -- Compose UI (app)
                                     \-- LifecycleOverlayService -> Compose overlay
`
- Engine utrzymuje sekwencję CellSnapshot i automatycznie rozmieszcza komórki w kontenerze.
- Komendy (przyciski/adb) zawsze działają na ostatniej komórce; po „Nowa komórka” rodzic przechodzi w stan Spawned.
- Animacje etapów są płynne (tween), brak pulsowania. Skala dopasowuje się do liczby komórek.

## Overlay Integration
- Foreground service LifecycleOverlayService using WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY.
- OverlayController handles permission checks (Settings.canDrawOverlays), start/stop, DataStore position storage.
- Gestures: drag (window params), double tap (launch MainActivity via FLAG_ACTIVITY_NEW_TASK).

## Persistence & Tools
- DataStore (proto/json) for overlay position + dev toggles (np. manual cycle).
- Hilt modules: engine binding in SingletonComponent, ViewModel injection.
- Broadcast receiver (LifecycleStageReceiver) eksponuje Reset, Narodziny, Dojrzewanie, Nowa komórka dla QA.

## Next Steps
- Finalize ADR with module/DI specifics.
- Align with Lumen implementation tasks and Kai test plan.
