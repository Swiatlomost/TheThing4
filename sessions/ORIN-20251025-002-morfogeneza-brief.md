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
- UX guard rails Echo: brak nakladania, feedback kolorystyczny, historia form (undo/redo, szkic w toku), autosort z potwierdzeniem.
- Priorytety przyrostow: 003 (status + dropdown) -> 004 (menu poziomu, alerty) -> 005 (edytor komorek, zapisy form) -> 006 (canvas UX, clamp) -> 007 (undo/redo + autosort) -> kolejne (presety, podglad roznic).

## Draft Plan (PDCA Seeds)
- **Plan**: Dostarczyc Morfogeneze w przyrostach UI + backend form, z integracja overlay i testami Kai (003-007).
- **Do**:
  - Echo utrzymuje notatki UX (`docs/ux/morphogeneza-ux-research.md`) i sygnalizuje ryzyka.
  - Vireal prowadzi ADR-2025-10-25 (status Accepted) oraz guard rails architektoniczne.
  - Lumen zrealizowal 003-006; rozpoczyna LUMEN-20251026-007 (undo/redo + autosort).
  - Nodus przygotowal event `forma_aktywna`; aktualnie rozszerza checkliste o przypadki undo/redo.
  - Kai rozbudowuje plan testow (`docs/testing/morphogeneza-test-plan.md`) o scenariusze historii i autosortu.
- **Check**: Po kazdym etapie Lumen uruchamia `./gradlew test` + `./gradlew connectedDebugAndroidTest`. Kai pokrywa guard rails i sanity overlay, Nodus raportuje logcat/dumpsys. Orin monitoruje status board.
- **Act**: Po walidacji zebrac feedback produktowy (np. poziom automatyzacji autosortu), wpisac do backlogu kolejne funkcje (presety, podglad roznic). Nyx uwzglednia zmiany w pamieci.

## Recommendations for Orin
1. `[ECHO]` Zaktualizowac guard rails o undo/redo i autosort (notatka UX + ADR), przekazac rekomendacje Lumenowi i Kai.
2. `[LUMEN]` Realizowac LUMEN-20251026-007 (historia operacji + autosort) wraz z testami.
3. `[NODUS]` Rozszerzyc checklisty adb/logcat o przypadki undo/redo i potwierdzic brak regresji kanalu.
4. `[KAI]` Dodac scenariusze undo/redo/autosort do planu testow i wskazac wymagane automaty.
5. `[SCRIBE]` Zaktualizowac brief po wdrozeniu historii oraz odnotowac pytania produktowe dotyczace poziomu automatyzacji.

## Key Files
- `docs/architecture/ADR-2025-10-25-morfogeneza.md`
- `docs/ux/morphogeneza-ux-research.md`
- `feature/morphogenesis/src/...` (UI, ViewModel, testy)
- `docs/testing/morphogeneza-event-checklist.md`
- `docs/testing/morphogeneza-test-plan.md`*** End Patch




