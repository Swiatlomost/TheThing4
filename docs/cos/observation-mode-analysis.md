# Observation Mode  Analyst Brief (v0.1)

## 1. Goal
- Udokumentowac wymagania dla trybu obserwacji caego organizmu: zbiorczego przesuwania komorek, gatingu paczkowania, podgladu saturacji i komunikatow dostepnosci.
- Zapewnic wejscia dla Vireal (architektura `ADR-2025-10-23-observation-mode-ui-and-gestures.md`), Lumen (implementacja UI/domeny) oraz Kai (plan i wyniki testow).

## 2. Stan repozytorium (2025-10-23)
- UI Compose zrealizowane w `app/src/main/java/com/example/thething4/ui/ObservationModeScreen.kt` (layout, potno, timeline, gating UI).
- Warstwa domenowa + repozytorium: `app/src/main/java/com/example/thething4/observation/ObservationViewModel.kt`, `ObservationRepository.kt`, `ObservationModels.kt`.
- Integracja z nawigacja aplikacji: `MainActivity.kt` udostepnia przeacznik Observation  Lifecycle.
- Testy jednostkowe: `app/src/test/java/com/example/thething4/observation/ObservationViewModelTest.kt` (readiness, cooldown, persystencja drag).
- Brak jeszcze instrumentacyjnych testow Compose (TODO Kai - zaplanowane w test planie).

## 3. Dane i kontrakty
| Element | Zrodo | Zawartosc / rola |
| --- | --- | --- |
| `ObservationUiState` | ViewModel | `OrganismSnapshot` (lista `OrganismCellSnapshot` + transform), `GatingReadiness`, `SaturationTimeline`, `ObservationHints`. |
| `ObservationRepository` | Storage | Przechowuje transformacje organizmu (`OrganismTransform`), timeline saturacji, hinty (persist / remote). |
| `ObservationGestures` | UI | `onOrganismDrag(DragDelta)`, `onGestureEnd()`  wspolny handler dla Compose Canvas. |
| `GatingReadiness` | Domain | Liczba komorek dojrzaych, status (Ready/NotReady/Cooldown), komunikat dla Readiness Pill. |

- Czas i ewaluacja stanu bazuja na `CellLifecycleStateMachine` (re-use state machine z trybu podstawowego).
- Transformacja organizmu musi byc wspodzielona z innymi trybami (edytor, overlay)  repozytorium jako warstwa prawdy.

## 4. Gesty i obserwacja
1. Drag rozpoznawany przez `pointerInput` (`detectDragGestures`) w `ObservationCanvas`.
2. Delta przekazywana do `ObservationGestures.onOrganismDrag` -> `ObservationViewModel.onOrganismDragged`.
3. ViewModel aktualizuje transformacje (offset + skala w przyszosci) i odkada persist do repozytorium (debounce ~32 ms).
4. `onGestureEnd` flushuje stan (zamyka pending job, pozwala na gating tap).
5. Hinty (`ObservationHints`) steruja banerami (drag hint, budding hint)  wstepnie generowane heurystycznie, moga byc nadpisane przez repo.

## 5. Gating i paczkowanie
- Readiness `Ready` kiedy 1 komorka w stanie `Mature`.
- `Cooldown`: 5 s od ostatniego paczkowania (`ObservationViewModel.onBuddingRequested` ustawia `cooldownUntil`).
- UI:
  - Readiness Pill w AppBar (kolor wg statusu, komunikat).
  - `ActionRow` z przyciskiem `Trigger budding` (enabled tylko gdy `Ready`).
  - Tekst statusu: `Awaiting maturity` / `Budding available` / `Cooling down`.
- Po paczkowaniu (kolejne zadanie ORIN-20251022-004) repozytorium powinno zarejestrowac nowa komorke + restart saturacji  obecny kod przygotowany na cooldown.

## 6. Performance & UX
- Render Canvas rysuje placeholder (organiczny okrag) oraz mapuje etapy (`drawCellStage`) z subtelnymi gradientami.
- Timeline saturacji (`SaturationTimelineRail`) prezentuje przeglad historyczny; w przyszosci mozliwe poszerzenie na tablety (`WindowSizeClass`).
- Wydajnosc: petla `viewModelScope.launch(dispatcher)` z `delay(32L)` (ok. 30 FPS). Profilowac przy wiekszej liczbie komorek (>32)  potencjalna optymalizacja: `snapshotFlow`.
- Accessibility: Semantyka przycisku gating (`contentDescription`), timeline jako list elementow (`AssistChip` z `state`). TODO Kai: potwierdzic z TalkBack (manual plan).
- Responsywnosc: transformacje bazuja na `Offset` w jednostkach ekranu; docelowo normalizowac do `DpSize` by zachowac spojnosc miedzy trybami.

## 7. Ryzyka
- **Brak instrumentacyjnych testow**  Compose UI nieobjete Espresso; konieczne w kolejnym sprincie (Kai/Nodus).
- **Persist transform**: rownolegy drag z innych trybow moze nadpisac repo  wymagane sekwencjonowanie przy multi-trybach (sesja ORIN-20251022-007).
- **Cooldown**: brak wizualizacji upywu czasu  rozwazyc progress indicator.
- **Timeline**: w tej wersji generowany lokalnie  w integracji z prawdziwa saturacja timeline musi pochodzic z repozytorium.

## 8. Rekomendacje i follow-up
- Vireal: potwierdzic, ze `ObservationRepository` stanie sie czescia wspolnej warstwy domain i uwzglednic integracje z storage (DataStore/Room).
- Lumen: rozwazyc mechanizm `Animatable` dla pynnego przesuwania offsetu; dodac instrumentation snapshot testy Compose.
- Kai: rozszerzyc `docs/cos/test-plan-observation.md` o scenariusze TalkBack i multi-touch; przygotowac `ObservationModeTest` w `androidTest`.
- Nyx: zaktualizowac pamiec (structure, heurystyki gatingu, link do ADR/analizy/testow).
- Scribe: odnotowac w logu release z Pulse/Observation.

## 9. Zroda
- `docs/cos/adr/ADR-2025-10-23-observation-mode-ui-and-gestures.md`
- Kod: `app/src/main/java/com/example/thething4/ui/ObservationModeScreen.kt`, `observation/ObservationViewModel.kt`, `observation/ObservationRepository.kt`
- Test: `app/src/test/java/com/example/thething4/observation/ObservationViewModelTest.kt`
- Session: `sessions/ORIN-20251023-003-observation-mode.md`

