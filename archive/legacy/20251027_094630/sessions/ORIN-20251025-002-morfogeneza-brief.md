# ORIN-20251025-002 - Brief Morfogenezy

## 4MAT Discovery
- **WHY**: Morfogeneza daje graczowi sprawczosc nad ksztaltem Cosia, wzmacnia doswiadczenie rozwoju organizmu i nadaje wartosc zebranym komorkom.
- **WHAT**: Dedykowane okno (przycisk "Morfogeneza" z cyklu Cos) z obrysem organizmu, paskiem statusu (level, komorki, aktywna forma) oraz lista zapisanych form. Uzytkownik tworzy formy w granicach dostepnych komorek, bez nakladania i z kontrola rozmiaru.
- **HOW**: Edytor pozwala dodawac/usuwac komorki, skalowac je suwakiem, zapisywac szkice i aktywowac wybrana forme. Aktywacja emituje zdarzenie `forma_aktywna` (SharedFlow + broadcast) i natychmiast odswieza overlay.
- **WHAT IF**: Kolejne iteracje obejma walidacje kolizji organow wewnetrznych, wizualny podglad roznic form oraz biblioteki presetow.

## Conversation Highlights
- Hexagonalna siatka z micro-offsetem (snap-to-hex) zostala zatwierdzona przez Orina; wpisana w ADR-2025-10-25.
- Limit komorek = liczba zasobow z CosLifecycleEngine; alert przy 10% pozostalych zasobow wdrozony w etapie 004.
- Event `forma_aktywna` gotowy (Nodus) i udokumentowany w checkliscie ADB (`docs/testing/morphogeneza-event-checklist.md`).
- UX guard rails Echo: brak nakladania, feedback kolorystyczny, planowana historia form i autosort (oznaczone jako backlog do kolejnych iteracji).
- Priorytety przyrostow: 003 (status + dropdown) -> 004 (menu poziomu, alerty) -> 005 (edytor komorek, zapisy form) -> 006 (canvas UX, clamp); 007 (undo/redo + autosort) przesuniete do backlogu przed kolejnymi etapami (presety, podglad roznic).

## Draft Plan (PDCA Seeds)
- **Plan**: Utrzymac Morfogeneze na przyrostach 003-006 jako stan bazowy (UI + overlay), przygotowujac notatki pod przyszle iteracje (undo/redo, autosort).
- **Do**:
  - Echo utrzymuje notatki UX (`docs/ux/morphogeneza-ux-research.md`) i sygnalizuje ryzyka.
  - Vireal prowadzi ADR-2025-10-25 (status Accepted) oraz guard rails architektoniczne.
  - Lumen dostarczyl pelna obsluge zapisanych form (wybor, zapis, aktywacja) wraz z testami UI/ViewModelu; undo/redo pozostaje w backlogu.
  - Nodus wdrozyl kanal SharedFlow (`MorphoFormChannel`) oraz integracje overlay; checklisty ADB/logcat rozszerzone o aktywacje form.
  - Kai rozbudowal plan testow o scenariusz zapis -> wybor -> aktywacja i potwierdzil sanity (logcat/dumpsys); historia/autosort pozostaje jako backlog.
- **Check**: Po kazdym etapie Lumen uruchamia `./gradlew test` + `./gradlew connectedDebugAndroidTest`. Kai pokrywa guard rails i sanity overlay, Nodus raportuje logcat/dumpsys. Orin monitoruje status board.
- **Act**: Monitorowac stabilnosc etapu 006 (Lumen/Kai), utrzymywac dokumentacje SharedFlow/overlay (Nodus), backlog undo/redo/autosort trzymac do kolejnych decyzji. Nyx uwzglednia zmiany w pamieci.

## Recommendations for Orin
1. `[ECHO]` Aktualizowac guard rails/ADR o przeplyw wyboru oraz utrzymac backlog undo/redo/autosort.
2. `[LUMEN]` Monitorowac stabilnosc canvasu 006 i raportowac ewentualne regresje.
3. `[NODUS]` Przygotowac automatyzacje logcat/dumpsys (CI) i monitorowac SharedFlow w overlay.
4. `[KAI]` Kontynuowac sanity zapis->wybor->aktywacja (logcat + SharedFlow) i raportowac wyniki.
5. `[SCRIBE]` Zaktualizowac brief/kronike o integracje SharedFlow i obsluge zapisanych form.

## Key Files
- `docs/architecture/ADR-2025-10-25-morfogeneza.md`
- `docs/ux/morphogeneza-ux-research.md`
- `feature/morphogenesis/src/...` (UI, ViewModel, testy)
- `docs/testing/morphogeneza-event-checklist.md`
- `docs/testing/morphogeneza-test-plan.md`*** End Patch




