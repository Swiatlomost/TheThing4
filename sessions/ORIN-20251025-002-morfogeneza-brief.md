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
  - Lumen zrealizowal 003-006; zadanie LUMEN-20251026-007 (undo/redo + autosort) pozostaje w backlogu.
  - Nodus przygotowal event `forma_aktywna`; rozszerzenia checklisty o undo/redo czekaja na wznowienie tematu.
  - Kai utrzymuje plan testow (`docs/testing/morphogeneza-test-plan.md`) dla etapu 006; scenariusze historii/autosortu oznaczone jako przyszle.
- **Check**: Po kazdym etapie Lumen uruchamia `./gradlew test` + `./gradlew connectedDebugAndroidTest`. Kai pokrywa guard rails i sanity overlay, Nodus raportuje logcat/dumpsys. Orin monitoruje status board.
- **Act**: Zebrac feedback z etapu 006 i okreslic warunki wznowienia undo/redo/autosortu, wpisujac je do backlogu wraz z presety/podgladem roznic. Nyx uwzglednia zmiany w pamieci.

## Recommendations for Orin
1. `[ECHO]` Utrzymac guard rails jako referencje dla etapu 006 i oznaczyc undo/redo/autosort jako backlog.
2. `[LUMEN]` Monitorowac stabilnosc canvasu 006 i raportowac ewentualne regresje.
3. `[NODUS]` Zachowac checklisty eventu w stanie bazowym; zanotowac, ze scenariusze undo/redo czekaja na wznowienie.
4. `[KAI]` Zostawic scenariusze undo/redo/autosort w planie testow jako przyszle; potwierdzic, ze sanity 006 jest aktualne.
5. `[SCRIBE]` Uaktualnic brief/kronike o decyzji zatrzymania undo/redo i warunki wznowienia.

## Key Files
- `docs/architecture/ADR-2025-10-25-morfogeneza.md`
- `docs/ux/morphogeneza-ux-research.md`
- `feature/morphogenesis/src/...` (UI, ViewModel, testy)
- `docs/testing/morphogeneza-event-checklist.md`
- `docs/testing/morphogeneza-test-plan.md`*** End Patch




