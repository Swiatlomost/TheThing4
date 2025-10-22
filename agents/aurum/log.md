# Aurum Log (Mentor)

## Active Tasks
### AURUM-20251021-001 - Przeglad sesji APK i wytyczne na przyszlosc
- Status: [ ] Pending [x] In Progress [ ] Done
- Notes:
  - Analizuje: instalacja CLI (Nodus), wrapper 8.7 (Lumen), problemy AndroidX i layout.
  - Cel: przygotowac zestaw startowy plikow oraz zalecenia czyszczenia kontekstu.

---

## Archive Template
- Task ID: AURUM-YYYYMMDD-XXX
- Status: [ ] Pending [ ] In Progress [ ] Done (YYYY-MM-DD)
- Notes:
  - Najwazniejsze wnioski i rekomendacje.

> Note: raporty maja byc syntetyczne – bullet points + linki do zrodel.
### Aurum Raport 2025-10-21
- **Srodowisko**: CLI Android zainstalowane (`sdkmanager --list` OK), Gradle wrapper zaktualizowany do 8.7, build APK przechodzi.
- **Bledy, ktore naprawilismy**:
  - brak commandline tools -> dodany punkt w `docs/install-checklist.md` i wpis w pamieci Nodusa.
  - wrapper 8.6 -> automatycznie aktualizuj `gradle-wrapper.properties` (use 8.7+).
  - brak layoutu/Compose -> szybki fallback na layout XML (warto miec bazowy szablon).
- **Zalecany zestaw startowy (czysta sesja)**:
  - `AGENTS.md`, `WORKFLOW.md`, `README.md` (Checklisty build APK), `docs/install-checklist.md`.
  - `agents/orin/log.md`, `agents/status.md`, aktualne `memory.json` (Orin, Aurum, Nyx).
- **Checklisty czystego kontekstu**:
  1. Upewnij sie, ze `agents/<name>/log.md` zawiera tylko ostatnie zadania (stare wpisy archiwizuj do `log-archive/`).
  2. Zrob snapshot Nyx po wiekszym kamieniu milowym (np. build OK, release).
  3. W README/SETUP trzymaj aktualne wersje narzedzi (JDK, SDK, Gradle).
- **TODO**: "archiwizacja logow" – Scribe i Nyx dogadajcie rotacje logow; od teraz minimalizujmy historyczne wpisy w obrębie jednej sesji.
---
