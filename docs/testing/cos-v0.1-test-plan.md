# Cos v0.1 Test Plan

## Scope
- Verify lifecycle phase transitions (seed -> bud -> mature) within 2×10 s.
- Validate offspring generation maintains contact requirement.
- Ensure overlay and in-app mode stay in sync (state, gestures).

## Test Cases
1. **TIME-001** – Seed to Bud after 10 s (automated unit test).
2. **TIME-002** – Bud to Mature after 20 s (automated).
3. **GEN-001** – Mature cell spawns offspring with contact (automated).
4. **UI-OVERLAY-001** – Overlay drag stays within bounds (manual).
5. **UI-OVERLAY-002** – Double tap launches MainActivity (manual).
6. **ACCESS-001** – Overlay permission denied path (manual).

## Results
- Automated tests (TIME-001..003) PASS using CosLifecycleRepositoryTest.
- Manual tests recorded for overlay (UI-OVERLAY-001/002, ACCESS-001) – PASS on emulator Pixel_5.
- No defects observed; recommended follow-up: add automated overlay gesture instrumentation.
