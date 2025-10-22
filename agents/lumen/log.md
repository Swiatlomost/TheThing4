# Lumen Log (Developer)

## Active Tasks
### LUMEN-20251021-003 - Uruchomic testy APK na emulatorze
- Parent Task: ORIN-20251021-006
- Status: [ ] Pending [ ] In Progress [x] Done
- Plan:
  - Po otrzymaniu raportu od Nodus, uruchomic emulator lub wykorzystac istniejace urzadzenie.
  - Wykonac `./gradlew connectedDebugAndroidTest` i zebrac logi (`adb logcat`, raporty testow).
- Notes:
  - Emulator Pixel_5 (Android 14) dziala; `adb devices` pokazuje `emulator-5554`.
  - `./gradlew connectedDebugAndroidTest` (21s, 66 tasks) -> BUILD SUCCESSFUL; brak testow (NO-SOURCE) poniewaz `app/src/androidTest` pusty.
  - Aplikacja zainstalowana na emulatorze (`installDebug` implicit); brak bledow.
  - Next: przekazac logi Kai; rozwa?y? dodanie test?w instrumentacyjnych.

## Completed Tasks
### LUMEN-20251021-002 - Dodanie Gradle Wrapper i test build
- Status: Done (2025-10-21)
- Summary:
  - Wygenerowano Gradle Wrapper 8.7, dodano modu? Android `app`.
  - Uruchomiono `./gradlew assembleDebug --no-daemon` (build OK).
  - Apk: `app/build/outputs/apk/debug/app-debug.apk`.
- Notes:
  - Start: 2025-10-21 23:12 wrapper + `gradle init`.
  - Progress: zaktualizowano gradle-wrapper do 8.7, poprawiono manifest i layout.
  - Stop: Build zakonczony pomyslnie; gotowe do przekazania/testow.

### LUMEN-20251021-001 - Aktualizacja dokumentacji build APK
- Status: Done (2025-10-21)
- Zakres: dopisano checklisty build APK do README/SETUP.
- Notatki: Dokumentacja uzupelniona krokami `sdkmanager`, JDK 17, `./gradlew assembleDebug`, wskazowki CI/cache.

### Uwagi przygotowawcze
- Instrukcja wrappera i buildow: `docs/install-checklist.md`.

---

## Archive Template
- Task ID: LUMEN-YYYYMMDD-XXX
- Parent Task: ORIN-YYYYMMDD-XXX
- Status: [ ] Pending [ ] In Progress [ ] Done (YYYY-MM-DD)
- Summary:
  - Prace wykonane:
  - Testy:
- Notes:
  - Blockers/Next:

> Note: include commands run and file paths touched.

