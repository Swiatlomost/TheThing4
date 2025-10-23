# Nodus Log (Integrator)

## Active Tasks
- (brak)

---

## Completed Tasks
### NODUS-20251022-001 - Konfiguracja trybu plywajacego
- Status: Done (2025-10-22)
- Scope:
  - Manifest uzupełniony o SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE i SPECIAL_USE.
  - OverlayPermissionActivity po nadaniu zgody od razu uruchamia serwis.
- Notes:
  - Monitorować UX zgody oraz notyfikacji foreground.

### NODUS-20251022-002 - Integracja serwisu zycia w tle
- Status: Done (2025-10-22)
- Scope:
  - LifecycleOverlayService jako LifecycleService z Compose (WindowManager, powiadomienie, SavedState tags).
  - `./gradlew connectedDebugAndroidTest` (IT-OV-001) potwierdził brak crashy przy starcie/stop.
- Notes:
  - Kolejny etap: DataStore pozycji + synchronizacja ORIN-20251022-007.

### NODUS-20251021-003 - Przygotowac srodowisko emulatora Android
- Status: Done (2025-10-21)
- Notes: Pixel_5 AVD uruchomiony, `adb devices` -> emulator-5554.

### NODUS-20251021-002 - Instalacja Android CLI
- Status: Done (2025-10-21)
- Notes: CLI + pakiety sdkmanager zainstalowane.

### NODUS-20251021-001 - Checklisty instalacji i automatyzacji
- Status: Done (2025-10-21)
- Notes: checklisty przekazane Orinowi i Scribe.

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
