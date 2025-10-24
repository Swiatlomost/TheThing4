# Cos v0.1 Test Plan

## Scope
- Verify lifecycle phase transitions (seed -> bud -> mature) within 2-10 s.
- Validate offspring generation maintains the contact requirement.
- Ensure overlay and in-app mode stay in sync (state, gestures).

## Test Cases
1. **TIME-001** - Seed to Bud after 10 s (automated unit test).
2. **TIME-002** - Bud to Mature after 20 s (automated).
3. **GEN-001** - Mature cell spawns offspring with contact (automated).
4. **UI-OVERLAY-001** - Overlay drag stays within bounds (manual).
5. **UI-OVERLAY-002** - Double tap launches main app (manual).
6. **ACCESS-001** - Overlay permission denied path (manual).

## Results
- Automated tests (TIME-001..003) pass via `./gradlew.bat test` (`feature/cos-lifecycle/src/test/java/com/example/cos/lifecycle/CosLifecycleEngineTest.kt`).
- Manual overlay scenarios (UI-OVERLAY-001/002, ACCESS-001) pending; execute on emulator Pixel_5 once visuals and gestures are signed off.
- Follow-up: add instrumentation coverage for overlay drag/double tap and permission flows.
- Observations to address next: (1) disable the current pulse scaling because it breaks cell contact in early stages, (2) smooth out the stage transition animation to avoid visual stuttering, (3) fix overlay launch so the organism renders after double tap.
