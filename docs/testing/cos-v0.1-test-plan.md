# Cos v0.1 Test Plan

## Scope
- Validate manual lifecycle commands (Reset, Narodziny, Dojrzałość, Nowa komórka) keep overlay and in-app views in sync.
- Confirm overlay permissions/gestures continue to operate with manual stage switching.
- Ensure adb broadcast path mirrors UI controls for QA automation.

## Test Cases
1. **LIFE-001** - Button "Reset" przywraca pojedynczą komórkę w stanie Seed (manual).
2. **LIFE-002** - Button "Narodziny" przełącza ostatnią komórkę na etap Bud (manual).
3. **LIFE-003** - Button "Dojrzewanie" przełącza ostatnią komórkę na etap Mature (manual).
4. **LIFE-004** - Button "Nowa komórka" dodaje potomka i oznacza rodzica jako Spawned (manual).
5. **ADB-001** - db shell am broadcast -a com.example.cos.action.{SEED|NARODZINY|DOJRZEWANIE|NOWA_KOMORKA} odwzorowuje sekwencję etapów (manual/automation).
6. **UI-OVERLAY-001** - Overlay drag stays within bounds (manual).
7. **UI-OVERLAY-002** - Double tap launches main app (manual).
8. **ACCESS-001** - Overlay permission denied path (manual).
9. **UI-OVERLAY-003** - Overlay start/stop (double tap) utrzymuje Compose bez crashy po lifecycle fix (manual + logcat).

## Results
- Unit tests (./gradlew.bat test) przechodzą wraz z CosLifecycleEngineTest (sekcja manualnego cyklu).
- Instrumentation UI-OVERLAY-003 (./gradlew.bat feature:cos-overlay:connectedDebugAndroidTest) potwierdza, że LifecycleOverlayService ma oregroundServiceType="specialUse" (eature/cos-overlay/src/androidTest/java/com/example/cos/overlay/LifecycleOverlayManifestTest.kt).
- Manual stage control: db shell am broadcast -a com.example.cos.action.SEED (reset łańcucha), ...NARODZINY, ...DOJRZEWANIE, ...NOWA_KOMORKA, oraz db shell am broadcast -a com.example.cos.action.SET_STAGE --es com.example.cos.extra.STAGE <BUD|MATURE> dla pojedynczych etapów.
- Manual overlay scenariusze (UI-OVERLAY-001/002) PASS na emulatorze Pixel_5; UI-OVERLAY-003 potwierdzony logcatem bez FATAL EXCEPTION; ACCESS-001 (odmowa uprawnień) PASS.
- Follow-up: add instrumentation coverage for overlay drag/double tap and permission flows.
- Observations do dalszych iteracji: (1) rozważyć dodatkowe warianty animacji przy zwiększającej się liczbie komórek, (2) dopracować wizualizację komórki oznaczonej jako Spawned.
