# Nodus Log (Integrator)

## Active Tasks
### NODUS-20251021-003 - Przygotowac srodowisko emulatora Android
- Parent Task: ORIN-20251021-006
- Status: [ ] Pending [ ] In Progress [x] Done
- Scope:
  - Ocenić, czy w obecnym środowisku dostepny jest Android Emulator (AVD) lub alternatywne urządzenie.
  - Uruchomić dostępny AVD i potwierdzić widoczność przez `adb`.
- Notes:
  - Uruchomiono `emulator.exe -avd Pixel_5`; emulator ładuje się poprawnie (Android 14, Google APIs, x86_64).
  - `adb devices` zgłasza `emulator-5554 device`; gotowe do testów.
  - Brak dodatkowych obejść potrzebnych; pozostaje utrzymać emulator w trakcie testów Lumen.
  - Next: przekazano Lumenowi informację o dostępności emulatora.

---

### NODUS-20251021-002 - Instalacja Android CLI
- Parent Task: ORIN-20251021-004
- Status: [ ] Pending [ ] In Progress [x] Done
- Scope:
  - Zainstalowac Android Commandline Tools (sdkmanager) lokalnie.
  - Skonfigurowac pakiety: platform-tools, platforms;android-34, build-tools;34.0.0.
  - Zaktualizowac README/SETUP (w razie zmian sciezek) i potwierdzic dzialanie `sdkmanager --list`.
- Notes:
  - Dependencje: dostep do internetu, sciezki SDK w systemie.
  - Progress: SDK zainstalowane, `sdkmanager --list` dziala; pakiety platform-tools/platforms;android-34/build-tools;34.0.0 pobrane i licencje zaakceptowane.
  - Next: przekazano potwierdzenie do Orina i Scribe, zadanie zamkniete.

---

## Completed Tasks
### NODUS-20251021-001 - Checklisty instalacji i automatyzacji
- Status: Done (2025-10-21)
- Zakres: Przygotowano checklisty instalacji Android SDK/CLI oraz konfiguracji Gradle.
- Notatki:
  - Dependencies: Wnioski Echo (narzedzia i ryzyka).
  - Next: Przekazano checklisty Orinowi i Scribe.

### Checklisty i komendy
1. **Instalacja narzedzi**:
   - Zainstaluj [Android Commandline Tools](https://developer.android.com/studio#command-tools).
   - Ustaw zmienne srodowiskowe: `ANDROID_SDK_ROOT` oraz dodaj `platform-tools` do PATH.
   - Uruchom `sdkmanager` z pakietami:
     ```
     sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
     sdkmanager --licenses
     ```
   - Wymagana wersja JDK: 17 (sprawdz `java -version`).

2. **Konfiguracja Gradle**:
   - Repozytorium musi zawierac Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`).
   - `settings.gradle.kts` z repozytoriami: `google()`, `mavenCentral()`, `gradlePluginPortal()` w `pluginManagement`.
   - `gradle/wrapper/gradle-wrapper.properties` moze wskazywac na lokalny plik ZIP (offline) lub `https://services.gradle.org/distributions/gradle-8.6-bin.zip`.

3. **Srodowisko buildowe**:
   - Zmienna `JAVA_HOME` wskazujaca na JDK 17.
   - Opcjonalny cache: pobierz `gradle-8.6-bin.zip` raz i skonfiguruj `distributionUrl=file:///...`.
   - Dla CI: przechowuj `~/.gradle` oraz katalog SDK jako cache.

4. **Budowa APK**:
   ```
   ./gradlew assembleDebug
   ```
   - Wynik: `app/build/outputs/apk/debug/app-debug.apk`.
   - Do testow instrumentacji: `./gradlew connectedDebugAndroidTest` (wymaga emulatora/urzadzenia).

5. **Dodatkowe wskazowki**:
   - Dokumentuj wersje narzedzi w README/SETUP.
   - W CI (np. GitHub Actions) konfiguruj `actions/setup-java@v3` z JDK 17 oraz instalacje CLI via `sdkmanager`.
   - Brak stabilnego internetu -> dostarcz lokalny mirror SDK/Gradle.

---

## Archive Template
- Task ID: NODUS-YYYYMMDD-XXX
- Parent Task: ORIN-YYYYMMDD-XXX
- Status: [ ] Pending [ ] In Progress [ ] Done
- Scope:
  - ...
- Notes:
  - ...
- Next:
  - ...

> Note: record credentials/config requirements separately (never store secrets here).

### Log dzialan
- 2025-10-21 16:05 Rozpoczeto instalacje Android Commandline Tools (sdkmanager). Instrukcja: docs/install-checklist.md
- 2025-10-21 23:25 `sdkmanager --list` potwierdzil instalacje; pakiety platform-tools, platforms;android-34, build-tools;34.0.0 zainstalowane; licencje zaakceptowane.

---
