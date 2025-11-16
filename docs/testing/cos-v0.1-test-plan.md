# Cos v0.1 Test Plan

## Scope
- Validate manual lifecycle commands (Reset, Narodziny, Dojrzalosc, Nowa komorka) keep overlay and in-app views in sync.
- Confirm overlay permissions/gestures continue to operate with manual stage switching.
- Ensure lifecycle broadcast permission gating prevents external apps while zachowuje sciezke dla wewnetrznych narzędzi QA.

## Test Cases
1. **LIFE-001** - Button "Reset" przywraca pojedyncza komorke w stanie Seed (manual).
2. **LIFE-002** - Button "Narodziny" przelacza ostatnia komorke na etap Bud (manual).
3. **LIFE-003** - Button "Dojrzewanie" przelacza ostatnia komorke na etap Mature (manual).
4. **LIFE-004** - Button "Nowa komorka" dodaje potomka i oznacza rodzica jako Spawned (manual).
5. **SEC-ADB-001** - Próba zewnętrznego `adb shell am broadcast` bez uprawnienia jest odrzucona; scenariusz przechodzi po stronie instrumentacji/app (posiada permission `com.thething.cos.permission.CONTROL_LIFECYCLE`).
6. **UI-OVERLAY-001** - Overlay drag stays within bounds (manual).
7. **UI-OVERLAY-002** - Double tap launches main app (manual).
8. **ACCESS-001** - Overlay permission denied path (manual).
9. **UI-OVERLAY-003** - Overlay start/stop (double tap) utrzymuje Compose bez crashy po lifecycle fix (manual + logcat).

## Results
- Unit tests (./gradlew.bat test) przechodza wraz z CosLifecycleEngineTest (sekcja manualnego cyklu).
- Instrumentation UI-OVERLAY-003 (./gradlew.bat feature:cos-overlay:connectedDebugAndroidTest) potwierdza, ze LifecycleOverlayService ma oregroundServiceType="specialUse" (eature/cos-overlay/src/androidTest/java/com/example/cos/overlay/LifecycleOverlayManifestTest.kt).
- Manual stage control: UI przyciski LIFE-001..004 PASS; `adb shell am broadcast` bez uprawnienia odrzucone (SEC-ADB-001 NEGATIVE PASS); instrumentacja posiadająca permission `com.thething.cos.permission.CONTROL_LIFECYCLE` potwierdza pozytywne przejście sekwencji etapów.
- Manual overlay scenariusze (UI-OVERLAY-001/002) PASS na emulatorze Pixel_5; UI-OVERLAY-003 potwierdzony logcatem bez FATAL EXCEPTION; ACCESS-001 (odmowa uprawnien) PASS.
- Follow-up: add instrumentation coverage for overlay drag/double tap and permission flows.
- Observations do dalszych iteracji: (1) rozwazyc dodatkowe warianty animacji przy zwiekszajacej sie liczbie komorek, (2) dopracowac wizualizacje komorki oznaczonej jako Spawned.
