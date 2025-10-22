# Nodus Log (Integrator)

## Active Tasks
### NODUS-20251021-003 - Przygotowac srodowisko emulatora Android
- Parent Task: ORIN-20251021-006
- Status: [ ] Pending [ ] In Progress [x] Done
- Scope:
  - Oceni�, czy w obecnym �rodowisku dost�pny jest Android Emulator (AVD) lub alternatywne urz�dzenie.
  - Uruchomi� dost�pny AVD i potwierdzi� widoczno�� przez db.
- Notes:
  - Uruchomiono emulator.exe -avd Pixel_5; emulator �aduje si� poprawnie (Android 14, Google APIs, x86_64).
  - db devices zg�asza emulator-5554 device; gotowe do test�w.
  - Brak dodatkowych obej�� potrzebnych; pozostaje utrzyma� emulator w trakcie test�w Lumen.
  - Next: przekazano Lumenowi informacj� o dost�pno�ci emulatora.

### NODUS-20251021-002 - Instalacja Android CLI
- Parent Task: ORIN-20251021-004
- Status: [ ] Pending [ ] In Progress [x] Done
- Scope:
  - Zainstalowa� Android Commandline Tools (sdkmanager) lokalnie.
  - Skonfigurowa� pakiety: platform-tools, platforms;android-34, build-tools;34.0.0.
  - Zaktualizowa� README/SETUP (w razie zmian �cie�ek) i potwierdzi� dzia�anie sdkmanager --list.
- Notes:
  - Dependencies: dost�p do internetu, �cie�ki SDK w systemie.
  - Progress: SDK zainstalowane, sdkmanager --list dzia�a; pakiety platform-tools/platforms;android-34/build-tools;34.0.0 pobrane i licencje zaakceptowane.
  - Next: przekazano potwierdzenie do Orina i Scribe, zadanie zamkni�te.

---

## Completed Tasks
### NODUS-20251022-001 - Konfiguracja trybu plywajacego
- Status: Done (2025-10-22)
- Scope:
  - Dodano uprawnienie SYSTEM_ALERT_WINDOW i zarejestrowano LifecycleOverlayService w manifestie.
  - Przygotowano OverlayPermissionActivity oraz integracj� prze��cznika w MainActivity.
- Notes:
  - Kolejny krok: monitorowa� zgod� u�ytkownika i zachowanie overlay w tle.

### NODUS-20251022-002 - Integracja serwisu zycia w tle
- Status: In Progress (2025-10-22)
- Scope:
  - Utworzono serwis foreground z powiadomieniem i obs�ug� WindowManager.
  - Pozycjonowanie overlay dzia�a; synchronizacja z trybem B i DataStore pozostaje do wykonania.
- Notes:
  - Koordynowa� z Orinem przy zadaniu ORIN-20251022-007.

### NODUS-20251021-001 - Checklisty instalacji i automatyzacji
- Status: Done (2025-10-21)
- Zakres: Przygotowano checklisty instalacji Android SDK/CLI oraz konfiguracji Gradle.
- Notatki:
  - Dependencies: Wnioski Echo (narz�dzia i ryzyka).
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
- 2025-10-21 16:05 Rozpocz�to instalacj� Android Commandline Tools (sdkmanager). Instrukcja: docs/install-checklist.md
- 2025-10-21 23:25 sdkmanager --list potwierdzi� instalacj�; pakiety platform-tools, platforms;android-34, build-tools;34.0.0 zainstalowane; licencje zaakceptowane.

---
