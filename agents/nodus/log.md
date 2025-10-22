# Nodus Log (Integrator)

## Active Tasks
### NODUS-20251021-003 - Przygotowac srodowisko emulatora Android
- Parent Task: ORIN-20251021-006
- Status: [ ] Pending [ ] In Progress [x] Done
- Scope:
  - Oceniæ, czy w obecnym œrodowisku dostêpny jest Android Emulator (AVD) lub alternatywne urz¹dzenie.
  - Uruchomiæ dostêpny AVD i potwierdziæ widocznoœæ przez db.
- Notes:
  - Uruchomiono emulator.exe -avd Pixel_5; emulator ³aduje siê poprawnie (Android 14, Google APIs, x86_64).
  - db devices zg³asza emulator-5554 device; gotowe do testów.
  - Brak dodatkowych obejœæ potrzebnych; pozostaje utrzymaæ emulator w trakcie testów Lumen.
  - Next: przekazano Lumenowi informacjê o dostêpnoœci emulatora.

### NODUS-20251021-002 - Instalacja Android CLI
- Parent Task: ORIN-20251021-004
- Status: [ ] Pending [ ] In Progress [x] Done
- Scope:
  - Zainstalowaæ Android Commandline Tools (sdkmanager) lokalnie.
  - Skonfigurowaæ pakiety: platform-tools, platforms;android-34, build-tools;34.0.0.
  - Zaktualizowaæ README/SETUP (w razie zmian œcie¿ek) i potwierdziæ dzia³anie sdkmanager --list.
- Notes:
  - Dependencies: dostêp do internetu, œcie¿ki SDK w systemie.
  - Progress: SDK zainstalowane, sdkmanager --list dzia³a; pakiety platform-tools/platforms;android-34/build-tools;34.0.0 pobrane i licencje zaakceptowane.
  - Next: przekazano potwierdzenie do Orina i Scribe, zadanie zamkniête.

---

## Completed Tasks
### NODUS-20251022-001 - Konfiguracja trybu plywajacego
- Status: Done (2025-10-22)
- Scope:
  - Dodano uprawnienie SYSTEM_ALERT_WINDOW i zarejestrowano LifecycleOverlayService w manifestie.
  - Przygotowano OverlayPermissionActivity oraz integracjê prze³¹cznika w MainActivity.
- Notes:
  - Kolejny krok: monitorowaæ zgodê u¿ytkownika i zachowanie overlay w tle.

### NODUS-20251022-002 - Integracja serwisu zycia w tle
- Status: In Progress (2025-10-22)
- Scope:
  - Utworzono serwis foreground z powiadomieniem i obs³ug¹ WindowManager.
  - Pozycjonowanie overlay dzia³a; synchronizacja z trybem B i DataStore pozostaje do wykonania.
- Notes:
  - Koordynowaæ z Orinem przy zadaniu ORIN-20251022-007.

### NODUS-20251021-001 - Checklisty instalacji i automatyzacji
- Status: Done (2025-10-21)
- Zakres: Przygotowano checklisty instalacji Android SDK/CLI oraz konfiguracji Gradle.
- Notatki:
  - Dependencies: Wnioski Echo (narzêdzia i ryzyka).
  - Next: Przekazano checklisty Orinowi i Scribe.

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
- 2025-10-21 16:05 Rozpoczêto instalacjê Android Commandline Tools (sdkmanager). Instrukcja: docs/install-checklist.md
- 2025-10-21 23:25 sdkmanager --list potwierdzi³ instalacjê; pakiety platform-tools, platforms;android-34, build-tools;34.0.0 zainstalowane; licencje zaakceptowane.

---
