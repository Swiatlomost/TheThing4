# ADR-2025-10-24: Cos v0.1 Architecture

## Status
Accepted - 2025-10-24

## Context
- Building Cos v0.1 from scratch requires shared lifecycle logic for in-app and overlay modes.
- Overlay must comply with Android 14 restrictions (SYSTEM_ALERT_WINDOW, foreground service type specialUse).
- We want modular structure to support future growth and testing.

## Decision
1. **Module Layout**
   - pp/: hosts Compose application, navigation, DI entry point.
   - eature/cos-lifecycle/: domain models, lifecycle engine, repository interfaces.
   - eature/cos-overlay/: overlay controller, foreground service, Compose overlay UI.
   - core/designsystem/: Material 3 theming and shared components.

2. **State Management**
   - CosLifecycleEngine exposes StateFlow<CosLifecycleState> with ordered list of CellSnapshot (etap + rodzic).
   - CosLifecycleViewModel udostępnia stan do aplikacji i overlay; przyciski sterują ostatnią komórką (Seed → Bud → Mature → Spawned → nowa komórka).
   - Broadcast receiver (LifecycleStageReceiver) mapuje komendy adb (SEED, NARODZINY, DOJRZEWANIE, NOWA_KOMORKA) na akcje silnika.

3. **Overlay Architecture**
    - OverlayController (Hilt singleton) checks permission, starts/stops service, persists position using DataStore.
    - LifecycleOverlayService runs as foreground service with WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, zapewnia Compose lifecycle + saved state owner.
    - Compose overlay UI uses same state as in-app view; gestures: drag + double tap.

4. **Dependency Injection**
   - @Module + @InstallIn(SingletonComponent::class) wiąże DefaultCosLifecycleEngine jako CosLifecycleEngine.
   - @HiltViewModel dla CosLifecycleViewModel wstrzykuje silnik.
   - Overlay service obtains dependencies via EntryPointAccessors.

5. **Rendering & Animations**
   - Canvas przelicza pozycje i promień bazowy tak, by cały organizm mieścił się w stałym kontenerze; więcej komórek → mniejszy promień.
   - Animacje etapów (Seed→Bud→Mature→Spawned) realizowane są przez płynne tweeny w Compose.
   - Brak wymogu styku komórek; pozycje układane są równomiernie po okręgu.

## Consequences
- Manual commands i adb pozwalają debugować overlay bez czekania na upływ czasu.
- Silnik zarządza kolejnością komórek (tylko ostatnia może przejść do kolejnego etapu i wygenerować potomka).
- Overlay i aplikacja dzielą ten sam stan, więc testy mogą działać na emulatorze Pixel 5.

## Diagram
`
app/MainActivity          feature/cos-overlay
     |                            |
CosLifecycleViewModel --- StateFlow<CosLifecycleState> --- LifecycleOverlayService
     |                            |
   Compose UI             Compose overlay (drag/double tap)

CosLifecycleEngine <-> Broadcast Receiver / UI commands
`

## Follow-up
- Lumen może eksperymentować z dodatkowymi wzorcami rozmieszczenia (spirala, gęste skupiska).
- Kai rozszerza testy o animacje i automatyczne sekwencje adb.
- Nyx aktualizuje pamięć długoterminową o nowy proces manualnego cyklu.
